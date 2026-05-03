package github.mczme.ruralroutes.menu.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 贸易展示容器
 * 用于管理贸易站 GUI 中的展示槽位数据
 *
 * 这是一个虚拟容器，不实际存储物品，仅作为 Slot 系统的容器接口
 */
public class TradeDisplayContainer implements Container {

    private int size;

    public TradeDisplayContainer(int size) {
        this.size = size;
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        // 实际数据由 TradeSlot 管理
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        // 不支持移除
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        // 不支持移除
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        // 实际数据由 TradeSlot 管理
    }

    @Override
    public void setChanged() {
        // 状态变化由外部管理
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        // 清空由外部管理
    }
}