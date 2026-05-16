package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.trade.CoinExchangeContract;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 货币交换请求网络包（客户端→服务端）
 */
public record CoinExchangeRequestPayload(
    int containerId,
    CoinExchangeContract.ExchangeType exchangeType,
    boolean exchangeAll
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CoinExchangeRequestPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "coin_exchange_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoinExchangeRequestPayload> STREAM_CODEC =
        StreamCodec.of(
            CoinExchangeRequestPayload::encode,
            CoinExchangeRequestPayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, CoinExchangeRequestPayload payload) {
        buf.writeVarInt(payload.containerId);
        buf.writeEnum(payload.exchangeType);
        buf.writeBoolean(payload.exchangeAll);
    }

    private static CoinExchangeRequestPayload decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        CoinExchangeContract.ExchangeType exchangeType = buf.readEnum(CoinExchangeContract.ExchangeType.class);
        boolean exchangeAll = buf.readBoolean();
        return new CoinExchangeRequestPayload(containerId, exchangeType, exchangeAll);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(CoinExchangeRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (player.containerMenu instanceof TradeStationMenu tradeMenu
                && player.containerMenu.containerId == payload.containerId) {
                tradeMenu.executeCoinExchange(payload.exchangeType(), payload.exchangeAll());
            }
        });
    }
}
