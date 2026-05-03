package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.menu.container.TradeDisplayContainer;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import github.mczme.ruralroutes.network.packet.TradeSlotSyncPayload;
import github.mczme.ruralroutes.register.RRMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
import java.util.List;
import java.util.Map;

/**
 * 贸易站 GUI 菜单
 *
 * 槽位布局：
 * - 出售槽位 (sellSlots): 玩家可从村庄购买物品
 * - 收购槽位 (buySlots): 玩家可向村庄出售物品
 *
 * 使用 TradeSlot 实现不可交互的展示槽位
 * 槽位数量动态创建，基于 CommercialNodeData 中的 sellItems/buyItems 列表
 */
public class TradeStationMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private TradeDisplayContainer sellContainer;
    private TradeDisplayContainer buyContainer;
    private final List<TradeSlot> sellSlots;
    private final List<TradeSlot> buySlots;

    // 滚动偏移数据槽
    private final DataSlot sellScrollOffset;
    private final DataSlot buyScrollOffset;

    // 常量
    public static final int SLOT_SIZE = 18;
    public static final int SLOT_SPACING = 2;

    // 默认布局常量（用于无数据时的空槽位）
    private static final int DEFAULT_SELL_SLOTS = 16;
    private static final int DEFAULT_BUY_SLOTS = 16;

    public TradeStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public TradeStationMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(RRMenuTypes.TRADE_STATION.get(), containerId);
        this.blockPos = blockPos;

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
            initializeFromNodeData(nodeData);
        } else {
            // 无数据时创建空槽位
            initializeEmptySlots();
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

        // 创建容器
        this.sellContainer = new TradeDisplayContainer(Math.max(1, sellItems.size()));
        this.buyContainer = new TradeDisplayContainer(Math.max(1, buyItems.size()));

        // 创建出售槽位
        int sellStartX = 10;
        int sellStartY = 22;
        for (int i = 0; i < sellItems.size(); i++) {
            ResourceLocation itemId = sellItems.get(i);
            ItemStack displayStack = createItemStack(itemId);
            StockEntry entry = stocks.get(itemId);
            int stockCount = entry != null ? entry.current() : 0;

            int col = i / 2;
            int row = i % 2;
            int x = sellStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = sellStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(sellContainer, i, x, y);
            slot.setDisplayStack(displayStack);
            slot.setStockCount(stockCount);
            slot.setPrice(calculateSellPrice(itemId));

            sellSlots.add(slot);
            addSlot(slot);
        }

        // 创建收购槽位
        int buyStartX = 10;
        int buyStartY = 62;
        for (int i = 0; i < buyItems.size(); i++) {
            ResourceLocation itemId = buyItems.get(i);
            ItemStack displayStack = createItemStack(itemId);
            StockEntry entry = stocks.get(itemId);
            int stockCount = entry != null ? entry.current() : 0;

            int col = i / 2;
            int row = i % 2;
            int x = buyStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = buyStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(buyContainer, i, x, y);
            slot.setDisplayStack(displayStack);
            slot.setStockCount(stockCount);
            slot.setPrice(calculateBuyPrice(itemId));

            buySlots.add(slot);
            addSlot(slot);
        }
    }

    /**
     * 初始化空槽位（无数据时）
     */
    private void initializeEmptySlots() {
        this.sellContainer = new TradeDisplayContainer(DEFAULT_SELL_SLOTS);
        this.buyContainer = new TradeDisplayContainer(DEFAULT_BUY_SLOTS);

        int sellStartX = 10;
        int sellStartY = 22;
        for (int i = 0; i < DEFAULT_SELL_SLOTS; i++) {
            int col = i / 2;
            int row = i % 2;
            int x = sellStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = sellStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(sellContainer, i, x, y);
            sellSlots.add(slot);
            addSlot(slot);
        }

        int buyStartX = 10;
        int buyStartY = 62;
        for (int i = 0; i < DEFAULT_BUY_SLOTS; i++) {
            int col = i / 2;
            int row = i % 2;
            int x = buyStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = buyStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(buyContainer, i, x, y);
            buySlots.add(slot);
            addSlot(slot);
        }
    }

    /**
     * 从资源位置创建物品堆
     */
    private ItemStack createItemStack(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    /**
     * 计算出售价格（预留接口）
     */
    public int calculateSellPrice(ResourceLocation itemId) {
        // TODO: 从价值表计算
        return 0;
    }

    /**
     * 计算收购价格（预留接口）
     */
    public int calculateBuyPrice(ResourceLocation itemId) {
        // TODO: 从价值表计算
        return 0;
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
                slot.setStockCount(data.stockCount());
                slot.setPrice(data.price());
            }
        }

        // 更新收购槽位
        for (TradeSlotSyncPayload.SlotData data : buyData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < buySlots.size()) {
                TradeSlot slot = buySlots.get(data.slotIndex());
                slot.setDisplayStack(data.displayStack());
                slot.setStockCount(data.stockCount());
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
            buySlots.add(slot);
            addSlot(slot);
        }

        // 填充数据
        for (TradeSlotSyncPayload.SlotData data : sellData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < sellSlots.size()) {
                TradeSlot slot = sellSlots.get(data.slotIndex());
                slot.setDisplayStack(data.displayStack());
                slot.setStockCount(data.stockCount());
                slot.setPrice(data.price());
            }
        }

        for (TradeSlotSyncPayload.SlotData data : buyData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < buySlots.size()) {
                TradeSlot slot = buySlots.get(data.slotIndex());
                slot.setDisplayStack(data.displayStack());
                slot.setStockCount(data.stockCount());
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
                slot.getStockCount(),
                slot.getPrice()
            ));
        }

        List<TradeSlotSyncPayload.SlotData> buyData = new ArrayList<>();
        for (int i = 0; i < buySlots.size(); i++) {
            TradeSlot slot = buySlots.get(i);
            buyData.add(new TradeSlotSyncPayload.SlotData(
                i,
                slot.getDisplayStack(),
                slot.getStockCount(),
                slot.getPrice()
            ));
        }

        PacketDistributor.sendToPlayer(player,
            new TradeSlotSyncPayload(containerId, sellSlots.size(), buySlots.size(), sellData, buyData));
    }

    /**
     * 设置出售槽位数据（用于初始化）
     */
    public void setSellSlotData(int index, ItemStack stack, int stockCount, int price) {
        if (index >= 0 && index < sellSlots.size()) {
            TradeSlot slot = sellSlots.get(index);
            slot.setDisplayStack(stack);
            slot.setStockCount(stockCount);
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
            slot.setStockCount(stockCount);
            slot.setPrice(price);
        }
    }
}