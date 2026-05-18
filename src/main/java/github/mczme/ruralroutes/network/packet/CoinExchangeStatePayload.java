package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 货币交换状态网络包（服务端→客户端）
 * 同步货币交换界面所需的钱包状态。
 */
public record CoinExchangeStatePayload(
    int containerId,
    int playerCopperCount,
    int playerIronCount,
    int playerGoldCount,
    int villageCopperCount,
    int villageIronCount,
    int villageGoldCount
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CoinExchangeStatePayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "coin_exchange_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoinExchangeStatePayload> STREAM_CODEC =
        StreamCodec.of(
            CoinExchangeStatePayload::encode,
            CoinExchangeStatePayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, CoinExchangeStatePayload payload) {
        buf.writeVarInt(payload.containerId);
        buf.writeVarInt(payload.playerCopperCount);
        buf.writeVarInt(payload.playerIronCount);
        buf.writeVarInt(payload.playerGoldCount);
        buf.writeVarInt(payload.villageCopperCount);
        buf.writeVarInt(payload.villageIronCount);
        buf.writeVarInt(payload.villageGoldCount);
    }

    private static CoinExchangeStatePayload decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        int playerCopperCount = buf.readVarInt();
        int playerIronCount = buf.readVarInt();
        int playerGoldCount = buf.readVarInt();
        int villageCopperCount = buf.readVarInt();
        int villageIronCount = buf.readVarInt();
        int villageGoldCount = buf.readVarInt();
        return new CoinExchangeStatePayload(
            containerId,
            playerCopperCount,
            playerIronCount,
            playerGoldCount,
            villageCopperCount,
            villageIronCount,
            villageGoldCount
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(CoinExchangeStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;

            if (player.containerMenu instanceof TradeStationMenu tradeMenu
                && player.containerMenu.containerId == payload.containerId) {
                tradeMenu.receiveCoinExchangeState(payload);
            }
        });
    }
}
