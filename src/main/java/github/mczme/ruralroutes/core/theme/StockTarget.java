package github.mczme.ruralroutes.core.theme;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * 目标库存表达。
 * 可使用共享范围，或按出售/收购方向分别声明。
 */
public record StockTarget(
    Optional<StockRange> shared,
    Optional<StockRange> sell,
    Optional<StockRange> buy
) {
    private static final Codec<StockTarget> OBJECT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            StockRange.CODEC.optionalFieldOf("shared").forGetter(StockTarget::shared),
            StockRange.CODEC.optionalFieldOf("sell").forGetter(StockTarget::sell),
            StockRange.CODEC.optionalFieldOf("buy").forGetter(StockTarget::buy)
        ).apply(instance, StockTarget::new)
    );

    public static final Codec<StockTarget> CODEC = Codec.either(StockRange.CODEC, OBJECT_CODEC)
        .xmap(
            either -> either.map(StockTarget::shared, target -> target),
            target -> target.canEncodeAsSharedRange()
                ? Either.left(target.shared().orElseThrow())
                : Either.right(target)
        );

    public StockTarget {
        shared = Objects.requireNonNull(shared, "shared");
        sell = Objects.requireNonNull(sell, "sell");
        buy = Objects.requireNonNull(buy, "buy");
        if (shared.isEmpty() && sell.isEmpty() && buy.isEmpty()) {
            throw new IllegalArgumentException("StockTarget must define shared, sell, or buy");
        }
    }

    public static StockTarget shared(StockRange range) {
        return new StockTarget(Optional.of(range), Optional.empty(), Optional.empty());
    }

    public static StockTarget directional(StockRange sell, StockRange buy) {
        return new StockTarget(Optional.empty(), Optional.of(sell), Optional.of(buy));
    }

    public boolean canEncodeAsSharedRange() {
        return shared.isPresent() && sell.isEmpty() && buy.isEmpty();
    }

    public StockRange toLegacyRange() {
        if (shared.isPresent()) {
            return shared.get();
        }
        if (sell.isPresent()) {
            return sell.get();
        }
        return buy.orElseThrow(() -> new IllegalStateException("StockTarget has no readable range"));
    }
}
