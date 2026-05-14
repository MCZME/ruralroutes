package github.mczme.ruralroutes.block;

import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 统一处理带 STYLE 属性的核心方块。
 */
public final class BlockStyleHelper {

    private BlockStyleHelper() {
    }

    public static boolean hasStyle(BlockState state) {
        return state.hasProperty(TradeStationBlock.STYLE)
            || state.hasProperty(DisplayCaseBlock.STYLE)
            || state.hasProperty(RumorBoardBlock.STYLE);
    }

    public static VillageStyle getStyle(BlockState state) {
        if (state.hasProperty(TradeStationBlock.STYLE)) {
            return state.getValue(TradeStationBlock.STYLE);
        }
        if (state.hasProperty(DisplayCaseBlock.STYLE)) {
            return state.getValue(DisplayCaseBlock.STYLE);
        }
        if (state.hasProperty(RumorBoardBlock.STYLE)) {
            return state.getValue(RumorBoardBlock.STYLE);
        }
        return VillageStyle.PLAINS;
    }

    public static BlockState withStyle(BlockState state, VillageStyle style) {
        if (state.hasProperty(TradeStationBlock.STYLE)) {
            return state.setValue(TradeStationBlock.STYLE, style);
        }
        if (state.hasProperty(DisplayCaseBlock.STYLE)) {
            return state.setValue(DisplayCaseBlock.STYLE, style);
        }
        if (state.hasProperty(RumorBoardBlock.STYLE)) {
            return state.setValue(RumorBoardBlock.STYLE, style);
        }
        return state;
    }
}
