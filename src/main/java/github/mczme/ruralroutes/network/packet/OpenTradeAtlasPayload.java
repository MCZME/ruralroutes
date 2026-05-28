package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.client.ClientGuiHooks;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 打开商路图册界面。
 */
public record OpenTradeAtlasPayload(
    TradeAtlasState state
) implements CustomPacketPayload {

    private static final String STATE_KEY = "state";

    public static final CustomPacketPayload.Type<OpenTradeAtlasPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "open_trade_atlas"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTradeAtlasPayload> STREAM_CODEC =
        StreamCodec.of(OpenTradeAtlasPayload::encode, OpenTradeAtlasPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, OpenTradeAtlasPayload payload) {
        CompoundTag tag = new CompoundTag();
        TradeAtlasState.CODEC.encodeStart(NbtOps.INSTANCE, payload.state)
            .resultOrPartial(err -> RuralRoutes.LOGGER.error("Failed to encode atlas payload: {}", err))
            .ifPresent(encoded -> tag.put(STATE_KEY, encoded));
        buf.writeNbt(tag);
    }

    private static OpenTradeAtlasPayload decode(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        TradeAtlasState state = TradeAtlasState.empty();
        if (tag != null && tag.contains(STATE_KEY)) {
            state = TradeAtlasState.CODEC.parse(NbtOps.INSTANCE, tag.get(STATE_KEY))
                .resultOrPartial(err -> RuralRoutes.LOGGER.error("Failed to decode atlas payload: {}", err))
                .orElseGet(TradeAtlasState::empty);
        }
        return new OpenTradeAtlasPayload(state);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(OpenTradeAtlasPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientGuiHooks.openTradeAtlasScreen(payload));
    }
}
