package github.mczme.ruralroutes.core.trade;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
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
}
