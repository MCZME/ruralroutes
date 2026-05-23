package github.mczme.ruralroutes.core.node;

import github.mczme.ruralroutes.core.theme.ResolvedTheme;
import github.mczme.ruralroutes.core.theme.StockConfig;
import github.mczme.ruralroutes.core.theme.StockRange;
import github.mczme.ruralroutes.core.theme.StockTarget;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommercialNodeManagerStockResolutionTest {

    @Test
    void tagExpandedItemUsesExactTargetOverrideWhenPresent() throws Exception {
        ResolvedTheme theme = themeWithStock(new StockConfig(
            Optional.of(new StockRange(8, 16)),
            Optional.of(Map.of(
                "ruralroutes:copper_coin", StockTarget.shared(new StockRange(3, 3)),
                "#ruralroutes:currency", StockTarget.shared(new StockRange(7, 7)),
                "wallet/group", StockTarget.shared(new StockRange(9, 9))
            )),
            Optional.empty()
        ));

        assertEquals(
            3,
            invokeBaseStock(theme, "#ruralroutes:currency", "ruralroutes:copper_coin", true)
        );
        assertEquals(
            9,
            invokeBaseStock(theme, "wallet/group", "ruralroutes:copper_coin", true)
        );
    }

    @Test
    void tagExpandedItemUsesExactSpecificOverrideWhenPresent() throws Exception {
        ResolvedTheme theme = themeWithStock(new StockConfig(
            Optional.of(new StockRange(8, 16)),
            Optional.empty(),
            Optional.of(Map.of(
                "ruralroutes:copper_coin", new StockRange(4, 4),
                "#ruralroutes:currency", new StockRange(7, 7),
                "wallet/group", new StockRange(9, 9)
            ))
        ));

        assertEquals(
            4,
            invokeBaseStock(theme, "#ruralroutes:currency", "ruralroutes:copper_coin", true)
        );
        assertEquals(
            9,
            invokeBaseStock(theme, "wallet/group", "ruralroutes:copper_coin", true)
        );
    }

    private static int invokeBaseStock(
        ResolvedTheme theme,
        String rawRef,
        String itemId,
        boolean isSell
    ) throws Exception {
        Method method = CommercialNodeManager.class.getDeclaredMethod(
            "getBaseStockMax",
            ResolvedTheme.class,
            String.class,
            ResourceLocation.class,
            int.class,
            int.class,
            boolean.class
        );
        method.setAccessible(true);
        return (int) method.invoke(
            null,
            theme,
            rawRef,
            ResourceLocation.parse(itemId),
            8,
            16,
            isSell
        );
    }

    private static ResolvedTheme themeWithStock(StockConfig stockConfig) {
        return new ResolvedTheme(
            ResourceLocation.parse("ruralroutes:test"),
            ResourceLocation.parse("minecraft:plains"),
            List.of(),
            List.of(),
            Optional.of(stockConfig),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }
}
