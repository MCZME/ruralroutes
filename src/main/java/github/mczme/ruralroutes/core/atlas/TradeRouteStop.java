package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.Objects;
import java.util.UUID;

/**
 * 某个图册节点在一条商路中的一次出现。
 */
public record TradeRouteStop(
    UUID id,
    UUID nodeId,
    String role,
    String note
) {

    public static final Codec<TradeRouteStop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(TradeRouteStop::id),
        UUIDUtil.CODEC.fieldOf("node_id").forGetter(TradeRouteStop::nodeId),
        Codec.STRING.optionalFieldOf("role", "").forGetter(TradeRouteStop::role),
        Codec.STRING.optionalFieldOf("note", "").forGetter(TradeRouteStop::note)
    ).apply(instance, TradeRouteStop::new));

    public TradeRouteStop {
        id = Objects.requireNonNull(id, "id");
        nodeId = Objects.requireNonNull(nodeId, "nodeId");
        role = role == null ? "" : role;
        note = note == null ? "" : note;
    }

    public static TradeRouteStop create(UUID nodeId) {
        return new TradeRouteStop(UUID.randomUUID(), nodeId, "", "");
    }

    public TradeRouteStop withRole(String newRole) {
        return new TradeRouteStop(id, nodeId, sanitize(newRole), note);
    }

    public TradeRouteStop withNote(String newNote) {
        return new TradeRouteStop(id, nodeId, role, sanitize(newNote));
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
