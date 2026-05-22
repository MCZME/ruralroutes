package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.data.builder.ThemeBuilder;
import github.mczme.ruralroutes.register.RRItemTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 主题模板数据生成器
 * 生成主题级 JSON 到 data/<namespace>/ruralroutes/themes/
 */
public class ThemeDataProvider extends JsonCodecProvider<ThemeTemplate> {

    public ThemeDataProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Target.DATA_PACK, "ruralroutes/themes", PackType.SERVER_DATA,
              ThemeTemplate.CODEC, lookupProvider, "ruralroutes", existingFileHelper);
    }

    @Override
    protected void gather() {
        defineThemes(builder -> builder.registrar(this::unconditional).register());
    }

    public static List<ResourceLocation> collectBuiltinThemeIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        defineThemes(builder -> ids.add(builder.getId()));
        return List.copyOf(ids);
    }

    static void defineThemes(Consumer<ThemeBuilder> consumer) {
        consumer.accept(theme("plains_granary")
            .biome("minecraft:plains")
            .tradeProfile("ruralroutes:plains_granary")
            .stock(10, 18)
            .stockSpecific("plains_granary/staples", 22, 34)
            .stockSpecific("minecraft:oak_planks", 12, 20)
            .stockSpecific("minecraft:wheat", 18, 30)
            .stockSpecific("plains_granary/procurement", 10, 16)
            .stockSpecific("minecraft:golden_carrot", 1, 3)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP), 0.82f, 0.76f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 0.92f, 0.86f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.10f, 1.16f)
            .priceModifier("minecraft:golden_carrot", 1.12f, 0.84f));

        consumer.accept(theme("plains_pasture")
            .biome("minecraft:plains")
            .tradeProfile("ruralroutes:plains_pasture")
            .stock(8, 14)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 16, 24)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 12, 20)
            .stockSpecific("minecraft:leather", 14, 20)
            .stockSpecific("plains_pasture/tools", 8, 14)
            .stockSpecific("minecraft:saddle", 1, 2)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 0.82f, 0.76f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 0.94f, 0.88f)
            .priceModifier("minecraft:saddle", 1.15f, 0.84f));

        consumer.accept(theme("plains_workshop")
            .biome("minecraft:plains")
            .tradeProfile("ruralroutes:plains_workshop")
            .stock(6, 12)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 8, 14)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD), 8, 14)
            .stockSpecific("minecraft:iron_pickaxe", 4, 7)
            .stockSpecific("plains_workshop/materials", 10, 16)
            .stockSpecific("minecraft:iron_ingot", 12, 18)
            .stockSpecific("minecraft:diamond", 2, 4)
            .stockSpecific("minecraft:anvil", 1, 2)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 0.88f, 0.82f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD), 0.96f, 0.90f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:anvil", 1.15f, 0.82f));

        consumer.accept(theme("desert_quarry")
            .biome("minecraft:desert")
            .tradeProfile("ruralroutes:desert_quarry")
            .stock(10, 18)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 22, 34)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 16, 26)
            .stockSpecific("minecraft:sandstone", 18, 28)
            .stockSpecific("desert_quarry/supplies", 10, 16)
            .stockSpecific("minecraft:chiseled_sandstone", 2, 4)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 0.78f, 0.72f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 0.84f, 0.78f)
            .priceModifier(tagRef(RRItemTags.POOL_FOOD), 1.04f, 1.12f)
            .priceModifier(tagRef(RRItemTags.POOL_WOOD), 1.06f, 1.12f)
            .priceModifier("minecraft:chiseled_sandstone", 1.10f, 0.86f));

        consumer.accept(theme("desert_oasis")
            .biome("minecraft:desert")
            .tradeProfile("ruralroutes:desert_oasis")
            .stock(8, 16)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 20, 30)
            .stockSpecific("minecraft:wheat", 16, 26)
            .stockSpecific("desert_oasis/inputs", 8, 14)
            .stockSpecific("minecraft:golden_carrot", 1, 3)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 0.84f, 0.78f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:golden_carrot", 1.12f, 0.84f));

        consumer.accept(theme("desert_dyeworks")
            .biome("minecraft:desert")
            .tradeProfile("ruralroutes:desert_dyeworks")
            .stock(6, 12)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 12, 20)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 10, 16)
            .stockSpecific("minecraft:terracotta", 12, 20)
            .stockSpecific("desert_dyeworks/inputs", 10, 16)
            .stockSpecific("minecraft:clay_ball", 14, 24)
            .stockSpecific("minecraft:bone_meal", 12, 20)
            .stockSpecific("minecraft:cyan_dye", 2, 4)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 0.84f, 0.78f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 0.90f, 0.84f)
            .priceModifier("minecraft:cyan_dye", 1.08f, 0.88f));

        consumer.accept(theme("savanna_woodworks")
            .biome("minecraft:savanna")
            .tradeProfile("ruralroutes:savanna_woodworks")
            .stock(10, 18)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 20, 32)
            .stockSpecific("minecraft:acacia_log", 16, 26)
            .stockSpecific("minecraft:charcoal", 14, 24)
            .stockSpecific("savanna_woodworks/tools", 8, 14)
            .stockSpecific("minecraft:saddle", 1, 2)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 0.80f, 0.74f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:saddle", 1.12f, 0.86f));

        consumer.accept(theme("savanna_terracotta")
            .biome("minecraft:savanna")
            .tradeProfile("ruralroutes:savanna_terracotta")
            .stock(7, 13)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 12, 20)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 10, 18)
            .stockSpecific("minecraft:terracotta", 12, 20)
            .stockSpecific("savanna_terracotta/supplies", 8, 14)
            .stockSpecific("minecraft:yellow_glazed_terracotta", 1, 3)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 0.84f, 0.78f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 0.88f, 0.82f)
            .priceModifier(tagRef(RRItemTags.POOL_FOOD), 1.04f, 1.10f)
            .priceModifier("minecraft:yellow_glazed_terracotta", 1.10f, 0.88f));

        consumer.accept(theme("savanna_herder")
            .biome("minecraft:savanna")
            .tradeProfile("ruralroutes:savanna_herder")
            .stock(8, 14)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 16, 24)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 12, 20)
            .stockSpecific("minecraft:leather", 14, 20)
            .stockSpecific("savanna_herder/tools", 8, 14)
            .stockSpecific("minecraft:rabbit_hide", 2, 4)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 0.82f, 0.76f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 0.94f, 0.88f)
            .priceModifier("minecraft:rabbit_hide", 1.08f, 0.88f));

        consumer.accept(theme("taiga_lumber")
            .biome("minecraft:taiga")
            .tradeProfile("ruralroutes:taiga_lumber")
            .stock(10, 18)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 20, 32)
            .stockSpecific("minecraft:spruce_log", 16, 26)
            .stockSpecific("minecraft:charcoal", 14, 24)
            .stockSpecific("taiga_lumber/tools", 8, 14)
            .stockSpecific("minecraft:spruce_boat", 1, 3)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 0.80f, 0.74f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:spruce_boat", 1.10f, 0.88f));

        consumer.accept(theme("taiga_berries")
            .biome("minecraft:taiga")
            .tradeProfile("ruralroutes:taiga_berries")
            .stock(7, 13)
            .stockSpecific("taiga_berries/produce", 14, 22)
            .stockSpecific("taiga_berries/farming", 8, 14)
            .stockSpecific("minecraft:iron_ingot", 8, 14)
            .stockSpecific("minecraft:cyan_dye", 4, 8)
            .stockSpecific("minecraft:glow_berries", 3, 6)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD), 0.84f, 0.78f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.06f, 1.14f)
            .priceModifier("minecraft:glow_berries", 1.10f, 0.88f));

        consumer.accept(theme("taiga_fur")
            .biome("minecraft:taiga")
            .tradeProfile("ruralroutes:taiga_fur")
            .stock(7, 13)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 14, 22)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 12, 20)
            .stockSpecific("minecraft:leather", 12, 18)
            .stockSpecific("taiga_fur/tools", 8, 14)
            .stockSpecific("minecraft:campfire", 1, 3)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 0.82f, 0.76f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 0.88f, 0.82f)
            .priceModifier("minecraft:campfire", 1.08f, 0.90f));

        consumer.accept(theme("snowy_iceworks")
            .biome("minecraft:snowy_plains")
            .tradeProfile("ruralroutes:snowy_iceworks")
            .stock(6, 12)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 14, 22)
            .stockSpecific(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 12, 18)
            .stockSpecific("minecraft:ice", 12, 18)
            .stockSpecific("snowy_iceworks/supplies", 8, 14)
            .stockSpecific("minecraft:blue_ice", 1, 2)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 0.78f, 0.72f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 0.84f, 0.78f)
            .priceModifier("minecraft:blue_ice", 1.14f, 0.86f));

        consumer.accept(theme("snowy_waystation")
            .biome("minecraft:snowy_plains")
            .tradeProfile("ruralroutes:snowy_waystation")
            .stock(8, 16)
            .stockSpecific("snowy_waystation/supplies", 16, 24)
            .stockSpecific("snowy_waystation/valuables", 8, 14)
            .stockSpecific("minecraft:iron_ingot", 10, 16)
            .stockSpecific("minecraft:gold_ingot", 6, 10)
            .stockSpecific("minecraft:campfire", 2, 4)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_WAYSTATION_SUPPLIES), 0.96f, 0.90f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD), 0.94f, 0.88f)
            .priceModifier(tagRef(RRItemTags.POOL_WOOD), 0.96f, 0.90f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.14f)
            .priceModifier("minecraft:campfire", 1.08f, 0.90f));

        consumer.accept(theme("snowy_hunter")
            .biome("minecraft:snowy_plains")
            .tradeProfile("ruralroutes:snowy_hunter")
            .stock(7, 13)
            .stockSpecific("snowy_hunter/game_goods", 14, 22)
            .stockSpecific("snowy_hunter/tools", 8, 14)
            .stockSpecific("minecraft:iron_sword", 6, 10)
            .stockSpecific("minecraft:spruce_log", 8, 14)
            .stockSpecific("minecraft:bone_block", 1, 3)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS), 0.86f, 0.80f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER), 0.80f, 0.74f)
            .priceModifier("minecraft:bone_block", 1.08f, 0.88f));
    }

    private static ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name).registrar((id, template) -> {});
    }

    private static String tagRef(TagKey<Item> tag) {
        return "#" + tag.location();
    }
}
