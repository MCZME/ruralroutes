package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import github.mczme.ruralroutes.core.value.ValueTableManager;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易契约统一执行器
 * 提供单一契约执行和混合交易执行两种模式
 */
public final class TradeContractExecutor {

    public static final TradeContractExecutor INSTANCE = new TradeContractExecutor();

    private final TradeLockManager lockManager;

    private TradeContractExecutor() {
        this.lockManager = new TradeLockManager();
    }

    /**
     * 执行单一契约
     * 包含库存更新：玩家输入物品加入村庄库存，玩家输出物品从村庄库存扣除
     */
    public TradeResult executeContract(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            TradeContract contract,
            net.minecraft.core.BlockPos blockPos) {

        // 获取锁
        ChunkPos chunkPos = new ChunkPos(blockPos);
        if (!lockManager.acquire(chunkPos)) {
            return TradeResult.fail(TradeResult.Reason.INVALID_INPUT);
        }

        try {
            // 先校验玩家输入
            if (!contract.validateInput(player, List.of())) {
                return TradeResult.fail(TradeResult.Reason.PLAYER_INSUFFICIENT);
            }

            TradeResult result = contract.execute(level, nodeData, player, List.of());

            if (result.isSuccess()) {
                // 更新村庄库存
                updateVillageStocksForContract(level, nodeData, result, blockPos);
            }

            return result;
        } finally {
            lockManager.release(chunkPos);
        }
    }

    /**
     * 更新村庄库存（单一契约）
     * - consumed: 玩家消耗的物品 -> 加入村庄库存
     * - outputs: 玩家获得的物品 -> 从村庄库存扣除
     */
    private void updateVillageStocksForContract(
            ServerLevel level,
            CommercialNodeData nodeData,
            TradeResult result,
            net.minecraft.core.BlockPos blockPos) {

        Map<ResourceLocation, StockEntry> stocks = new HashMap<>(nodeData.stocks());

        // 玩家消耗的物品加入村庄库存
        for (ItemStack item : result.consumed()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
            StockEntry current = stocks.get(itemId);
            if (current != null) {
                stocks.put(itemId, current.increase(item.getCount()));
            }
        }

        // 玩家获得的物品从村庄库存扣除
        for (ItemStack item : result.outputs()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
            StockEntry current = stocks.get(itemId);
            if (current != null) {
                stocks.put(itemId, current.decrease(item.getCount()));
            }
        }

        CommercialNodeData newData = new CommercialNodeData(
            nodeData.tradeNodeId(),
            nodeData.themeName(),
            nodeData.sellItems(),
            nodeData.buyItems(),
            stocks,
            nodeData.refreshTimestamp()
        );
        CommercialNodeManager.updateNodeData(level, blockPos, newData);
    }

    /**
     * 执行混合交易（贸易站）
     */
    public TradeResult executeMixedTrade(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            List<PendingTradeSlot> pendingSlots,
            long currentCycleIndex,
            net.minecraft.core.BlockPos blockPos) {

        if (pendingSlots.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.INVALID_REQUEST, List.of());
        }

        CycleManager cycleManager = CycleManager.get(level);
        long actualCycle = cycleManager.getCycleIndex(level.getGameTime());
        if (currentCycleIndex != actualCycle) {
            return TradeResult.fail(TradeResult.Reason.CYCLE_CHANGED, List.of());
        }

        ChunkPos chunkPos = new ChunkPos(blockPos);
        if (!lockManager.acquire(chunkPos)) {
            return TradeResult.fail(TradeResult.Reason.INVALID_REQUEST, List.of());
        }

