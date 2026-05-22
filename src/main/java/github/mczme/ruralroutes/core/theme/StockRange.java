package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 库存范围
 */
public record StockRange(
    int min,
    int max
) {
    public static final Codec<StockRange> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(StockRange::min),
            Codec.INT.fieldOf("max").forGetter(StockRange::max)
        ).apply(instance, StockRange::new)
    );
}
