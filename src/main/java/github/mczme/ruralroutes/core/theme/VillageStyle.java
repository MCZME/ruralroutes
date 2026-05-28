package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
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
    PLAINS("plains", ResourceLocation.parse("minecraft:plains"), ResourceLocation.parse("minecraft:village_plains")),
    DESERT("desert", ResourceLocation.parse("minecraft:desert"), ResourceLocation.parse("minecraft:village_desert")),
    SAVANNA("savanna", ResourceLocation.parse("minecraft:savanna"), ResourceLocation.parse("minecraft:village_savanna")),
    TAIGA("taiga", ResourceLocation.parse("minecraft:taiga"), ResourceLocation.parse("minecraft:village_taiga")),
    SNOWY("snowy", ResourceLocation.parse("minecraft:snowy_plains"), ResourceLocation.parse("minecraft:village_snowy"));

    private static final Map<ResourceLocation, VillageStyle> BY_BIOME = Map.of(
        ResourceLocation.parse("minecraft:plains"), PLAINS,
        ResourceLocation.parse("minecraft:desert"), DESERT,
        ResourceLocation.parse("minecraft:savanna"), SAVANNA,
        ResourceLocation.parse("minecraft:taiga"), TAIGA,
        ResourceLocation.parse("minecraft:snowy_plains"), SNOWY
    );

    public static final Codec<VillageStyle> CODEC =
        Codec.STRING.xmap(VillageStyle::byName, VillageStyle::getSerializedName);

    private final String serializedName;
    private final ResourceLocation biomeId;
    private final ResourceLocation structureId;

    VillageStyle(String serializedName, ResourceLocation biomeId, ResourceLocation structureId) {
        this.serializedName = serializedName;
        this.biomeId = biomeId;
        this.structureId = structureId;
    }

    public ResourceLocation biomeId() {
        return biomeId;
    }

    /**
     * 与风格一一对应的原版村庄结构。
     *
     * 不使用 `StructureTags.ON_*_VILLAGE_MAPS` 作为图册搜索入口，因为这些 tag
     * 位于 vanilla 的 trade_rebalance 内置数据包中，普通世界默认不会加载。
     */
    public ResourceLocation structureId() {
        return structureId;
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

    public static VillageStyle byName(String serializedName) {
        return switch (serializedName) {
            case "plains" -> PLAINS;
            case "desert" -> DESERT;
            case "savanna" -> SAVANNA;
            case "taiga" -> TAIGA;
            case "snowy" -> SNOWY;
            default -> PLAINS;
        };
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
