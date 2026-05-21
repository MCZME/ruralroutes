package github.mczme.ruralroutes.core.market;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import github.mczme.ruralroutes.core.rumor.RumorFamily;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * 市场事件规则
 *
 * 定义如何生成市场事件的规则模板。
 * 规则由数据包 JSON 文件定义，在贸易周期刷新时被抽取并展开为 MarketEvent。
 */
public record MarketEventRule(
        ResourceLocation id,
        String nameKey,
        TradeTargetRef targetRef,
        List<MarketEventScopeRule> scopes,
        float delta,
        Optional<MarketStockModifier> stock,
        Optional<Integer> weight,
        RumorFamily rumorFamily,
        Optional<String> rumorTargetKey
) {
    public static final Codec<MarketEventRule> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(MarketEventRule::id),
                    Codec.STRING.fieldOf("name_key").forGetter(MarketEventRule::nameKey),
                    TradeTargetRef.CODEC.fieldOf("target_ref").forGetter(MarketEventRule::targetRef),
                    MarketEventScopeRule.CODEC.listOf().fieldOf("scopes").forGetter(MarketEventRule::scopes),
                    Codec.FLOAT.fieldOf("delta").forGetter(MarketEventRule::delta),
                    MarketStockModifier.CODEC.optionalFieldOf("stock").forGetter(MarketEventRule::stock),
                    Codec.INT.optionalFieldOf("weight").forGetter(MarketEventRule::weight),
                    RumorFamily.CODEC.fieldOf("rumor_family").forGetter(MarketEventRule::rumorFamily),
                    Codec.STRING.optionalFieldOf("rumor_target_key").forGetter(MarketEventRule::rumorTargetKey)
            ).apply(instance, MarketEventRule::new)
    );

    /**
     * 获取有效权重（默认 100）
     */
    public int effectiveWeight() {
        return weight.orElse(100);
    }

    /**
     * 判断目标引用是否表示标签
     */
    public boolean isTargetTag() {
        return targetRef.isTag();
    }

    /**
     * 获取实际的目标 ID（去除 # 前缀）
     */
    public String getTargetId() {
        return targetRef.asString();
    }

    /**
     * 创建简单规则（单一全局作用域）
     */
    public static MarketEventRule simple(ResourceLocation id, String nameKey, TradeTargetRef targetRef,
                                         float delta, RumorFamily rumorFamily) {
        return new MarketEventRule(
                id, nameKey, targetRef,
                List.of(MarketEventScopeRule.global()),
                delta,
                Optional.empty(),
                Optional.empty(),
                rumorFamily,
                Optional.empty()
        );
    }

    /**
     * 创建带权重的规则
     */
    public static MarketEventRule weighted(ResourceLocation id, String nameKey, TradeTargetRef targetRef,
                                           float delta, int weight, List<MarketEventScopeRule> scopes,
                                           RumorFamily rumorFamily) {
        return new MarketEventRule(
                id, nameKey, targetRef,
                scopes,
                delta,
                Optional.empty(),
                Optional.of(weight),
                rumorFamily,
                Optional.empty()
        );
    }
}
