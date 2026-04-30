package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.block.DisplayCaseBlock;
import github.mczme.ruralroutes.block.RumorBoardBlock;
import github.mczme.ruralroutes.block.TradeStationBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块注册类
 */
public final class RRBlocks {

    private static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(RuralRoutes.MODID);

    public static final DeferredBlock<TradeStationBlock> TRADE_STATION =
        BLOCKS.registerBlock("trade_station", TradeStationBlock::new);

    public static final DeferredBlock<DisplayCaseBlock> DISPLAY_CASE =
        BLOCKS.registerBlock("display_case", DisplayCaseBlock::new);

    public static final DeferredBlock<RumorBoardBlock> RUMOR_BOARD =
        BLOCKS.registerBlock("rumor_board", RumorBoardBlock::new);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}