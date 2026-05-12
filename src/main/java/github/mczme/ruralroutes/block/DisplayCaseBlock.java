package github.mczme.ruralroutes.block;

import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.client.gui.screen.DisplayCaseScreen;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;

import java.util.UUID;

/**
 * 展示柜方块 - 展示村庄特产
 * 不需要主题，仅通过 tradeNodeId 与贸易站关联
 */
public class DisplayCaseBlock extends BaseEntityBlock {

    public static final MapCodec<DisplayCaseBlock> CODEC =
        simpleCodec(DisplayCaseBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE = Block.box(
        2, 0, 2, 14, 13, 14
    );

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public DisplayCaseBlock(Properties properties) {
        super(properties.mapColor(MapColor.NONE)
            .strength(1.5f)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DisplayCaseBlockEntity displayCase) {
            if (level.isClientSide) {
                // 客户端：检查是否已激活
                if (displayCase.getTradeStationPos() == null) {
                    return InteractionResult.PASS;
                }
                // 打开 Screen
                Minecraft.getInstance().setScreen(new DisplayCaseScreen(displayCase.getDisplayItem()));
                return InteractionResult.SUCCESS;
            } else {
                // 服务端：校验数据
                BlockPos stationPos = displayCase.getTradeStationPos();
                if (stationPos == null) {
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.display_case.not_activated"),
                        true);
                    return InteractionResult.FAIL;
                }

                if (!CommercialNodeManager.hasNodeData(level, stationPos)) {
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.display_case.not_activated"),
                        true);
                    return InteractionResult.FAIL;
                }

                CommercialNodeData nodeData = CommercialNodeManager.getNodeData(level, stationPos);

                if (!validateNodeEntity(displayCase, nodeData)) {
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.display_case.mismatch"),
                        true);
                    return InteractionResult.FAIL;
                }

                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * 校验节点实体与区块数据的一致性
     */
    private boolean validateNodeEntity(DisplayCaseBlockEntity nodeEntity, CommercialNodeData nodeData) {
        if (nodeEntity == null || nodeData == null) {
            return false;
        }

        UUID entityId = nodeEntity.getTradeNodeId();
        // 如果实体没有节点ID，说明尚未同步，允许通过（等待贸易站同步）
        if (entityId == null) {
            return true;
        }

        // 如果有节点ID，必须与区块数据一致
        return entityId.equals(nodeData.tradeNodeId());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType pathType) {
        return false;
    }
}
