package github.mczme.ruralroutes.network;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.block.BlockStyleHelper;
import github.mczme.ruralroutes.blockentity.TradeNodeBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.theme.ResolvedTheme;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.item.ConfigToolItem;
import github.mczme.ruralroutes.register.RRDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

/**
 * 配置工具操作的网络包
 * 从客户端发送到服务端：
 * - 设置主题（贸易站）
 * - 复制节点信息（贸易站）
 * - 粘贴节点信息（展示柜/传闻板）
 */
public record ConfigToolApplyPayload(
    BlockPos blockPos,
    @Nullable ResourceLocation themeName,
    @Nullable VillageStyle style,
    @Nullable UUID nodeId,
    @Nullable BlockPos stationPos,
    Operation operation
) implements CustomPacketPayload {

    public enum Operation {
        SET_THEME,      // 设置主题
        SET_STYLE,      // 设置外观风格
        COPY_NODE_INFO, // 复制节点信息
        PASTE_NODE_INFO // 粘贴节点信息
    }

    public static final CustomPacketPayload.Type<ConfigToolApplyPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "config_tool_apply"));

    public static final StreamCodec<ByteBuf, ConfigToolApplyPayload> STREAM_CODEC =
        new StreamCodec<ByteBuf, ConfigToolApplyPayload>() {
            @Override
            public ConfigToolApplyPayload decode(ByteBuf buf) {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                int opOrdinal = buf.readInt();
                Operation op = Operation.values()[opOrdinal];

                ResourceLocation theme = null;
                VillageStyle style = null;
                UUID nodeId = null;
                BlockPos stationPos = null;

                switch (op) {
                    case SET_THEME -> {
                        theme = ResourceLocation.STREAM_CODEC.decode(buf);
                    }
                    case SET_STYLE -> {
                        style = VillageStyle.values()[buf.readInt()];
                    }
                    case COPY_NODE_INFO -> {
                        nodeId = UUIDUtil.STREAM_CODEC.decode(buf);
                        stationPos = BlockPos.STREAM_CODEC.decode(buf);
                    }
                    case PASTE_NODE_INFO -> {
                        nodeId = UUIDUtil.STREAM_CODEC.decode(buf);
                        stationPos = BlockPos.STREAM_CODEC.decode(buf);
                    }
                }

                return new ConfigToolApplyPayload(pos, theme, style, nodeId, stationPos, op);
            }

            @Override
            public void encode(ByteBuf buf, ConfigToolApplyPayload payload) {
                BlockPos.STREAM_CODEC.encode(buf, payload.blockPos());
                buf.writeInt(payload.operation().ordinal());

                switch (payload.operation()) {
                    case SET_THEME -> {
                        ResourceLocation.STREAM_CODEC.encode(buf, payload.themeName());
                    }
                    case SET_STYLE -> {
                        buf.writeInt(payload.style().ordinal());
                    }
                    case COPY_NODE_INFO -> {
                        UUIDUtil.STREAM_CODEC.encode(buf, payload.nodeId());
                        BlockPos.STREAM_CODEC.encode(buf, payload.stationPos());
                    }
                    case PASTE_NODE_INFO -> {
                        UUIDUtil.STREAM_CODEC.encode(buf, payload.nodeId());
                        BlockPos.STREAM_CODEC.encode(buf, payload.stationPos());
                    }
                }
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 创建设置主题的 Payload
     */
    public static ConfigToolApplyPayload setTheme(BlockPos pos, ResourceLocation theme) {
        return new ConfigToolApplyPayload(pos, theme, null, null, null, Operation.SET_THEME);
    }

    /**
     * 创建设置外观风格的 Payload
     */
    public static ConfigToolApplyPayload setStyle(BlockPos pos, VillageStyle style) {
        return new ConfigToolApplyPayload(pos, null, style, null, null, Operation.SET_STYLE);
    }

    /**
     * 创建复制节点信息的 Payload
     */
    public static ConfigToolApplyPayload copyNodeInfo(BlockPos pos, UUID nodeId, BlockPos stationPos) {
        return new ConfigToolApplyPayload(pos, null, null, nodeId, stationPos, Operation.COPY_NODE_INFO);
    }

    /**
     * 创建粘贴节点信息的 Payload
     */
    public static ConfigToolApplyPayload pasteNodeInfo(BlockPos pos, UUID nodeId, BlockPos stationPos) {
        return new ConfigToolApplyPayload(pos, null, null, nodeId, stationPos, Operation.PASTE_NODE_INFO);
    }

    /**
     * 服务端处理
     */
    public static void handleServer(ConfigToolApplyPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        Level level = player.level();

        BlockEntity be = level.getBlockEntity(payload.blockPos());

        switch (payload.operation()) {
            case SET_THEME -> {
                if (payload.themeName() != null && be instanceof TradeStationBlockEntity station) {
                    station.setVillageTheme(payload.themeName());

                    ResolvedTheme template = ThemeManager.INSTANCE.getTheme(payload.themeName());
                    VillageStyle resolvedStyle = template != null
                        ? VillageStyle.fromBiome(template.biome())
                        : VillageStyle.PLAINS;
                    applyStyle(level, payload.blockPos(), resolvedStyle);
                }
            }
            case SET_STYLE -> {
                if (payload.style() != null) {
                    applyStyle(level, payload.blockPos(), payload.style());
                }
            }
            case COPY_NODE_INFO -> {
                if (payload.nodeId() != null && payload.stationPos() != null) {
                    // 写入玩家手持物品的 DataComponent
                    ItemStack mainHand = player.getMainHandItem();
                    if (mainHand.getItem() instanceof ConfigToolItem) {
                        mainHand.set(RRDataComponents.COPIED_NODE_ID.get(), payload.nodeId());
                        mainHand.set(RRDataComponents.COPIED_STATION_POS.get(), payload.stationPos());
                    }
                }
            }
            case PASTE_NODE_INFO -> {
                if (payload.nodeId() != null && payload.stationPos() != null
                    && be instanceof TradeNodeBlockEntity nodeEntity) {
                    nodeEntity.setTradeNodeInfo(payload.nodeId(), payload.stationPos());
                }
            }
        }
    }

    private static void applyStyle(Level level, BlockPos blockPos, VillageStyle style) {
        BlockState state = level.getBlockState(blockPos);
        if (!BlockStyleHelper.hasStyle(state)) {
            return;
        }

        BlockState updated = BlockStyleHelper.withStyle(state, style);
        if (updated != state) {
            level.setBlock(blockPos, updated, Block.UPDATE_CLIENTS);
        }
    }
}
