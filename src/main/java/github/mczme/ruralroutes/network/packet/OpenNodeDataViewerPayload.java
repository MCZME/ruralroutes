package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.client.gui.screen.NodeDataViewerScreen;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * 打开节点数据查看器的网络包。
 * 服务端发送节点快照，客户端直接打开只读 Screen。
 */
public record OpenNodeDataViewerPayload(
    TargetBlockType targetBlockType,
    ViewStatus viewStatus,
    @Nullable CommercialNodeData nodeData
) implements CustomPacketPayload {

    private static final String NODE_DATA_KEY = "node_data";

    public enum TargetBlockType {
        TRADE_STATION("gui.ruralroutes.node_data_viewer.target.trade_station"),
        DISPLAY_CASE("gui.ruralroutes.node_data_viewer.target.display_case"),
        RUMOR_BOARD("gui.ruralroutes.node_data_viewer.target.rumor_board");

        private final String translationKey;

        TargetBlockType(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    public enum ViewStatus {
        HAS_DATA(null),
        MISSING_STATION("gui.ruralroutes.node_data_viewer.status.missing_station"),
        MISSING_NODE_DATA("gui.ruralroutes.node_data_viewer.status.missing_node_data");

        private final String messageKey;

        ViewStatus(@Nullable String messageKey) {
            this.messageKey = messageKey;
        }

        @Nullable
        public String messageKey() {
            return messageKey;
        }
    }

    public static final CustomPacketPayload.Type<OpenNodeDataViewerPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "open_node_data_viewer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenNodeDataViewerPayload> STREAM_CODEC =
        StreamCodec.of(
            OpenNodeDataViewerPayload::encode,
            OpenNodeDataViewerPayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, OpenNodeDataViewerPayload payload) {
        buf.writeEnum(payload.targetBlockType);
        buf.writeEnum(payload.viewStatus);

        CompoundTag tag = new CompoundTag();
        if (payload.nodeData != null) {
            CommercialNodeData.CODEC.encodeStart(NbtOps.INSTANCE, payload.nodeData)
                .resultOrPartial(err -> RuralRoutes.LOGGER.error("Failed to encode node data viewer payload: {}", err))
                .ifPresent(encoded -> tag.put(NODE_DATA_KEY, encoded));
        }
        buf.writeNbt(tag);
    }

    private static OpenNodeDataViewerPayload decode(RegistryFriendlyByteBuf buf) {
        TargetBlockType targetBlockType = buf.readEnum(TargetBlockType.class);
        ViewStatus viewStatus = buf.readEnum(ViewStatus.class);
        CompoundTag tag = buf.readNbt();

        CommercialNodeData nodeData = null;
        if (tag != null && tag.contains(NODE_DATA_KEY)) {
            nodeData = CommercialNodeData.CODEC.parse(NbtOps.INSTANCE, tag.get(NODE_DATA_KEY))
                .resultOrPartial(err -> RuralRoutes.LOGGER.error("Failed to decode node data viewer payload: {}", err))
                .orElse(null);
        }

        return new OpenNodeDataViewerPayload(targetBlockType, viewStatus, nodeData);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(OpenNodeDataViewerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(
            new NodeDataViewerScreen(payload.targetBlockType, payload.viewStatus, payload.nodeData)
        ));
    }
}
