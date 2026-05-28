package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.Objects;
import java.util.UUID;

/**
 * 连接同一条商路内两个停靠点实例的路段。
 */
public record TradeRouteSegment(
    UUID id,
    UUID fromStopId,
    UUID toStopId,
    TradeRouteDirection direction,
    String note
) {

    public static final Codec<TradeRouteSegment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(TradeRouteSegment::id),
        UUIDUtil.CODEC.fieldOf("from_stop_id").forGetter(TradeRouteSegment::fromStopId),
        UUIDUtil.CODEC.fieldOf("to_stop_id").forGetter(TradeRouteSegment::toStopId),
        TradeRouteDirection.CODEC.optionalFieldOf("direction", TradeRouteDirection.ONE_WAY)
            .forGetter(TradeRouteSegment::direction),
        Codec.STRING.optionalFieldOf("note", "").forGetter(TradeRouteSegment::note)
    ).apply(instance, TradeRouteSegment::new));

    public TradeRouteSegment {
        id = Objects.requireNonNull(id, "id");
        fromStopId = Objects.requireNonNull(fromStopId, "fromStopId");
        toStopId = Objects.requireNonNull(toStopId, "toStopId");
        direction = Objects.requireNonNull(direction, "direction");
        note = note == null ? "" : note;
    }

    public static TradeRouteSegment oneWay(UUID fromStopId, UUID toStopId) {
        return new TradeRouteSegment(UUID.randomUUID(), fromStopId, toStopId, TradeRouteDirection.ONE_WAY, "");
    }

    public TradeRouteSegment withDirection(TradeRouteDirection newDirection) {
        return new TradeRouteSegment(id, fromStopId, toStopId, newDirection, note);
    }
}
