package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 交易请求网络包（客户端→服务端）
 */
public record TradeRequestPayload(
    int containerId,
    int requestType,    // 0=ADD_BUY, 1=ADD_SELL, 2=REMOVE_ENTRY, 3=CLEAR, 4=REMOVE_BUY, 5=REMOVE_SELL, 6=CONFIRM
    int slotIndex       // 槽位索引（ADD/REMOVE 时使用），CONFIRM 时忽略
) implements CustomPacketPayload {

    public static final int ADD_BUY = 0;
    public static final int ADD_SELL = 1;
    public static final int REMOVE_ENTRY = 2;
    public static final int CLEAR = 3;
    public static final int REMOVE_BUY = 4;   // 移除买入条目
    public static final int REMOVE_SELL = 5;  // 移除卖出条目
    public static final int CONFIRM = 6;      // 确认交易

    public static final CustomPacketPayload.Type<TradeRequestPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "trade_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TradeRequestPayload> STREAM_CODEC =
        StreamCodec.of(
            TradeRequestPayload::encode,
            TradeRequestPayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, TradeRequestPayload payload) {
        buf.writeVarInt(payload.containerId);
        buf.writeVarInt(payload.requestType);
        buf.writeVarInt(payload.slotIndex);
    }

    private static TradeRequestPayload decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        int requestType = buf.readVarInt();
        int slotIndex = buf.readVarInt();
        return new TradeRequestPayload(containerId, requestType, slotIndex);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理
     */
    public static void handleServer(TradeRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (player.containerMenu instanceof TradeStationMenu tradeMenu
                && player.containerMenu.containerId == payload.containerId) {
                tradeMenu.handleTradeRequest(payload.requestType(), payload.slotIndex());
            }
        });
    }
}
