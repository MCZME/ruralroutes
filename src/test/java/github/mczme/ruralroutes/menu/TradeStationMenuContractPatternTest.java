package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.core.node.NodeTradeEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeStationMenuContractPatternTest {

    @Test
    void sourceKeyPatternMatchesWithoutTreatingItAsItemReference() {
        NodeTradeEntry entry = NodeTradeEntry.of(
            "test_refactor_lab/catalog",
            ResourceLocation.parse("minecraft:book")
        );
        ItemStack stack = new ItemStack(Items.BOOK);

        assertTrue(TradeStationMenu.matchesContractPattern(entry, stack, "@test_refactor_lab/catalog"));
        assertFalse(TradeStationMenu.matchesContractPattern(entry, stack, "@test_refactor_lab/other"));
    }

    @Test
    void itemAndWildcardPatternsStillMatchNormally() {
        NodeTradeEntry entry = NodeTradeEntry.of(
            "test_refactor_lab/catalog",
            ResourceLocation.parse("minecraft:book")
        );
        ItemStack stack = new ItemStack(Items.BOOK);

        assertTrue(TradeStationMenu.matchesContractPattern(entry, stack, "*"));
        assertTrue(TradeStationMenu.matchesContractPattern(entry, stack, "minecraft:book"));
        assertFalse(TradeStationMenu.matchesContractPattern(entry, stack, "minecraft:paper"));
    }
}
