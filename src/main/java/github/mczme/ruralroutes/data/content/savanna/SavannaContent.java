package github.mczme.ruralroutes.data.content.savanna;

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

public final class SavannaContent {
    private SavannaContent() {
    }

    public static void defineThemes(Consumer<ThemeBuilder> consumer) {
        consumer.accept(theme("savanna_woodworks")
            .biome("minecraft:savanna")
            .tradeProfile("ruralroutes:savanna_woodworks")
            .stock(4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 18, 30, 4, 8)
            .stockTarget("savanna_woodworks/tools", 1, 3, 14, 24)
            .stockTarget("minecraft:acacia_log", 16, 26, 4, 8)
            .stockTarget("minecraft:acacia_planks", 14, 24, 4, 8)
            .stockTarget("minecraft:charcoal", 12, 22, 6, 10)
            .stockTarget("minecraft:campfire", 1, 3, 1, 3)
            .stockTarget("minecraft:saddle", 1, 1, 0, 1)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 0.70f, 0.64f)
            .priceModifier(sourceRef("savanna_woodworks/tools"), 1.08f, 1.28f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.24f)
            .priceModifier("minecraft:campfire", 0.94f, 0.84f)
            .priceModifier("minecraft:saddle", 1.14f, 0.82f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("savanna_terracotta")
            .biome("minecraft:savanna")
            .tradeProfile("ruralroutes:savanna_terracotta")
            .stock(3, 7)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 8, 16, 5, 10)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 8, 16, 4, 8)
            .stockTarget("savanna_terracotta/supplies", 1, 3, 16, 28)
            .stockTarget("minecraft:yellow_glazed_terracotta", 1, 3, 1, 3)
            .stockTarget("minecraft:cyan_dye", 1, 3, 8, 14)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 0.84f, 0.90f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 0.86f, 0.82f)
            .priceModifier(sourceRef("savanna_terracotta/supplies"), 1.08f, 1.28f)
            .priceModifier("minecraft:yellow_glazed_terracotta", 0.94f, 0.84f)
            .priceModifier("minecraft:cyan_dye", 1.04f, 1.18f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("savanna_herder")
            .biome("minecraft:savanna")
            .tradeProfile("ruralroutes:savanna_herder")
            .stock(4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 14, 24, 4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 10, 18, 4, 8)
            .stockTarget("savanna_herder/tools", 1, 3, 14, 24)
            .stockTarget("minecraft:lead", 1, 3, 1, 2)
            .stockTarget("minecraft:saddle", 1, 2, 0, 1)
            .stockTarget("minecraft:rabbit_hide", 2, 4, 1, 3)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 0.74f, 0.68f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 0.88f, 0.80f)
            .priceModifier(sourceRef("savanna_herder/tools"), 1.08f, 1.28f)
            .priceModifier("minecraft:lead", 0.94f, 0.84f)
            .priceModifier("minecraft:saddle", 0.92f, 0.82f)
            .priceModifier("minecraft:rabbit_hide", 0.94f, 0.82f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));
    }

    public static void defineTradeProfiles(Consumer<TradeProfileBuilder> consumer) {
        consumer.accept(profile("savanna_woodworks")
            .withCurrency(CurrencyStockTiers.CARAVAN)
            .sell("minecraft:acacia_log", "minecraft:acacia_planks", "minecraft:charcoal",
                "minecraft:campfire", "minecraft:barrel", "minecraft:chest")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 3)
            .sellPick("savanna_woodworks/travel_goods", 1,
                "minecraft:saddle",
                "minecraft:lead",
                "minecraft:campfire")
            .buy("minecraft:iron_ingot", "minecraft:raw_iron", "minecraft:coal", "minecraft:lantern")
            .buyPick("savanna_woodworks/tools", 3,
                "minecraft:wooden_axe",
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:raw_iron",
                "minecraft:stick")
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), sourceRef("savanna_woodworks/tools")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("savanna_terracotta")
            .withCurrency(CurrencyStockTiers.WORKSHOP)
            .sell("minecraft:terracotta", "minecraft:brick", "minecraft:yellow_glazed_terracotta",
                "minecraft:orange_dye", "minecraft:yellow_dye", "minecraft:orange_terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 2)
            .buy("minecraft:clay_ball", "minecraft:charcoal", "minecraft:cyan_dye",
                "minecraft:green_dye", "minecraft:coal", "minecraft:red_sandstone")
            .buyPick("savanna_terracotta/supplies", 3,
                "minecraft:acacia_log",
                "minecraft:glass",
                "minecraft:sandstone",
                "minecraft:yellow_dye",
                "minecraft:bread",
                "minecraft:cooked_beef")
            .addFixedTrade(inputs(
                in("minecraft:terracotta", 1),
                in("minecraft:charcoal", 1),
                in("minecraft:yellow_dye", 1)),
                outputs(out("minecraft:yellow_glazed_terracotta", 1)))
            .addFixedTrade(inputs(
                in("minecraft:yellow_glazed_terracotta", 1),
                in("minecraft:cyan_dye", 4),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:ender_pearl", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(sourceRef("savanna_terracotta/supplies"), "minecraft:clay_ball", "minecraft:charcoal", "minecraft:cyan_dye"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("savanna_herder")
            .withCurrency(CurrencyStockTiers.PETTY)
            .sell("minecraft:leather", "minecraft:rabbit_hide", "minecraft:lead", "minecraft:saddle")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 3)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 2)
            .buy("minecraft:shears", "minecraft:stone_axe", "minecraft:iron_sword",
                "minecraft:bucket", "minecraft:acacia_fence")
            .buyPick("savanna_herder/tools", 3,
                "minecraft:wooden_axe",
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:stone_sword",
                "minecraft:iron_sword",
                "minecraft:stone")
            .addFixedTrade(inputs(
                in("minecraft:feather", 1),
                in("minecraft:cooked_beef", 1)),
                outputs(out("minecraft:rabbit_hide", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears", sourceRef("savanna_herder/tools")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));
    }

    public static void defineMarketRules(MarketRuleRegistrar rules) {
        rules.biomeRule("biome/savanna/kiln_season", pool("wood"), 0.16f, -0.08f, 0.28f, 60,
            RumorFamily.DEMAND, rumorTarget("wood"), "savanna");
        rules.biomeRule("biome/savanna/ceramic_shipment", pool("dye_decor"), -0.16f, 0.32f, -0.05f, 50,
            RumorFamily.RELEASE, rumorTarget("terracotta"), "savanna");

        rules.themeRule("theme/savanna_woodworks/timber_release", pool("wood"), -0.22f, 0.40f, -0.10f, 35,
            RumorFamily.RELEASE, rumorTarget("wood"), "savanna_woodworks");
        rules.themeRule("theme/savanna_terracotta/new_firing", pool("dye_decor"), -0.20f, 0.36f, -0.05f, 35,
            RumorFamily.RELEASE, rumorTarget("terracotta"), "savanna_terracotta");
        rules.themeRule("theme/savanna_herder/hide_shortage", pool("leather_fiber"), 0.22f, -0.32f, 0.12f, 35,
            RumorFamily.SHORTAGE, rumorTarget("leather_fiber"), "savanna_herder");
    }
}
