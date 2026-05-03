package github.mczme.ruralroutes.menu.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 贸易展示槽位
 * 用于贸易站 GUI 中展示可交易物品
 *
 * 特点：
 * - 数量独立管理（baseStock 和 pendingCount），不修改 ItemStack 本身
 * - 不可拿起、不可放入
 * - displayStack 可变（非 final），用于初始化赋值
 */
public class TradeSlot extends Slot {

    private ResourceLocation itemId;    // 物品ID
    private ItemStack displayStack;     // 展示物品
    private int baseStock;              // 原始库存
    private int pendingCount;           // 暂存数量
    private int price;                  // 价格
    private boolean isBuy;              // true=购买（村庄卖给玩家），false=出售（玩家卖给村庄）

    /**
     * 创建空的贸易槽位
     */
    public TradeSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.displayStack = ItemStack.EMPTY;
        this.baseStock = 0;
        this.pendingCount = 0;
        this.price = 0;
        this.isBuy = true;
        this.itemId = null;
    }

    /**
     * 设置物品ID
     */
    public void setItemId(ResourceLocation itemId) {
        this.itemId = itemId;
    }

    /**
     * 获取物品ID
     */
    public ResourceLocation getItemId() {
        return itemId;
    }

    /**
     * 设置展示物品
     */
    public void setDisplayStack(ItemStack stack) {
        this.displayStack = stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * 获取展示物品（返回副本，数量为 stockCount）
     */
    public ItemStack getDisplayStack() {
        return displayStack;
    }

    /**
     * 设置原始库存数量
     */
    public void setBaseStock(int count) {
        this.baseStock = Math.max(0, count);
    }

    /**
     * 获取原始库存数量
     */
    public int getBaseStock() {
        return baseStock;
    }

    /**
     * 获取当前可用库存数量（原始库存 - 暂存数量）
     */
    public int getStockCount() {
        return Math.max(0, baseStock - pendingCount);
    }

    /**
     * 设置暂存数量
     */
    public void setPendingCount(int count) {
        this.pendingCount = Math.max(0, Math.min(count, baseStock));
    }

    /**
     * 获取暂存数量
     */
    public int getPendingCount() {
        return pendingCount;
    }

    /**
     * 添加暂存数量
     * @param count 要添加的数量
     * @return 实际添加的数量
     */
    public int addPending(int count) {
        int available = getStockCount();
        int toAdd = Math.min(count, available);
        pendingCount += toAdd;
        return toAdd;
    }

    /**
     * 清空暂存数量
     */
    public void clearPending() {
        this.pendingCount = 0;
    }

    /**
     * 设置价格
     */
    public void setPrice(int price) {
        this.price = Math.max(0, price);
    }

    /**
     * 获取价格
     */
    public int getPrice() {
        return price;
    }

    /**
     * 设置是否为购买槽位
     */
    public void setIsBuy(boolean isBuy) {
        this.isBuy = isBuy;
    }

    /**
     * 是否为购买槽位（村庄卖给玩家）
     */
    public boolean isBuy() {
        return isBuy;
    }

    /**
     * 是否为出售槽位（玩家卖给村庄）
     */
    public boolean isSell() {
        return !isBuy;
    }

    /**
     * 获取展示用物品堆（数量为 stockCount）
     */
    @Override
    public ItemStack getItem() {
        if (displayStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = displayStack.copy();
        result.setCount(getStockCount());
        return result;
    }

    @Override
    public void set(ItemStack stack) {
        // 不允许外部设置
    }

    @Override
    public ItemStack remove(int amount) {
        // 不允许移除
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // 不允许放入
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        // 不允许拿起
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return getStockCount();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getStockCount();
    }

    /**
     * 是否为空槽位
     */
    public boolean isEmpty() {
        return displayStack.isEmpty() || getStockCount() <= 0;
    }

    /**
     * 清空槽位
     */
    public void clear() {
        this.itemId = null;
        this.displayStack = ItemStack.EMPTY;
        this.baseStock = 0;
        this.pendingCount = 0;
        this.price = 0;
        this.isBuy = true;
    }

    /**
     * 复制另一个槽位的数据
     */
    public void copyFrom(TradeSlot other) {
        this.itemId = other.itemId;
        this.displayStack = other.displayStack.copy();
        this.baseStock = other.baseStock;
        this.pendingCount = other.pendingCount;
        this.price = other.price;
        this.isBuy = other.isBuy;
    }
}