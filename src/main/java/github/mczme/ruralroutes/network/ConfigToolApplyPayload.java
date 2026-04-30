package github.mczme.ruralroutes.network;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 配置工具应用主题的网络包
 * 从客户端发送到服务端，设置贸易站的主题
 */
public record ConfigToolApplyPayload(
    BlockPos blockPos,
    ResourceLocation themeName
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConfigToolApplyPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "config_tool_apply"));

    public static final StreamCodec<ByteBuf, ConfigToolApplyPayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConfigToolApplyPayload::blockPos,
            ResourceLocation.STREAM_CODEC, ConfigToolApplyPayload::themeName,
            ConfigToolApplyPayload::new
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理
     */
    public static void handleServer(ConfigToolApplyPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        Level level = player.level();

        if (level.getBlockEntity(payload.blockPos) instanceof TradeStationBlockEntity station) {
            station.setVillageTheme(payload.themeName);
        }
    }
}