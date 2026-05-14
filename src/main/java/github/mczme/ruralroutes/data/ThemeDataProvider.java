package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.data.builder.ThemeBuilder;
import github.mczme.ruralroutes.register.RRItemTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

/**
 * 主题模板数据生成器
 * 生成试点主题 JSON 文件到 data/<namespace>/ruralroutes/themes/
 */
public class ThemeDataProvider extends JsonCodecProvider<ThemeTemplate> {

    public ThemeDataProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Target.DATA_PACK, "ruralroutes/themes", PackType.SERVER_DATA,
              ThemeTemplate.CODEC, lookupProvider, "ruralroutes", existingFileHelper);
    }

    @Override
    protected void gather() {
        // ==================== 平原群系 ====================

        theme("plains_granary")
            .biome("minecraft:plains")
            .sell("minecraft:bread", "minecraft:oak_planks")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP), 1)
            .buy("minecraft:wheat", "minecraft:iron_ingot", "minecraft:coal")
            .specialty("minecraft:golden_carrot")
            .register();

        theme("plains_pasture")
            .biome("minecraft:plains")
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 1)
            .buy("minecraft:stone_axe", "minecraft:iron_sword")
            .specialty("minecraft:saddle")
            .register();

        theme("plains_workshop")
            .biome("minecraft:plains")
            .sell("minecraft:iron_pickaxe")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 1)
            .buy("minecraft:iron_ingot", "minecraft:coal", "minecraft:diamond")
            .specialty("minecraft:anvil")
            .register();

        // ==================== 沙漠群系 ====================

        theme("desert_quarry")
            .biome("minecraft:desert")
            .sell("minecraft:sandstone")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 1)
            .buy("minecraft:bread", "minecraft:oak_log")
            .specialty("minecraft:chiseled_sandstone")
            .register();

        theme("desert_oasis")
            .biome("minecraft:desert")
            .sell("minecraft:wheat")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 1)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .specialty("minecraft:golden_carrot")
            .register();

        theme("desert_dyeworks")
            .biome("minecraft:desert")
            .sell("minecraft:terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 1)
            .buy("minecraft:clay_ball", "minecraft:bone_meal")
            .specialty("minecraft:cyan_dye")
            .register();

        // ==================== 热带草原群系 ====================

        theme("savanna_woodworks")
            .biome("minecraft:savanna")
            .sell("minecraft:acacia_log", "minecraft:charcoal")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 1)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .specialty("minecraft:saddle")
            .register();

        theme("savanna_terracotta")
            .biome("minecraft:savanna")
            .sell("minecraft:terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 1)
            .buy("minecraft:bread", "minecraft:cooked_beef")
            .specialty("minecraft:yellow_glazed_terracotta")
            .register();

        theme("savanna_herder")
            .biome("minecraft:savanna")
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 1)
            .buy("minecraft:stone_axe", "minecraft:iron_sword")
            .specialty("minecraft:rabbit_hide")
            .register();

        // ==================== 针叶林群系 ====================

        theme("taiga_lumber")
            .biome("minecraft:taiga")
            .sell("minecraft:spruce_log", "minecraft:charcoal")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 1)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .specialty("minecraft:spruce_boat")
            .register();

        theme("taiga_berries")
            .biome("minecraft:taiga")
            .sell("minecraft:sweet_berries")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD), 1)
            .buy("minecraft:iron_ingot", "minecraft:cyan_dye")
            .specialty("minecraft:glow_berries")
            .register();

        theme("taiga_fur")
            .biome("minecraft:taiga")
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 1)
            .buy("minecraft:iron_sword", "minecraft:iron_axe")
            .specialty("minecraft:campfire")
            .register();

        // ==================== 积雪平原群系 ====================

        theme("snowy_iceworks")
            .biome("minecraft:snowy_plains")
            .sell("minecraft:ice")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 1)
            .buy("minecraft:bread", "minecraft:spruce_log")
            .specialty("minecraft:blue_ice")
            .register();

        theme("snowy_waystation")
            .biome("minecraft:snowy_plains")
            .sell("minecraft:bread")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_WAYSTATION_SUPPLIES), 1)
            .buy("minecraft:iron_ingot", "minecraft:gold_ingot")
            .specialty("minecraft:campfire")
            .register();

        theme("snowy_hunter")
            .biome("minecraft:snowy_plains")
            .sell("minecraft:beef")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER), 1)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS), 1)
            .buy("minecraft:iron_sword", "minecraft:spruce_log")
            .specialty("minecraft:bone_block")
            .register();
    }

    /**
     * 创建主题构建器
     */
    private ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name).registrar(this::unconditional);
    }

    private static String tagRef(TagKey<Item> tag) {
        return "tag:" + tag.location();
    }
}
