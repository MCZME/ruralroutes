package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 库存条目，记录单个物品的当前库存与上限
 */
public record StockEntry(
    int current,
    int max
) {
    public static final Codec<StockEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.INT.fieldOf("current").forGetter(StockEntry::current),
            Codec.INT.fieldOf("max").forGetter(StockEntry::max)
        ).apply(instance, StockEntry::new)
    );

    /** 创建指定上限的满库存条目 */
    public static StockEntry full(int max) {
        return new StockEntry(max, max);
    }

    /** 创建空库存条目 */
    public static StockEntry empty(int max) {
        return new StockEntry(0, max);
    }

    /** 是否库存已满 */
    public boolean isFull() {
        return current >= max;
    }

    /** 是否库存为空 */
    public boolean isEmpty() {
        return current <= 0;
    }

    /** 减少库存 */
    public StockEntry decrease(int amount) {
        int newCurrent = Math.max(0, current - amount);
        return new StockEntry(newCurrent, max);
    }

    /** 增加库存 */
    public StockEntry increase(int amount) {
        int newCurrent = Math.min(max, current + amount);
        return new StockEntry(newCurrent, max);
    }
}