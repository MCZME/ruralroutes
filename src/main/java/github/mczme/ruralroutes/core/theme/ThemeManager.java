package github.mczme.ruralroutes.core.theme;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主题模板加载器
 * 扫描 data/<namespace>/ruralroutes/themes/ 目录下的 JSON 文件
 */
public class ThemeManager extends SimpleJsonResourceReloadListener {

    public static final ThemeManager INSTANCE = new ThemeManager();

    private static final String PATH = "ruralroutes/themes";
    private static final Gson GSON = new Gson();

    private Map<ResourceLocation, ThemeTemplate> byName = Map.of();
    private Map<ResourceLocation, List<ThemeTemplate>> byBiome = Map.of();

    private ThemeManager() {
        super(GSON, PATH);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ThemeTemplate> nameMap = new HashMap<>();
        Map<ResourceLocation, List<ThemeTemplate>> biomeMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                ThemeTemplate template = ThemeTemplate.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(msg -> new RuntimeException("Failed to parse theme " + entry.getKey() + ": " + msg));

                nameMap.put(template.name(), template);
                biomeMap.computeIfAbsent(template.biome(), k -> new ArrayList<>()).add(template);

                RuralRoutes.LOGGER.debug("Loaded theme: {} for biome: {}", template.name(), template.biome());
            } catch (RuntimeException e) {
                RuralRoutes.LOGGER.error("Failed to parse theme: {}", entry.getKey(), e);
            }
        }

        this.byName = Map.copyOf(nameMap);
        this.byBiome = biomeMap.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> List.copyOf(e.getValue())));

        RuralRoutes.LOGGER.info("Loaded {} themes across {} biomes", byName.size(), byBiome.size());
    }

    /**
     * 按名称获取主题模板
     */
    public ThemeTemplate getTheme(ResourceLocation name) {
        return byName.get(name);
    }

    /**
     * 获取指定群系下的所有主题模板
     */
    public List<ThemeTemplate> getThemesForBiome(ResourceLocation biome) {
        return byBiome.getOrDefault(biome, List.of());
    }

    /**
     * 获取所有已加载的主题
     */
    public Map<ResourceLocation, ThemeTemplate> getAllThemes() {
        return byName;
    }
}