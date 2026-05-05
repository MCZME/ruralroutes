package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import github.mczme.ruralroutes.register.RRItemTags;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 交易引擎实现
 * 处理贸易站交易执行，使用服务端权威价格计算
 */
public final class TradeEngineImpl implements TradeEngine {

    public static final TradeEngineImpl INSTANCE = new TradeEngineImpl();

    private final TradeLockManager lockManager;

    private TradeEngineImpl() {
        this.lockManager = new TradeLockManager();
    }

    /**
     * 执行贸易站交易
     * 直接从暂存区读取数据，服务端权威计算价格
     *
     * @param level 服务端世界实例
     * @param nodeData 商业节点数据
     * @param player 服务端玩家实例
     * @param pendingSlots 暂存区槽位列表
     * @param currentCycleIndex 当前周期索引（用于跨周期检测）
     * @param blockPos 贸易站位置
     * @return 交易结果
     */
    public TradeResult executeTrade(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            List<PendingTradeSlot> pendingSlots,
            long currentCycleIndex,
            net.minecraft.core.BlockPos blockPos) {

        if (pendingSlots.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.INVALID_REQUEST, List.of());
        }

        ChunkPos chunkPos = new ChunkPos(blockPos);
        if (!lockManager.acquire(chunkPos)) {
            return TradeResult.fail(TradeResult.Reason.INVALID_REQUEST, List.of());
        }

