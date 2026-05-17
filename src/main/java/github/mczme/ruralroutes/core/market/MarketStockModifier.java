package github.mczme.ruralroutes.core.market;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 市场事件的库存修正声明。
 *
 * sell/buy 分别表示对出售初始现货量与收购容量基线的增减百分比。
 */
public record MarketStockModifier(
        float sell,
        float buy
) {
    public static final MarketStockModifier NONE = new MarketStockModifier(0.0f, 0.0f);

    public static final Codec<MarketStockModifier> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.FLOAT.optionalFieldOf("sell", 0.0f).forGetter(MarketStockModifier::sell),
                    Codec.FLOAT.optionalFieldOf("buy", 0.0f).forGetter(MarketStockModifier::buy)
            ).apply(instance, MarketStockModifier::new)
    );

    public boolean hasEffect() {
        return Math.abs(sell) > 0.001f || Math.abs(buy) > 0.001f;
    }
}
