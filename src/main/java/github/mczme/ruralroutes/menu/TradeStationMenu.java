package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.core.trade.TradeContractExecutor;
import github.mczme.ruralroutes.core.trade.TradePricingService;
import github.mczme.ruralroutes.core.trade.TradeResult;
import github.mczme.ruralroutes.core.trade.TradeSide;
import github.mczme.ruralroutes.menu.container.TradeDisplayContainer;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import github.mczme.ruralroutes.network.packet.PendingTradeSyncPayload;
import github.mczme.ruralroutes.network.packet.TradeSlotSyncPayload;
import github.mczme.ruralroutes.register.RRItemTags;
import github.mczme.ruralroutes.register.RRMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 贸易站 GUI 菜单
 *
 * 槽位布局：
 * - 出售槽位 (sellSlots): 玩家可从村庄购买物品（村庄出售）
 * - 收购槽位 (buySlots): 玩家可向村庄出售物品（村庄收购）
 * - 暂存区：支持多物品暂存
 *
 * 使用 TradeSlot 实现不可交互的展示槽位
 * 槽位数量动态创建，基于 CommercialNodeData 中的 sellItems/buyItems 列表
 */
public class TradeStationMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final Player player;
    private TradeDisplayContainer sellContainer;
    private TradeDisplayContainer buyContainer;
    private final List<TradeSlot> sellSlots;
    private final List<TradeSlot> buySlots;

    // 多物品暂存区
    private List<PendingTradeSlot> pendingSlots = new ArrayList<>();
    private boolean isBuyTrade = true; // true=购买交易，false=出售交易

    // 当前周期索引（用于跨周期检测）
    private long currentCycleIndex;

    // 滚动偏移数据槽
    private final DataSlot sellScrollOffset;
    private final DataSlot buyScrollOffset;

    // 常量
    public static final int SLOT_SIZE = 18;
    public static final int SLOT_SPACING = 2;

    public TradeStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos(), data.readVarInt(), data.readVarInt());
    }

    public TradeStationMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        this(containerId, playerInventory, blockPos, 0, 0);
    }

    /**
     * 完整构造函数
     * @param sellSlotCount 出售槽位数量（客户端从服务端接收）
     * @param buySlotCount 收购槽位数量（客户端从服务端接收）
     */
    public TradeStationMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
            int sellSlotCount, int buySlotCount) {
        super(RRMenuTypes.TRADE_STATION.get(), containerId);
        this.blockPos = blockPos;
        this.player = playerInventory.player;

        // 初始化槽位列表
        this.sellSlots = new ArrayList<>();
        this.buySlots = new ArrayList<>();

        // 初始化滚动偏移数据槽
        this.sellScrollOffset = DataSlot.standalone();
        this.buyScrollOffset = DataSlot.standalone();

        // 从区块数据查询并初始化
        Level level = playerInventory.player.level();
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(level, blockPos);

        if (nodeData != null) {
            // 服务端：从节点数据初始化
            initializeFromNodeData(nodeData);
        } else {
            // 客户端：使用服务端传递的槽位数量创建空槽位
            initializeEmptySlots(sellSlotCount, buySlotCount);
        }

        // 添加数据槽
        addDataSlot(sellScrollOffset);
        addDataSlot(buyScrollOffset);
    }

    /**
     * 从商业节点数据初始化槽位
     */
    private void initializeFromNodeData(CommercialNodeData nodeData) {
        sellSlots.clear();
        buySlots.clear();
        slots.removeIf(slot -> slot instanceof TradeSlot);

        List<ResourceLocation> sellItems = nodeData.sellItems();
        List<ResourceLocation> buyItems = nodeData.buyItems();
        Map<ResourceLocation, StockEntry> stocks = nodeData.stocks();

        // 计算过滤货币后的实际槽位数量
        int sellSlotCount = countNonCurrencyItems(sellItems);
        int buySlotCount = countNonCurrencyItems(buyItems);

        // 创建容器
        this.sellContainer = new TradeDisplayContainer(Math.max(1, sellSlotCount));
        this.buyContainer = new TradeDisplayContainer(Math.max(1, buySlotCount));

        // 创建出售槽位（村庄卖给玩家），过滤货币物品
        int sellStartX = 10;
        int sellStartY = 22;
        int sellSlotIndex = 0;
        for (int i = 0; i < sellItems.size(); i++) {
            ResourceLocation itemId = sellItems.get(i);
            ItemStack displayStack = createItemStack(itemId);

            // 跳过货币物品
            if (isCurrencyItem(displayStack)) {
                continue;
            }

            StockEntry entry = stocks.get(itemId);
            int stockCount = entry != null ? entry.current() : 0;

            int col = sellSlotIndex / 2;
            int row = sellSlotIndex % 2;
            int x = sellStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = sellStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(sellContainer, sellSlotIndex, x, y);
            slot.setItemId(itemId);
            slot.setDisplayStack(displayStack);
            slot.setBaseStock(stockCount);
            slot.setPrice(calculateSellPrice(itemId));
            slot.setIsBuy(true); // 此槽位用于玩家购买

            sellSlots.add(slot);
            addSlot(slot);
            sellSlotIndex++;
        }

        // 创建收购槽位（村庄收购玩家物品），过滤货币物品
        int buyStartX = 10;
        int buyStartY = 62;
        int buySlotIndex = 0;
        for (int i = 0; i < buyItems.size(); i++) {
            ResourceLocation itemId = buyItems.get(i);
            ItemStack displayStack = createItemStack(itemId);

            // 跳过货币物品
            if (isCurrencyItem(displayStack)) {
                continue;
            }

            StockEntry entry = stocks.get(itemId);
            int currentStock = entry != null ? entry.current() : 0;
            int maxStock = entry != null ? entry.max() : 0;

            int col = buySlotIndex / 2;
            int row = buySlotIndex % 2;
            int x = buyStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = buyStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(buyContainer, buySlotIndex, x, y);
            slot.setItemId(itemId);
            slot.setDisplayStack(displayStack);
            slot.setBaseStock(currentStock);  // 已收购数量
            slot.setMaxStock(maxStock);       // 收购上限
            slot.setPrice(calculateBuyPrice(itemId));
            slot.setIsBuy(false); // 此槽位用于玩家出售

            buySlots.add(slot);
            addSlot(slot);
            buySlotIndex++;
        }

        // 初始化周期索引（服务端）
        if (player instanceof ServerPlayer serverPlayer) {
            CycleManager cycleManager = CycleManager.get(serverPlayer.serverLevel());
            this.currentCycleIndex = cycleManager.getCycleIndex(serverPlayer.serverLevel().getGameTime());
        }
    }

    /**
     * 检查物品是否为货币（静态方法，供外部调用）
     */
    public static boolean isCurrencyItem(ItemStack stack) {
        return stack.is(RRItemTags.CURRENCY) || stack.is(RRItemTags.CURRENCY_BASE);
    }

    /**
     * 计算非货币物品数量（静态方法，供外部调用）
     */
    public static int countNonCurrencyItems(List<ResourceLocation> itemIds) {
        int count = 0;
        for (ResourceLocation itemId : itemIds) {
            ItemStack stack = createItemStack(itemId);
            if (!isCurrencyItem(stack)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 初始化空槽位（客户端使用服务端传递的槽位数量）
     */
    private void initializeEmptySlots(int sellSlotCount, int buySlotCount) {
        this.sellContainer = new TradeDisplayContainer(Math.max(1, sellSlotCount));
        this.buyContainer = new TradeDisplayContainer(Math.max(1, buySlotCount));

        int sellStartX = 10;
        int sellStartY = 22;
        for (int i = 0; i < sellSlotCount; i++) {
            int col = i / 2;
            int row = i % 2;
            int x = sellStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = sellStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(sellContainer, i, x, y);
            slot.setIsBuy(true);
            sellSlots.add(slot);
            addSlot(slot);
        }

        int buyStartX = 10;
        int buyStartY = 62;
        for (int i = 0; i < buySlotCount; i++) {
            int col = i / 2;
            int row = i % 2;
            int x = buyStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = buyStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(buyContainer, i, x, y);
            slot.setIsBuy(false);
            buySlots.add(slot);
            addSlot(slot);
        }
    }

    /**
     * 从资源位置创建物品堆
     */
    private static ItemStack createItemStack(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    /**
     * 计算出售价格（村庄卖给玩家）
     * 使用统一定价服务
     */
    public int calculateSellPrice(ResourceLocation itemId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        ItemStack stack = createItemStack(itemId);
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            return 0;
        }

        return TradePricingService.calculateFinalPrice(
            serverPlayer.serverLevel(),
            nodeData,
            stack,
            TradeSide.SELL_TO_PLAYER
        );
    }

    /**
     * 计算收购价格（玩家卖给村庄）
     * 使用统一定价服务
     */
    public int calculateBuyPrice(ResourceLocation itemId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        ItemStack stack = createItemStack(itemId);
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            return 0;
        }

        return TradePricingService.calculateFinalPrice(
            serverPlayer.serverLevel(),
            nodeData,
            stack,
            TradeSide.BUY_FROM_PLAYER
        );
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(blockPos) instanceof TradeStationBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 不支持快速移动
        return ItemStack.EMPTY;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * 获取出售槽位列表
     */
    public List<TradeSlot> getSellSlots() {
        return sellSlots;
    }

    /**
     * 获取收购槽位列表
     */
    public List<TradeSlot> getBuySlots() {
        return buySlots;
    }

    /**
     * 检查是否可以添加物品到暂存区
     * @param isBuy true=购买交易，false=出售交易
     */
    public boolean canAddItem(boolean isBuy) {
        // 允许混合交易，不再限制交易类型
        return true;
    }

    /**
     * 获取暂存区槽位列表
     */
    public List<PendingTradeSlot> getPendingSlots() {
        return pendingSlots;
    }

    /**
     * 是否为购买交易
     */
    public boolean isBuyTrade() {
        return isBuyTrade;
    }

    /**
     * 获取总价
     */
    public int getTotalPrice() {
        return pendingSlots.stream()
            .mapToInt(slot -> slot.getPrice() * slot.getBaseStock())
            .sum();
    }

    /**
     * 暂存区是否有待处理交易
     */
    public boolean hasPendingTrade() {
        return !pendingSlots.isEmpty();
    }

    /**
     * 添加物品到暂存区
     * @param isBuy true=购买交易（点击出售槽位），false=出售交易（点击收购槽位）
     * @param slotIndex 来源槽位索引
     */
    public void addTradeEntry(boolean isBuy, int slotIndex) {
        // 获取来源槽位
        List<TradeSlot> sourceSlots = isBuy ? sellSlots : buySlots;
        if (slotIndex < 0 || slotIndex >= sourceSlots.size()) return;

        TradeSlot sourceSlot = sourceSlots.get(slotIndex);
        // 收购区（玩家卖给村庄）只检查 displayStack，出售区检查完整 isEmpty
        if (isBuy) {
            if (sourceSlot.isEmpty()) return;
        } else {
            if (sourceSlot.getDisplayStack().isEmpty()) return;
        }

        int toAdd = 1;
        int actualAdd = sourceSlot.addPending(toAdd, isBuy);
        if (actualAdd <= 0) return;

        ResourceLocation itemId = sourceSlot.getItemId();

        // 查找是否已有该物品的 PendingTradeSlot
        PendingTradeSlot existingSlot = null;
        for (PendingTradeSlot pending : pendingSlots) {
            if (pending.getItemId() != null && pending.getItemId().equals(itemId)
                && pending.getSourceSlotIndex() == slotIndex) {
                existingSlot = pending;
                break;
            }
        }

        if (existingSlot != null) {
            // 累加数量：增加 baseStock
            existingSlot.setBaseStock(existingSlot.getBaseStock() + actualAdd);
        } else {
            // 新建 PendingTradeSlot
            PendingTradeSlot newSlot = new PendingTradeSlot(
                new TradeDisplayContainer(1), pendingSlots.size(), 0, 0);
            newSlot.setItemId(itemId);
            newSlot.setDisplayStack(sourceSlot.getDisplayStack().copy());
            newSlot.setBaseStock(actualAdd);
            newSlot.setPrice(sourceSlot.getPrice());
            newSlot.setIsBuy(isBuy);
            newSlot.setSource(slotIndex, isBuy);
            pendingSlots.add(newSlot);
        }

        // 6. 同步到客户端
        syncPendingTradeToClient();
    }

    /**
     * 移除暂存区条目（点击暂存区物品）
     * @param slotIndex 槽位索引
     */
    public void removeTradeEntry(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= pendingSlots.size()) return;

        PendingTradeSlot pending = pendingSlots.get(slotIndex);

        // 恢复来源槽位
        if (pending.hasSource()) {
            List<TradeSlot> sourceSlots = isBuyTrade ? sellSlots : buySlots;
            int srcIdx = pending.getSourceSlotIndex();
            if (srcIdx >= 0 && srcIdx < sourceSlots.size()) {
                TradeSlot source = sourceSlots.get(srcIdx);
                int currentPending = source.getPendingCount();
                source.setPendingCount(Math.max(0, currentPending - pending.getBaseStock()));
            }
        }

        pendingSlots.remove(slotIndex);

        if (pendingSlots.isEmpty()) {
            isBuyTrade = true;
        }

        syncPendingTradeToClient();
    }

    /**
     * 清空暂存区
     */
    public void clearPendingTrade() {
        List<TradeSlot> sourceSlots = isBuyTrade ? sellSlots : buySlots;
        for (PendingTradeSlot pending : pendingSlots) {
            if (pending.hasSource()) {
                int srcIdx = pending.getSourceSlotIndex();
                if (srcIdx >= 0 && srcIdx < sourceSlots.size()) {
                    TradeSlot source = sourceSlots.get(srcIdx);
                    int currentPending = source.getPendingCount();
                    source.setPendingCount(Math.max(0, currentPending - pending.getBaseStock()));
                }
            }
        }

        pendingSlots.clear();
        isBuyTrade = true;

        syncPendingTradeToClient();
    }

    /**
     * 获取出售区滚动偏移
     */
    public int getSellScrollOffset() {
        return sellScrollOffset.get();
    }

    /**
     * 设置出售区滚动偏移
     */
    public void setSellScrollOffset(int offset) {
        sellScrollOffset.set(offset);
    }

    /**
     * 获取收购区滚动偏移
     */
    public int getBuyScrollOffset() {
        return buyScrollOffset.get();
    }

    /**
     * 设置收购区滚动偏移
     */
    public void setBuyScrollOffset(int offset) {
        buyScrollOffset.set(offset);
    }

    /**
     * 处理交易请求
     * @param requestType 请求类型：0=ADD_BUY, 1=ADD_SELL, 2=REMOVE_ENTRY, 3=CLEAR, 4=REMOVE_BUY, 5=REMOVE_SELL, 6=CONFIRM
     * @param slotIndex 槽位索引或条目索引
     */
    public void handleTradeRequest(int requestType, int slotIndex) {
        switch (requestType) {
            case 0 -> addTradeEntry(true, slotIndex);   // ADD_BUY
            case 1 -> addTradeEntry(false, slotIndex);  // ADD_SELL
            case 2 -> removeTradeEntry(slotIndex);       // REMOVE_ENTRY (legacy)
            case 3 -> clearPendingTrade();               // CLEAR
            case 4 -> removeTradeEntryByType(true, slotIndex);  // REMOVE_BUY
            case 5 -> removeTradeEntryByType(false, slotIndex); // REMOVE_SELL
            case 6 -> executeTrade();                    // CONFIRM
        }
    }

    /**
     * 执行交易
     * 由服务端在收到 CONFIRM 请求时调用
     * 直接从暂存区读取数据，服务端权威计算价格
     */
    public void executeTrade() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (pendingSlots.isEmpty()) return;

        // 获取商业节点数据
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            player.sendSystemMessage(Component.translatable("gui.ruralroutes.trade_station.error.no_data"));
            return;
        }

        // 执行交易（直接从 pendingSlots 读取）
        TradeResult result = TradeContractExecutor.INSTANCE.executeMixedTrade(
            serverPlayer.serverLevel(),
            nodeData,
            serverPlayer,
            pendingSlots,
            currentCycleIndex,
            blockPos
        );

        if (result.isSuccess()) {
            // 交易成功：��空暂存区，刷新库存显示
            pendingSlots.clear();
            isBuyTrade = true;

            // 重置所有槽位的 pendingCount
            for (TradeSlot slot : sellSlots) {
                slot.setPendingCount(0);
            }
            for (TradeSlot slot : buySlots) {
                slot.setPendingCount(0);
            }

            // 重新加载节点数据（已被 TradeEngine 更新）
            CommercialNodeData updatedData = CommercialNodeManager.getNodeData(player.level(), blockPos);
            if (updatedData != null) {
                updateSlotsFromNodeData(updatedData);
            }

            // 同步到客户端
            syncSlotDataToClient(serverPlayer);
            syncPendingTradeToClient();

            player.sendSystemMessage(Component.translatable("gui.ruralroutes.trade_station.success"));
        } else if (result.reason() == TradeResult.Reason.CYCLE_CHANGED) {
            // 周期变化：刷新 GUI 并通知玩家
            CycleManager cycleManager = CycleManager.get(serverPlayer.serverLevel());
            this.currentCycleIndex = cycleManager.getCycleIndex(serverPlayer.serverLevel().getGameTime());

            // 刷新节点数据
            CommercialNodeData updatedData = CommercialNodeManager.getNodeData(player.level(), blockPos);
            if (updatedData != null) {
                updateSlotsFromNodeData(updatedData);
                // 重新计算价格
                for (TradeSlot slot : sellSlots) {
                    slot.setPrice(calculateSellPrice(slot.getItemId()));
                }
                for (TradeSlot slot : buySlots) {
                    slot.setPrice(calculateBuyPrice(slot.getItemId()));
                }
            }

            syncSlotDataToClient(serverPlayer);
            player.sendSystemMessage(Component.translatable("gui.ruralroutes.trade_station.error.cycle_changed"));
        } else {
            // 其他交易失败：发送失败原因
            Component failMessage = Component.translatable(result.reason().getTranslationKey());
            player.sendSystemMessage(failMessage);
        }
    }

    /**
     * 从更新后的节点数据更新槽位库存
     */
    private void updateSlotsFromNodeData(CommercialNodeData nodeData) {
        Map<ResourceLocation, StockEntry> stocks = nodeData.stocks();

        for (TradeSlot slot : sellSlots) {
            ResourceLocation itemId = slot.getItemId();
            StockEntry entry = stocks.get(itemId);
            if (entry != null) {
                slot.setBaseStock(entry.current());
            }
        }

        for (TradeSlot slot : buySlots) {
            ResourceLocation itemId = slot.getItemId();
            StockEntry entry = stocks.get(itemId);
            if (entry != null) {
                slot.setBaseStock(entry.current());
            }
        }
    }

    /**
     * 根据类型移除暂存区条目
     * @param isBuy true=移除买入条目，false=移除卖出条目
     * @param sourceSlotIndex 来源槽位索引
     */
    private void removeTradeEntryByType(boolean isBuy, int sourceSlotIndex) {
        // 找到对应的 pendingSlot
        for (int i = 0; i < pendingSlots.size(); i++) {
            PendingTradeSlot pending = pendingSlots.get(i);
            if (pending.isBuy() == isBuy && pending.getSourceSlotIndex() == sourceSlotIndex) {
                // 恢复来源槽位的暂存计数
                List<TradeSlot> sourceSlots = isBuy ? sellSlots : buySlots;
                if (sourceSlotIndex >= 0 && sourceSlotIndex < sourceSlots.size()) {
                    TradeSlot source = sourceSlots.get(sourceSlotIndex);
                    int currentPending = source.getPendingCount();
                    source.setPendingCount(Math.max(0, currentPending - pending.getBaseStock()));
                }

                pendingSlots.remove(i);
                break;
            }
        }

        if (pendingSlots.isEmpty()) {
            isBuyTrade = true;
        }

        syncPendingTradeToClient();
    }

    /**
     * 接收从服务端同步的槽位数据（客户端调用）
     * 支持动态槽位数量
     */
    public void receiveSlotData(int newSellSlotCount, int newBuySlotCount,
            List<TradeSlotSyncPayload.SlotData> sellData, List<TradeSlotSyncPayload.SlotData> buyData) {

        // 如果槽位数量不匹配，需要重建槽位
        if (sellSlots.size() != newSellSlotCount || buySlots.size() != newBuySlotCount) {
            rebuildSlotsFromSyncData(newSellSlotCount, newBuySlotCount, sellData, buyData);
            return;
        }

        // 更新出售槽位
        for (TradeSlotSyncPayload.SlotData data : sellData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < sellSlots.size()) {
                TradeSlot slot = sellSlots.get(data.slotIndex());
                slot.setDisplayStack(data.displayStack());
                slot.setBaseStock(data.stockCount());
                slot.setMaxStock(data.maxStock());
                slot.setPrice(data.price());
            }
        }

        // 更新收购槽位
        for (TradeSlotSyncPayload.SlotData data : buyData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < buySlots.size()) {
                TradeSlot slot = buySlots.get(data.slotIndex());
                slot.setDisplayStack(data.displayStack());
                slot.setBaseStock(data.stockCount());
                slot.setMaxStock(data.maxStock());
                slot.setPrice(data.price());
            }
        }
    }

    /**
     * 根据同步数据重建槽位（客户端调用）
     */
    private void rebuildSlotsFromSyncData(int newSellSlotCount, int newBuySlotCount,
            List<TradeSlotSyncPayload.SlotData> sellData, List<TradeSlotSyncPayload.SlotData> buyData) {

        // 清除旧槽位
        sellSlots.clear();
        buySlots.clear();
        slots.removeIf(slot -> slot instanceof TradeSlot);

        // 创建新容器
        this.sellContainer = new TradeDisplayContainer(Math.max(1, newSellSlotCount));
        this.buyContainer = new TradeDisplayContainer(Math.max(1, newBuySlotCount));

        int sellStartX = 10;
        int sellStartY = 22;
        for (int i = 0; i < newSellSlotCount; i++) {
            int col = i / 2;
            int row = i % 2;
            int x = sellStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = sellStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(sellContainer, i, x, y);
            slot.setIsBuy(true);
            sellSlots.add(slot);
            addSlot(slot);
        }

        int buyStartX = 10;
        int buyStartY = 62;
        for (int i = 0; i < newBuySlotCount; i++) {
            int col = i / 2;
            int row = i % 2;
            int x = buyStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = buyStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(buyContainer, i, x, y);
            slot.setIsBuy(false);
            buySlots.add(slot);
            addSlot(slot);
        }

        // 填充数据
        for (TradeSlotSyncPayload.SlotData data : sellData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < sellSlots.size()) {
                TradeSlot slot = sellSlots.get(data.slotIndex());
                slot.setDisplayStack(data.displayStack());
                slot.setBaseStock(data.stockCount());
                slot.setMaxStock(data.maxStock());
                slot.setPrice(data.price());
            }
        }

        for (TradeSlotSyncPayload.SlotData data : buyData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < buySlots.size()) {
                TradeSlot slot = buySlots.get(data.slotIndex());
                slot.setDisplayStack(data.displayStack());
                slot.setBaseStock(data.stockCount());
                slot.setMaxStock(data.maxStock());
                slot.setPrice(data.price());
            }
        }
    }

    /**
     * 同步槽位数据到客户端（服务端调用）
     */
    public void syncSlotDataToClient(ServerPlayer player) {
        List<TradeSlotSyncPayload.SlotData> sellData = new ArrayList<>();
        for (int i = 0; i < sellSlots.size(); i++) {
            TradeSlot slot = sellSlots.get(i);
            sellData.add(new TradeSlotSyncPayload.SlotData(
                i,
                slot.getDisplayStack(),
                slot.getBaseStock(),
                slot.getMaxStock(),
                slot.getPrice(),
                true
            ));
        }

        List<TradeSlotSyncPayload.SlotData> buyData = new ArrayList<>();
        for (int i = 0; i < buySlots.size(); i++) {
            TradeSlot slot = buySlots.get(i);
            buyData.add(new TradeSlotSyncPayload.SlotData(
                i,
                slot.getDisplayStack(),
                slot.getBaseStock(),
                slot.getMaxStock(),
                slot.getPrice(),
                false
            ));
        }

        PacketDistributor.sendToPlayer(player,
            new TradeSlotSyncPayload(containerId, sellSlots.size(), buySlots.size(), sellData, buyData));
    }

    /**
     * 同步暂存区数据到客户端（服务端调用）
     */
    private void syncPendingTradeToClient() {
        if (!(getPlayer() instanceof ServerPlayer serverPlayer)) return;

        // 构建槽位数据列表
        List<PendingTradeSyncPayload.PendingSlotData> slotDataList = new ArrayList<>();
        for (PendingTradeSlot slot : pendingSlots) {
            slotDataList.add(new PendingTradeSyncPayload.PendingSlotData(
                slot.getItemId(),
                slot.getDisplayStack(),
                slot.getBaseStock(),
                slot.getPrice(),
                slot.isBuy(),
                slot.getSourceSlotIndex()
            ));
        }

        // 构建 pendingCountMap（发送所有槽位的 pendingCount，包括 0）
        Map<Integer, Integer> pendingCountMap = new HashMap<>();
        for (int i = 0; i < sellSlots.size(); i++) {
            pendingCountMap.put(i, sellSlots.get(i).getPendingCount());
        }
        for (int i = 0; i < buySlots.size(); i++) {
            pendingCountMap.put(sellSlots.size() + i, buySlots.get(i).getPendingCount());
        }

        PacketDistributor.sendToPlayer(serverPlayer,
            new PendingTradeSyncPayload(containerId, isBuyTrade, slotDataList, pendingCountMap));
    }

    /**
     * 接收暂存区同步数据（客户端调用）
     */
    public void receivePendingTradeData(PendingTradeSyncPayload payload) {
        // 更新暂存区槽位
        pendingSlots.clear();
        for (PendingTradeSyncPayload.PendingSlotData data : payload.pendingSlots()) {
            PendingTradeSlot slot = new PendingTradeSlot(
                new TradeDisplayContainer(1), pendingSlots.size(), 0, 0);
            slot.setItemId(data.itemId());
            slot.setDisplayStack(data.displayStack().copy());
            slot.setBaseStock(data.stockCount());
            slot.setPrice(data.price());
            slot.setIsBuy(data.isBuy());
            slot.setSource(data.sourceSlotIndex(), data.isBuy());
            pendingSlots.add(slot);
        }

        // 更新交易类型
        isBuyTrade = payload.isBuyTrade();

        // 更新来源槽位的暂存计数
        Map<Integer, Integer> pendingCountMap = payload.pendingCountMap();
        for (Map.Entry<Integer, Integer> mapEntry : pendingCountMap.entrySet()) {
            int slotIndex = mapEntry.getKey();
            int pendingCount = mapEntry.getValue();

            if (slotIndex < sellSlots.size()) {
                sellSlots.get(slotIndex).setPendingCount(pendingCount);
            } else if (slotIndex < sellSlots.size() + buySlots.size()) {
                int buyIndex = slotIndex - sellSlots.size();
                if (buyIndex < buySlots.size()) {
                    buySlots.get(buyIndex).setPendingCount(pendingCount);
                }
            }
        }
    }

    /**
     * 获取玩家
     */
    private Player getPlayer() {
        return player;
    }

    /**
     * 设置出售槽位数据（用于初始化）
     */
    public void setSellSlotData(int index, ItemStack stack, int stockCount, int price) {
        if (index >= 0 && index < sellSlots.size()) {
            TradeSlot slot = sellSlots.get(index);
            slot.setDisplayStack(stack);
            slot.setBaseStock(stockCount);
            slot.setPrice(price);
        }
    }

    /**
     * 设置收购槽位数据（用于初始化）
     */
    public void setBuySlotData(int index, ItemStack stack, int stockCount, int price) {
        if (index >= 0 && index < buySlots.size()) {
            TradeSlot slot = buySlots.get(index);
            slot.setDisplayStack(stack);
            slot.setBaseStock(stockCount);
            slot.setPrice(price);
        }
    }
}