        try {
            return doExecuteMixedTrade(level, nodeData, player, pendingSlots, blockPos);
        } finally {
            lockManager.release(chunkPos);
        }
    }

    private TradeResult doExecuteMixedTrade(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            List<PendingTradeSlot> pendingSlots,
            net.minecraft.core.BlockPos blockPos) {

        int sellValueTotal = 0;
        int buyValueTotal = 0;
        List<ItemStack> buyFromPlayerItems = new ArrayList<>();
        List<ItemStack> sellToPlayerItems = new ArrayList<>();

        for (PendingTradeSlot slot : pendingSlots) {
            TradeSide side = slot.isBuy() ? TradeSide.SELL_TO_PLAYER : TradeSide.BUY_FROM_PLAYER;
            int count = slot.getBaseStock();
            ItemStack stack = slot.getDisplayStack().copy();
            stack.setCount(count);

            int unitPrice = TradePricingService.calculateFinalPrice(level, nodeData, stack, side);
            int lineValue = unitPrice * count;

            if (slot.isBuy()) {
                sellValueTotal += lineValue;
                sellToPlayerItems.add(stack.copy());
            } else {
                buyValueTotal += lineValue;
                buyFromPlayerItems.add(stack.copy());
            }
        }

        int netCoinValue = sellValueTotal - buyValueTotal;

        TradePaymentPlan plan = buildPaymentPlan(
            buyFromPlayerItems, sellToPlayerItems, netCoinValue);

        List<ItemStack> playerShortfall = validatePlayerInventory(player, plan);
        if (!playerShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.PLAYER_INSUFFICIENT, playerShortfall);
        }

        List<ItemStack> villageShortfall = validateVillageInventory(nodeData, plan);
        if (!villageShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.VILLAGE_INSUFFICIENT, villageShortfall);
        }

        executeTransfer(level, nodeData, player, plan, blockPos);

        return TradeResult.success(sellValueTotal, buyValueTotal);
    }

    /**
     * 构建支付计划（默认使用基础货币）
     * 使用 TagLookupCache 获取基础货币物品
     */
    private TradePaymentPlan buildPaymentPlan(
            List<ItemStack> buyFromPlayerItems,
            List<ItemStack> sellToPlayerItems,
            int netCoinValue) {

        List<ItemStack> playerInputs = new ArrayList<>(buyFromPlayerItems);
        List<ItemStack> playerOutputs = new ArrayList<>(sellToPlayerItems);
        List<ItemStack> villageInputs = new ArrayList<>(sellToPlayerItems);
        List<ItemStack> villageOutputs = new ArrayList<>(buyFromPlayerItems);

        if (netCoinValue != 0) {
            // 获取基础货币物品（通常是铜板）
            ItemStack baseCurrency = getBaseCurrencyItem();
            if (!baseCurrency.isEmpty()) {
                int baseValue = ValueTableManager.queryBaseValue(baseCurrency);
                if (baseValue > 0) {
                    int count = Math.abs(netCoinValue) / baseValue;
                    if (count > 0) {
                        ItemStack currencyStack = new ItemStack(baseCurrency.getItem(), count);
                        if (netCoinValue > 0) {
                            // 玩家净支付货币 -> 玩家输入货币，村庄输出货币
                            playerInputs.add(currencyStack);
                            villageOutputs.add(currencyStack.copy());
                        } else {
                            // 村庄净支付货币 -> 村庄输入货币，玩家输出货币
                            villageInputs.add(currencyStack.copy());
                            playerOutputs.add(currencyStack);
                        }
                    }
                }
            }
        }

        return new TradePaymentPlan(
            playerInputs,
            playerOutputs,
            villageInputs,
            villageOutputs
        );
    }

    /**
     * 获取基础货币物品
     * 使用 TagLookupCache 从 CURRENCY_BASE 标签获取
     */
    private ItemStack getBaseCurrencyItem() {
        String baseCurrencyTag = "#ruralroutes:currency_base";
        for (Item item : TagLookupCache.getItems(baseCurrencyTag)) {
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }

    /**
     * 验证玩家库存
     */
    private List<ItemStack> validatePlayerInventory(ServerPlayer player, TradePaymentPlan plan) {
        List<ItemStack> shortfall = new ArrayList<>();
        for (ItemStack required : plan.playerInputs()) {
            int has = countItemInInventory(player, required.getItem());
            if (has < required.getCount()) {
                shortfall.add(new ItemStack(required.getItem(), required.getCount() - has));
            }
        }
        return shortfall;
    }

    /**
     * 验证村庄库存
     */
    private List<ItemStack> validateVillageInventory(CommercialNodeData nodeData, TradePaymentPlan plan) {
        List<ItemStack> shortfall = new ArrayList<>();
        for (ItemStack required : plan.villageInputs()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(required.getItem());
            StockEntry entry = nodeData.getStock(itemId);
            if (entry == null || entry.current() < required.getCount()) {
                shortfall.add(new ItemStack(required.getItem(), required.getCount() - (entry != null ? entry.current() : 0)));
            }
        }
        return shortfall;
    }

    /**
     * 执行转移
     */
    private void executeTransfer(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            TradePaymentPlan plan,
            net.minecraft.core.BlockPos blockPos) {

        for (ItemStack item : plan.playerInputs()) {
            removeItemFromPlayer(player, item);
        }

        for (ItemStack item : plan.playerOutputs()) {
            addItemToPlayer(player, item.copy());
        }

        updateVillageStocks(level, nodeData, plan, blockPos);
    }

    /**
     * 更新村庄库存
     */
    private void updateVillageStocks(
            ServerLevel level,
            CommercialNodeData nodeData,
            TradePaymentPlan plan,
            net.minecraft.core.BlockPos blockPos) {

        Map<ResourceLocation, StockEntry> stocks = new HashMap<>(nodeData.stocks());

        // 村庄输入：扣除库存（村庄卖出的商品 + 村庄支付的货币）
        for (ItemStack item : plan.villageInputs()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
            StockEntry current = stocks.get(itemId);
            if (current != null) {
                stocks.put(itemId, current.decrease(item.getCount()));
            }
        }

        // 村庄输出：增加库存（村庄买入的商品 + 村庄收到的货币）
        for (ItemStack item : plan.villageOutputs()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
            StockEntry current = stocks.get(itemId);
            if (current != null) {
                stocks.put(itemId, current.increase(item.getCount()));
            }
        }

        CommercialNodeData newData = new CommercialNodeData(
            nodeData.tradeNodeId(),
            nodeData.themeName(),
            nodeData.sellItems(),
            nodeData.buyItems(),
            stocks,
            nodeData.refreshTimestamp()
        );
        CommercialNodeManager.updateNodeData(level, blockPos, newData);
    }

    private int countItemInInventory(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeItemFromPlayer(ServerPlayer player, ItemStack toRemove) {
        int remaining = toRemove.getCount();
        for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.getItem() == toRemove.getItem()) {
                int toTake = Math.min(remaining, stack.getCount());
                stack.shrink(toTake);
                remaining -= toTake;
            }
        }
    }

    private void addItemToPlayer(ServerPlayer player, ItemStack toAdd) {
        boolean success = player.getInventory().add(toAdd);
        if (!success && !toAdd.isEmpty()) {
            player.spawnAtLocation(toAdd);
        }
    }
}
