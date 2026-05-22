package github.mczme.ruralroutes.data.builder;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.theme.PriceModifier;
import github.mczme.ruralroutes.core.theme.StockConfig;
import github.mczme.ruralroutes.core.theme.StockRange;
import github.mczme.ruralroutes.core.theme.StockTarget;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * 主题模板构建器。
 * 仅负责主题级字段：群系、profile 引用、库存与价格修正。
 */
public class ThemeBuilder {

    private final String name;
    private String biome;
    private final java.util.List<ResourceLocation> tradeProfiles = new java.util.ArrayList<>();
    private StockRange defaultStock;
    private final Map<String, StockTarget> stockTargets = new LinkedHashMap<>();
    private final Map<String, StockRange> stockSpecific = new LinkedHashMap<>();
    private final java.util.List<PriceModifier> priceModifiers = new java.util.ArrayList<>();
    private BiConsumer<ResourceLocation, ThemeTemplate> registrar;

    private ThemeBuilder(String name) {
        this.name = name;
    }

    public static ThemeBuilder create(String name) {
        return new ThemeBuilder(name);
    }

    public ThemeBuilder registrar(BiConsumer<ResourceLocation, ThemeTemplate> registrar) {
        this.registrar = registrar;
        return this;
    }

    public ThemeBuilder biome(String biome) {
        this.biome = biome;
        return this;
    }

    public ThemeBuilder tradeProfile(String profileId) {
        this.tradeProfiles.add(ResourceLocation.parse(profileId));
        return this;
    }

    public ThemeBuilder tradeProfile(ResourceLocation profileId) {
        this.tradeProfiles.add(profileId);
        return this;
    }

    public ThemeBuilder stock(int min, int max) {
        this.defaultStock = new StockRange(min, max);
        return this;
    }

    public ThemeBuilder stockSpecific(String key, int min, int max) {
        stockSpecific.put(key, new StockRange(min, max));
        return this;
    }

    public ThemeBuilder stockTarget(String key, int min, int max) {
        stockTargets.put(key, StockTarget.shared(new StockRange(min, max)));
        return this;
    }

    public ThemeBuilder stockTarget(String key, int sellMin, int sellMax, int buyMin, int buyMax) {
        stockTargets.put(key, StockTarget.directional(
            new StockRange(sellMin, sellMax),
            new StockRange(buyMin, buyMax)
        ));
        return this;
    }

    public ThemeBuilder priceModifier(String key, float sell, float buy) {
        priceModifiers.add(PriceModifier.of(
            github.mczme.ruralroutes.core.trade.TradeTargetRef.fromString(key),
            sell,
            buy
        ));
        return this;
    }

    public ThemeBuilder priceModifier(TradeTargetRef targetRef, float sell, float buy) {
        priceModifiers.add(PriceModifier.of(targetRef, sell, buy));
        return this;
    }

    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, name);
    }

    public ThemeTemplate build() {
        return new ThemeTemplate(
            getId(),
            ResourceLocation.parse(biome),
            defaultStock == null && stockTargets.isEmpty() && stockSpecific.isEmpty()
                ? Optional.empty()
                : Optional.of(new StockConfig(
                    Optional.ofNullable(defaultStock),
                    stockTargets.isEmpty() ? Optional.empty()
                        : Optional.of(Collections.unmodifiableMap(new LinkedHashMap<>(stockTargets))),
                    stockSpecific.isEmpty() ? Optional.empty()
                        : Optional.of(Collections.unmodifiableMap(new LinkedHashMap<>(stockSpecific)))
                )),
            priceModifiers.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(priceModifiers)),
            tradeProfiles.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(tradeProfiles))
        );
    }

    public void register() {
        if (registrar != null) {
            registrar.accept(getId(), build());
        }
    }
}
