package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * BlockEntity 注册类
 */
public final class RRBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RuralRoutes.MODID);

    @SuppressWarnings("null")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TradeStationBlockEntity>> TRADE_STATION =
        BLOCK_ENTITIES.register("trade_station", () ->
            BlockEntityType.Builder.of(TradeStationBlockEntity::new,
                RRBlocks.TRADE_STATION.get()).build(null));

    @SuppressWarnings("null")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayCaseBlockEntity>> DISPLAY_CASE =
        BLOCK_ENTITIES.register("display_case", () ->
            BlockEntityType.Builder.of(DisplayCaseBlockEntity::new,
                RRBlocks.DISPLAY_CASE.get()).build(null));

    @SuppressWarnings("null")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RumorBoardBlockEntity>> RUMOR_BOARD =
        BLOCK_ENTITIES.register("rumor_board", () ->
            BlockEntityType.Builder.of(RumorBoardBlockEntity::new,
                RRBlocks.RUMOR_BOARD.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}