        try {
            return doExecute(level, nodeData, player, pendingSlots, currentCycleIndex, blockPos);
        } finally {
            lockManager.release(chunkPos);
        }
    }

    private TradeResult doExecute(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            List<PendingTradeSlot> pendingSlots,
            long currentCycleIndex,
            net.minecraft.core.BlockPos blockPos) {

        // 1. 验证周期是否一致
        CycleManager cycleManager = CycleManager.get(level);
        long actualCycle = cycleManager.getCycleIndex(level.getGameTime());
        if (currentCycleIndex != actualCycle) {
            return TradeResult.fail(TradeResult.Reason.CYCLE_CHANGED, List.of());
        }

        // 2. 服务端权威价格计算
        int sellValueTotal = 0;  // 玩家需支付的货币（购买物品总价）
        int buyValueTotal = 0;   // 玩家将获得的货币（出售物品总价）

        List<ItemStack> buyFromPlayerItems = new ArrayList<>();  // 玩家出售的物品
        List<ItemStack> sellToPlayerItems = new ArrayList<>();   // 玩家购买的物品

        for (PendingTradeSlot slot : pendingSlots) {
            ResourceLocation itemId = slot.getItemId();
            int count = slot.getBaseStock();
            TradeSide side = slot.isBuy() ? TradeSide.SELL_TO_PLAYER : TradeSide.BUY_FROM_PLAYER;

            ItemStack stack = createItemStack(itemId);
            stack.setCount(count);

            // 服务端重新计算权威价格
            int unitPrice = TradePricingService.calculateFinalPrice(level, nodeData, stack, side);
            int lineValue = unitPrice * count;

            if (slot.isBuy()) {
                // 玩家购买：付出货币，获得物品
                sellValueTotal += lineValue;
                sellToPlayerItems.add(stack.copy());
            } else {
                // 玩家出售：付出物品，获得货币
                buyValueTotal += lineValue;
                buyFromPlayerItems.add(stack.copy());
            }
        }

        // 3. 计算净货币差额
        int netCoinValue = sellValueTotal - buyValueTotal;

        // 4. 验证玩家物品库存（出售物品）
        List<ItemStack> playerItemShortfall = checkPlayerInventory(player, buyFromPlayerItems);
        if (!playerItemShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.PLAYER_INSUFFICIENT, playerItemShortfall);
        }

        // 5. 验证玩家货币库存（仅当净支出时）
        if (netCoinValue > 0) {
            int playerCoins = countCurrencyInPlayerInventory(player);
            if (playerCoins < netCoinValue) {
                ItemStack shortfall = new ItemStack(RRItems.COPPER_COIN.get(), netCoinValue - playerCoins);
                return TradeResult.fail(TradeResult.Reason.PLAYER_INSUFFICIENT, List.of(shortfall));
            }
        }

        // 6. 验证村庄库存（只检查玩家购买的物品）
        List<ItemStack> villageShortfall = checkVillageInventoryForBuy(nodeData, pendingSlots);
        if (!villageShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.VILLAGE_INSUFFICIENT, villageShortfall);
        }

        // 7. 原子执行
        executeTransfer(level, nodeData, player, buyFromPlayerItems, sellToPlayerItems, netCoinValue, pendingSlots, blockPos);

        return TradeResult.success(sellValueTotal, buyValueTotal);
    }

    /**
     * 验证村庄库存（只检查玩家购买的物品）
     */
    private List<ItemStack> checkVillageInventoryForBuy(
            CommercialNodeData nodeData,
            List<PendingTradeSlot> pendingSlots) {

        List<ItemStack> shortfall = new ArrayList<>();
        for (PendingTradeSlot slot : pendingSlots) {
            if (slot.isBuy()) {
                ResourceLocation itemId = slot.getItemId();
                int required = slot.getBaseStock();
                StockEntry entry = nodeData.getStock(itemId);

                int available = (entry != null) ? entry.current() : 0;
                if (available < required) {
                    ItemStack stack = createItemStack(itemId);
                    shortfall.add(new ItemStack(stack.getItem(), required - available));
                }
            }
        }
        return shortfall;
    }

    /**
     * 执行物品转移
     */
    private void executeTransfer(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            List<ItemStack> buyFromPlayerItems,  // 玩家出售的物品
            List<ItemStack> sellToPlayerItems,   // 玩家购买的物品
            int netCoinValue,                     // 净货币差额
            List<PendingTradeSlot> pendingSlots,
            net.minecraft.core.BlockPos blockPos) {

        // 1. 从玩家背包移除出售物品
        for (ItemStack item : buyFromPlayerItems) {
            removeItemFromPlayer(player, item);
        }

        // 2. 给玩家添加购买物品
        for (ItemStack item : sellToPlayerItems) {
            addItemToPlayer(player, item.copy());
        }

        // 3. 处理货币转移
        if (netCoinValue > 0) {
            // 玩家支付货币
            removeCurrencyFromPlayer(player, netCoinValue);
        } else if (netCoinValue < 0) {
            // 玩家获得货币
            int coinsToGive = -netCoinValue;
            addItemToPlayer(player, new ItemStack(RRItems.COPPER_COIN.get(), coinsToGive));
        }

        // 4. 更新村庄库存
        updateVillageStocks(level, nodeData, player, pendingSlots, blockPos);
    }

    /**
     * 更新村庄库存
     */
    private void updateVillageStocks(
            ServerLevel level,
            CommercialNodeData nodeData,
            ServerPlayer player,
            List<PendingTradeSlot> pendingSlots,
            net.minecraft.core.BlockPos blockPos) {

        Map<ResourceLocation, StockEntry> stocks = new java.util.HashMap<>(nodeData.stocks());

        for (PendingTradeSlot slot : pendingSlots) {
            ResourceLocation itemId = slot.getItemId();
            int count = slot.getBaseStock();
            StockEntry current = stocks.get(itemId);

            if (current != null) {
                if (slot.isBuy()) {
                    // 玩家购买：村庄库存减少
                    stocks.put(itemId, current.decrease(count));
                } else {
                    // 玩家出售：村庄库存增加
                    stocks.put(itemId, current.increase(count));
                }
            }
        }

        // 保存更新后的数据
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
     * 创建物品栈
     */
    private ItemStack createItemStack(ResourceLocation itemId) {
        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    /**
     * 检查玩家库存
     */
    private List<ItemStack> checkPlayerInventory(ServerPlayer player, List<ItemStack> required) {
        List<ItemStack> shortfall = new ArrayList<>();
        for (ItemStack requiredStack : required) {
            int has = countItemInPlayerInventory(player, requiredStack.getItem());
            if (has < requiredStack.getCount()) {
                shortfall.add(new ItemStack(requiredStack.getItem(), requiredStack.getCount() - has));
            }
        }
        return shortfall;
    }

    /**
     * 计算玩家背包中某物品的数量
     */
    private int countItemInPlayerInventory(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * 从玩家背包移除物品
     */
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

    /**
     * 向玩家背包添加物品，背包满时生成掉落物
     */
    private void addItemToPlayer(ServerPlayer player, ItemStack toAdd) {
        boolean success = player.getInventory().add(toAdd);
        if (!success && !toAdd.isEmpty()) {
            player.spawnAtLocation(toAdd);
        }
    }

    /**
     * 计算玩家背包中货币的总价值（铜板数）
     * 当前仅支持铜板，后续可扩展多面额货币
     */
    private int countCurrencyInPlayerInventory(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(RRItemTags.CURRENCY_BASE)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * 从玩家背包移除指定价值的货币
     */
    private void removeCurrencyFromPlayer(ServerPlayer player, int value) {
        int remaining = value;
        for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.is(RRItemTags.CURRENCY_BASE)) {
                int toTake = Math.min(remaining, stack.getCount());
                stack.shrink(toTake);
                remaining -= toTake;
            }
        }
    }
}