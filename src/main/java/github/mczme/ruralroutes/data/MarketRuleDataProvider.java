package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.core.market.MarketEventScopeRule;
import github.mczme.ruralroutes.core.market.MarketScopeType;
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
        globalRule("common/food_shortage", "#ruralroutes:pool/food", 0.15f, 100);
        globalRule("common/crop_surplus", "#ruralroutes:pool/crop", -0.12f, 90);
        globalRule("common/wood_demand", "#ruralroutes:pool/wood", 0.12f, 85);
        globalRule("common/stone_glut", "#ruralroutes:pool/stone", -0.10f, 80);
        globalRule("common/mineral_rush", "#ruralroutes:pool/mineral", 0.15f, 100);
        globalRule("common/leather_slump", "#ruralroutes:pool/leather_fiber", -0.10f, 75);
        globalRule("common/dye_hot", "#ruralroutes:pool/dye_decor", 0.14f, 80);
    }

    private void addBiomeRules() {
        biomeRule("biome/plains/harvest_year", "#ruralroutes:pool/crop", -0.15f, 65, "plains");
        biomeRule("biome/plains/tool_demand", "#ruralroutes:pool/mineral", 0.14f, 55, "plains");

        biomeRule("biome/desert/caravan_delay", "#ruralroutes:pool/food", 0.16f, 60, "desert");
        biomeRule("biome/desert/stone_boom", "#ruralroutes:pool/stone", 0.14f, 55, "desert");

        biomeRule("biome/savanna/kiln_season", "#ruralroutes:pool/wood", 0.15f, 60, "savanna");
        biomeRule("biome/savanna/ceramic_shipment", "#ruralroutes:pool/dye_decor", -0.12f, 50, "savanna");

        biomeRule("biome/taiga/logging_season", "#ruralroutes:pool/wood", -0.15f, 60, "taiga");
        biomeRule("biome/taiga/fur_demand", "#ruralroutes:pool/leather_fiber", 0.14f, 50, "taiga");

        biomeRule("biome/snowy/cold_supply", "#ruralroutes:pool/food", 0.18f, 60, "snowy_plains");
        biomeRule("biome/snowy/thaw", "#ruralroutes:pool/ice_snow", -0.15f, 50, "snowy_plains");
    }

    private void addThemeRules() {
        themeRule("theme/plains_granary/bumper_crop", "#ruralroutes:pool/crop", -0.22f, 40, "plains_granary");
        themeRule("theme/plains_pasture/full_sheds", "#ruralroutes:pool/leather_fiber", -0.20f, 35, "plains_pasture");
        themeRule("theme/plains_workshop/ore_shortage", "#ruralroutes:pool/mineral", 0.22f, 40, "plains_workshop");

        themeRule("theme/desert_quarry/masonry_rush", "#ruralroutes:pool/stone", 0.20f, 35, "desert_quarry");
        themeRule("theme/desert_oasis/green_harvest", "#ruralroutes:pool/crop", -0.22f, 40, "desert_oasis");
        themeRule("theme/desert_dyeworks/dye_boom", "#ruralroutes:pool/dye_decor", 0.22f, 35, "desert_dyeworks");

        themeRule("theme/savanna_woodworks/timber_release", "#ruralroutes:pool/wood", -0.20f, 35, "savanna_woodworks");
        themeRule("theme/savanna_terracotta/new_firing", "#ruralroutes:pool/dye_decor", -0.20f, 35, "savanna_terracotta");
        themeRule("theme/savanna_herder/hide_shortage", "#ruralroutes:pool/leather_fiber", 0.22f, 35, "savanna_herder");

        themeRule("theme/taiga_lumber/fresh_cut", "#ruralroutes:pool/wood", -0.22f, 40, "taiga_lumber");
        themeRule("theme/taiga_berries/berry_season", "#ruralroutes:pool/food", -0.20f, 35, "taiga_berries");
        themeRule("theme/taiga_fur/winter_fur", "#ruralroutes:pool/leather_fiber", 0.22f, 35, "taiga_fur");

        themeRule("theme/snowy_iceworks/cold_storage", "#ruralroutes:pool/ice_snow", -0.22f, 40, "snowy_iceworks");
        themeRule("theme/snowy_waystation/rations_low", "#ruralroutes:pool/food", 0.22f, 35, "snowy_waystation");
        themeRule("theme/snowy_hunter/good_hunt", "#ruralroutes:pool/leather_fiber", -0.20f, 35, "snowy_hunter");
    }

    private void addRareRules() {
        globalRule("rare/nether_goods_surge", "#ruralroutes:pool/nether_goods", 0.28f, 10);
        globalRule("rare/ocean_goods_shortage", "#ruralroutes:pool/ocean_goods", 0.25f, 10);
        globalRule("rare/end_goods_surge", "#ruralroutes:pool/end_goods", 0.35f, 8);
        globalRule("rare/precious_surge", "#ruralroutes:pool/precious", 0.40f, 5);

        themeRule("rare/snowy_iceworks/blue_ice_surge", "minecraft:blue_ice", 0.30f, 8, "snowy_iceworks");
        themeRule("rare/taiga_berries/glow_berries_surge", "minecraft:glow_berries", 0.22f, 10, "taiga_berries");
    }

    private void globalRule(String id, String targetRef, float delta, int weight) {
        rule(id)
                .nameKey(toNameKey(id))
                .targetRef(targetRef)
                .scope(MarketEventScopeRule.global())
                .delta(delta)
                .weight(weight)
                .register();
    }

    private void biomeRule(String id, String targetRef, float delta, int weight, String biomePath) {
        scopedRule(id, targetRef, delta, weight,
                MarketEventScopeRule.list(MarketScopeType.BIOME, List.of(vanilla(biomePath))));
    }

    private void themeRule(String id, String targetRef, float delta, int weight, String themePath) {
        scopedRule(id, targetRef, delta, weight,
                MarketEventScopeRule.list(MarketScopeType.THEME, List.of(mod(themePath))));
    }

    private void scopedRule(String id, String targetRef, float delta, int weight, MarketEventScopeRule scope) {
        rule(id)
                .nameKey(toNameKey(id))
                .targetRef(targetRef)
                .scope(scope)
                .delta(delta)
                .weight(weight)
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

    /**
     * 创建规则构建器
     */
    private MarketRuleBuilder rule(String id) {
        return MarketRuleBuilder.create(id, this::unconditional);
    }
}
