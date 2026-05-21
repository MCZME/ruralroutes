package github.mczme.ruralroutes.block;

import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeNodeBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.advancement.trigger.OpenTradeStationTrigger.TradeStationEvent;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import github.mczme.ruralroutes.register.RRBlockEntities;
import github.mczme.ruralroutes.register.RRCriteriaTriggers;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 贸易站方块 - 村庄交易的核心方块
 */
public class TradeStationBlock extends BaseEntityBlock {

    public static final MapCodec<TradeStationBlock> CODEC =
        simpleCodec(TradeStationBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<VillageStyle> STYLE = EnumProperty.create("style", VillageStyle.class);

    /** 同步范围：贸易站周围查找展示柜和传闻板的半径 */
    private static final int SYNC_RADIUS = 16;

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public TradeStationBlock(Properties properties) {
        super(properties.mapColor(MapColor.WOOD)
            .strength(3.5f)
            .requiresCorrectToolForDrops()
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(STYLE, VillageStyle.PLAINS));
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
                        // 检查并刷新周期（如果需要）
                        if (level instanceof ServerLevel serverLevel) {
                            CommercialNodeData oldData = nodeData;
                            nodeData = CommercialNodeManager.checkAndRefreshCycle(serverLevel, pos, nodeData);
                            // 如果周期刷新了（数据发生变化），重新分配展示柜
                            if (nodeData != oldData) {
                                refreshDisplayCases(level, pos, nodeData);
                            }
                        }
                        if (player instanceof ServerPlayer serverPlayer) {
                            recordVillageDiscovery(serverPlayer, themeName);
                            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(serverPlayer, TradeStationEvent.OPEN);
                        }
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
                    UUID nodeId = newData.tradeNodeId();
                    // 同步 tradeNodeId 到贸易站 BlockEntity
                    station.setTradeNodeId(nodeId);
                    // 同步 tradeNodeId 到附近的展示柜和传闻板，并分配展示物品
                    syncNodeIdToNearbyBlocks(level, pos, nodeId, newData);
                    if (player instanceof ServerPlayer serverPlayer) {
                        recordVillageDiscovery(serverPlayer, themeName);
                        RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(serverPlayer, TradeStationEvent.OPEN);
                    }
                    openMenu(player, station);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * 同步 tradeNodeId 和贸易站位置到附近的展示柜和传闻板
     * 并为展示柜分配展示物品（特产或库存随机物品）
     */
    private void syncNodeIdToNearbyBlocks(Level level, BlockPos stationPos, UUID nodeId, CommercialNodeData nodeData) {
        List<DisplayCaseBlockEntity> displayCases = forEachBlockEntityInRange(level, stationPos, be -> {
            if (be instanceof TradeNodeBlockEntity nodeEntity) {
                nodeEntity.setTradeNodeInfo(nodeId, stationPos);
            }
        });
        assignDisplayItems(displayCases, nodeData);
    }

    /**
     * 周期刷新时重新分配展示柜的展示物品
     */
    private void refreshDisplayCases(Level level, BlockPos stationPos, CommercialNodeData nodeData) {
        List<DisplayCaseBlockEntity> displayCases = forEachBlockEntityInRange(level, stationPos, be -> {});
        assignDisplayItems(displayCases, nodeData);
    }

    /**
     * 遍历贸易站周围的方块实体
     * @param action 对每个方块实体执行的操作
     * @return 收集的展示柜列表
     */
    private List<DisplayCaseBlockEntity> forEachBlockEntityInRange(Level level, BlockPos center, Consumer<BlockEntity> action) {
        List<DisplayCaseBlockEntity> displayCases = new ArrayList<>();

        for (int dx = -SYNC_RADIUS; dx <= SYNC_RADIUS; dx++) {
            for (int dy = -SYNC_RADIUS; dy <= SYNC_RADIUS; dy++) {
                for (int dz = -SYNC_RADIUS; dz <= SYNC_RADIUS; dz++) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be != null) {
                        action.accept(be);
                        if (be instanceof DisplayCaseBlockEntity displayCase) {
                            displayCases.add(displayCase);
                        }
                    }
                }
            }
        }

        return displayCases;
    }

