package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.NodeStockEntry;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

        Map<TradeItemKey, NodeStockEntry> stocks = new HashMap<>(nodeData.stocks());

        // 玩家消耗的物品加入村庄库存
        for (ItemStack item : result.consumed()) {
            TradeItemKey itemKey = TradeItemKey.from(item);
            NodeStockEntry current = stocks.get(itemKey);
            if (current == null && itemKey.hasComponents()) {
                current = stocks.get(TradeItemKey.of(itemKey.itemId()));
            }
            if (current != null) {
                stocks.put(itemKey, current.increase(item.getCount()));
            }
        }

        // 玩家获得的物品从村庄库存扣除
        for (ItemStack item : result.outputs()) {
            TradeItemKey itemKey = TradeItemKey.from(item);
            NodeStockEntry current = stocks.get(itemKey);
            if (current == null && itemKey.hasComponents()) {
                current = stocks.get(TradeItemKey.of(itemKey.itemId()));
            }
            if (current != null) {
                stocks.put(itemKey, current.decrease(item.getCount()));
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
        long actualCycle = cycleManager.getCycleIndex(level);
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

        // 契约驱动：从每个暂存槽位构建支付计划
        TradePaymentPlan totalPlan = TradePaymentPlan.empty();

        for (PendingTradeSlot slot : pendingSlots) {
            TradePaymentPlan slotPlan = buildPlanForSlot(slot);
            totalPlan = totalPlan.merge(slotPlan);
        }

        // 合并相同物品的数量
        totalPlan = consolidatePlan(totalPlan);

        // 验证玩家库存
        List<ItemStack> playerShortfall = validatePlayerInventory(player, totalPlan);
        if (!playerShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.PLAYER_INSUFFICIENT, playerShortfall);
        }

        // 验证村庄库存
        List<ItemStack> villageShortfall = validateVillageInventory(nodeData, totalPlan);
        if (!villageShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.Reason.VILLAGE_INSUFFICIENT, villageShortfall);
        }

        // 执行转移
        executeTransfer(level, nodeData, player, totalPlan, blockPos);

        // 计算总价值（用于返回）
        int sellValue = calculateTotalValue(totalPlan.playerOutputs());
        int buyValue = calculateTotalValue(totalPlan.playerInputs());

        return TradeResult.success(sellValue, buyValue);
    }

    /**
     * 为单个暂存槽位构建支付计划
     */
    private TradePaymentPlan buildPlanForSlot(PendingTradeSlot slot) {
        int count = slot.getBaseStock();
        ItemStack goodsStack = slot.getDisplayStack().copy();
        goodsStack.setCount(count);
        TradeContractType tradeType = slot.getTradeType();
        boolean isBuy = slot.isBuy();

        if (tradeType == TradeContractType.FIXED) {
            // 固定交换：使用 inputStacks，按数量缩放
            List<ItemStack> scaledInputs = scaleStacks(slot.getInputStacks(), count);

            if (isBuy) {
                // 玩家买入：支付 inputStacks，获得 goodsStack
                return new TradePaymentPlan(
                    scaledInputs,          // 玩家输入（支付的物品）
                    List.of(goodsStack),   // 玩家输出（获得的商品）
                    List.of(goodsStack),   // 村庄输入（卖出的商品）
                    scaledInputs           // 村庄输出（收到的物品）
                );
            } else {
                // 玩家卖出：支付 goodsStack，获得 inputStacks（不合理，但保持一致性）
                // 实际上收购区不应该有固定交换，如果有的话逻辑相反
                return new TradePaymentPlan(
                    List.of(goodsStack),   // 玩家输入（卖出的商品）
                    scaledInputs,          // 玩家输出（获得的物品）
                    scaledInputs,          // 村庄输入（收到的物品）
                    List.of(goodsStack)    // 村庄输出（付出的商品）
                );
            }
        } else {
            // 货币篮（包括默认）：使用 priceStacks，按数量缩放
            List<ItemStack> scaledPrice = scaleStacks(slot.getPriceStacks(), count);

            if (isBuy) {
                // 玩家买入：支付货币，获得商品
                return new TradePaymentPlan(
                    scaledPrice,           // 玩家输入（支付的货币）
                    List.of(goodsStack),   // 玩家输出（获得的商品）
                    List.of(goodsStack),   // 村庄输入（卖出的商品）
                    scaledPrice            // 村庄输出（收到的货币）
                );
            } else {
                // 玩家卖出：支付商品，获得货币
                return new TradePaymentPlan(
                    List.of(goodsStack),   // 玩家输入（卖出的商品）
                    scaledPrice,           // 玩家输出（获得的货币）
                    scaledPrice,           // 村庄输入（付出的货币）
                    List.of(goodsStack)    // 村庄输出（收到的商品）
                );
            }
        }
    }

    /**
     * 按数量缩放物品列表
     */
    private List<ItemStack> scaleStacks(List<ItemStack> stacks, int multiplier) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack scaled = stack.copy();
            scaled.setCount(stack.getCount() * multiplier);
            result.add(scaled);
        }
        return result;
    }

    /**
     * 合并相同物品的数量
     */
    TradePaymentPlan consolidatePlan(TradePaymentPlan plan) {
        return new TradePaymentPlan(
            consolidateStacks(plan.playerInputs()),
            consolidateStacks(plan.playerOutputs()),
            consolidateStacks(plan.villageInputs()),
            consolidateStacks(plan.villageOutputs())
        );
    }

    /**
     * 合并物品列表中相同物品的数量
     */
    private List<ItemStack> consolidateStacks(List<ItemStack> stacks) {
        Map<TradeItemKey, Integer> itemCounts = new LinkedHashMap<>();
        Map<TradeItemKey, ItemStack> prototypes = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            TradeItemKey key = TradeItemKey.from(stack);
            itemCounts.merge(key, stack.getCount(), Integer::sum);
            prototypes.putIfAbsent(key, normalizedPrototype(stack));
        }
        List<ItemStack> result = new ArrayList<>();
        for (Map.Entry<TradeItemKey, Integer> entry : itemCounts.entrySet()) {
            ItemStack merged = prototypes.get(entry.getKey()).copy();
            merged.setCount(entry.getValue());
            result.add(merged);
        }
        return result;
    }

    /**
     * 计算物品总价值（用于返回结果）
     */
    private int calculateTotalValue(List<ItemStack> stacks) {
        int total = 0;
        for (ItemStack stack : stacks) {
            // 使用基础价值估算（实际执行时不需要，仅用于返回值）
            total += stack.getCount();  // 简化计算，实际可查询价值表
        }
        return total;
    }

    /**
     * 验证玩家库存
     */
    private List<ItemStack> validatePlayerInventory(ServerPlayer player, TradePaymentPlan plan) {
        return calculatePlayerShortfall(player.getInventory().items, plan.playerInputs());
    }

    /**
     * 验证村庄库存
     */
    private List<ItemStack> validateVillageInventory(CommercialNodeData nodeData, TradePaymentPlan plan) {
        List<ItemStack> shortfall = new ArrayList<>();
        for (ItemStack required : plan.villageInputs()) {
            NodeStockEntry entry = nodeData.getStock(required);
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

        for (ItemStack item : sortRequirementsForMatching(plan.playerInputs())) {
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

        Map<TradeItemKey, NodeStockEntry> stocks = new HashMap<>(nodeData.stocks());

        // 村庄输入：扣除库存（村庄卖出的商品 + 村庄支付的货币）
        for (ItemStack item : plan.villageInputs()) {
            TradeItemKey itemKey = TradeItemKey.from(item);
            NodeStockEntry current = stocks.get(itemKey);
            if (current == null && itemKey.hasComponents()) {
                current = stocks.get(TradeItemKey.of(itemKey.itemId()));
            }
            if (current != null) {
                stocks.put(itemKey, current.decrease(item.getCount()));
            }
        }

        // 村庄输出：增加库存（村庄买入的商品 + 村庄收到的货币）
        for (ItemStack item : plan.villageOutputs()) {
            TradeItemKey itemKey = TradeItemKey.from(item);
            NodeStockEntry current = stocks.get(itemKey);
            if (current == null && itemKey.hasComponents()) {
                current = stocks.get(TradeItemKey.of(itemKey.itemId()));
            }
            if (current != null) {
                stocks.put(itemKey, current.increase(item.getCount()));
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

    private void removeItemFromPlayer(ServerPlayer player, ItemStack toRemove) {
        removeMatchingItems(player.getInventory().items, toRemove);
    }

    private void addItemToPlayer(ServerPlayer player, ItemStack toAdd) {
        boolean success = player.getInventory().add(toAdd);
        if (!success && !toAdd.isEmpty()) {
            player.spawnAtLocation(toAdd);
        }
    }

    static int countMatchingItems(List<ItemStack> inventory, ItemStack required) {
        if (required == null || required.isEmpty()) {
            return 0;
        }

        TradeItemKey requiredKey = TradeItemKey.from(required);
        int count = 0;
        for (ItemStack stack : inventory) {
            if (matchesRequiredStack(stack, requiredKey, required.getItem())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    static int removeMatchingItems(List<ItemStack> inventory, ItemStack toRemove) {
        if (toRemove == null || toRemove.isEmpty()) {
            return 0;
        }

        TradeItemKey requiredKey = TradeItemKey.from(toRemove);
        int remaining = toRemove.getCount();
        for (int i = 0; i < inventory.size() && remaining > 0; i++) {
            ItemStack stack = inventory.get(i);
            if (!matchesRequiredStack(stack, requiredKey, toRemove.getItem())) {
                continue;
            }

            int toTake = Math.min(remaining, stack.getCount());
            stack.shrink(toTake);
            remaining -= toTake;
        }
        return toRemove.getCount() - remaining;
    }

    private static ItemStack normalizedPrototype(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static boolean matchesRequiredStack(ItemStack candidate, TradeItemKey requiredKey, Item requiredItem) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }
        if (requiredKey.hasComponents()) {
            return TradeItemKey.from(candidate).equals(requiredKey);
        }
        return candidate.getItem() == requiredItem;
    }

    static List<ItemStack> calculatePlayerShortfall(List<ItemStack> inventory, List<ItemStack> requiredItems) {
        List<ItemStack> workingInventory = copyInventory(inventory);
        List<ItemStack> shortfall = new ArrayList<>();
        for (ItemStack required : sortRequirementsForMatching(requiredItems)) {
            int removed = removeMatchingItems(workingInventory, required.copy());
            if (removed < required.getCount()) {
                ItemStack missing = required.copy();
                missing.setCount(required.getCount() - removed);
                shortfall.add(missing);
            }
        }
        return shortfall;
    }

    private static List<ItemStack> copyInventory(List<ItemStack> inventory) {
        List<ItemStack> copy = new ArrayList<>(inventory.size());
        for (ItemStack stack : inventory) {
            copy.add(stack.copy());
        }
        return copy;
    }

    private static List<ItemStack> sortRequirementsForMatching(List<ItemStack> requiredItems) {
        List<ItemStack> sorted = new ArrayList<>(requiredItems.size());
        for (ItemStack stack : requiredItems) {
            sorted.add(stack.copy());
        }
        sorted.sort((left, right) -> Boolean.compare(
            TradeItemKey.from(right).hasComponents(),
            TradeItemKey.from(left).hasComponents()
        ));
        return sorted;
    }
}
