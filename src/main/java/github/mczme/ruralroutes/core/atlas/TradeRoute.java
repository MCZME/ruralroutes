package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 玩家创建的一条可选择显示的商路图层。
 */
public final class TradeRoute {

    public static final Codec<TradeRoute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(TradeRoute::id),
        Codec.STRING.optionalFieldOf("name", "").forGetter(TradeRoute::name),
        Codec.INT.optionalFieldOf("color", 0xFF9BD2F7).forGetter(TradeRoute::color),
        TradeRouteStatus.CODEC.optionalFieldOf("status", TradeRouteStatus.DRAFT).forGetter(TradeRoute::status),
        TradeRouteStop.CODEC.listOf().fieldOf("stops").forGetter(TradeRoute::stops),
        TradeRouteSegment.CODEC.listOf().fieldOf("segments").forGetter(TradeRoute::segments),
        Codec.STRING.optionalFieldOf("note", "").forGetter(TradeRoute::note)
    ).apply(instance, TradeRoute::new));

    private final UUID id;
    private final String name;
    private final int color;
    private final TradeRouteStatus status;
    private final List<TradeRouteStop> stops;
    private final List<TradeRouteSegment> segments;
    private final String note;

    public TradeRoute(UUID id, String name, int color, TradeRouteStatus status,
            List<TradeRouteStop> stops, List<TradeRouteSegment> segments, String note) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = sanitizeName(name);
        this.color = color;
        this.status = Objects.requireNonNull(status, "status");
        this.stops = new ArrayList<>(Objects.requireNonNull(stops, "stops"));
        this.segments = new ArrayList<>(Objects.requireNonNull(segments, "segments"));
        this.note = note == null ? "" : note.trim();
    }

    public static TradeRoute create(String name, int color, UUID firstNodeId, UUID secondNodeId) {
        TradeRouteStop firstStop = TradeRouteStop.create(firstNodeId);
        TradeRouteStop secondStop = TradeRouteStop.create(secondNodeId);
        return new TradeRoute(
            UUID.randomUUID(),
            name,
            color,
            TradeRouteStatus.DRAFT,
            List.of(firstStop, secondStop),
            List.of(TradeRouteSegment.oneWay(firstStop.id(), secondStop.id())),
            ""
        );
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int color() {
        return color;
    }

    public TradeRouteStatus status() {
        return status;
    }

    public List<TradeRouteStop> stops() {
        return Collections.unmodifiableList(stops);
    }

    public List<TradeRouteSegment> segments() {
        return Collections.unmodifiableList(segments);
    }

    public String note() {
        return note;
    }

    public Optional<TradeRouteStop> findStop(UUID stopId) {
        if (stopId == null) {
            return Optional.empty();
        }
        return stops.stream().filter(stop -> stop.id().equals(stopId)).findFirst();
    }

    public Optional<TradeRouteSegment> findSegment(UUID segmentId) {
        if (segmentId == null) {
            return Optional.empty();
        }
        return segments.stream().filter(segment -> segment.id().equals(segmentId)).findFirst();
    }

    public TradeRoute withName(String newName) {
        return new TradeRoute(id, newName, color, status, stops, segments, note);
    }

    public TradeRoute withStatus(TradeRouteStatus newStatus) {
        return new TradeRoute(id, name, color, newStatus, stops, segments, note);
    }

    public TradeRoute withUpdatedStop(TradeRouteStop updatedStop) {
        List<TradeRouteStop> updatedStops = new ArrayList<>(stops);
        for (int i = 0; i < updatedStops.size(); i++) {
            if (updatedStops.get(i).id().equals(updatedStop.id())) {
                updatedStops.set(i, updatedStop);
                return new TradeRoute(id, name, color, status, updatedStops, segments, note);
            }
        }
        return this;
    }

    public TradeRoute withUpdatedSegment(TradeRouteSegment updatedSegment) {
        List<TradeRouteSegment> updatedSegments = new ArrayList<>(segments);
        for (int i = 0; i < updatedSegments.size(); i++) {
            if (updatedSegments.get(i).id().equals(updatedSegment.id())) {
                updatedSegments.set(i, updatedSegment);
                return new TradeRoute(id, name, color, status, stops, updatedSegments, note);
            }
        }
        return this;
    }

    public TradeRoute withAddedStop(UUID nodeId, UUID fromStopId) {
        if (nodeId == null || stops.isEmpty()) {
            return this;
        }

        TradeRouteStop anchor = findStop(fromStopId).orElse(stops.get(stops.size() - 1));
        TradeRouteStop newStop = TradeRouteStop.create(nodeId);
        List<TradeRouteStop> updatedStops = new ArrayList<>(stops);
        updatedStops.add(newStop);
        List<TradeRouteSegment> updatedSegments = new ArrayList<>(segments);
        updatedSegments.add(TradeRouteSegment.oneWay(anchor.id(), newStop.id()));
        return new TradeRoute(id, name, color, status, updatedStops, updatedSegments, note);
    }

    public TradeRoute withoutStop(UUID stopId) {
        if (stopId == null || stops.size() <= 2) {
            return this;
        }

        List<TradeRouteStop> updatedStops = stops.stream()
            .filter(stop -> !stop.id().equals(stopId))
            .toList();
        List<TradeRouteSegment> updatedSegments = segments.stream()
            .filter(segment -> !segment.fromStopId().equals(stopId) && !segment.toStopId().equals(stopId))
            .toList();
        if (updatedStops.size() < 2 || updatedSegments.isEmpty()) {
            return this;
        }
        return new TradeRoute(id, name, color, status, updatedStops, updatedSegments, note);
    }

    public boolean isValid() {
        return stops.size() >= 2 && !segments.isEmpty();
    }

    private static String sanitizeName(String value) {
        String name = value == null ? "" : value.trim();
        return name.isEmpty() ? "Route" : name;
    }
}
