package github.mczme.ruralroutes.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Set;

public record RRPlayerProgressState(
    int successfulTradeCount,
    Set<String> purchasedSpecialties
) {

    public static final RRPlayerProgressState EMPTY = new RRPlayerProgressState(0, Set.of());

    public static final Codec<RRPlayerProgressState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("successful_trade_count", 0).forGetter(RRPlayerProgressState::successfulTradeCount),
        Codec.STRING.listOf().optionalFieldOf("purchased_specialties", List.of()).xmap(Set::copyOf, List::copyOf)
            .forGetter(RRPlayerProgressState::purchasedSpecialties)
    ).apply(instance, RRPlayerProgressState::new));

    public RRPlayerProgressState withSuccessfulTradeCount(int count) {
        return new RRPlayerProgressState(count, purchasedSpecialties);
    }

    public RRPlayerProgressState withPurchasedSpecialties(Set<String> specialties) {
        return new RRPlayerProgressState(successfulTradeCount, Set.copyOf(specialties));
    }
}
