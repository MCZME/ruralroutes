package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import net.minecraft.world.item.ItemStack;

/**
 * 节点库存条目。
 *
 * 一个规范化的 ItemStack 原型对应一份库存数量。
 */
public record NodeStockEntry(
    ItemStack stack,
    int current,
    int max
) {
    public static final Codec<NodeStockEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(NodeStockEntry::stack),
            Codec.INT.fieldOf("current").forGetter(NodeStockEntry::current),
            Codec.INT.fieldOf("max").forGetter(NodeStockEntry::max)
        ).apply(instance, NodeStockEntry::new)
    );

    public NodeStockEntry {
        stack = normalizeStack(stack);
    }

    /** 创建指定上限的满库存条目 */
    public static NodeStockEntry full(ItemStack stack, int max) {
        return new NodeStockEntry(stack, max, max);
    }

    /** 创建空库存条目 */
    public static NodeStockEntry empty(ItemStack stack, int max) {
        return new NodeStockEntry(stack, 0, max);
    }

    public TradeItemKey stockKey() {
        return TradeItemKey.from(stack);
    }

    /** 是否库存已满 */
    public boolean isFull() {
        return current >= max;
    }

    /** 是否库存为空 */
    public boolean isEmpty() {
        return current <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeStockEntry that)) {
            return false;
        }
        return current == that.current
            && max == that.max
            && stockKey().equals(that.stockKey());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(stockKey(), current, max);
    }

    /** 减少库存 */
    public NodeStockEntry decrease(int amount) {
        int newCurrent = Math.max(0, current - amount);
        return new NodeStockEntry(stack, newCurrent, max);
    }

    /** 增加库存 */
    public NodeStockEntry increase(int amount) {
        return increase(amount, false);
    }

    /** 增加库存，允许超过上限 */
    public NodeStockEntry increase(int amount, boolean allowOverflow) {
        int newCurrent = allowOverflow
            ? current + amount
            : Math.min(max, current + amount);
        return new NodeStockEntry(stack, newCurrent, max);
    }

    @Override
    public ItemStack stack() {
        return stack.copy();
    }

    private static ItemStack normalizeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }
}
