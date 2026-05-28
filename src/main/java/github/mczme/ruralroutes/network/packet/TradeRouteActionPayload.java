package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.atlas.TradeAtlasManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 商路图层编辑请求。
 */
public record TradeRouteActionPayload(
    Action action,
    @Nullable UUID routeId,
    @Nullable UUID nodeId,
    @Nullable UUID stopId,
    @Nullable UUID segmentId,
    String text
) implements CustomPacketPayload {

    public enum Action {
        CREATE_ROUTE,
        DELETE_ROUTE,
        TOGGLE_ROUTE_VISIBLE,
        CYCLE_ROUTE_STATUS,
        RENAME_ROUTE,
        ADD_STOP,
        REMOVE_STOP,
        UPDATE_STOP_ROLE,
        UPDATE_STOP_NOTE,
        CYCLE_SEGMENT_DIRECTION
    }

    public static final CustomPacketPayload.Type<TradeRouteActionPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "trade_route_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TradeRouteActionPayload> STREAM_CODEC =
        StreamCodec.of(TradeRouteActionPayload::encode, TradeRouteActionPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, TradeRouteActionPayload payload) {
        buf.writeInt(payload.action().ordinal());
        writeOptionalUuid(buf, payload.routeId());
        writeOptionalUuid(buf, payload.nodeId());
        writeOptionalUuid(buf, payload.stopId());
        writeOptionalUuid(buf, payload.segmentId());
        buf.writeUtf(payload.text() == null ? "" : payload.text(), 64);
    }

    private static TradeRouteActionPayload decode(RegistryFriendlyByteBuf buf) {
        Action action = Action.values()[buf.readInt()];
        UUID routeId = readOptionalUuid(buf);
        UUID nodeId = readOptionalUuid(buf);
        UUID stopId = readOptionalUuid(buf);
        UUID segmentId = readOptionalUuid(buf);
        String text = buf.readUtf(64);
        return new TradeRouteActionPayload(action, routeId, nodeId, stopId, segmentId, text);
    }

    public static TradeRouteActionPayload createRoute(UUID firstNodeId, UUID secondNodeId) {
        return new TradeRouteActionPayload(Action.CREATE_ROUTE, null, firstNodeId, secondNodeId, null, "");
    }

    public static TradeRouteActionPayload deleteRoute(UUID routeId) {
        return new TradeRouteActionPayload(Action.DELETE_ROUTE, routeId, null, null, null, "");
    }

    public static TradeRouteActionPayload toggleRouteVisible(UUID routeId) {
        return new TradeRouteActionPayload(Action.TOGGLE_ROUTE_VISIBLE, routeId, null, null, null, "");
    }

    public static TradeRouteActionPayload cycleRouteStatus(UUID routeId) {
        return new TradeRouteActionPayload(Action.CYCLE_ROUTE_STATUS, routeId, null, null, null, "");
    }

    public static TradeRouteActionPayload renameRoute(UUID routeId, String name) {
        return new TradeRouteActionPayload(Action.RENAME_ROUTE, routeId, null, null, null, name);
    }

    public static TradeRouteActionPayload addStop(UUID routeId, UUID nodeId, @Nullable UUID anchorStopId) {
        return new TradeRouteActionPayload(Action.ADD_STOP, routeId, nodeId, anchorStopId, null, "");
    }

    public static TradeRouteActionPayload removeStop(UUID routeId, UUID stopId) {
        return new TradeRouteActionPayload(Action.REMOVE_STOP, routeId, null, stopId, null, "");
    }

    public static TradeRouteActionPayload updateStopRole(UUID routeId, UUID stopId, String role) {
        return new TradeRouteActionPayload(Action.UPDATE_STOP_ROLE, routeId, null, stopId, null, role);
    }

    public static TradeRouteActionPayload updateStopNote(UUID routeId, UUID stopId, String note) {
        return new TradeRouteActionPayload(Action.UPDATE_STOP_NOTE, routeId, null, stopId, null, note);
    }

    public static TradeRouteActionPayload cycleSegmentDirection(UUID routeId, UUID segmentId) {
        return new TradeRouteActionPayload(Action.CYCLE_SEGMENT_DIRECTION, routeId, null, null, segmentId, "");
    }

    private static void writeOptionalUuid(RegistryFriendlyByteBuf buf, @Nullable UUID value) {
        buf.writeBoolean(value != null);
        if (value != null) {
            UUIDUtil.STREAM_CODEC.encode(buf, value);
        }
    }

    @Nullable
    private static UUID readOptionalUuid(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? UUIDUtil.STREAM_CODEC.decode(buf) : null;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(TradeRouteActionPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> TradeAtlasManager.handleRouteAction(player, payload));
    }
}
