package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.core.value.ValueTableManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 交易引擎实现
 */
public final class TradeEngineImpl implements TradeEngine {

    public static final TradeEngineImpl INSTANCE = new TradeEngineImpl();

    private final TradeLockManager lockManager;

    private TradeEngineImpl() {
        this.lockManager = new TradeLockManager();
    }

    @Override
    public TradeResult executeTrade(CommercialNodeData nodeData, Player player, TradeRequest request) {
        ChunkPos chunkPos = extractChunkPos(nodeData, player);
        if (chunkPos == null || !lockManager.acquire(chunkPos)) {
            return TradeResult.fail(TradeResult.FailReason.INVALID_REQUEST, List.of());
        }

        try {
            return doExecute(nodeData, player, request);
        } finally {
            lockManager.release(chunkPos);
        }
    }

    private TradeResult doExecute(CommercialNodeData nodeData, Player player, TradeRequest request) {
        // 1. 验证请求有效性
        if (!request.isValid()) {
            return TradeResult.fail(TradeResult.FailReason.INVALID_REQUEST, List.of());
        }

        // 2. 验证价值匹配
        int giveValue = calculateTotalValue(request.giveItems());
        int takeValue = calculateTotalValue(request.takeItems());

        if (giveValue != takeValue) {
            return TradeResult.fail(TradeResult.FailReason.VALUE_MISMATCH, List.of());
        }

        // 3. 验证玩家库存
        List<ItemStack> playerShortfall = checkPlayerInventory(player, request.giveItems());
        if (!playerShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.FailReason.PLAYER_INSUFFICIENT, playerShortfall);
        }

        // 4. 验证村庄库存
        List<ItemStack> villageShortfall = checkVillageInventory(nodeData, request.takeItems());
        if (!villageShortfall.isEmpty()) {
            return TradeResult.fail(TradeResult.FailReason.VILLAGE_INSUFFICIENT, villageShortfall);
        }

        // 5. 验证玩家背包空间
        if (!checkPlayerSpace(player, request)) {
            return TradeResult.fail(TradeResult.FailReason.PLAYER_NO_SPACE, List.of());
        }

        // 6. 原子执行
        executeTransfer(nodeData, player, request);

