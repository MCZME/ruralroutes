package github.mczme.ruralroutes.block;

import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 贸易站方块 - 村庄交易的核心方块
 */
public class TradeStationBlock extends BaseEntityBlock {

    public static final MapCodec<TradeStationBlock> CODEC =
        simpleCodec(TradeStationBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public TradeStationBlock(Properties properties) {
        super(properties.mapColor(MapColor.WOOD)
            .strength(3.5f)
            .requiresCorrectToolForDrops()
            .noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RRBlockEntities.TRADE_STATION.get().create(pos, state);
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
            if (be instanceof TradeStationBlockEntity station) {
                // 检查主题是否有效
                ResourceLocation themeName = station.getVillageTheme();
                if (themeName == null) {
                    return InteractionResult.PASS;
                }

                // 检查区块是否已有商业节点数据
                if (CommercialNodeManager.hasNodeData(level, pos)) {
                    CommercialNodeData nodeData = CommercialNodeManager.getNodeData(level, pos);
                    // 校验数据一致性
                    if (CommercialNodeManager.validateTradeStation(station, nodeData)) {
                        openMenu(player, station);
                        return InteractionResult.CONSUME;
                    }
                    // 数据不一致，可能是错误的贸易站
                    player.displayClientMessage(
                        Component.translatable("block.ruralroutes.trade_station.mismatch"),
                        true);
                    return InteractionResult.FAIL;
                }

                // 区块没有数据，检查附近是否有村庄
                if (!CommercialNodeManager.hasVillageNearby(level, pos)) {
                    // 无村庄，无响应
                    return InteractionResult.PASS;
                }

                // 有村庄，创建商业节点数据
                CommercialNodeData newData = CommercialNodeManager.createNodeData(level, pos, themeName);
                if (newData != null) {
                    // 同步 tradeNodeId 到 BlockEntity
                    station.setTradeNodeId(newData.tradeNodeId());
                    openMenu(player, station);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * 打开贸易站菜单，发送 BlockPos 数据到客户端
     */
    private void openMenu(Player player, TradeStationBlockEntity station) {
        BlockPos pos = station.getBlockPos();
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.ruralroutes.trade_station");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return station.createMenu(containerId, inventory, player);
            }
        }, buffer -> buffer.writeBlockPos(pos));
    }
}