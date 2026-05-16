package github.mczme.ruralroutes.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.core.trade.TradeSide;
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
            .sell("minecraft:oak_planks")
            .sellPick("plains_granary/staples", 3,
                "minecraft:bread",
                tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP))
            .buy("minecraft:wheat", "minecraft:iron_ingot", "minecraft:coal")
            .buyPick("plains_granary/procurement", 2,
                "minecraft:raw_iron",
                "minecraft:charcoal",
                "minecraft:stone_hoe")
            .stock(10, 18)
            .stockSpecific("plains_granary/staples", 22, 34)
            .stockSpecific("minecraft:oak_planks", 12, 20)
            .stockSpecific("minecraft:wheat", 18, 30)
            .stockSpecific("plains_granary/procurement", 10, 16)
            .stockSpecific("minecraft:golden_carrot", 1, 3)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP), 0.82f, 0.76f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 0.92f, 0.86f)
            .priceModifier(priceTagRef(RRItemTags.POOL_MINERAL), 1.10f, 1.16f)
            .priceModifier("minecraft:golden_carrot", 1.12f, 0.84f)
            .addFixedTrade(inputs(
                in("minecraft:carrot", 8),
                in("minecraft:bread", 2),
                in("minecraft:wheat", 6)),
                outputs(out("minecraft:golden_carrot", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(priceTagRef(RRItemTags.POOL_MINERAL)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:golden_carrot")
            .register();

        theme("plains_pasture")
            .biome("minecraft:plains")
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 2)
            .buy("minecraft:stone_axe", "minecraft:iron_sword")
            .buyPick("plains_pasture/tools", 2,
                "minecraft:iron_axe",
                "minecraft:shears",
                "minecraft:stone_sword")
            .stock(8, 14)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 16, 24)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 12, 20)
            .stockSpecific("minecraft:leather", 14, 20)
            .stockSpecific("plains_pasture/tools", 8, 14)
            .stockSpecific("minecraft:saddle", 1, 2)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 0.82f, 0.76f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 0.94f, 0.88f)
            .priceModifier("minecraft:saddle", 1.15f, 0.84f)
            .addFixedTrade(inputs(
                in("minecraft:leather", 4),
                in("minecraft:white_wool", 2),
                in("minecraft:cooked_beef", 3)),
                outputs(out("minecraft:saddle", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:saddle")
            .register();

        theme("plains_workshop")
            .biome("minecraft:plains")
            .sell("minecraft:iron_pickaxe")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal", "minecraft:diamond")
            .buyPick("plains_workshop/materials", 2,
                "minecraft:raw_iron",
                "minecraft:charcoal",
                "minecraft:oak_planks")
            .stock(6, 12)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 8, 14)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD), 8, 14)
            .stockSpecific("minecraft:iron_pickaxe", 4, 7)
            .stockSpecific("plains_workshop/materials", 10, 16)
            .stockSpecific("minecraft:iron_ingot", 12, 18)
            .stockSpecific("minecraft:diamond", 2, 4)
            .stockSpecific("minecraft:anvil", 1, 2)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 0.88f, 0.82f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD), 0.96f, 0.90f)
            .priceModifier(priceTagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:anvil", 1.15f, 0.82f)
            .addFixedTrade(inputs(
                in("minecraft:iron_ingot", 6),
                in("minecraft:charcoal", 3),
                in("minecraft:oak_planks", 6)),
                outputs(out("minecraft:anvil", 1)))
            .addCurrencyBasketTrade(
                TradeSide.SELL_TO_PLAYER,
                refs(
                    "minecraft:iron_pickaxe",
                    priceTagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(priceTagRef(RRItemTags.POOL_MINERAL)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:anvil")
            .register();

        // ==================== 沙漠群系 ====================

        theme("desert_quarry")
            .biome("minecraft:desert")
            .sell("minecraft:sandstone")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 3)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 2)
            .buy("minecraft:bread", "minecraft:oak_log")
            .buyPick("desert_quarry/supplies", 2,
                "minecraft:charcoal",
                "minecraft:stick",
                "minecraft:cooked_beef")
            .stock(10, 18)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 22, 34)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 16, 26)
            .stockSpecific("minecraft:sandstone", 18, 28)
            .stockSpecific("desert_quarry/supplies", 10, 16)
            .stockSpecific("minecraft:chiseled_sandstone", 2, 4)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 0.78f, 0.72f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 0.84f, 0.78f)
            .priceModifier(priceTagRef(RRItemTags.POOL_FOOD), 1.04f, 1.12f)
            .priceModifier(priceTagRef(RRItemTags.POOL_WOOD), 1.06f, 1.12f)
            .priceModifier("minecraft:chiseled_sandstone", 1.10f, 0.86f)
            .addFixedTrade(inputs(
                in("minecraft:red_sandstone", 2),
                in("minecraft:stick", 1)),
                outputs(out("minecraft:chiseled_sandstone", 1)))
            .specialty("minecraft:chiseled_sandstone")
            .register();

        theme("desert_oasis")
            .biome("minecraft:desert")
            .sell("minecraft:wheat")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .buyPick("desert_oasis/inputs", 2,
                "minecraft:raw_iron",
                "minecraft:bone_meal",
                "minecraft:bucket")
            .stock(8, 16)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 20, 30)
            .stockSpecific("minecraft:wheat", 16, 26)
            .stockSpecific("desert_oasis/inputs", 8, 14)
            .stockSpecific("minecraft:golden_carrot", 1, 3)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 0.84f, 0.78f)
            .priceModifier(priceTagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:golden_carrot", 1.12f, 0.84f)
            .addFixedTrade(inputs(
                in("minecraft:melon", 4),
                in("minecraft:wheat", 6),
                in("minecraft:carrot", 8)),
                outputs(out("minecraft:golden_carrot", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(priceTagRef(RRItemTags.POOL_MINERAL), "minecraft:bucket"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:golden_carrot")
            .register();

        theme("desert_dyeworks")
            .biome("minecraft:desert")
            .sell("minecraft:terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 2)
            .buy("minecraft:clay_ball", "minecraft:bone_meal")
            .buyPick("desert_dyeworks/inputs", 2,
                "minecraft:cactus",
                "minecraft:sand",
                "minecraft:charcoal")
            .stock(6, 12)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 12, 20)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 10, 16)
            .stockSpecific("minecraft:terracotta", 12, 20)
            .stockSpecific("desert_dyeworks/inputs", 10, 16)
            .stockSpecific("minecraft:clay_ball", 14, 24)
            .stockSpecific("minecraft:bone_meal", 12, 20)
            .stockSpecific("minecraft:cyan_dye", 2, 4)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 0.84f, 0.78f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 0.90f, 0.84f)
            .priceModifier("minecraft:cyan_dye", 1.08f, 0.88f)
            .addFixedTrade(inputs(
                in("minecraft:cactus", 1),
                in("minecraft:bone_meal", 1)),
                outputs(out("minecraft:cyan_dye", 1)))
            .specialty("minecraft:cyan_dye")
            .register();

        // ==================== 热带草原群系 ====================

        theme("savanna_woodworks")
            .biome("minecraft:savanna")
            .sell("minecraft:acacia_log", "minecraft:charcoal")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .buyPick("savanna_woodworks/tools", 2,
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:raw_iron")
            .stock(10, 18)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 20, 32)
            .stockSpecific("minecraft:acacia_log", 16, 26)
            .stockSpecific("minecraft:charcoal", 14, 24)
            .stockSpecific("savanna_woodworks/tools", 8, 14)
            .stockSpecific("minecraft:saddle", 1, 2)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 0.80f, 0.74f)
            .priceModifier(priceTagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:saddle", 1.12f, 0.86f)
            .addFixedTrade(inputs(
                in("minecraft:leather", 4),
                in("minecraft:acacia_log", 4),
                in("minecraft:charcoal", 4)),
                outputs(out("minecraft:saddle", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(priceTagRef(RRItemTags.POOL_MINERAL), "minecraft:iron_axe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:saddle")
            .register();

        theme("savanna_terracotta")
            .biome("minecraft:savanna")
            .sell("minecraft:terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 2)
            .buy("minecraft:bread", "minecraft:cooked_beef")
            .buyPick("savanna_terracotta/supplies", 2,
                "minecraft:charcoal",
                "minecraft:clay_ball",
                "minecraft:acacia_log")
            .stock(7, 13)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 12, 20)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 10, 18)
            .stockSpecific("minecraft:terracotta", 12, 20)
            .stockSpecific("savanna_terracotta/supplies", 8, 14)
            .stockSpecific("minecraft:yellow_glazed_terracotta", 1, 3)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 0.84f, 0.78f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 0.88f, 0.82f)
            .priceModifier(priceTagRef(RRItemTags.POOL_FOOD), 1.04f, 1.10f)
            .priceModifier("minecraft:yellow_glazed_terracotta", 1.10f, 0.88f)
            .addFixedTrade(inputs(
                in("minecraft:terracotta", 1),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:yellow_glazed_terracotta", 1)))
            .specialty("minecraft:yellow_glazed_terracotta")
            .register();

        theme("savanna_herder")
            .biome("minecraft:savanna")
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 2)
            .buy("minecraft:stone_axe", "minecraft:iron_sword")
            .buyPick("savanna_herder/tools", 2,
                "minecraft:iron_axe",
                "minecraft:shears",
                "minecraft:stone_sword")
            .stock(8, 14)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 16, 24)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 12, 20)
            .stockSpecific("minecraft:leather", 14, 20)
            .stockSpecific("savanna_herder/tools", 8, 14)
            .stockSpecific("minecraft:rabbit_hide", 2, 4)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 0.82f, 0.76f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 0.94f, 0.88f)
            .priceModifier("minecraft:rabbit_hide", 1.08f, 0.88f)
            .addFixedTrade(inputs(
                in("minecraft:feather", 1),
                in("minecraft:cooked_beef", 1)),
                outputs(out("minecraft:rabbit_hide", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:rabbit_hide")
            .register();

        // ==================== 针叶林群系 ====================

        theme("taiga_lumber")
            .biome("minecraft:taiga")
            .sell("minecraft:spruce_log", "minecraft:charcoal")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .buyPick("taiga_lumber/tools", 2,
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:raw_iron")
            .stock(10, 18)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 20, 32)
            .stockSpecific("minecraft:spruce_log", 16, 26)
            .stockSpecific("minecraft:charcoal", 14, 24)
            .stockSpecific("taiga_lumber/tools", 8, 14)
            .stockSpecific("minecraft:spruce_boat", 1, 3)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 0.80f, 0.74f)
            .priceModifier(priceTagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.16f)
            .priceModifier("minecraft:spruce_boat", 1.10f, 0.88f)
            .addFixedTrade(inputs(
                in("minecraft:spruce_log", 2),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:spruce_boat", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(priceTagRef(RRItemTags.POOL_MINERAL), "minecraft:iron_axe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:spruce_boat")
            .register();

        theme("taiga_berries")
            .biome("minecraft:taiga")
            .sellPick("taiga_berries/produce", 3,
                tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD),
                "minecraft:glow_berries")
            .buy("minecraft:iron_ingot", "minecraft:cyan_dye")
            .buyPick("taiga_berries/farming", 2,
                "minecraft:bone_meal",
                "minecraft:stone_hoe",
                "minecraft:iron_hoe")
            .stock(7, 13)
            .stockSpecific("taiga_berries/produce", 14, 22)
            .stockSpecific("taiga_berries/farming", 8, 14)
            .stockSpecific("minecraft:iron_ingot", 8, 14)
            .stockSpecific("minecraft:cyan_dye", 4, 8)
            .stockSpecific("minecraft:glow_berries", 3, 6)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD), 0.84f, 0.78f)
            .priceModifier(priceTagRef(RRItemTags.POOL_MINERAL), 1.06f, 1.14f)
            .priceModifier("minecraft:glow_berries", 1.10f, 0.88f)
            .addFixedTrade(inputs(
                in("minecraft:sweet_berries", 1),
                in("minecraft:bone_meal", 1)),
                outputs(out("minecraft:glow_berries", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_ingot", "minecraft:iron_hoe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:glow_berries")
            .register();

        theme("taiga_fur")
            .biome("minecraft:taiga")
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 2)
            .buy("minecraft:iron_sword", "minecraft:iron_axe")
            .buyPick("taiga_fur/tools", 2,
                "minecraft:stone_axe",
                "minecraft:shears",
                "minecraft:charcoal")
            .stock(7, 13)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 14, 22)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 12, 20)
            .stockSpecific("minecraft:leather", 12, 18)
            .stockSpecific("taiga_fur/tools", 8, 14)
            .stockSpecific("minecraft:campfire", 1, 3)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 0.82f, 0.76f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 0.88f, 0.82f)
            .priceModifier("minecraft:campfire", 1.08f, 0.90f)
            .addFixedTrade(inputs(
                in("minecraft:leather", 1),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:campfire", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:campfire")
            .register();

        // ==================== 积雪平原群系 ====================

        theme("snowy_iceworks")
            .biome("minecraft:snowy_plains")
            .sell("minecraft:ice")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 2)
            .buy("minecraft:bread", "minecraft:spruce_log")
            .buyPick("snowy_iceworks/supplies", 2,
                "minecraft:charcoal",
                "minecraft:cooked_beef",
                "minecraft:oak_log")
            .stock(6, 12)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 14, 22)
            .stockSpecific(stockKey(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 12, 18)
            .stockSpecific("minecraft:ice", 12, 18)
            .stockSpecific("snowy_iceworks/supplies", 8, 14)
            .stockSpecific("minecraft:blue_ice", 1, 2)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 0.78f, 0.72f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 0.84f, 0.78f)
            .priceModifier("minecraft:blue_ice", 1.14f, 0.86f)
            .addFixedTrade(inputs(
                in("minecraft:packed_ice", 2),
                in("minecraft:cooked_beef", 1)),
                outputs(out("minecraft:blue_ice", 1)))
            .specialty("minecraft:blue_ice")
            .register();

        theme("snowy_waystation")
            .biome("minecraft:snowy_plains")
            .sellPick("snowy_waystation/supplies", 4,
                tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD),
                "minecraft:bread",
                "minecraft:oak_log",
                "minecraft:spruce_log",
                "minecraft:charcoal")
            .buy("minecraft:iron_ingot", "minecraft:gold_ingot")
            .buyPick("snowy_waystation/valuables", 2,
                "minecraft:raw_iron",
                "minecraft:coal",
                "minecraft:charcoal")
            .stock(8, 16)
            .stockSpecific("snowy_waystation/supplies", 16, 24)
            .stockSpecific("snowy_waystation/valuables", 8, 14)
            .stockSpecific("minecraft:iron_ingot", 10, 16)
            .stockSpecific("minecraft:gold_ingot", 6, 10)
            .stockSpecific("minecraft:campfire", 2, 4)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_SNOWY_WAYSTATION_SUPPLIES), 0.96f, 0.90f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD), 0.94f, 0.88f)
            .priceModifier(priceTagRef(RRItemTags.POOL_WOOD), 0.96f, 0.90f)
            .priceModifier(priceTagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.14f)
            .priceModifier("minecraft:campfire", 1.08f, 0.90f)
            .addFixedTrade(inputs(
                in("minecraft:bread", 1),
                in("minecraft:spruce_log", 1)),
                outputs(out("minecraft:campfire", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(priceTagRef(RRItemTags.POOL_MINERAL)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:campfire")
            .register();

        theme("snowy_hunter")
            .biome("minecraft:snowy_plains")
            .sellPick("snowy_hunter/game_goods", 3,
                tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER),
                "minecraft:beef",
                "minecraft:rabbit")
            .buy("minecraft:iron_sword", "minecraft:spruce_log")
            .buyPick("snowy_hunter/tools", 2,
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:charcoal")
            .stock(7, 13)
            .stockSpecific("snowy_hunter/game_goods", 14, 22)
            .stockSpecific("snowy_hunter/tools", 8, 14)
            .stockSpecific("minecraft:iron_sword", 6, 10)
            .stockSpecific("minecraft:spruce_log", 8, 14)
            .stockSpecific("minecraft:bone_block", 1, 3)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS), 0.86f, 0.80f)
            .priceModifier(priceTagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER), 0.80f, 0.74f)
            .priceModifier("minecraft:bone_block", 1.08f, 0.88f)
            .addFixedTrade(inputs(
                in("minecraft:bone", 3)),
                outputs(out("minecraft:bone_block", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:bone_block")
            .register();
    }

    /**
     * 创建主题构建器
     */
    private ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name)
            .withCurrency()
            .registrar(this::unconditional);
    }

    private static String tagRef(TagKey<Item> tag) {
        return "tag:" + tag.location();
    }

    private static String stockKey(TagKey<Item> tag) {
        return "#" + tag.location();
    }

    private static String priceTagRef(TagKey<Item> tag) {
        return "#" + tag.location();
    }

    private static ThemeTemplate.InputEntry in(String item, int count) {
        return new ThemeTemplate.InputEntry(item, count);
    }

    private static ThemeTemplate.OutputEntry out(String item, int count) {
        return new ThemeTemplate.OutputEntry(item, count);
    }

    @SafeVarargs
    private static List<ThemeTemplate.InputEntry> inputs(ThemeTemplate.InputEntry... entries) {
        return List.of(entries);
    }

    @SafeVarargs
    private static List<ThemeTemplate.OutputEntry> outputs(ThemeTemplate.OutputEntry... entries) {
        return List.of(entries);
    }

    private static List<String> currencies(String... ids) {
        return List.of(ids);
    }

    private static List<String> refs(String... ids) {
        return List.of(ids);
    }
}
