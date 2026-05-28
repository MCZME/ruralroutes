package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.atlas.TradeAtlasManager;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 商路图册操作请求。
 */
public record TradeAtlasActionPayload(
    Action action,
    @Nullable VillageStyle style,
    @Nullable UUID nodeId
) implements CustomPacketPayload {

    public enum Action {
        REQUEST_LOCATE,
        CANCEL_PENDING_CLUE,
        SET_TARGET,
        CLEAR_TARGET
    }

    public static final CustomPacketPayload.Type<TradeAtlasActionPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "trade_atlas_action"));

    public static final StreamCodec<ByteBuf, TradeAtlasActionPayload> STREAM_CODEC =
        new StreamCodec<ByteBuf, TradeAtlasActionPayload>() {
            @Override
            public TradeAtlasActionPayload decode(ByteBuf buf) {
                Action action = Action.values()[buf.readInt()];
                VillageStyle style = null;
                UUID nodeId = null;

                switch (action) {
                    case REQUEST_LOCATE -> style = VillageStyle.values()[buf.readInt()];
                    case CANCEL_PENDING_CLUE -> {
                    }
                    case SET_TARGET -> nodeId = UUIDUtil.STREAM_CODEC.decode(buf);
                    case CLEAR_TARGET -> {
                    }
                }

                return new TradeAtlasActionPayload(action, style, nodeId);
            }

            @Override
            public void encode(ByteBuf buf, TradeAtlasActionPayload payload) {
                buf.writeInt(payload.action().ordinal());
                switch (payload.action()) {
                    case REQUEST_LOCATE -> buf.writeInt(payload.style().ordinal());
                    case CANCEL_PENDING_CLUE -> {
                    }
                    case SET_TARGET -> UUIDUtil.STREAM_CODEC.encode(buf, payload.nodeId());
                    case CLEAR_TARGET -> {
                    }
                }
            }
        };

    public static TradeAtlasActionPayload requestLocate(VillageStyle style) {
        return new TradeAtlasActionPayload(Action.REQUEST_LOCATE, style, null);
    }

    public static TradeAtlasActionPayload setTarget(UUID nodeId) {
        return new TradeAtlasActionPayload(Action.SET_TARGET, null, nodeId);
    }

    public static TradeAtlasActionPayload cancelPendingClue() {
        return new TradeAtlasActionPayload(Action.CANCEL_PENDING_CLUE, null, null);
    }

    public static TradeAtlasActionPayload clearTarget() {
        return new TradeAtlasActionPayload(Action.CLEAR_TARGET, null, null);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(TradeAtlasActionPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> TradeAtlasManager.handleAtlasAction(player, payload.action(), payload.style(), payload.nodeId()));
    }
}
