package github.mczme.ruralroutes.core.theme;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 已解析主题。
 * 运行时消费对象，保留 `ThemeTemplate` 的读取方式，但内容已完成 profile 合并。
 */
public final class ResolvedTheme extends ThemeTemplate {

    public ResolvedTheme(
        ResourceLocation name,
        ResourceLocation biome,
        List<ItemReference> sellItems,
        List<ItemReference> buyItems,
        Optional<List<ItemReference>> themeSpecialties,
        Optional<StockConfig> stock,
        Optional<Map<String, PriceModifier>> priceModifiers,
        Optional<List<TradeContractEntry>> tradeContracts,
        Optional<List<ResourceLocation>> tradeProfiles
    ) {
        super(name, biome, sellItems, buyItems, themeSpecialties, stock, priceModifiers, tradeContracts, tradeProfiles);
    }

    public ResolvedTheme(
        ResourceLocation name,
        ResourceLocation biome,
        List<ItemReference> sellItems,
        List<ItemReference> buyItems,
        Optional<List<ItemReference>> themeSpecialties,
        Optional<StockConfig> stock,
        Optional<Map<String, PriceModifier>> priceModifiers,
        Optional<List<TradeContractEntry>> tradeContracts
    ) {
        this(name, biome, sellItems, buyItems, themeSpecialties, stock, priceModifiers, tradeContracts, Optional.empty());
    }
}
