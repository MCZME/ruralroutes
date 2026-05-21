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

class MarketPriceAdjustmentTest {

    @Test
    void returnsNoneForEmptyEvents() {
        // 空事件列表应直接复用 NONE，避免调用方处理出一堆“空但不相等”的结果对象。
        MarketPriceAdjustment adjustment = MarketPriceAdjustment.of(List.of(), 0.5f);

        assertSame(MarketPriceAdjustment.NONE, adjustment);
        assertFalse(adjustment.hasAdjustment());
    }

    @Test
    void sumsAndClampsDelta() {
        // 这里同时覆盖“多事件叠加”和“超过配置上限后截断”两条规则。
        MarketPriceAdjustment adjustment = MarketPriceAdjustment.of(
                List.of(event("a", 0.30f), event("b", 0.35f), event("c", -0.05f)),
                0.50f
        );

        assertTrue(adjustment.hasAdjustment());
        assertEquals(0.50f, adjustment.delta());
        assertEquals(1.50f, adjustment.factor());
        assertEquals(3, adjustment.matchedEvents().size());
    }

    @Test
    void supportsNegativeDirectionAndDisplay() {
        MarketPriceAdjustment adjustment = MarketPriceAdjustment.of(
                List.of(event("drop", -0.12f)),
                0.50f
        );

        assertEquals("down", adjustment.getDirectionKey());
        assertEquals("-12%", adjustment.getPercentDisplay());
    }

    @Test
    void usesStableDisplayForPositiveAndZeroDelta() {
        // GUI 和调试面板会直接消费这两个展示字段，因此把正向与零值都固定住。
        MarketPriceAdjustment positive = MarketPriceAdjustment.of(List.of(event("rise", 0.15f)), 0.50f);
        MarketPriceAdjustment flat = MarketPriceAdjustment.of(List.of(event("flat", 0.0f)), 0.50f);

        assertEquals("up", positive.getDirectionKey());
        assertEquals("+15%", positive.getPercentDisplay());
        assertEquals("stable", flat.getDirectionKey());
        assertEquals("0%", flat.getPercentDisplay());
    }

    private static MarketEvent event(String path, float delta) {
        return new MarketEvent(
                ResourceLocation.parse("ruralroutes:" + path),
                TradeTargetRef.item("minecraft:apple"),
                MarketScopeType.GLOBAL,
                Optional.empty(),
                delta,
                Optional.empty(),
                RumorFamily.DEMAND,
                Optional.empty()
        );
    }
}
