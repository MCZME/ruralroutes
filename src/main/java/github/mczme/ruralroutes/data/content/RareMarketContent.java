package github.mczme.ruralroutes.data.content;

import github.mczme.ruralroutes.core.rumor.RumorFamily;

import static github.mczme.ruralroutes.data.content.MarketRuleRegistrar.pool;
import static github.mczme.ruralroutes.data.content.MarketRuleRegistrar.rumorTarget;

public final class RareMarketContent {
    private RareMarketContent() {
    }

    public static void defineMarketRules(MarketRuleRegistrar rules) {
        rules.globalRule("rare/nether_goods_surge", pool("nether_goods"), 0.28f, -0.20f, 0.35f, 10,
            RumorFamily.DEMAND, rumorTarget("nether_goods"));
        rules.globalRule("rare/ocean_goods_shortage", pool("ocean_goods"), 0.25f, -0.40f, 0.05f, 10,
            RumorFamily.SHORTAGE, rumorTarget("ocean_goods"));
        rules.globalRule("rare/end_goods_surge", pool("end_goods"), 0.35f, -0.22f, 0.40f, 8,
            RumorFamily.DEMAND, rumorTarget("end_goods"));
        rules.globalRule("rare/precious_surge", pool("precious"), 0.40f, -0.25f, 0.32f, 5,
            RumorFamily.DEMAND, rumorTarget("precious"));

        rules.globalRule("rare/ender_pearl_import", "minecraft:ender_pearl", 0.35f, 0.45f, -0.25f, 8,
            RumorFamily.RELEASE, rumorTarget("end_goods"));
        rules.globalRule("rare/blaze_rod_import", "minecraft:blaze_rod", 0.40f, 0.35f, -0.25f, 6,
            RumorFamily.RELEASE, rumorTarget("nether_goods"));

        rules.themeRule("rare/snowy_iceworks/blue_ice_surge", "minecraft:blue_ice", 0.18f, -0.10f, 0.25f, 8,
            RumorFamily.DEMAND, null, "snowy_iceworks");
        rules.themeRule("rare/taiga_berries/glow_berries_surge", "minecraft:glow_berries", 0.18f, -0.10f, 0.28f, 10,
            RumorFamily.DEMAND, null, "taiga_berries");
    }
}
