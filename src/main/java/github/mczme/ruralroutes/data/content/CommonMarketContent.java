package github.mczme.ruralroutes.data.content;

import github.mczme.ruralroutes.core.rumor.RumorFamily;

import static github.mczme.ruralroutes.data.content.MarketRuleRegistrar.pool;
import static github.mczme.ruralroutes.data.content.MarketRuleRegistrar.rumorTarget;

public final class CommonMarketContent {
    private CommonMarketContent() {
    }

    public static void defineMarketRules(MarketRuleRegistrar rules) {
        rules.globalRule("common/food_shortage", pool("food"), 0.15f, -0.35f, 0.10f, 100,
            RumorFamily.SHORTAGE, rumorTarget("food"));
        rules.globalRule("common/crop_surplus", pool("crop"), -0.16f, 0.35f, -0.10f, 90,
            RumorFamily.SURPLUS, rumorTarget("crop"));
        rules.globalRule("common/wood_demand", pool("wood"), 0.14f, -0.12f, 0.30f, 85,
            RumorFamily.DEMAND, rumorTarget("wood"));
        rules.globalRule("common/stone_glut", pool("stone"), -0.12f, 0.25f, -0.15f, 80,
            RumorFamily.SURPLUS, rumorTarget("stone"));
        rules.globalRule("common/mineral_rush", pool("mineral"), 0.16f, -0.15f, 0.30f, 100,
            RumorFamily.DEMAND, rumorTarget("mineral"));
        rules.globalRule("common/leather_slump", pool("leather_fiber"), -0.12f, 0.25f, -0.15f, 75,
            RumorFamily.SURPLUS, rumorTarget("leather_fiber"));
        rules.globalRule("common/dye_hot", pool("dye_decor"), 0.16f, -0.12f, 0.25f, 80,
            RumorFamily.DEMAND, rumorTarget("decor"));
    }
}
