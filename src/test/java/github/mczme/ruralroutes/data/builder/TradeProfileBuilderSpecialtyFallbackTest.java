package github.mczme.ruralroutes.data.builder;

import github.mczme.ruralroutes.core.theme.ItemReference;
import github.mczme.ruralroutes.core.theme.TradeProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeProfileBuilderSpecialtyFallbackTest {

    @Test
    void specialtyEntriesFallBackToNormalSellItems() {
        TradeProfile profile = TradeProfileBuilder.create("test_specialty_fallback")
            .specialty("minecraft:campfire", "minecraft:spruce_boat")
            .build();

        assertEquals(
            List.of(
                ItemReference.single("minecraft:campfire"),
                ItemReference.single("minecraft:spruce_boat")
            ),
            profile.sellItems()
        );
        assertEquals(List.of(), profile.buyItems());
        assertEquals(Optional.empty(), profile.tradeContracts());
    }
}
