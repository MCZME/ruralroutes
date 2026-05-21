package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.core.market.MarketEvent;
import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.core.market.MarketScopeType;
import github.mczme.ruralroutes.core.rumor.RumorFamily;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeTargetRefCodecTest {

    @Test
    void codecSupportsStringShortcutsAndObjectForm() {
        assertEquals(
            TradeTargetRef.item("minecraft:apple"),
            TradeTargetRef.CODEC.parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString("\"minecraft:apple\""))
                .getOrThrow()
        );
        assertEquals(
            TradeTargetRef.tag("ruralroutes:pool/crop"),
            TradeTargetRef.CODEC.parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString("\"#ruralroutes:pool/crop\""))
                .getOrThrow()
        );
        assertEquals(
            TradeTargetRef.sourceKey("foo/bar"),
            TradeTargetRef.CODEC.parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString("\"@foo/bar\""))
                .getOrThrow()
        );
    }

    @Test
    void priceModifierPrefersComponentThenSourceThenItemThenTag() {
        ThemeTemplate.PriceModifier exact = ThemeTemplate.PriceModifier.of(
            TradeTargetRef.exactItem("minecraft:book", java.util.Map.of("minecraft:custom_name", "{\"text\":\"A\"}")),
            1.4f,
            1.5f
        );
        ThemeTemplate.PriceModifier source = ThemeTemplate.PriceModifier.of(TradeTargetRef.sourceKey("demo/key"), 1.3f, 1.4f);
        ThemeTemplate.PriceModifier item = ThemeTemplate.PriceModifier.of(TradeTargetRef.item("minecraft:book"), 1.2f, 1.3f);
        ThemeTemplate.PriceModifier tag = ThemeTemplate.PriceModifier.of(TradeTargetRef.tag("minecraft:bookshelf"), 1.1f, 1.2f);

        ResolvedTheme template = new ResolvedTheme(
            ResourceLocation.parse("ruralroutes:test"),
            ResourceLocation.parse("minecraft:plains"),
            List.of(),
            List.of(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(List.of(exact, source, item, tag)),
            Optional.empty(),
            Optional.empty()
        );

        ItemStack stack = new ItemStack(Items.BOOK);
        assertEquals(source, ThemePriceModifierResolver.resolve(template, stack, Optional.of("demo/key")));
        assertEquals(item, ThemePriceModifierResolver.resolve(template, stack));

        github.mczme.ruralroutes.core.trade.TradeItemKey exactKey = github.mczme.ruralroutes.core.trade.TradeItemKey.of(
            ResourceLocation.parse("minecraft:book"),
            java.util.Map.of("minecraft:custom_name", "{\"text\":\"A\"}")
        );
        assertEquals(3, exact.targetRef().matchSpecificity(null, Optional.empty(), exactKey));
        assertEquals(2, source.targetRef().matchSpecificity(stack, Optional.of("demo/key"), github.mczme.ruralroutes.core.trade.TradeItemKey.of(ResourceLocation.parse("minecraft:book"))));
        assertEquals(1, item.targetRef().matchSpecificity(stack, Optional.empty(), github.mczme.ruralroutes.core.trade.TradeItemKey.of(ResourceLocation.parse("minecraft:book"))));
        assertEquals(exact, ThemePriceModifierResolver.resolve(template, exactKey, Optional.empty()));
        assertEquals(exact, ThemePriceModifierResolver.resolve(template, exactKey, Optional.of("demo/key")));
        assertTrue(ThemePriceModifierResolver.resolve(template, stack, Optional.empty()).buy() > 0.0f);
    }

    @Test
    void marketEventTargetRefKeepsSkeletonMatchingShape() {
        MarketEventRule rule = new MarketEventRule(
            ResourceLocation.parse("ruralroutes:test_rule"),
            "market.ruralroutes.test_rule",
            TradeTargetRef.item("minecraft:apple"),
            List.of(),
            0.1f,
            Optional.empty(),
            Optional.empty(),
            RumorFamily.DEMAND,
            Optional.empty()
        );
        MarketEvent event = new MarketEvent(
            rule.id(),
            rule.targetRef(),
            MarketScopeType.GLOBAL,
            Optional.empty(),
            rule.delta(),
            rule.stock(),
            rule.rumorFamily(),
            rule.rumorTargetKey()
        );

        assertTrue(event.targetRef().isItem());
        assertEquals("minecraft:apple", event.getTargetId());
    }
}
