package github.mczme.ruralroutes.menu.slot;

import net.minecraft.world.Container;

/**
 * 暂存区专用槽位，追踪来源信息
 */
public class PendingTradeSlot extends TradeSlot {
    private int sourceSlotIndex = -1;    // 来源槽位索引，-1 表示无
    private boolean sourceIsBuy = true;  // 来源是出售区(true)还是收购区(false)

    public PendingTradeSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    /**
     * 设置来源信息
     */
    public void setSource(int sourceSlotIndex, boolean sourceIsBuy) {
        this.sourceSlotIndex = sourceSlotIndex;
        this.sourceIsBuy = sourceIsBuy;
    }

    /**
     * 清除来源信息
     */
    public void clearSource() {
        this.sourceSlotIndex = -1;
        this.sourceIsBuy = true;
    }

    /**
     * 获取来源槽位索引
     */
    public int getSourceSlotIndex() {
        return sourceSlotIndex;
    }

    /**
     * 来源是否为出售区
     */
    public boolean isSourceIsBuy() {
        return sourceIsBuy;
    }

    /**
     * 是否有来源信息
     */
    public boolean hasSource() {
        return sourceSlotIndex >= 0;
    }

    /**
     * 重写 clear() 方法，同时清除来源信息
     */
    @Override
    public void clear() {
        super.clear();
        clearSource();
    }
}
