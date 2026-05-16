package github.mczme.ruralroutes.item;

import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.network.packet.OpenNodeDataViewerPayload;
import github.mczme.ruralroutes.network.packet.OpenNodeDataViewerPayload.TargetBlockType;
import github.mczme.ruralroutes.network.packet.OpenNodeDataViewerPayload.ViewStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 节点数据查看器。
 * 仅用于读取核心方块对应的商业节点快照，不修改任何世界数据。
 */
public class NodeDataViewerItem extends Item {

    public NodeDataViewerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        TargetBlockType targetBlockType = resolveTargetType(blockEntity);

        if (targetBlockType == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, createPayload(level, pos, blockEntity, targetBlockType));
        }

        return InteractionResult.SUCCESS;
    }

    private OpenNodeDataViewerPayload createPayload(Level level, BlockPos pos, BlockEntity blockEntity,
            TargetBlockType targetBlockType) {

        return switch (targetBlockType) {
            case TRADE_STATION -> createPayloadForNodeData(targetBlockType,
                CommercialNodeManager.getNodeData(level, pos));
            case DISPLAY_CASE -> createPayloadForLinkedNode(targetBlockType,
                blockEntity instanceof DisplayCaseBlockEntity displayCase ? displayCase.getTradeStationPos() : null,
                level);
            case RUMOR_BOARD -> createPayloadForLinkedNode(targetBlockType,
                blockEntity instanceof RumorBoardBlockEntity rumorBoard ? rumorBoard.getTradeStationPos() : null,
                level);
        };
    }

    private OpenNodeDataViewerPayload createPayloadForLinkedNode(TargetBlockType targetBlockType,
            @Nullable BlockPos stationPos, Level level) {
        if (stationPos == null) {
            return new OpenNodeDataViewerPayload(targetBlockType, ViewStatus.MISSING_STATION, null);
        }

        return createPayloadForNodeData(targetBlockType, CommercialNodeManager.getNodeData(level, stationPos));
    }

    private OpenNodeDataViewerPayload createPayloadForNodeData(TargetBlockType targetBlockType,
            @Nullable CommercialNodeData nodeData) {
        ViewStatus viewStatus = nodeData != null ? ViewStatus.HAS_DATA : ViewStatus.MISSING_NODE_DATA;
        return new OpenNodeDataViewerPayload(targetBlockType, viewStatus, nodeData);
    }

    @Nullable
    private TargetBlockType resolveTargetType(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof TradeStationBlockEntity) {
            return TargetBlockType.TRADE_STATION;
        }
        if (blockEntity instanceof DisplayCaseBlockEntity) {
            return TargetBlockType.DISPLAY_CASE;
        }
        if (blockEntity instanceof RumorBoardBlockEntity) {
            return TargetBlockType.RUMOR_BOARD;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.ruralroutes.node_data_viewer.tooltip"));
    }
}
