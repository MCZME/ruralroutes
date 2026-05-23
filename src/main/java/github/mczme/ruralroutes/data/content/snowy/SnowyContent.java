package github.mczme.ruralroutes.data.content.snowy;

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

public final class SnowyContent {
    private SnowyContent() {
    }

    public static void defineThemes(Consumer<ThemeBuilder> consumer) {
        consumer.accept(theme("snowy_iceworks")
            .biome("minecraft:snowy_plains")
            .tradeProfile("ruralroutes:snowy_iceworks")
            .stock(3, 7)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 12, 22, 3, 6)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 10, 18, 3, 6)
            .stockTarget("snowy_iceworks/supplies", 1, 3, 16, 28)
            .stockTarget("minecraft:blue_ice", 1, 2, 0, 1)
            .stockTarget("minecraft:bucket", 1, 2, 1, 3)
            .stockTarget("minecraft:glass_bottle", 2, 4, 1, 3)
            .stockTarget("minecraft:packed_ice", 4, 8, 2, 4)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 0.70f, 0.64f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 0.80f, 0.72f)
            .priceModifier(sourceRef("snowy_iceworks/supplies"), 1.12f, 1.36f)
            .priceModifier("minecraft:blue_ice", 0.96f, 0.82f)
            .priceModifier("minecraft:bucket", 0.98f, 1.10f)
            .priceModifier("minecraft:glass_bottle", 0.92f, 0.84f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("snowy_waystation")
            .biome("minecraft:snowy_plains")
            .tradeProfile("ruralroutes:snowy_waystation")
            .stock(3, 7)
            .stockTarget("snowy_waystation/supplies", 4, 8, 16, 28)
            .stockTarget("snowy_waystation/valuables", 1, 3, 14, 24)
            .stockTarget("minecraft:bread", 4, 8, 16, 28)
            .stockTarget("minecraft:charcoal", 3, 6, 16, 28)
            .stockTarget("minecraft:spruce_log", 3, 6, 14, 24)
            .stockTarget("minecraft:campfire", 1, 3, 4, 8)
            .stockTarget("minecraft:gold_ingot", 1, 2, 8, 14)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(sourceRef("snowy_waystation/supplies"), 1.02f, 1.30f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD), 1.04f, 1.34f)
            .priceModifier(tagRef(RRItemTags.POOL_WOOD), 1.02f, 1.34f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.22f)
            .priceModifier("minecraft:campfire", 0.96f, 1.12f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("snowy_hunter")
            .biome("minecraft:snowy_plains")
            .tradeProfile("ruralroutes:snowy_hunter")
            .stock(3, 7)
            .stockTarget("snowy_hunter/game_goods", 10, 18, 3, 6)
            .stockTarget("snowy_hunter/tools", 1, 3, 14, 24)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS), 10, 18, 3, 6)
            .stockTarget("minecraft:bone", 12, 24, 4, 8)
            .stockTarget("minecraft:bone_block", 1, 3, 1, 3)
            .stockTarget("minecraft:bow", 2, 5, 1, 3)
            .stockTarget("minecraft:arrow", 12, 24, 2, 6)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS), 0.82f, 0.76f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER), 0.78f, 0.72f)
            .priceModifier(sourceRef("snowy_hunter/tools"), 1.12f, 1.32f)
            .priceModifier("minecraft:bone_block", 0.94f, 0.82f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));
    }

    public static void defineTradeProfiles(Consumer<TradeProfileBuilder> consumer) {
        consumer.accept(profile("snowy_iceworks")
            .withCurrency(CurrencyStockTiers.WORKSHOP)
            .sell("minecraft:snow_block", "minecraft:ice", "minecraft:packed_ice", "minecraft:blue_ice",
                "minecraft:bucket", "minecraft:glass_bottle")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 2)
            .buy("minecraft:bread", "minecraft:charcoal", "minecraft:spruce_log",
                "minecraft:campfire", "minecraft:coal", "minecraft:iron_shovel")
            .buyPick("snowy_iceworks/supplies", 3,
                "minecraft:cooked_beef",
                "minecraft:baked_potato",
                "minecraft:pumpkin_pie",
                "minecraft:stone_pickaxe",
                "minecraft:iron_pickaxe",
                "minecraft:oak_log")
            .addFixedTrade(inputs(
                in("minecraft:packed_ice", 2),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:blue_ice", 1)))
            .addFixedTrade(inputs(
                in("minecraft:blue_ice", 1),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:ender_pearl", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(sourceRef("snowy_iceworks/supplies"), tagRef(RRItemTags.POOL_FOOD), tagRef(RRItemTags.POOL_WOOD)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("snowy_waystation")
            .withCurrency(CurrencyStockTiers.CARAVAN)
            .sell("minecraft:apple", "minecraft:rabbit_stew", "minecraft:torch")
            .sellPick("snowy_waystation/supplies", 5,
                tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD),
                "minecraft:bread",
                "minecraft:baked_potato",
                "minecraft:oak_log",
                "minecraft:spruce_log",
                "minecraft:charcoal",
                "minecraft:campfire")
            .buy("minecraft:bread", "minecraft:charcoal", "minecraft:spruce_log",
                "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:oak_log", "minecraft:coal")
            .buyPick("snowy_waystation/valuables", 3,
                "minecraft:raw_iron",
                "minecraft:coal",
                "minecraft:charcoal",
                "minecraft:gold_ingot",
                "minecraft:campfire")
            .addFixedTrade(inputs(
                in("minecraft:bread", 1),
                in("minecraft:spruce_log", 1)),
                outputs(out("minecraft:campfire", 1)))
            .addFixedTrade(inputs(
                in("minecraft:blue_ice", 1),
                in("minecraft:campfire", 2),
                in("ruralroutes:gold_coin", 1)),
                outputs(out("minecraft:blaze_rod", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), tagRef(RRItemTags.POOL_FOOD), tagRef(RRItemTags.POOL_WOOD), sourceRef("snowy_waystation/valuables")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("snowy_hunter")
            .withCurrency(CurrencyStockTiers.PETTY)
            .sell("minecraft:bow", "minecraft:arrow", "minecraft:bone",
                "minecraft:cooked_rabbit", "minecraft:leather", "minecraft:bone_block")
            .sellPick("snowy_hunter/game_goods", 4,
                tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER),
                tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS),
                "minecraft:rabbit",
                "minecraft:cooked_rabbit",
                "minecraft:bone")
            .buy("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears",
                "minecraft:charcoal", "minecraft:spruce_log")
            .buyPick("snowy_hunter/tools", 3,
                "minecraft:stone_axe",
                "minecraft:stone_sword",
                "minecraft:iron_axe",
                "minecraft:iron_sword",
                "minecraft:charcoal",
                "minecraft:spruce_log",
                "minecraft:shears")
            .addFixedTrade(inputs(in("minecraft:bone", 9)),
                outputs(out("minecraft:bone_block", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears", sourceRef("snowy_hunter/tools")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));
    }

    public static void defineMarketRules(MarketRuleRegistrar rules) {
        rules.biomeRule("biome/snowy/cold_supply", pool("food"), 0.22f, -0.38f, 0.20f, 60,
            RumorFamily.SHORTAGE, rumorTarget("food"), "snowy_plains");
        rules.biomeRule("biome/snowy/thaw", pool("ice_snow"), -0.18f, 0.35f, -0.05f, 50,
            RumorFamily.RELEASE, rumorTarget("ice_snow"), "snowy_plains");

        rules.themeRule("theme/snowy_iceworks/cold_storage", pool("ice_snow"), -0.24f, 0.45f, -0.05f, 40,
            RumorFamily.RELEASE, rumorTarget("ice_snow"), "snowy_iceworks");
        rules.themeRule("theme/snowy_waystation/rations_low", sourceRef("snowy_waystation/supplies"), 0.24f, -0.40f, 0.18f, 35,
            RumorFamily.SHORTAGE, rumorTarget("food"), "snowy_waystation");
        rules.themeRule("theme/snowy_hunter/good_hunt", sourceRef("snowy_hunter/game_goods"), -0.22f, 0.35f, -0.08f, 35,
            RumorFamily.RELEASE, rumorTarget("leather_fiber"), "snowy_hunter");
    }
}
