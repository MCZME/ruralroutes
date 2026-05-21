package github.mczme.ruralroutes.core.theme;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 已解析主题。
 * 运行时消费对象：主题级字段来自 ThemeTemplate，交易内容来自一个或多个 TradeProfile 的合并结果。
 */
public final class ResolvedTheme {

    private final ResourceLocation name;
    private final ResourceLocation biome;
    private final List<ThemeTemplate.ItemReference> sellItems;
    private final List<ThemeTemplate.ItemReference> buyItems;
    private final Optional<List<ThemeTemplate.ItemReference>> themeSpecialtyItems;
    private final Optional<ThemeTemplate.StockConfig> stock;
    private final Optional<List<ThemeTemplate.PriceModifier>> priceModifiers;
    private final Optional<List<ThemeTemplate.TradeContractEntry>> tradeContracts;
    private final Optional<List<ResourceLocation>> tradeProfiles;

    public ResolvedTheme(
        ResourceLocation name,
        ResourceLocation biome,
        List<ThemeTemplate.ItemReference> sellItems,
        List<ThemeTemplate.ItemReference> buyItems,
        Optional<List<ThemeTemplate.ItemReference>> themeSpecialtyItems,
        Optional<ThemeTemplate.StockConfig> stock,
        Optional<List<ThemeTemplate.PriceModifier>> priceModifiers,
        Optional<List<ThemeTemplate.TradeContractEntry>> tradeContracts,
        Optional<List<ResourceLocation>> tradeProfiles
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.biome = Objects.requireNonNull(biome, "biome");
        this.sellItems = List.copyOf(Objects.requireNonNull(sellItems, "sellItems"));
        this.buyItems = List.copyOf(Objects.requireNonNull(buyItems, "buyItems"));
        this.themeSpecialtyItems = Objects.requireNonNull(themeSpecialtyItems, "themeSpecialtyItems").map(List::copyOf);
        this.stock = Objects.requireNonNull(stock, "stock");
        this.priceModifiers = Objects.requireNonNull(priceModifiers, "priceModifiers").map(List::copyOf);
        this.tradeContracts = Objects.requireNonNull(tradeContracts, "tradeContracts").map(List::copyOf);
        this.tradeProfiles = Objects.requireNonNull(tradeProfiles, "tradeProfiles").map(List::copyOf);
    }

    public ResourceLocation name() {
        return name;
    }

    public ResourceLocation biome() {
        return biome;
    }

    public List<ThemeTemplate.ItemReference> sellItems() {
        return sellItems;
    }

    public List<ThemeTemplate.ItemReference> buyItems() {
        return buyItems;
    }

    public Optional<List<ThemeTemplate.ItemReference>> themeSpecialtyItems() {
        return themeSpecialtyItems;
    }

    public Optional<List<ResourceLocation>> themeSpecialties() {
        return themeSpecialtyItems.map(items -> items.stream()
            .filter(ThemeTemplate.ItemReference::isExactItem)
            .map(ThemeTemplate.ItemReference::itemId)
            .map(ResourceLocation::parse)
            .toList());
    }

    public Optional<ThemeTemplate.StockConfig> stock() {
        return stock;
    }

    public Optional<List<ThemeTemplate.PriceModifier>> priceModifiers() {
        return priceModifiers;
    }

    public Optional<List<ThemeTemplate.TradeContractEntry>> tradeContracts() {
        return tradeContracts;
    }

    public Optional<List<ResourceLocation>> tradeProfiles() {
        return tradeProfiles;
    }
}
