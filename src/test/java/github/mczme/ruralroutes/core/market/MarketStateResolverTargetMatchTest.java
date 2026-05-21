package github.mczme.ruralroutes.core.market;

import github.mczme.ruralroutes.core.rumor.RumorFamily;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketStateResolverTargetMatchTest {

    @Test
    void resolvesTargetPriorityForItemSourceAndExactComponent() {
        TradeTargetRef item = TradeTargetRef.item("minecraft:book");
        TradeTargetRef source = TradeTargetRef.sourceKey("demo/key");
        ItemStack exactStack = new ItemStack(Items.BOOK);
        exactStack.set(DataComponents.CUSTOM_NAME, Component.literal("A"));
        TradeItemKey exactKey = TradeItemKey.from(exactStack);
        TradeTargetRef exact = TradeTargetRef.exactItem(
            "minecraft:book",
            exactKey.componentSignature().orElseThrow()
        );

        MarketEvent itemEvent = new MarketEvent(
            ResourceLocation.parse("ruralroutes:item"),
            item,
            MarketScopeType.GLOBAL,
            Optional.empty(),
            0.05f,
            Optional.of(new MarketStockModifier(0.10f, 0.10f)),
            RumorFamily.DEMAND,
            Optional.empty()
        );
        MarketEvent sourceEvent = new MarketEvent(
            ResourceLocation.parse("ruralroutes:source"),
            source,
            MarketScopeType.GLOBAL,
            Optional.empty(),
            0.08f,
            Optional.of(new MarketStockModifier(0.20f, 0.20f)),
            RumorFamily.DEMAND,
            Optional.empty()
        );
        MarketEvent exactEvent = new MarketEvent(
            ResourceLocation.parse("ruralroutes:exact"),
            exact,
            MarketScopeType.GLOBAL,
            Optional.empty(),
            0.12f,
            Optional.of(new MarketStockModifier(0.30f, 0.30f)),
            RumorFamily.DEMAND,
            Optional.empty()
        );

        MarketState state = new MarketState(1L, List.of(itemEvent, sourceEvent, exactEvent));
        MarketContext context = MarketContext.empty();

        ItemStack stack = new ItemStack(Items.BOOK);
        MarketPriceAdjustment stackAdjustment = MarketStateResolver.resolvePriceAdjustment(state, context, stack, Optional.of("demo/key"));
        assertEquals(2, stackAdjustment.matchedEvents().size());
        assertEquals(0.13f, stackAdjustment.delta(), 0.0001f);

        MarketPriceAdjustment exactAdjustment = MarketStateResolver.resolvePriceAdjustment(state, context, exactKey, Optional.of("demo/key"));
        assertEquals(3, exactAdjustment.matchedEvents().size());
        assertEquals(0.25f, exactAdjustment.delta(), 0.0001f);
    }
}
