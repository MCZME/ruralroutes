package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.core.market.MarketEventScopeRule;
import github.mczme.ruralroutes.core.market.MarketScopeType;
import github.mczme.ruralroutes.core.rumor.RumorFamily;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import github.mczme.ruralroutes.data.builder.MarketRuleBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 市场规则数据生成器
 *
 * 生成默认规则文件到 data/<namespace>/ruralroutes/market_event_rules/
 */
public class MarketRuleDataProvider extends JsonCodecProvider<MarketEventRule> {

    public MarketRuleDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  ExistingFileHelper existingFileHelper) {
        super(output, Target.DATA_PACK, "ruralroutes/market_event_rules", PackType.SERVER_DATA,
                MarketEventRule.CODEC, lookupProvider, "ruralroutes", existingFileHelper);
    }

    @Override
    protected void gather() {
        addCommonRules();
        addBiomeRules();
        addThemeRules();
        addRareRules();
    }

    private void addCommonRules() {
        globalRule("common/food_shortage", pool("food"), 0.15f, -0.35f, 0.10f, 100, RumorFamily.SHORTAGE, rumorTarget("food"));
        globalRule("common/crop_surplus", pool("crop"), -0.12f, 0.25f, -0.10f, 90, RumorFamily.SURPLUS, rumorTarget("crop"));
        globalRule("common/wood_demand", pool("wood"), 0.12f, -0.10f, 0.25f, 85, RumorFamily.DEMAND, rumorTarget("wood"));
        globalRule("common/stone_glut", pool("stone"), -0.10f, 0.15f, -0.15f, 80, RumorFamily.SURPLUS, rumorTarget("stone"));
        globalRule("common/mineral_rush", pool("mineral"), 0.15f, -0.15f, 0.30f, 100, RumorFamily.DEMAND, rumorTarget("mineral"));
        globalRule("common/leather_slump", pool("leather_fiber"), -0.10f, 0.18f, -0.15f, 75, RumorFamily.SURPLUS, rumorTarget("leather_fiber"));
        globalRule("common/dye_hot", pool("dye_decor"), 0.14f, -0.12f, 0.22f, 80, RumorFamily.DEMAND, rumorTarget("decor"));
    }

    private void addBiomeRules() {
        biomeRule("biome/plains/harvest_year", pool("crop"), -0.15f, 0.32f, -0.10f, 65, RumorFamily.SURPLUS, rumorTarget("crop"), "plains");
        biomeRule("biome/plains/tool_demand", pool("mineral"), 0.14f, -0.10f, 0.22f, 55, RumorFamily.DEMAND, rumorTarget("mineral"), "plains");

        biomeRule("biome/desert/caravan_delay", pool("food"), 0.16f, -0.30f, 0.08f, 60, RumorFamily.SHORTAGE, rumorTarget("food"), "desert");
        biomeRule("biome/desert/stone_boom", pool("stone"), 0.14f, -0.08f, 0.24f, 55, RumorFamily.DEMAND, rumorTarget("stone"), "desert");

        biomeRule("biome/savanna/kiln_season", pool("wood"), 0.15f, -0.08f, 0.26f, 60, RumorFamily.DEMAND, rumorTarget("wood"), "savanna");
        biomeRule("biome/savanna/ceramic_shipment", pool("dye_decor"), -0.12f, 0.28f, -0.05f, 50, RumorFamily.RELEASE, rumorTarget("terracotta"), "savanna");

        biomeRule("biome/taiga/logging_season", pool("wood"), -0.15f, 0.30f, -0.08f, 60, RumorFamily.RELEASE, rumorTarget("wood"), "taiga");
        biomeRule("biome/taiga/fur_demand", pool("leather_fiber"), 0.14f, -0.10f, 0.24f, 50, RumorFamily.DEMAND, rumorTarget("leather_fiber"), "taiga");

        biomeRule("biome/snowy/cold_supply", pool("food"), 0.18f, -0.35f, 0.05f, 60, RumorFamily.SHORTAGE, rumorTarget("food"), "snowy_plains");
        biomeRule("biome/snowy/thaw", pool("ice_snow"), -0.15f, 0.30f, -0.05f, 50, RumorFamily.RELEASE, rumorTarget("ice_snow"), "snowy_plains");
    }

    private void addThemeRules() {
        themeRule("theme/plains_granary/bumper_crop", pool("crop"), -0.22f, 0.40f, -0.12f, 40, RumorFamily.SURPLUS, rumorTarget("crop"), "plains_granary");
        themeRule("theme/plains_pasture/full_sheds", pool("leather_fiber"), -0.20f, 0.30f, -0.15f, 35, RumorFamily.SURPLUS, rumorTarget("leather_fiber"), "plains_pasture");
        themeRule("theme/plains_workshop/ore_shortage", pool("mineral"), 0.22f, -0.35f, 0.12f, 40, RumorFamily.SHORTAGE, rumorTarget("mineral"), "plains_workshop");

        themeRule("theme/desert_quarry/masonry_rush", pool("stone"), 0.20f, -0.10f, 0.30f, 35, RumorFamily.DEMAND, rumorTarget("stone"), "desert_quarry");
        themeRule("theme/desert_oasis/green_harvest", pool("crop"), -0.22f, 0.42f, -0.10f, 40, RumorFamily.SURPLUS, rumorTarget("crop"), "desert_oasis");
        themeRule("theme/desert_dyeworks/dye_boom", pool("dye_decor"), 0.22f, -0.12f, 0.32f, 35, RumorFamily.DEMAND, rumorTarget("dye"), "desert_dyeworks");

        themeRule("theme/savanna_woodworks/timber_release", pool("wood"), -0.20f, 0.35f, -0.10f, 35, RumorFamily.RELEASE, rumorTarget("wood"), "savanna_woodworks");
        themeRule("theme/savanna_terracotta/new_firing", pool("dye_decor"), -0.20f, 0.35f, -0.05f, 35, RumorFamily.RELEASE, rumorTarget("terracotta"), "savanna_terracotta");
        themeRule("theme/savanna_herder/hide_shortage", pool("leather_fiber"), 0.22f, -0.32f, 0.10f, 35, RumorFamily.SHORTAGE, rumorTarget("leather_fiber"), "savanna_herder");

        themeRule("theme/taiga_lumber/fresh_cut", pool("wood"), -0.22f, 0.38f, -0.08f, 40, RumorFamily.RELEASE, rumorTarget("wood"), "taiga_lumber");
        themeRule("theme/taiga_berries/berry_season", pool("food"), -0.20f, 0.28f, -0.05f, 35, RumorFamily.RELEASE, rumorTarget("food"), "taiga_berries");
        themeRule("theme/taiga_fur/winter_fur", pool("leather_fiber"), 0.22f, -0.10f, 0.35f, 35, RumorFamily.DEMAND, rumorTarget("leather_fiber"), "taiga_fur");

        themeRule("theme/snowy_iceworks/cold_storage", pool("ice_snow"), -0.22f, 0.40f, -0.05f, 40, RumorFamily.RELEASE, rumorTarget("ice_snow"), "snowy_iceworks");
        themeRule("theme/snowy_waystation/rations_low", pool("food"), 0.22f, -0.38f, 0.08f, 35, RumorFamily.SHORTAGE, rumorTarget("food"), "snowy_waystation");
        themeRule("theme/snowy_hunter/good_hunt", pool("leather_fiber"), -0.20f, 0.28f, -0.08f, 35, RumorFamily.RELEASE, rumorTarget("leather_fiber"), "snowy_hunter");
    }

    private void addRareRules() {
        globalRule("rare/nether_goods_surge", pool("nether_goods"), 0.28f, -0.20f, 0.35f, 10, RumorFamily.DEMAND, rumorTarget("nether_goods"));
        globalRule("rare/ocean_goods_shortage", pool("ocean_goods"), 0.25f, -0.40f, 0.05f, 10, RumorFamily.SHORTAGE, rumorTarget("ocean_goods"));
        globalRule("rare/end_goods_surge", pool("end_goods"), 0.35f, -0.22f, 0.40f, 8, RumorFamily.DEMAND, rumorTarget("end_goods"));
        globalRule("rare/precious_surge", pool("precious"), 0.40f, -0.25f, 0.32f, 5, RumorFamily.DEMAND, rumorTarget("precious"));

        themeRule("rare/snowy_iceworks/blue_ice_surge", "minecraft:blue_ice", 0.30f, -0.18f, 0.40f, 8, RumorFamily.DEMAND, null, "snowy_iceworks");
        themeRule("rare/taiga_berries/glow_berries_surge", "minecraft:glow_berries", 0.22f, -0.10f, 0.30f, 10, RumorFamily.DEMAND, null, "taiga_berries");
    }

    private void globalRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta, int weight,
                            RumorFamily rumorFamily, String rumorTargetKey) {
        rule(id)
                .nameKey(toNameKey(id))
                .targetRef(targetRef)
                .scope(MarketEventScopeRule.global())
                .delta(delta)
                .stock(sellStockDelta, buyStockDelta)
                .weight(weight)
                .rumorFamily(rumorFamily)
                .rumorTargetKey(rumorTargetKey)
                .register();
    }

    private void biomeRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta, int weight,
                           RumorFamily rumorFamily, String rumorTargetKey, String biomePath) {
        scopedRule(id, targetRef, delta, sellStockDelta, buyStockDelta, weight, rumorFamily, rumorTargetKey,
                MarketEventScopeRule.list(MarketScopeType.BIOME, List.of(vanilla(biomePath))));
    }

    private void themeRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta, int weight,
                           RumorFamily rumorFamily, String rumorTargetKey, String themePath) {
        scopedRule(id, targetRef, delta, sellStockDelta, buyStockDelta, weight, rumorFamily, rumorTargetKey,
                MarketEventScopeRule.list(MarketScopeType.THEME, List.of(mod(themePath))));
    }

    private void scopedRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta, int weight,
                            RumorFamily rumorFamily, String rumorTargetKey, MarketEventScopeRule scope) {
        rule(id)
                .nameKey(toNameKey(id))
                .targetRef(targetRef)
                .scope(scope)
                .delta(delta)
                .stock(sellStockDelta, buyStockDelta)
                .weight(weight)
                .rumorFamily(rumorFamily)
                .rumorTargetKey(rumorTargetKey)
                .register();
    }

    private static String toNameKey(String id) {
        return "market.ruralroutes." + id.replace('/', '.');
    }

    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    private static ResourceLocation mod(String path) {
        return ResourceLocation.fromNamespaceAndPath("ruralroutes", path);
    }

    private static String pool(String poolName) {
        return "#ruralroutes:pool/" + poolName;
    }

    private static String rumorTarget(String targetId) {
        return "rumor.target." + targetId;
    }

    /**
     * 创建规则构建器
     */
    private MarketRuleBuilder rule(String id) {
        return MarketRuleBuilder.create(id, this::unconditional);
    }
}
