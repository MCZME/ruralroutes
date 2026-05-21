package github.mczme.ruralroutes.core.theme;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 主题加载器。
 * 同时加载 theme 和 trade profile，并在加载阶段合并为最终可消费的已解析主题。
 */
public class ThemeManager extends SimpleJsonResourceReloadListener {

    private static final String THEME_PATH = "ruralroutes/themes";
    private static final String PROFILE_PATH = "ruralroutes/trade_profiles";
    private static final Gson GSON = new Gson();

    public static final ThemeManager INSTANCE = new ThemeManager();

    private Map<ResourceLocation, ResolvedTheme> byName = Map.of();
    private Map<ResourceLocation, List<ResolvedTheme>> byBiome = Map.of();
    private Map<ResourceLocation, ThemeTemplate> rawThemes = Map.of();
    private Map<ResourceLocation, TradeProfile> profiles = Map.of();

    private ThemeManager() {
        super(GSON, THEME_PATH);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ThemeTemplate> themeMap = loadThemes(objects);
        Map<ResourceLocation, TradeProfile> profileMap = loadProfiles(resourceManager);
        Map<ResourceLocation, ResolvedTheme> resolvedMap = new LinkedHashMap<>();
        Map<ResourceLocation, List<ResolvedTheme>> biomeMap = new HashMap<>();

        for (ThemeTemplate template : themeMap.values()) {
            ResolvedTheme resolved = resolveTheme(template, profileMap);
            resolvedMap.put(resolved.name(), resolved);
            biomeMap.computeIfAbsent(resolved.biome(), key -> new ArrayList<>()).add(resolved);
        }

        this.rawThemes = Map.copyOf(themeMap);
        this.profiles = Map.copyOf(profileMap);
        this.byName = Map.copyOf(resolvedMap);
        this.byBiome = biomeMap.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));

        ThemePriceModifierResolver.invalidate();
        RuralRoutes.LOGGER.info("Loaded {} themes, {} profiles across {} biomes", byName.size(), profiles.size(), byBiome.size());
    }

    private Map<ResourceLocation, ThemeTemplate> loadThemes(Map<ResourceLocation, JsonElement> objects) {
        Map<ResourceLocation, ThemeTemplate> themeMap = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                ThemeTemplate template = ThemeTemplate.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(msg -> new RuntimeException("Failed to parse theme " + entry.getKey() + ": " + msg));
                themeMap.put(template.name(), template);
                RuralRoutes.LOGGER.debug("Loaded theme: {} for biome: {}", template.name(), template.biome());
            } catch (RuntimeException e) {
                RuralRoutes.LOGGER.error("Failed to parse theme: {}", entry.getKey(), e);
            }
        }
        return themeMap;
    }

    private Map<ResourceLocation, TradeProfile> loadProfiles(ResourceManager resourceManager) {
        Map<ResourceLocation, TradeProfile> profileMap = new LinkedHashMap<>();
        try {
            var resources = resourceManager.listResources(PROFILE_PATH, path -> path.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> entry : resources.entrySet()) {
                ResourceLocation fileId = entry.getKey();
                try {
                    JsonElement json;
                    try (var reader = entry.getValue().openAsReader()) {
                        json = GSON.fromJson(reader, JsonElement.class);
                    }
                    TradeProfile profile = TradeProfile.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(msg -> new RuntimeException("Failed to parse trade profile " + fileId + ": " + msg));
                    profileMap.put(profile.name(), profile);
                    RuralRoutes.LOGGER.debug("Loaded trade profile: {}", profile.name());
                } catch (RuntimeException e) {
                    RuralRoutes.LOGGER.error("Failed to parse trade profile: {}", fileId, e);
                }
            }
        } catch (Exception e) {
            RuralRoutes.LOGGER.warn("Failed to list trade profile resources from {}", PROFILE_PATH, e);
        }
        return profileMap;
    }

    private ResolvedTheme resolveTheme(ThemeTemplate template, Map<ResourceLocation, TradeProfile> profileMap) {
        List<ThemeTemplate.ItemReference> sellItems = new ArrayList<>();
        List<ThemeTemplate.ItemReference> buyItems = new ArrayList<>();
        List<ThemeTemplate.ItemReference> specialties = new ArrayList<>();
        List<ThemeTemplate.TradeContractEntry> tradeContracts = new ArrayList<>();
        List<ThemeTemplate.PriceModifier> priceModifiers = new ArrayList<>();
        ThemeTemplate.StockConfig themeStock = template.stock().orElse(null);
        Map<String, ThemeTemplate.StockTarget> targetEntries = new LinkedHashMap<>();
        Map<String, ThemeTemplate.StockRange> stockTargets = new LinkedHashMap<>();

        for (ResourceLocation profileId : template.tradeProfiles().orElse(List.of())) {
            TradeProfile profile = profileMap.get(profileId);
            if (profile == null) {
                RuralRoutes.LOGGER.warn("Theme {} references missing trade profile {}", template.name(), profileId);
                continue;
            }
            sellItems.addAll(profile.sellItems());
            buyItems.addAll(profile.buyItems());
            profile.themeSpecialties().ifPresent(specialties::addAll);
            profile.tradeContracts().ifPresent(tradeContracts::addAll);
            if (profile.stock().isPresent()) {
                ThemeTemplate.StockConfig profileStock = profile.stock().get();
                profileStock.targetEntries().ifPresent(targetEntries::putAll);
                profileStock.targets().ifPresent(stockTargets::putAll);
                profileStock.specific().ifPresent(stockTargets::putAll);
            }
        }

        sellItems.addAll(template.sellItems());
        buyItems.addAll(template.buyItems());
        template.themeSpecialtyItems().ifPresent(specialties::addAll);
        template.tradeContracts().ifPresent(tradeContracts::addAll);
        template.priceModifiers().ifPresent(priceModifiers::addAll);
        template.stock().ifPresent(stock -> {
            stock.targetEntries().ifPresent(targetEntries::putAll);
            stock.targets().ifPresent(stockTargets::putAll);
            stock.specific().ifPresent(stockTargets::putAll);
        });

        Optional<ThemeTemplate.StockConfig> resolvedStock;
        if (themeStock == null && stockTargets.isEmpty() && targetEntries.isEmpty()) {
            resolvedStock = Optional.empty();
        } else {
            resolvedStock = Optional.of(new ThemeTemplate.StockConfig(
                themeStock != null ? themeStock.defaultRange() : Optional.empty(),
                targetEntries.isEmpty() ? Optional.empty() : Optional.of(Map.copyOf(targetEntries)),
                stockTargets.isEmpty() ? Optional.empty() : Optional.of(Map.copyOf(stockTargets))
            ));
        }

        return new ResolvedTheme(
            template.name(),
            template.biome(),
            List.copyOf(sellItems),
            List.copyOf(buyItems),
            specialties.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(specialties)),
            resolvedStock,
            priceModifiers.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(priceModifiers)),
            tradeContracts.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(tradeContracts))
        );
    }

    /**
     * 按名称获取主题。
     */
    public ResolvedTheme getTheme(ResourceLocation name) {
        return byName.get(name);
    }

    /**
     * 获取指定群系下的所有主题。
     */
    public List<ResolvedTheme> getThemesForBiome(ResourceLocation biome) {
        return byBiome.getOrDefault(biome, List.of());
    }

    /**
     * 获取所有已加载主题。
     */
    public Map<ResourceLocation, ResolvedTheme> getAllThemes() {
        return byName;
    }

    public Map<ResourceLocation, ThemeTemplate> getAllRawThemes() {
        return rawThemes;
    }

    public Map<ResourceLocation, ThemeTemplate> getRawThemes() {
        return rawThemes;
    }

    public Map<ResourceLocation, TradeProfile> getAllProfiles() {
        return profiles;
    }
}
