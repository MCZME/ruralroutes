package github.mczme.ruralroutes.data.content;

import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.core.market.MarketEventScopeRule;
import github.mczme.ruralroutes.core.market.MarketScopeType;
import github.mczme.ruralroutes.core.rumor.RumorFamily;
import github.mczme.ruralroutes.data.builder.MarketRuleBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 市场规则内容层封装，让 Provider 只负责收集注册结果。
 */
public final class MarketRuleRegistrar {
    private final BiConsumer<ResourceLocation, MarketEventRule> registrar;

    public MarketRuleRegistrar(BiConsumer<ResourceLocation, MarketEventRule> registrar) {
        this.registrar = registrar;
    }

    public void globalRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta,
                           int weight, RumorFamily rumorFamily, String rumorTargetKey) {
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

    public void biomeRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta,
                          int weight, RumorFamily rumorFamily, String rumorTargetKey, String biomePath) {
        scopedRule(id, targetRef, delta, sellStockDelta, buyStockDelta, weight, rumorFamily, rumorTargetKey,
            MarketEventScopeRule.list(MarketScopeType.BIOME, List.of(vanilla(biomePath))));
    }

    public void themeRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta,
                          int weight, RumorFamily rumorFamily, String rumorTargetKey, String themePath) {
        scopedRule(id, targetRef, delta, sellStockDelta, buyStockDelta, weight, rumorFamily, rumorTargetKey,
            MarketEventScopeRule.list(MarketScopeType.THEME, List.of(mod(themePath))));
    }

    private void scopedRule(String id, String targetRef, float delta, float sellStockDelta, float buyStockDelta,
                            int weight, RumorFamily rumorFamily, String rumorTargetKey,
                            MarketEventScopeRule scope) {
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

    public MarketRuleBuilder rule(String id) {
        return MarketRuleBuilder.create(id, registrar);
    }

    public static String pool(String poolName) {
        return "#ruralroutes:pool/" + poolName;
    }

    public static String rumorTarget(String targetId) {
        return "rumor.target." + targetId;
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
}
