package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 交易反馈网络包（服务端→客户端）
 * 用于在贸易站 GUI 内展示交易成功/失败/警告信息。
 */
public record TradeFeedbackPayload(
    int containerId,
    String translationKey,
    FeedbackType feedbackType
) implements CustomPacketPayload {

    public enum FeedbackType {
        SUCCESS,
        WARNING,
        ERROR
    }

    public static final CustomPacketPayload.Type<TradeFeedbackPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "trade_feedback"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TradeFeedbackPayload> STREAM_CODEC =
        StreamCodec.of(
            TradeFeedbackPayload::encode,
            TradeFeedbackPayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, TradeFeedbackPayload payload) {
        buf.writeVarInt(payload.containerId);
        buf.writeUtf(payload.translationKey);
        buf.writeEnum(payload.feedbackType);
    }

    private static TradeFeedbackPayload decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        String translationKey = buf.readUtf();
        FeedbackType feedbackType = buf.readEnum(FeedbackType.class);
        return new TradeFeedbackPayload(containerId, translationKey, feedbackType);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理
     */
    public static void handleClient(TradeFeedbackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            if (player.containerMenu instanceof TradeStationMenu tradeMenu
                && player.containerMenu.containerId == payload.containerId) {
                tradeMenu.receiveTradeFeedback(payload);
                return;
            }

            player.displayClientMessage(Component.translatable(payload.translationKey), true);
        });
    }
}
