package github.mczme.ruralroutes.block;

import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

/**
 * 展示柜方块 - 展示村庄特产
 * 不需要主题，仅通过 tradeNodeId 与贸易站关联
 */
public class DisplayCaseBlock extends BaseEntityBlock {

    public static final MapCodec<DisplayCaseBlock> CODEC =
        simpleCodec(DisplayCaseBlock::new);

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 12, 15);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public DisplayCaseBlock(Properties properties) {
        super(properties.mapColor(MapColor.NONE)
            .strength(1.5f)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RRBlockEntities.DISPLAY_CASE.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DisplayCaseBlockEntity displayCase) {
                // 检查区块是否已有商业节点数据
                if (!CommercialNodeManager.hasNodeData(level, pos)) {
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.display_case.not_activated"),
                        true);
                    return InteractionResult.FAIL;
                }

                CommercialNodeData nodeData = CommercialNodeManager.getNodeData(level, pos);

                // 校验节点ID一致性
                if (!validateDisplayCase(displayCase, nodeData)) {
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.display_case.mismatch"),
                        true);
                    return InteractionResult.FAIL;
                }

                // 第一阶段：显示特产列表信息
                player.displayClientMessage(
                    Component.translatable("block.ruralroutes.display_case.specialties",
                        nodeData.specialties().size()),
                    false);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * 校验展示柜与区块数据的一致性
     */
    private boolean validateDisplayCase(DisplayCaseBlockEntity displayCase, CommercialNodeData nodeData) {
        if (displayCase == null || nodeData == null) {
            return false;
        }

        UUID caseId = displayCase.getTradeNodeId();
        // 如果展示柜没有节点ID，说明尚未同步，允许通过（等待贸易站同步）
        if (caseId == null) {
            return true;
        }

        // 如果有节点ID，必须与区块数据一致
        return caseId.equals(nodeData.tradeNodeId());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}