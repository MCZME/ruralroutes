package github.mczme.ruralroutes.data.content.desert;

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

public final class DesertContent {
    private DesertContent() {
    }

    public static void defineThemes(Consumer<ThemeBuilder> consumer) {
        consumer.accept(theme("desert_quarry")
            .biome("minecraft:desert")
            .tradeProfile("ruralroutes:desert_quarry")
            .stock(4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 18, 32, 4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 12, 22, 4, 8)
            .stockTarget("desert_quarry/supplies", 1, 3, 16, 28)
            .stockTarget("minecraft:sandstone", 16, 28, 4, 8)
            .stockTarget("minecraft:chiseled_sandstone", 2, 4, 1, 3)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 0.64f, 0.58f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 0.74f, 0.68f)
            .priceModifier(sourceRef("desert_quarry/supplies"), 1.12f, 1.34f)
            .priceModifier(tagRef(RRItemTags.POOL_FOOD), 1.10f, 1.28f)
            .priceModifier(tagRef(RRItemTags.POOL_WOOD), 1.15f, 1.36f)
            .priceModifier("minecraft:chiseled_sandstone", 0.90f, 0.76f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("desert_oasis")
            .biome("minecraft:desert")
            .tradeProfile("ruralroutes:desert_oasis")
            .stock(3, 7)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 8, 16, 4, 8)
            .stockTarget("desert_oasis/supplies", 4, 8, 10, 18)
            .stockTarget("desert_oasis/inputs", 1, 3, 14, 24)
            .stockTarget("minecraft:bucket", 1, 2, 8, 14)
            .stockTarget("minecraft:water_bucket", 1, 1, 0, 0)
            .stockTarget("minecraft:dried_kelp", 2, 4, 1, 3)
            .stockTarget("minecraft:ice", 1, 2, 10, 18)
            .stockTarget("minecraft:packed_ice", 1, 2, 8, 14)
            .stockTarget("minecraft:glistering_melon_slice", 1, 3, 1, 4)
            .stockTarget("minecraft:rabbit_stew", 2, 4, 1, 3)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 0.82f, 0.78f)
            .priceModifier(sourceRef("desert_oasis/supplies"), 0.92f, 1.12f)
            .priceModifier(sourceRef("desert_oasis/inputs"), 1.12f, 1.32f)
            .priceModifier(tagRef(RRItemTags.POOL_ICE_SNOW), 1.10f, 1.36f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.22f)
            .priceModifier("minecraft:water_bucket", 0.96f, 0.84f)
            .priceModifier("minecraft:dried_kelp", 0.96f, 0.84f)
            .priceModifier("minecraft:glistering_melon_slice", 0.96f, 0.82f)
            .priceModifier("minecraft:rabbit_stew", 0.92f, 0.82f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("desert_dyeworks")
            .biome("minecraft:desert")
            .tradeProfile("ruralroutes:desert_dyeworks")
            .stock(3, 7)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 10, 18, 4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 8, 16, 3, 6)
            .stockTarget("desert_dyeworks/materials", 1, 3, 16, 30)
            .stockTarget("minecraft:clay_ball", 1, 3, 16, 28)
            .stockTarget("minecraft:bone_meal", 1, 3, 14, 24)
            .stockTarget("minecraft:cyan_dye", 2, 4, 1, 3)
            .stockTarget("minecraft:green_dye", 4, 8, 2, 4)
            .stockTarget("minecraft:yellow_glazed_terracotta", 1, 2, 1, 2)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 0.78f, 0.72f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 0.86f, 0.78f)
            .priceModifier(sourceRef("desert_dyeworks/materials"), 1.08f, 1.30f)
            .priceModifier("minecraft:cyan_dye", 0.90f, 0.78f)
            .priceModifier("minecraft:green_dye", 0.78f, 0.70f)
            .priceModifier("minecraft:yellow_glazed_terracotta", 0.94f, 0.84f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));
    }

    public static void defineTradeProfiles(Consumer<TradeProfileBuilder> consumer) {
        consumer.accept(profile("desert_quarry")
            .withCurrency(CurrencyStockTiers.CARAVAN)
            .sell("minecraft:sand", "minecraft:sandstone", "minecraft:chiseled_sandstone",
                "minecraft:cut_sandstone", "minecraft:smooth_sandstone", "minecraft:stone")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 4)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 2)
            .buy("minecraft:bread", "minecraft:oak_log", "minecraft:charcoal",
                "minecraft:wooden_shovel", "minecraft:iron_pickaxe")
            .buyPick("desert_quarry/supplies", 3,
                "minecraft:coal",
                "minecraft:stick",
                "minecraft:cooked_beef",
                "minecraft:wooden_pickaxe",
                "minecraft:stone_pickaxe",
                "minecraft:stone_shovel")
            .addFixedTrade(inputs(
                in("minecraft:red_sandstone", 2),
                in("minecraft:stick", 1)),
                outputs(out("minecraft:chiseled_sandstone", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(sourceRef("desert_quarry/supplies"), tagRef(RRItemTags.POOL_WOOD), tagRef(RRItemTags.POOL_FOOD)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("desert_oasis")
            .withCurrency(CurrencyStockTiers.PETTY)
            .sell("minecraft:cactus", "minecraft:rabbit_stew", "minecraft:water_bucket", "minecraft:dried_kelp")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 3)
            .sellPick("desert_oasis/supplies", 2,
                "minecraft:bread",
                "minecraft:apple",
                "minecraft:melon",
                "minecraft:glistering_melon_slice",
                "minecraft:rabbit_stew")
            .buy("minecraft:bucket", "minecraft:ice", "minecraft:packed_ice",
                "minecraft:bone_meal", "minecraft:apple", "minecraft:coal")
            .buyPick("desert_oasis/inputs", 3,
                "minecraft:raw_iron",
                "minecraft:coal",
                "minecraft:bread",
                "minecraft:baked_potato",
                "minecraft:pumpkin_pie")
            .addFixedTrade(inputs(
                in("minecraft:glistering_melon_slice", 2),
                in("minecraft:golden_carrot", 1),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:ender_pearl", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), tagRef(RRItemTags.POOL_ICE_SNOW), "minecraft:bucket"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("desert_dyeworks")
            .withCurrency(CurrencyStockTiers.WORKSHOP)
            .sell("minecraft:terracotta", "minecraft:glass", "minecraft:green_dye",
                "minecraft:yellow_dye", "minecraft:orange_dye", "minecraft:yellow_glazed_terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 2)
            .buy("minecraft:clay_ball", "minecraft:bone_meal", "minecraft:cactus",
                "minecraft:charcoal", "minecraft:coal")
            .buyPick("desert_dyeworks/materials", 3,
                "minecraft:sand",
                "minecraft:red_sand",
                "minecraft:white_concrete_powder",
                "minecraft:orange_concrete_powder",
                "minecraft:yellow_concrete_powder")
            .addFixedTrade(inputs(
                in("minecraft:cactus", 1),
                in("minecraft:bone_meal", 1)),
                outputs(out("minecraft:cyan_dye", 1)))
            .addFixedTrade(inputs(
                in("minecraft:cyan_dye", 4),
                in("minecraft:yellow_glazed_terracotta", 1),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:ender_pearl", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(sourceRef("desert_dyeworks/materials"), "minecraft:clay_ball", "minecraft:bone_meal", "minecraft:cactus"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));
    }

    public static void defineMarketRules(MarketRuleRegistrar rules) {
        rules.biomeRule("biome/desert/caravan_delay", pool("food"), 0.20f, -0.35f, 0.18f, 60,
            RumorFamily.SHORTAGE, rumorTarget("food"), "desert");
        rules.biomeRule("biome/desert/fuel_shortage", pool("wood"), 0.18f, -0.25f, 0.32f, 55,
            RumorFamily.SHORTAGE, rumorTarget("wood"), "desert");

        rules.themeRule("theme/desert_quarry/masonry_rush", pool("stone"), 0.22f, -0.08f, 0.32f, 35,
            RumorFamily.DEMAND, rumorTarget("stone"), "desert_quarry");
        rules.themeRule("theme/desert_oasis/caravan_stop", sourceRef("desert_oasis/inputs"), 0.20f, -0.25f, 0.35f, 40,
            RumorFamily.SHORTAGE, rumorTarget("food"), "desert_oasis");
        rules.themeRule("theme/desert_dyeworks/dye_boom", pool("dye_decor"), 0.22f, -0.10f, 0.34f, 35,
            RumorFamily.DEMAND, rumorTarget("dye"), "desert_dyeworks");
    }
}
