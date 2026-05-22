package github.mczme.ruralroutes.core.trade;

import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeItemKeyCodecTest {

    @Test
    void codecRoundTripsCanonicalStringAndExactComponents() {
        TradeItemKey key = TradeItemKey.of(
            ResourceLocation.parse("minecraft:book"),
            Map.of("minecraft:custom_name", "{\"text\":\"A\"}")
        );

        var encoded = TradeItemKey.CODEC.encodeStart(JsonOps.INSTANCE, key)
            .getOrThrow();
        TradeItemKey decoded = TradeItemKey.CODEC.parse(JsonOps.INSTANCE, encoded)
            .getOrThrow();

        assertEquals(key, decoded);
        assertTrue(decoded.hasComponents());
        assertTrue(decoded.canonicalKey().startsWith("minecraft:book|"));
    }

    @Test
    void asItemStackRestoresCustomNameComponent() {
        ItemStack source = new ItemStack(Items.BOOK);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("Route Catalog"));
        TradeItemKey key = TradeItemKey.from(source);

        var restored = key.asItemStack();

        assertTrue(restored.has(DataComponents.CUSTOM_NAME));
        assertEquals(Component.literal("Route Catalog"), restored.get(DataComponents.CUSTOM_NAME));
        assertTrue(restored.getDisplayName().getString().contains("Route Catalog"));
    }

    @Test
    void distinctBookVariantsProduceDifferentKeys() {
        ItemStack glintCatalog = new ItemStack(Items.BOOK);
        glintCatalog.set(DataComponents.CUSTOM_NAME, Component.literal("Route Catalog"));
        glintCatalog.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        ItemStack namedLedger = new ItemStack(Items.BOOK);
        namedLedger.set(DataComponents.CUSTOM_NAME, Component.literal("Survey Ledger"));

        TradeItemKey catalogKey = TradeItemKey.from(glintCatalog);
        TradeItemKey ledgerKey = TradeItemKey.from(namedLedger);

        assertTrue(catalogKey.hasComponents());
        assertTrue(ledgerKey.hasComponents());
        assertTrue(!catalogKey.equals(ledgerKey));
    }
}
