package github.mczme.ruralroutes.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RRPlayerProgressState(
    int successfulTradeCount
) {

    public static final RRPlayerProgressState EMPTY = new RRPlayerProgressState(0);

    public static final Codec<RRPlayerProgressState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("successful_trade_count", 0).forGetter(RRPlayerProgressState::successfulTradeCount)
    ).apply(instance, RRPlayerProgressState::new));

    public RRPlayerProgressState withSuccessfulTradeCount(int count) {
        return new RRPlayerProgressState(count);
    }
}
