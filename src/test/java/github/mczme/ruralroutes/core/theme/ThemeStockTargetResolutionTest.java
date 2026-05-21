package github.mczme.ruralroutes.core.theme;

import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeStockTargetResolutionTest {

    @Test
    void resolvesTargetByItemSourceAndTag() {
        ThemeTemplate.StockConfig config = new ThemeTemplate.StockConfig(
            Optional.of(new ThemeTemplate.StockRange(8, 16)),
            Optional.of(Map.of(
                "minecraft:book", ThemeTemplate.StockTarget.shared(new ThemeTemplate.StockRange(3, 5)),
                "demo/key", ThemeTemplate.StockTarget.shared(new ThemeTemplate.StockRange(6, 7)),
                "#ruralroutes:pool/book", ThemeTemplate.StockTarget.shared(new ThemeTemplate.StockRange(9, 11))
            )),
            Optional.of(Map.of(
                "minecraft:book", new ThemeTemplate.StockRange(12, 14),
                "demo/key", new ThemeTemplate.StockRange(15, 16),
                "#ruralroutes:pool/book", new ThemeTemplate.StockRange(17, 18)
            ))
        );

        assertTrue(config.resolveTarget(TradeTargetRef.item("minecraft:book")).isPresent());
        assertEquals(3, config.resolveTarget(TradeTargetRef.item("minecraft:book")).orElseThrow().shared().orElseThrow().min());
        assertEquals(6, config.resolveTarget(TradeTargetRef.sourceKey("demo/key")).orElseThrow().shared().orElseThrow().min());
        assertEquals(9, config.resolveTarget(TradeTargetRef.tag("ruralroutes:pool/book")).orElseThrow().shared().orElseThrow().min());
        assertEquals(12, config.resolveSpecific(TradeTargetRef.item("minecraft:book")).orElseThrow().min());
        assertEquals(15, config.resolveSpecific(TradeTargetRef.sourceKey("demo/key")).orElseThrow().min());
        assertEquals(17, config.resolveSpecific(TradeTargetRef.tag("ruralroutes:pool/book")).orElseThrow().min());
    }
}
