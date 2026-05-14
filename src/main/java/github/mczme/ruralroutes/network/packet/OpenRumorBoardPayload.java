package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.client.gui.screen.RumorBoardScreen;
import github.mczme.ruralroutes.core.market.MarketState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 打开传闻板界面的网络包
 * 从服务端发送到客户端，携带当前周期的市场状态
 */
public record OpenRumorBoardPayload(
    BlockPos blockPos,
    MarketState marketState
) implements CustomPacketPayload {

    private static final String MARKET_STATE_KEY = "market_state";

    public static final CustomPacketPayload.Type<OpenRumorBoardPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "open_rumor_board"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRumorBoardPayload> STREAM_CODEC =
        StreamCodec.of(
            OpenRumorBoardPayload::encode,
            OpenRumorBoardPayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, OpenRumorBoardPayload payload) {
        buf.writeBlockPos(payload.blockPos);

        CompoundTag tag = new CompoundTag();
        MarketState.CODEC.encodeStart(NbtOps.INSTANCE, payload.marketState)
            .resultOrPartial(err -> RuralRoutes.LOGGER.error("Failed to encode rumor board market state: {}", err))
            .ifPresent(encoded -> tag.put(MARKET_STATE_KEY, encoded));
        buf.writeNbt(tag);
    }

    private static OpenRumorBoardPayload decode(RegistryFriendlyByteBuf buf) {
        BlockPos blockPos = buf.readBlockPos();
        CompoundTag tag = buf.readNbt();

        MarketState marketState = MarketState.empty(0);
        if (tag != null && tag.contains(MARKET_STATE_KEY)) {
            marketState = MarketState.CODEC.parse(NbtOps.INSTANCE, tag.get(MARKET_STATE_KEY))
                .resultOrPartial(err -> RuralRoutes.LOGGER.error("Failed to decode rumor board market state: {}", err))
                .orElse(MarketState.empty(0));
        }

        return new OpenRumorBoardPayload(blockPos, marketState);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理
     */
    public static void handleClient(OpenRumorBoardPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance()
            .setScreen(new RumorBoardScreen(payload.blockPos, payload.marketState)));
    }
}
