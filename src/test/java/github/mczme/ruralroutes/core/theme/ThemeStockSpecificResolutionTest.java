package github.mczme.ruralroutes.core.theme;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeStockSpecificResolutionTest {

    @Test
    void resolvesSpecificStockWithItemSourceAndTagFallbacks() {
        ThemeTemplate.StockConfig config = new ThemeTemplate.StockConfig(
            Optional.of(new ThemeTemplate.StockRange(8, 16)),
            Optional.empty(),
            Optional.of(Map.of(
                "minecraft:book", new ThemeTemplate.StockRange(11, 12),
                "demo/key", new ThemeTemplate.StockRange(13, 14),
                "#ruralroutes:pool/book", new ThemeTemplate.StockRange(15, 16)
            ))
        );

        assertTrue(config.resolveSpecific("minecraft:book", ResourceLocation.parse("minecraft:book")).isPresent());
        assertEquals(11, config.resolveSpecific("minecraft:book", ResourceLocation.parse("minecraft:book")).orElseThrow().min());
        assertEquals(13, config.resolveSpecific("demo/key", ResourceLocation.parse("minecraft:book")).orElseThrow().min());
        assertEquals(15, config.resolveSpecific("#ruralroutes:pool/book", ResourceLocation.parse("minecraft:book")).orElseThrow().min());
    }
}
