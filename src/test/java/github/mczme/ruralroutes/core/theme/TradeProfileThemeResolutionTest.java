package github.mczme.ruralroutes.core.theme;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import github.mczme.ruralroutes.core.trade.TradeSide;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeProfileThemeResolutionTest {

    @Test
    void profilesAreMergedInDeclaredOrder() throws Exception {
        TradeProfile first = profile(
            "ruralroutes:first",
            List.of(ThemeTemplate.ItemReference.single("minecraft:apple")),
            List.of(),
            List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "bread")),
            stockConfig(1, 2, Map.of("minecraft:apple", stockRangeValue(3, 4))),
            Map.of("minecraft:apple", new ThemeTemplate.PriceModifier(1.1f, 1.2f)),
            List.of(new ThemeTemplate.FixedTradeEntry(List.of(), List.of())),
            null
        );
        TradeProfile second = profile(
            "ruralroutes:second",
            List.of(ThemeTemplate.ItemReference.single("minecraft:carrot")),
            List.of(ThemeTemplate.ItemReference.single("minecraft:potato")),
            List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "wheat")),
            stockConfig(5, 6, Map.of("minecraft:carrot", stockRangeValue(7, 8))),
            Map.of("minecraft:carrot", new ThemeTemplate.PriceModifier(1.3f, 1.4f)),
            List.of(new ThemeTemplate.CurrencyBasketEntry(TradeSide.SELL_TO_PLAYER, List.of("*"), List.of("#ruralroutes:currency"), ThemeTemplate.CompositionStrategy.LARGEST_FIRST)),
            null
        );

        ThemeTemplate theme = theme(
            "ruralroutes:test_theme",
            "minecraft:plains",
            List.of(ThemeTemplate.ItemReference.single("minecraft:melon")),
            List.of(ThemeTemplate.ItemReference.single("minecraft:pumpkin")),
            List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "beetroot")),
            stockConfig(9, 10, Map.of("minecraft:melon", stockRangeValue(11, 12))),
            Map.of("minecraft:melon", new ThemeTemplate.PriceModifier(1.5f, 1.6f)),
            List.of(new ThemeTemplate.FixedTradeEntry(List.of(), List.of())),
            List.of(
                ResourceLocation.fromNamespaceAndPath("ruralroutes", "first"),
                ResourceLocation.fromNamespaceAndPath("ruralroutes", "second")
            )
        );

        Method method = ThemeManager.class.getDeclaredMethod("resolveTheme", ThemeTemplate.class, Map.class);
        method.setAccessible(true);
        ResolvedTheme resolved = (ResolvedTheme) method.invoke(ThemeManager.INSTANCE, theme, Map.of(first.name(), first, second.name(), second));

        assertEquals(
            List.of(
                ThemeTemplate.ItemReference.single("minecraft:apple"),
                ThemeTemplate.ItemReference.single("minecraft:carrot"),
                ThemeTemplate.ItemReference.single("minecraft:melon")
            ),
            resolved.sellItems()
        );
        assertEquals(
            List.of(
                ThemeTemplate.ItemReference.single("minecraft:potato"),
                ThemeTemplate.ItemReference.single("minecraft:pumpkin")
            ),
            resolved.buyItems()
        );
        assertEquals(
            List.of(
                ResourceLocation.fromNamespaceAndPath("minecraft", "bread"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "wheat"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "beetroot")
            ),
            resolved.themeSpecialties().orElseThrow()
        );
        assertTrue(resolved.priceModifiers().orElseThrow().containsKey("minecraft:apple"));
        assertTrue(resolved.priceModifiers().orElseThrow().containsKey("minecraft:carrot"));
        assertTrue(resolved.stock().isPresent());
        assertEquals(9, resolved.stock().orElseThrow().defaultRange().orElseThrow().min());
    }

    @Test
    void themeStockTargetsOverrideProfileTargetsAndDefaultStaysThemeOnly() throws Exception {
        TradeProfile profile = profile(
            "ruralroutes:profile",
            List.of(),
            List.of(),
            List.of(),
            stockConfig(1, 2, Map.of("minecraft:apple", stockRangeValue(3, 4))),
            Map.of(),
            List.of(),
            null
        );

        ThemeTemplate theme = theme(
            "ruralroutes:test_theme",
            "minecraft:plains",
            List.of(),
            List.of(),
            List.of(),
            stockConfig(9, 10, Map.of("minecraft:apple", stockRangeValue(11, 12), "minecraft:carrot", stockRangeValue(13, 14))),
            Map.of(),
            List.of(),
            List.of(ResourceLocation.fromNamespaceAndPath("ruralroutes", "profile"))
        );

        Method method = ThemeManager.class.getDeclaredMethod("resolveTheme", ThemeTemplate.class, Map.class);
        method.setAccessible(true);
        ResolvedTheme resolved = (ResolvedTheme) method.invoke(ThemeManager.INSTANCE, theme, Map.of(profile.name(), profile));

        assertEquals(9, resolved.stock().orElseThrow().defaultRange().orElseThrow().min());
        assertEquals(11, resolved.stock().orElseThrow().targets().orElseThrow().get("minecraft:apple").min());
        assertEquals(13, resolved.stock().orElseThrow().targets().orElseThrow().get("minecraft:carrot").min());
        assertFalse(resolved.stock().orElseThrow().targets().orElseThrow().containsKey("minecraft:missing"));
    }

    @Test
    void themeDefaultStockDoesNotFallBackToProfileDefault() throws Exception {
        TradeProfile profile = profile(
            "ruralroutes:profile",
            List.of(),
            List.of(),
            List.of(),
            stockConfig(1, 2, Map.of()),
            Map.of(),
            List.of(),
            null
        );

        ThemeTemplate theme = theme(
            "ruralroutes:test_theme",
            "minecraft:plains",
            List.of(),
            List.of(),
            List.of(),
            null,
            Map.of(),
            List.of(),
            List.of(ResourceLocation.fromNamespaceAndPath("ruralroutes", "profile"))
        );

        Method method = ThemeManager.class.getDeclaredMethod("resolveTheme", ThemeTemplate.class, Map.class);
        method.setAccessible(true);
        ResolvedTheme resolved = (ResolvedTheme) method.invoke(ThemeManager.INSTANCE, theme, Map.of(profile.name(), profile));

        assertFalse(resolved.stock().isPresent());
    }

    private static TradeProfile profile(
        String name,
        List<ThemeTemplate.ItemReference> sellItems,
        List<ThemeTemplate.ItemReference> buyItems,
        List<ResourceLocation> specialties,
        ThemeTemplate.StockConfig stock,
        Map<String, ThemeTemplate.PriceModifier> priceModifiers,
        List<ThemeTemplate.TradeContractEntry> tradeContracts,
        List<ResourceLocation> ignoredThemeProfiles
    ) {
        return new TradeProfile(
            ResourceLocation.parse(name),
            sellItems,
            buyItems,
            Optional.of(specialties),
            Optional.ofNullable(stock),
            priceModifiers.isEmpty() ? Optional.empty() : Optional.of(priceModifiers),
            tradeContracts.isEmpty() ? Optional.empty() : Optional.of(tradeContracts)
        );
    }

    private static ThemeTemplate theme(
        String name,
        String biome,
        List<ThemeTemplate.ItemReference> sellItems,
        List<ThemeTemplate.ItemReference> buyItems,
        List<ResourceLocation> specialties,
        ThemeTemplate.StockConfig stock,
        Map<String, ThemeTemplate.PriceModifier> priceModifiers,
        List<ThemeTemplate.TradeContractEntry> tradeContracts,
        List<ResourceLocation> tradeProfiles
    ) {
        return new ThemeTemplate(
            ResourceLocation.parse(name),
            ResourceLocation.parse(biome),
            sellItems,
            buyItems,
            Optional.of(specialties),
            Optional.ofNullable(stock),
            priceModifiers.isEmpty() ? Optional.empty() : Optional.of(priceModifiers),
            tradeContracts.isEmpty() ? Optional.empty() : Optional.of(tradeContracts),
            Optional.of(tradeProfiles)
        );
    }

    private static ThemeTemplate.StockConfig stockConfig(int min, int max, Map<String, ThemeTemplate.StockRange> targets) {
        return new ThemeTemplate.StockConfig(Optional.of(new ThemeTemplate.StockRange(min, max)), Optional.of(targets));
    }

    private static ThemeTemplate.StockRange stockRangeValue(int min, int max) {
        return new ThemeTemplate.StockRange(min, max);
    }
}
