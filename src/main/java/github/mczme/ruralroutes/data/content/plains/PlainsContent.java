package github.mczme.ruralroutes.data.content.plains;

import github.mczme.ruralroutes.core.rumor.RumorFamily;
import github.mczme.ruralroutes.core.theme.CompositionStrategy;
import github.mczme.ruralroutes.core.trade.TradeSide;
import github.mczme.ruralroutes.data.builder.ThemeBuilder;
import github.mczme.ruralroutes.data.builder.TradeProfileBuilder;
import github.mczme.ruralroutes.data.content.CurrencyStockTiers;
import github.mczme.ruralroutes.data.content.MarketRuleRegistrar;
import github.mczme.ruralroutes.register.RRItemTags;

import java.util.function.Consumer;

import static github.mczme.ruralroutes.data.content.ContentRefs.*;
import static github.mczme.ruralroutes.data.content.MarketRuleRegistrar.pool;
import static github.mczme.ruralroutes.data.content.MarketRuleRegistrar.rumorTarget;

public final class PlainsContent {
    private PlainsContent() {
    }

    public static void defineThemes(Consumer<ThemeBuilder> consumer) {
        consumer.accept(theme("plains_granary")
            .biome("minecraft:plains")
            .tradeProfile("ruralroutes:plains_granary")
            .stock(4, 8)
            .stockTarget("plains_granary/staples", 18, 32, 4, 8)
            .stockTarget("plains_granary/rare_food", 1, 3, 1, 4)
            .stockTarget("minecraft:oak_planks", 10, 16, 6, 10)
            .stockTarget("minecraft:composter", 4, 8, 1, 3)
            .stockTarget("plains_granary/procurement", 1, 3, 14, 24)
            .stockTarget("minecraft:bone_meal", 1, 3, 16, 28)
            .stockTarget("minecraft:iron_ingot", 1, 3, 16, 28)
            .stockTarget("minecraft:raw_iron", 1, 2, 14, 24)
            .stockTarget("minecraft:coal", 1, 3, 14, 24)
            .stockTarget("minecraft:charcoal", 1, 3, 14, 24)
            .stockTarget("minecraft:golden_carrot", 1, 3, 1, 4)
            .stockTarget("minecraft:glistering_melon_slice", 1, 3, 1, 4)
            .stockTarget("minecraft:golden_apple", 1, 2, 1, 3)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP), 0.68f, 0.62f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 0.78f, 0.70f)
            .priceModifier(sourceRef("plains_granary/staples"), 0.72f, 0.66f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.15f, 1.30f)
            .priceModifier("minecraft:bone_meal", 1.08f, 1.35f)
            .priceModifier(sourceRef("plains_granary/rare_food"), 0.94f, 0.82f)
            .priceModifier("minecraft:golden_apple", 1.05f, 0.80f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("plains_pasture")
            .biome("minecraft:plains")
            .tradeProfile("ruralroutes:plains_pasture")
            .stock(4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 14, 24, 4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 8, 16, 4, 8)
            .stockTarget("plains_pasture/leather_armor", 4, 8, 1, 3)
            .stockTarget("plains_pasture/tools", 1, 3, 12, 20)
            .stockTarget("plains_pasture/travel_goods", 1, 2, 1, 3)
            .stockTarget("minecraft:lead", 1, 2, 1, 3)
            .stockTarget("minecraft:name_tag", 1, 1, 0, 1)
            .stockTarget("minecraft:saddle", 1, 2, 0, 1)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 0.72f, 0.66f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 0.86f, 0.78f)
            .priceModifier(sourceRef("plains_pasture/tools"), 1.08f, 1.28f)
            .priceModifier(sourceRef("plains_pasture/leather_armor"), 0.90f, 0.82f)
            .priceModifier("minecraft:lead", 1.08f, 0.84f)
            .priceModifier("minecraft:name_tag", 1.16f, 0.78f)
            .priceModifier("minecraft:saddle", 1.16f, 0.78f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("plains_workshop")
            .biome("minecraft:plains")
            .tradeProfile("ruralroutes:plains_workshop")
            .stock(3, 7)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 6, 12, 2, 5)
            .stockTarget("plains_workshop/basic_tools", 6, 12, 2, 5)
            .stockTarget("plains_workshop/armor_goods", 3, 7, 1, 3)
            .stockTarget("plains_workshop/rare_tools", 1, 2, 0, 1)
            .stockTarget("plains_workshop/materials", 1, 3, 14, 24)
            .stockTarget("minecraft:arrow", 16, 32, 2, 6)
            .stockTarget("minecraft:bow", 3, 6, 1, 3)
            .stockTarget("minecraft:iron_ingot", 1, 3, 16, 28)
            .stockTarget("minecraft:raw_iron", 1, 2, 14, 24)
            .stockTarget("minecraft:coal", 1, 3, 14, 24)
            .stockTarget("minecraft:charcoal", 1, 3, 14, 24)
            .stockTarget("minecraft:diamond", 1, 2, 4, 8)
            .stockTarget("minecraft:anvil", 1, 1, 0, 1)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 0.82f, 0.78f)
            .priceModifier(sourceRef("plains_workshop/basic_tools"), 0.82f, 0.78f)
            .priceModifier(sourceRef("plains_workshop/armor_goods"), 0.88f, 0.82f)
            .priceModifier(sourceRef("plains_workshop/rare_tools"), 0.94f, 0.82f)
            .priceModifier(sourceRef("plains_workshop/materials"), 1.08f, 1.24f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.24f)
            .priceModifier("minecraft:anvil", 1.12f, 0.78f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));
    }

    public static void defineTradeProfiles(Consumer<TradeProfileBuilder> consumer) {
        consumer.accept(profile("plains_granary")
            .withCurrency(CurrencyStockTiers.BULK)
            .sell("minecraft:oak_planks", "minecraft:composter")
            .sellPick("plains_granary/staples", 5,
                "minecraft:bread",
                "minecraft:baked_potato",
                "minecraft:pumpkin_pie",
                "minecraft:apple",
                "minecraft:sugar_cane",
                tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP))
            .sellPick("plains_granary/rare_food", 1,
                "minecraft:golden_carrot",
                "minecraft:glistering_melon_slice",
                "minecraft:golden_apple")
            .buy("minecraft:wheat", "minecraft:iron_ingot", "minecraft:coal", "minecraft:bone_meal")
            .buyPick("plains_granary/procurement", 3,
                "minecraft:raw_iron",
                "minecraft:charcoal",
                "minecraft:wooden_hoe",
                "minecraft:wooden_axe",
                "minecraft:wooden_pickaxe",
                "minecraft:stone_hoe",
                "minecraft:stone_axe",
                "minecraft:stone_pickaxe")
            .addFixedTrade(inputs(
                in("minecraft:apple", 1),
                in("minecraft:gold_nugget", 8)),
                outputs(out("minecraft:golden_apple", 1)))
            .addFixedTrade(inputs(
                in("minecraft:golden_apple", 1),
                in("minecraft:golden_carrot", 1),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:ender_pearl", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), "minecraft:bone_meal"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("plains_pasture")
            .withCurrency(CurrencyStockTiers.PETTY)
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 3)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 2)
            .sellPick("plains_pasture/leather_armor", 2,
                "minecraft:leather_helmet",
                "minecraft:leather_chestplate",
                "minecraft:leather_leggings",
                "minecraft:leather_boots")
            .sellPick("plains_pasture/travel_goods", 1,
                "minecraft:saddle",
                "minecraft:lead",
                "minecraft:name_tag")
            .buy("minecraft:wooden_sword", "minecraft:stone_sword", "minecraft:wooden_axe", "minecraft:stone_axe", "minecraft:shears")
            .buyPick("plains_pasture/tools", 2,
                "minecraft:wooden_sword",
                "minecraft:stone_sword",
                "minecraft:wooden_axe",
                "minecraft:stone_axe",
                "minecraft:wooden_shovel",
                "minecraft:stone_shovel")
            .addFixedTrade(inputs(
                in("minecraft:leather", 4),
                in("minecraft:white_wool", 2),
                in("minecraft:iron_nugget", 6)),
                outputs(out("minecraft:saddle", 1)))
            .addFixedTrade(inputs(
                in("minecraft:golden_apple", 1),
                in("minecraft:saddle", 1),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:ender_pearl", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:wooden_sword", "minecraft:stone_sword", "minecraft:wooden_axe", "minecraft:stone_axe", "minecraft:shears"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("plains_workshop")
            .withCurrency(CurrencyStockTiers.WORKSHOP)
            .sell("minecraft:bow", "minecraft:arrow")
            .sellPick("plains_workshop/basic_tools", 5,
                "minecraft:stone_pickaxe",
                "minecraft:stone_axe",
                "minecraft:stone_shovel",
                "minecraft:stone_hoe",
                "minecraft:iron_pickaxe",
                "minecraft:iron_axe",
                "minecraft:iron_shovel",
                "minecraft:iron_hoe",
                "minecraft:iron_sword")
            .sellPick("plains_workshop/armor_goods", 3,
                "minecraft:chainmail_helmet",
                "minecraft:chainmail_chestplate",
                "minecraft:chainmail_leggings",
                "minecraft:chainmail_boots",
                "minecraft:iron_helmet",
                "minecraft:iron_chestplate",
                "minecraft:iron_leggings",
                "minecraft:iron_boots")
            .sellPick("plains_workshop/rare_tools", 2,
                "minecraft:diamond_pickaxe",
                "minecraft:diamond_axe",
                "minecraft:diamond_sword",
                "minecraft:diamond_shovel",
                "minecraft:diamond_hoe",
                "minecraft:golden_pickaxe",
                "minecraft:golden_axe",
                "minecraft:golden_sword",
                "minecraft:anvil")
            .buy("minecraft:iron_ingot", "minecraft:raw_iron", "minecraft:coal", "minecraft:charcoal", "minecraft:diamond")
            .buyPick("plains_workshop/materials", 3,
                "minecraft:raw_iron",
                "minecraft:charcoal",
                "minecraft:coal",
                "minecraft:oak_planks",
                "minecraft:stick",
                "minecraft:iron_nugget")
            .addFixedTrade(inputs(
                in("minecraft:iron_ingot", 6),
                in("minecraft:charcoal", 3),
                in("minecraft:oak_planks", 6)),
                outputs(out("minecraft:anvil", 1)))
            .addFixedTrade(inputs(
                in("minecraft:anvil", 1),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:blaze_rod", 1)))
            .addCurrencyBasketTrade(
                TradeSide.SELL_TO_PLAYER,
                refs("minecraft:bow", "minecraft:arrow", sourceRef("plains_workshop/basic_tools"), sourceRef("plains_workshop/armor_goods")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .addCurrencyBasketTrade(
                TradeSide.SELL_TO_PLAYER,
                refs(sourceRef("plains_workshop/rare_tools")),
                currencies("ruralroutes:gold_coin", "ruralroutes:iron_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), sourceRef("plains_workshop/materials")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));
    }

    public static void defineMarketRules(MarketRuleRegistrar rules) {
        rules.biomeRule("biome/plains/harvest_year", pool("crop"), -0.18f, 0.40f, -0.12f, 65,
            RumorFamily.SURPLUS, rumorTarget("crop"), "plains");
        rules.biomeRule("biome/plains/tool_demand", sourceRef("plains_workshop/basic_tools"), 0.18f, -0.10f, 0.28f, 55,
            RumorFamily.DEMAND, rumorTarget("tool"), "plains");

        rules.themeRule("theme/plains_granary/bumper_crop", sourceRef("plains_granary/staples"), -0.24f, 0.45f, -0.12f, 40,
            RumorFamily.SURPLUS, rumorTarget("crop"), "plains_granary");
        rules.themeRule("theme/plains_pasture/full_sheds", pool("leather_fiber"), -0.22f, 0.35f, -0.15f, 35,
            RumorFamily.SURPLUS, rumorTarget("leather_fiber"), "plains_pasture");
        rules.themeRule("theme/plains_workshop/rush_order", sourceRef("plains_workshop/basic_tools"), 0.22f, -0.12f, 0.35f, 40,
            RumorFamily.DEMAND, rumorTarget("tool"), "plains_workshop");
    }
}
