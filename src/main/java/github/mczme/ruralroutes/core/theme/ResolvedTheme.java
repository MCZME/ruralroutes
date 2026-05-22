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
    private final List<ItemReference> sellItems;
    private final List<ItemReference> buyItems;
    private final Optional<List<ItemReference>> themeSpecialtyItems;
    private final Optional<StockConfig> stock;
    private final Optional<List<PriceModifier>> priceModifiers;
    private final Optional<List<TradeContractEntry>> tradeContracts;
    private final Optional<List<ResourceLocation>> tradeProfiles;

    public ResolvedTheme(
        ResourceLocation name,
        ResourceLocation biome,
        List<ItemReference> sellItems,
        List<ItemReference> buyItems,
        Optional<List<ItemReference>> themeSpecialtyItems,
        Optional<StockConfig> stock,
        Optional<List<PriceModifier>> priceModifiers,
        Optional<List<TradeContractEntry>> tradeContracts,
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

    public List<ItemReference> sellItems() {
        return sellItems;
    }

    public List<ItemReference> buyItems() {
        return buyItems;
    }

    public Optional<List<ItemReference>> themeSpecialtyItems() {
        return themeSpecialtyItems;
    }

    public Optional<List<ResourceLocation>> themeSpecialties() {
        return themeSpecialtyItems.map(items -> items.stream()
            .filter(ItemReference::isExactItem)
            .map(ItemReference::itemId)
            .map(ResourceLocation::parse)
            .toList());
    }

    public Optional<StockConfig> stock() {
        return stock;
    }

    public Optional<List<PriceModifier>> priceModifiers() {
        return priceModifiers;
    }

    public Optional<List<TradeContractEntry>> tradeContracts() {
        return tradeContracts;
    }

    public Optional<List<ResourceLocation>> tradeProfiles() {
        return tradeProfiles;
    }
}
