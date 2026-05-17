package github.mczme.ruralroutes.data.builder;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.core.market.MarketEventScopeRule;
import github.mczme.ruralroutes.core.market.MarketStockModifier;
import github.mczme.ruralroutes.core.rumor.RumorFamily;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * 市场规则构建器
 *
 * 用于流畅地构建市场事件规则。
 */
public class MarketRuleBuilder {
    private final String id;
    private final BiConsumer<ResourceLocation, MarketEventRule> registrar;
    private String nameKey;
    private String targetRef;
    private final List<MarketEventScopeRule> scopes = new ArrayList<>();
    private float delta;
    private MarketStockModifier stock;
    private Integer weight;
    private RumorFamily rumorFamily;
    private String rumorTargetKey;

    private MarketRuleBuilder(String id, BiConsumer<ResourceLocation, MarketEventRule> registrar) {
        this.id = id;
        this.registrar = registrar;
    }

    /**
     * 创建规则构建器
     * @param id 规则 ID
     * @param registrar 注册回调函数
     * @return 构建器实例
     */
    public static MarketRuleBuilder create(String id, BiConsumer<ResourceLocation, MarketEventRule> registrar) {
        return new MarketRuleBuilder(id, registrar);
    }

    /**
     * 设置本地化 key
     */
    public MarketRuleBuilder nameKey(String key) {
        this.nameKey = key;
        return this;
    }

    /**
     * 设置目标引用
     */
    public MarketRuleBuilder targetRef(String ref) {
        this.targetRef = ref;
        return this;
    }

    /**
     * 添加作用域规则
     */
    public MarketRuleBuilder scope(MarketEventScopeRule scope) {
        this.scopes.add(scope);
        return this;
    }

    /**
     * 设置涨跌幅
     */
    public MarketRuleBuilder delta(float delta) {
        this.delta = delta;
        return this;
    }

    /**
     * 设置库存修正
     */
    public MarketRuleBuilder stock(float sellDelta, float buyDelta) {
        this.stock = new MarketStockModifier(sellDelta, buyDelta);
        return this;
    }

    /**
     * 设置权重
     */
    public MarketRuleBuilder weight(int weight) {
        this.weight = weight;
        return this;
    }

    /**
     * 设置传闻语义族
     */
    public MarketRuleBuilder rumorFamily(RumorFamily family) {
        this.rumorFamily = Objects.requireNonNull(family, "family");
        return this;
    }

    /**
     * 设置传闻目标翻译 key
     */
    public MarketRuleBuilder rumorTargetKey(String key) {
        this.rumorTargetKey = key;
        return this;
    }

    /**
     * 构建并注册规则
     */
    public void register() {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, id);
        MarketEventRule rule = new MarketEventRule(
                location,
                nameKey,
                targetRef,
                List.copyOf(scopes),
                delta,
                Optional.ofNullable(stock),
                Optional.ofNullable(weight),
                Objects.requireNonNull(rumorFamily, "rumorFamily"),
                Optional.ofNullable(rumorTargetKey)
        );
        registrar.accept(location, rule);
    }
}
