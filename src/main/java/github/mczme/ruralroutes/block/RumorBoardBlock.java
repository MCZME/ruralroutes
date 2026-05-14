package github.mczme.ruralroutes.block;

import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.menu.RumorBoardMenu;
import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

/**
 * 传闻板方块 - 显示市场波动情报
 * 不需要主题，仅通过 tradeNodeId 与贸易站关联
 */
public class RumorBoardBlock extends BaseEntityBlock {

    public static final MapCodec<RumorBoardBlock> CODEC =
        simpleCodec(RumorBoardBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<VillageStyle> STYLE = EnumProperty.create("style", VillageStyle.class);

    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 14, 16, 12, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 0, 16, 12, 2);
    private static final VoxelShape EAST_SHAPE = Block.box(0, 0, 0, 2, 12, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(14, 0, 0, 16, 12, 16);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public RumorBoardBlock(Properties properties) {
        super(properties.mapColor(MapColor.WOOD)
            .strength(2.0f)
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(STYLE, VillageStyle.PLAINS));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RRBlockEntities.RUMOR_BOARD.get().create(pos, state);
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
            if (be instanceof RumorBoardBlockEntity rumorBoard) {
                // 获取贸易站位置
                BlockPos stationPos = rumorBoard.getTradeStationPos();
                if (stationPos == null) {
                    // 尚未同步，提示激活贸易站
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.rumor_board.not_activated"),
                        true);
                    return InteractionResult.FAIL;
                }

                // 检查贸易站所在区块是否已有商业节点数据
                if (!CommercialNodeManager.hasNodeData(level, stationPos)) {
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.rumor_board.not_activated"),
                        true);
                    return InteractionResult.FAIL;
                }

                CommercialNodeData nodeData = CommercialNodeManager.getNodeData(level, stationPos);

                // 校验节点ID一致性
                if (!validateRumorBoard(rumorBoard, nodeData)) {
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.rumor_board.mismatch"),
                        true);
                    return InteractionResult.FAIL;
                }

                // 打开 GUI
                openMenu(player, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * 打开传闻板菜单
     */
    private void openMenu(Player player, BlockPos pos) {
        if (player instanceof ServerPlayer) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.ruralroutes.rumor_board");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                    return new RumorBoardMenu(containerId, inventory, pos);
                }
            }, buffer -> buffer.writeBlockPos(pos));
        }
    }

    /**
     * 校验传闻板与区块数据的一致性
     */
    private boolean validateRumorBoard(RumorBoardBlockEntity rumorBoard, CommercialNodeData nodeData) {
        if (rumorBoard == null || nodeData == null) {
            return false;
        }

        UUID boardId = rumorBoard.getTradeNodeId();
        // 如果传闻板没有节点ID，说明尚未同步，允许通过（等待贸易站同步）
        if (boardId == null) {
            return true;
        }

        // 如果有节点ID，必须与区块数据一致
        return boardId.equals(nodeData.tradeNodeId());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STYLE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
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