    /**
     * 为展示柜分配展示物品
     * 前 N 个展示柜分配特产，多余的从库存随机选取
     */
    private void assignDisplayItems(List<DisplayCaseBlockEntity> displayCases, CommercialNodeData nodeData) {
        List<ResourceLocation> specialties = nodeData.specialtyIds();
        Map<ResourceLocation, StockEntry> stocks = nodeData.stocks();
        Random random = new Random();

        int specialtyCount = specialties.size();

        for (int i = 0; i < displayCases.size(); i++) {
            DisplayCaseBlockEntity displayCase = displayCases.get(i);
            ItemStack displayItem;

            if (i < specialtyCount) {
                // 分配特产
                ResourceLocation specialtyId = specialties.get(i);
                displayItem = createItemStack(specialtyId);
            } else {
                // 从库存随机选取有库存的物品
                displayItem = getRandomItemFromStocks(stocks, random);
            }

            displayCase.setDisplayItem(displayItem);
        }
    }

    /**
     * 根据物品 ID 创建 ItemStack
     */
    private ItemStack createItemStack(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, 1);
    }

    /**
     * 从库存中随机选取一个有库存的物品
     */
    private ItemStack getRandomItemFromStocks(Map<ResourceLocation, StockEntry> stocks, Random random) {
        List<ResourceLocation> availableItems = new ArrayList<>();

        for (Map.Entry<ResourceLocation, StockEntry> entry : stocks.entrySet()) {
            if (entry.getValue().current() > 0) {
                availableItems.add(entry.getKey());
            }
        }

        if (availableItems.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ResourceLocation randomId = availableItems.get(random.nextInt(availableItems.size()));
        return createItemStack(randomId);
    }

    /**
     * 打开贸易站菜单，发送 BlockPos 和槽位数量数据到客户端
     * 并同步槽位数据到客户端
     */
    private void openMenu(Player player, TradeStationBlockEntity station) {
        BlockPos pos = station.getBlockPos();
        if (player instanceof ServerPlayer serverPlayer) {
            // 获取商业节点数据，提取槽位数量（过滤货币物品）
            CommercialNodeData nodeData = CommercialNodeManager.getNodeData(serverPlayer.level(), pos);
            final int sellSlotCount = nodeData != null ? TradeStationMenu.countNonCurrencyItems(nodeData.sellItemIds())
                    : 0;
            final int buySlotCount = nodeData != null ? TradeStationMenu.countNonCurrencyItems(nodeData.buyItemIds()) : 0;

            // 使用 openMenu 的返回值获取创建的 Menu
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.ruralroutes.trade_station");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                    return new TradeStationMenu(containerId, inventory, pos, sellSlotCount, buySlotCount);
                }
            }, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(sellSlotCount);
                buffer.writeVarInt(buySlotCount);
            });

            // openMenu 完成后，客户端已创建 Menu，此时发送同步数据
            if (player.containerMenu instanceof TradeStationMenu tradeMenu) {
                tradeMenu.syncSlotDataToClient(serverPlayer);
                tradeMenu.syncCoinExchangeStateToClient(serverPlayer);
            }
        }
    }

    private void recordVillageDiscovery(ServerPlayer player, ResourceLocation themeName) {
        ThemeTemplate theme = ThemeManager.INSTANCE.getTheme(themeName);
        if (theme == null) {
            return;
        }

        VillageStyle.tryFromBiome(theme.biome()).ifPresent(style ->
            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(
                player,
                TradeStationEvent.DISCOVER_VILLAGE_STYLE,
                style.getSerializedName(),
                null
            )
        );
        RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(
            player,
            TradeStationEvent.DISCOVER_VILLAGE_THEME,
            null,
            themeName.toString()
        );
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
