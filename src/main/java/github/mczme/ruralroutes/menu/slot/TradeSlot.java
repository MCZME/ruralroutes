package github.mczme.ruralroutes.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 贸易展示槽位
 * 用于贸易站 GUI 中展示可交易物品
 *
 * 特点：
 * - 数量独立管理（stockCount 变量），不修改 ItemStack 本身
 * - 不可拿起、不可放入
 * - displayStack 可变（非 final），用于初始化赋值
 */
public class TradeSlot extends Slot {

    private ItemStack displayStack;
    private int stockCount;
    private int price;

    /**
     * 创建空的贸易槽位
     */
    public TradeSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.displayStack = ItemStack.EMPTY;
        this.stockCount = 0;
        this.price = 0;
    }

    /**
     * 设置展示物品
     */
    public void setDisplayStack(ItemStack stack) {
        this.displayStack = stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * 设置库存数量
     */
    public void setStockCount(int count) {
        this.stockCount = Math.max(0, count);
    }

    /**
     * 设置价格
     */
    public void setPrice(int price) {
        this.price = Math.max(0, price);
    }

    /**
     * 获取展示物品（返回副本，数量由 stockCount 决定）
     */
    public ItemStack getDisplayStack() {
        return displayStack;
    }

    /**
     * 获取库存数量
     */
    public int getStockCount() {
        return stockCount;
    }

    /**
     * 获取价格
     */
    public int getPrice() {
        return price;
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
        result.setCount(stockCount);
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
        return stockCount;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stockCount;
    }

    /**
     * 是否为空槽位
     */
    public boolean isEmpty() {
        return displayStack.isEmpty() || stockCount <= 0;
    }

    /**
     * 清空槽位
     */
    public void clear() {
        this.displayStack = ItemStack.EMPTY;
        this.stockCount = 0;
        this.price = 0;
    }

    /**
     * 复制另一个槽位的数据
     */
    public void copyFrom(TradeSlot other) {
        this.displayStack = other.displayStack.copy();
        this.stockCount = other.stockCount;
        this.price = other.price;
    }
}