package github.mczme.ruralroutes.data.content.taiga;

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

public final class TaigaContent {
    private TaigaContent() {
    }

    public static void defineThemes(Consumer<ThemeBuilder> consumer) {
        consumer.accept(theme("taiga_lumber")
            .biome("minecraft:taiga")
            .tradeProfile("ruralroutes:taiga_lumber")
            .stock(4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 18, 30, 4, 8)
            .stockTarget("taiga_lumber/tools", 1, 3, 14, 24)
            .stockTarget("minecraft:spruce_log", 16, 26, 4, 8)
            .stockTarget("minecraft:spruce_planks", 14, 24, 4, 8)
            .stockTarget("minecraft:charcoal", 12, 22, 6, 10)
            .stockTarget("minecraft:spruce_boat", 1, 3, 1, 3)
            .stockTarget("minecraft:campfire", 1, 3, 1, 3)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 0.70f, 0.64f)
            .priceModifier(sourceRef("taiga_lumber/tools"), 1.08f, 1.28f)
            .priceModifier(tagRef(RRItemTags.POOL_MINERAL), 1.08f, 1.24f)
            .priceModifier("minecraft:spruce_boat", 0.92f, 0.82f)
            .priceModifier("minecraft:campfire", 0.94f, 0.84f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("taiga_berries")
            .biome("minecraft:taiga")
            .tradeProfile("ruralroutes:taiga_berries")
            .stock(3, 7)
            .stockTarget("taiga_berries/produce", 10, 18, 4, 8)
            .stockTarget("taiga_berries/farming", 1, 3, 14, 24)
            .stockTarget("minecraft:bone_meal", 1, 3, 16, 28)
            .stockTarget("minecraft:dried_kelp", 2, 4, 1, 3)
            .stockTarget("minecraft:mushroom_stew", 1, 3, 1, 2)
            .stockTarget("minecraft:glow_berries", 2, 4, 1, 3)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD), 0.78f, 0.72f)
            .priceModifier(sourceRef("taiga_berries/farming"), 1.08f, 1.30f)
            .priceModifier("minecraft:bone_meal", 1.08f, 1.32f)
            .priceModifier("minecraft:dried_kelp", 0.96f, 0.86f)
            .priceModifier("minecraft:mushroom_stew", 0.94f, 0.84f)
            .priceModifier("minecraft:glow_berries", 0.92f, 0.82f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));

        consumer.accept(theme("taiga_fur")
            .biome("minecraft:taiga")
            .tradeProfile("ruralroutes:taiga_fur")
            .stock(3, 7)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 12, 22, 4, 8)
            .stockTarget(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 10, 18, 4, 8)
            .stockTarget("taiga_fur/tools", 1, 3, 14, 24)
            .stockTarget("minecraft:campfire", 1, 3, 1, 3)
            .stockTarget("minecraft:bow", 2, 5, 1, 3)
            .stockTarget("minecraft:arrow", 12, 24, 2, 6)
            .stockTarget("minecraft:ender_pearl", 1, 1, 0, 0)
            .stockTarget("minecraft:blaze_rod", 1, 1, 0, 0)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 0.76f, 0.70f)
            .priceModifier(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 0.84f, 0.76f)
            .priceModifier(sourceRef("taiga_fur/tools"), 1.08f, 1.28f)
            .priceModifier("minecraft:campfire", 0.92f, 0.82f)
            .priceModifier("minecraft:ender_pearl", 1.40f, 0.60f)
            .priceModifier("minecraft:blaze_rod", 1.45f, 0.55f));
    }

    public static void defineTradeProfiles(Consumer<TradeProfileBuilder> consumer) {
        consumer.accept(profile("taiga_lumber")
            .withCurrency(CurrencyStockTiers.CARAVAN)
            .sell("minecraft:spruce_log", "minecraft:spruce_planks", "minecraft:charcoal", "minecraft:spruce_boat",
                "minecraft:campfire", "minecraft:barrel", "minecraft:chest")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 3)
            .buy("minecraft:iron_ingot", "minecraft:raw_iron", "minecraft:coal", "minecraft:lantern")
            .buyPick("taiga_lumber/tools", 3,
                "minecraft:wooden_axe",
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:raw_iron",
                "minecraft:stick")
            .addFixedTrade(inputs(
                in("minecraft:spruce_log", 2),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:spruce_boat", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), sourceRef("taiga_lumber/tools")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("taiga_berries")
            .withCurrency(CurrencyStockTiers.PETTY)
            .sell("minecraft:glow_berries", "minecraft:dried_kelp", "minecraft:mushroom_stew")
            .sellPick("taiga_berries/produce", 5,
                tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD),
                "minecraft:carrot",
                "minecraft:potato",
                "minecraft:beetroot",
                "minecraft:glow_berries",
                "minecraft:brown_mushroom",
                "minecraft:red_mushroom",
                "minecraft:rabbit_stew")
            .buy("minecraft:bone_meal", "minecraft:lantern", "minecraft:bucket")
            .buyPick("taiga_berries/farming", 4,
                "minecraft:wooden_hoe",
                "minecraft:stone_hoe",
                "minecraft:iron_hoe",
                "minecraft:bone_meal",
                "minecraft:iron_ingot",
                "minecraft:stone_shovel")
            .addFixedTrade(inputs(
                in("minecraft:sweet_berries", 1),
                in("minecraft:bone_meal", 1)),
                outputs(out("minecraft:glow_berries", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:bone_meal", "minecraft:iron_hoe", "minecraft:iron_ingot", sourceRef("taiga_berries/farming")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));

        consumer.accept(profile("taiga_fur")
            .withCurrency(CurrencyStockTiers.CARAVAN)
            .sell("minecraft:leather", "minecraft:bow", "minecraft:arrow", "minecraft:campfire")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 2)
            .buy("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears")
            .buyPick("taiga_fur/tools", 3,
                "minecraft:stone_axe",
                "minecraft:stone_sword",
                "minecraft:shears",
                "minecraft:charcoal",
                "minecraft:iron_sword",
                "minecraft:iron_axe")
            .addFixedTrade(inputs(
                in("minecraft:leather", 1),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:campfire", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears", sourceRef("taiga_fur/tools")),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST));
    }

    public static void defineMarketRules(MarketRuleRegistrar rules) {
        rules.biomeRule("biome/taiga/logging_season", pool("wood"), -0.18f, 0.35f, -0.08f, 60,
            RumorFamily.RELEASE, rumorTarget("wood"), "taiga");
        rules.biomeRule("biome/taiga/fur_demand", pool("leather_fiber"), 0.16f, -0.10f, 0.26f, 50,
            RumorFamily.DEMAND, rumorTarget("leather_fiber"), "taiga");

        rules.themeRule("theme/taiga_lumber/fresh_cut", pool("wood"), -0.24f, 0.42f, -0.08f, 40,
            RumorFamily.RELEASE, rumorTarget("wood"), "taiga_lumber");
        rules.themeRule("theme/taiga_berries/berry_season", sourceRef("taiga_berries/produce"), -0.22f, 0.32f, -0.05f, 35,
            RumorFamily.RELEASE, rumorTarget("food"), "taiga_berries");
        rules.themeRule("theme/taiga_fur/winter_fur", pool("leather_fiber"), 0.22f, -0.10f, 0.35f, 35,
            RumorFamily.DEMAND, rumorTarget("leather_fiber"), "taiga_fur");
    }
}