        return TradeResult.success(giveValue);
    }

    @Override
    public boolean canExecuteTrade(CommercialNodeData nodeData, Player player, TradeRequest request) {
        if (!request.isValid()) {
            return false;
        }

        int giveValue = calculateTotalValue(request.giveItems());
        int takeValue = calculateTotalValue(request.takeItems());

        if (giveValue != takeValue) {
            return false;
        }

        if (!checkPlayerInventory(player, request.giveItems()).isEmpty()) {
            return false;
        }

        if (!checkVillageInventory(nodeData, request.takeItems()).isEmpty()) {
            return false;
        }

        return checkPlayerSpace(player, request);
    }

    @Override
    public List<ItemStack> calculateTradeShortfall(CommercialNodeData nodeData, Player player, TradeRequest request) {
        List<ItemStack> shortfall = new ArrayList<>();
        shortfall.addAll(checkPlayerInventory(player, request.giveItems()));
        shortfall.addAll(checkVillageInventory(nodeData, request.takeItems()));
        return shortfall;
    }

    // ===== 内部辅助方法 =====

    /**
     * 计算物品组的总价值
     */
    private int calculateTotalValue(List<ItemStack> items) {
        int total = 0;
        for (ItemStack stack : items) {
            total += ValueTableManager.queryBaseValue(stack) * stack.getCount();
        }
        return total;
    }

    /**
     * 检查玩家库存
     * @return 不足的物品列表
     */
    private List<ItemStack> checkPlayerInventory(Player player, List<ItemStack> required) {
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
     * 检查村庄库存
     * @return 不足的物品列表
     */
    private List<ItemStack> checkVillageInventory(CommercialNodeData nodeData, List<ItemStack> required) {
        List<ItemStack> shortfall = new ArrayList<>();
        for (ItemStack requiredStack : required) {
            ResourceLocation itemId = getItemId(requiredStack);
            StockEntry entry = nodeData.getStock(itemId);

            int available = (entry != null) ? entry.current() : 0;
            if (available < requiredStack.getCount()) {
                shortfall.add(new ItemStack(requiredStack.getItem(), requiredStack.getCount() - available));
            }
        }
        return shortfall;
    }

    /**
     * 检查玩家背包空间
     */
    private boolean checkPlayerSpace(Player player, TradeRequest request) {
        int emptySlots = countEmptySlots(player);
        int neededSlots = calculateNeededSlots(player, request.takeItems());
        int releasedSlots = calculateReleasedSlots(player, request.giveItems());

        return emptySlots + releasedSlots >= neededSlots;
    }

    /**
     * 执行物品转移
     */
    private void executeTransfer(CommercialNodeData nodeData, Player player, TradeRequest request) {
        // 玩家背包 -= 付出组
        for (ItemStack give : request.giveItems()) {
            removeItemFromPlayer(player, give);
        }

        // 玩家背包 += 获得组
        for (ItemStack take : request.takeItems()) {
            addItemToPlayer(player, take.copy());
        }

        // 更新村庄库存
        updateVillageStocks(nodeData, player, request);
    }

    /**
     * 更新村庄库存
     */
    private void updateVillageStocks(CommercialNodeData nodeData, Player player, TradeRequest request) {
        Map<ResourceLocation, StockEntry> stocks = new java.util.HashMap<>(nodeData.stocks());

        // 村庄获得玩家付出的物品
        for (ItemStack give : request.giveItems()) {
            ResourceLocation itemId = getItemId(give);
            StockEntry current = stocks.get(itemId);
            if (current != null) {
                stocks.put(itemId, current.increase(give.getCount()));
            }
        }

        // 村庄失去玩家获得的物品
        for (ItemStack take : request.takeItems()) {
            ResourceLocation itemId = getItemId(take);
            StockEntry current = stocks.get(itemId);
            if (current != null) {
                stocks.put(itemId, current.decrease(take.getCount()));
            }
        }

        // 保存更新后的数据
        CommercialNodeData newData = new CommercialNodeData(
            nodeData.tradeNodeId(),
            nodeData.themeName(),
            stocks,
            nodeData.specialties(),
            nodeData.refreshTimestamp()
        );
        CommercialNodeManager.updateNodeData(player.level(), player.blockPosition(), newData);
    }

    /**
     * 计算玩家背包中某物品的数量
     */
    private int countItemInPlayerInventory(Player player, net.minecraft.world.item.Item item) {
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
    private void removeItemFromPlayer(Player player, ItemStack toRemove) {
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
     * 向玩家背包添加物品
     */
    private void addItemToPlayer(Player player, ItemStack toAdd) {
        player.getInventory().add(toAdd);
    }

    /**
     * 计算玩家背包空槽数量
     */
    private int countEmptySlots(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 计算获得组需要的空槽数
     */
    private int calculateNeededSlots(Player player, List<ItemStack> takeItems) {
        int needed = 0;
        for (ItemStack take : takeItems) {
            int has = countItemInPlayerInventory(player, take.getItem());
            int maxStack = take.getMaxStackSize();
            // 当前占用的槽数
            int currentSlots = (has + maxStack - 1) / maxStack;
            // 添加后需要的总槽数
            int totalAfter = (has + take.getCount() + maxStack - 1) / maxStack;
            needed += totalAfter - currentSlots;
        }
        return needed;
    }

    /**
     * 计算付出组释放的空槽数
     */
    private int calculateReleasedSlots(Player player, List<ItemStack> giveItems) {
        int released = 0;
        for (ItemStack give : giveItems) {
            int has = countItemInPlayerInventory(player, give.getItem());
            int maxStack = give.getMaxStackSize();
            if (has <= give.getCount() && has > 0) {
                released++;
            }
        }
        return released;
    }

    /**
     * 获取物品的 ResourceLocation
     */
    private ResourceLocation getItemId(ItemStack stack) {
        return stack.getItemHolder().unwrapKey()
            .map(key -> key.location())
            .orElse(ResourceLocation.parse("minecraft:air"));
    }

    /**
     * 提取区块坐标（简化实现）
     */
    private ChunkPos extractChunkPos(CommercialNodeData nodeData, Player player) {
        return new ChunkPos(player.blockPosition());
    }
}
