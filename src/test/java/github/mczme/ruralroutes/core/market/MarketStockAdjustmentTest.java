package github.mczme.ruralroutes.core.market;

import github.mczme.ruralroutes.core.rumor.RumorFamily;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketStockAdjustmentTest {

    @Test
    void returnsNoneWhenNoEffectiveStockModifiersExist() {
        // 价格事件可以没有库存字段；这种情况下不应制造“伪调整”结果。
        MarketStockAdjustment adjustment = MarketStockAdjustment.of(
                List.of(
                        event("no_stock", Optional.empty()),
                        event("neutral_stock", Optional.of(MarketStockModifier.NONE))
                ),
                0.75f
        );

        assertSame(MarketStockAdjustment.NONE, adjustment);
        assertFalse(adjustment.hasAdjustment());
    }

    @Test
    void clampsSellAndBuyDeltaIndependently() {
        // 卖出库存和收购容量走两条独立曲线，不能共享同一个截断结果。
        MarketStockAdjustment adjustment = MarketStockAdjustment.of(
                List.of(
                        event("one", Optional.of(new MarketStockModifier(0.60f, -0.40f))),
                        event("two", Optional.of(new MarketStockModifier(0.40f, -0.50f))),
                        event("three", Optional.of(new MarketStockModifier(0.10f, 0.25f)))
                ),
                0.75f
        );

        assertTrue(adjustment.hasAdjustment());
        assertEquals(0.75f, adjustment.sellDelta(), 0.0001f);
        assertEquals(-0.65f, adjustment.buyDelta(), 0.0001f);
        assertEquals(1.75f, adjustment.sellFactor(), 0.0001f);
        assertEquals(0.35f, adjustment.buyFactor(), 0.0001f);
        assertEquals(3, adjustment.matchedEvents().size());
    }

    @Test
    void appliesIndependentSellAndBuyBases() {
        // 这个用例防的是 sell/buy 误用同一 factor，导致库存基线串线。
        MarketStockAdjustment adjustment = MarketStockAdjustment.of(
                List.of(event("supply", Optional.of(new MarketStockModifier(0.25f, -0.20f)))),
                0.75f
        );

        assertEquals(15, adjustment.applySellBase(12));
        assertEquals(10, adjustment.applyBuyBase(12));
    }

    @Test
    void returnsZeroWhenApplyingToNonPositiveBase() {
        // 非法或空基线应直接归零，避免在刷新阶段被放大成正数库存。
        MarketStockAdjustment adjustment = MarketStockAdjustment.of(
                List.of(event("supply", Optional.of(new MarketStockModifier(0.50f, 0.50f)))),
                0.75f
        );

        assertEquals(0, adjustment.applySellBase(0));
        assertEquals(0, adjustment.applyBuyBase(-3));
    }

    private static MarketEvent event(String path, Optional<MarketStockModifier> stock) {
        return new MarketEvent(
                ResourceLocation.parse("ruralroutes:" + path),
                TradeTargetRef.item("minecraft:apple"),
                MarketScopeType.GLOBAL,
                Optional.empty(),
                0.10f,
                stock,
                RumorFamily.DEMAND,
                Optional.empty()
        );
    }
}
