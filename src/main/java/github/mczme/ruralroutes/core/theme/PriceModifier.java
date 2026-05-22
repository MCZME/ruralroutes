package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;

/**
 * 价格修正系数
 */
public record PriceModifier(
    TradeTargetRef targetRef,
    float sell,
    float buy
) {
    public static final Codec<PriceModifier> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            TradeTargetRef.CODEC.fieldOf("target").forGetter(PriceModifier::targetRef),
            Codec.FLOAT.fieldOf("sell").forGetter(PriceModifier::sell),
            Codec.FLOAT.fieldOf("buy").forGetter(PriceModifier::buy)
        ).apply(instance, PriceModifier::new)
    );

    public static PriceModifier of(TradeTargetRef targetRef, float sell, float buy) {
        return new PriceModifier(targetRef, sell, buy);
    }
}
