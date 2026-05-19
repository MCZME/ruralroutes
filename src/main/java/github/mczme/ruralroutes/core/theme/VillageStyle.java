package github.mczme.ruralroutes.core.theme;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Map;
import java.util.Optional;

/**
 * 村庄方块外观风格。
 * 由主题所属群系推导，用于驱动方块状态和模型变体。
 */
public enum VillageStyle implements StringRepresentable {
    PLAINS("plains", ResourceLocation.parse("minecraft:plains")),
    DESERT("desert", ResourceLocation.parse("minecraft:desert")),
    SAVANNA("savanna", ResourceLocation.parse("minecraft:savanna")),
    TAIGA("taiga", ResourceLocation.parse("minecraft:taiga")),
    SNOWY("snowy", ResourceLocation.parse("minecraft:snowy_plains"));

    private static final Map<ResourceLocation, VillageStyle> BY_BIOME = Map.of(
        ResourceLocation.parse("minecraft:plains"), PLAINS,
        ResourceLocation.parse("minecraft:desert"), DESERT,
        ResourceLocation.parse("minecraft:savanna"), SAVANNA,
        ResourceLocation.parse("minecraft:taiga"), TAIGA,
        ResourceLocation.parse("minecraft:snowy_plains"), SNOWY
    );

    private final String serializedName;
    private final ResourceLocation biomeId;

    VillageStyle(String serializedName, ResourceLocation biomeId) {
        this.serializedName = serializedName;
        this.biomeId = biomeId;
    }

    public ResourceLocation biomeId() {
        return biomeId;
    }

    public String translationKey() {
        return "gui.ruralroutes.config_tool.style." + serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static VillageStyle fromBiome(ResourceLocation biomeId) {
        if (biomeId == null) {
            RuralRoutes.LOGGER.warn("Biome was null when resolving village style, falling back to plains");
            return PLAINS;
        }

        VillageStyle style = BY_BIOME.get(biomeId);
        if (style == null) {
            RuralRoutes.LOGGER.warn("Unsupported biome {} for village style, falling back to plains", biomeId);
            return PLAINS;
        }

        return style;
    }

    public static Optional<VillageStyle> tryFromBiome(ResourceLocation biomeId) {
        if (biomeId == null) {
            RuralRoutes.LOGGER.warn("Biome was null when resolving village style for optional lookup");
            return Optional.empty();
        }

        VillageStyle style = BY_BIOME.get(biomeId);
        if (style == null) {
            RuralRoutes.LOGGER.warn("Unsupported biome {} for village style progress tracking, skipping style progress", biomeId);
            return Optional.empty();
        }

        return Optional.of(style);
    }
}
