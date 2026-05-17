package github.mczme.ruralroutes.core.market;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.rumor.RumorFamily;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 市场事件
 *
 * 表示一个影响价格的市场事件实例。
 * 每个事件由规则生成，包含实际的目标引用、作用域和涨跌幅。
 */
public record MarketEvent(
        ResourceLocation ruleId,
        String targetRef,
        MarketScopeType scopeType,
        Optional<ResourceLocation> scopeTarget,
        float delta,
        Optional<MarketStockModifier> stock,
        RumorFamily rumorFamily,
        Optional<String> rumorTargetKey
) {
    public static final Codec<MarketEvent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("rule_id").forGetter(MarketEvent::ruleId),
                    Codec.STRING.fieldOf("target_ref").forGetter(MarketEvent::targetRef),
                    MarketScopeType.CODEC.fieldOf("scope_type").forGetter(MarketEvent::scopeType),
                    ResourceLocation.CODEC.optionalFieldOf("scope_target").forGetter(MarketEvent::scopeTarget),
                    Codec.FLOAT.fieldOf("delta").forGetter(MarketEvent::delta),
                    MarketStockModifier.CODEC.optionalFieldOf("stock").forGetter(MarketEvent::stock),
                    RumorFamily.CODEC.fieldOf("rumor_family").forGetter(MarketEvent::rumorFamily),
                    Codec.STRING.optionalFieldOf("rumor_target_key").forGetter(MarketEvent::rumorTargetKey)
            ).apply(instance, MarketEvent::new)
    );

    /**
     * 判断目标引用是否表示标签
     * @return 如果 targetRef 以 # 开头则返回 true
     */
    public boolean isTargetTag() {
        return targetRef.startsWith("#");
    }

    /**
     * 获取实际的目标 ID（去除 # 前缀）
     * @return 标签或物品 ID
     */
    public String getTargetId() {
        return isTargetTag() ? targetRef.substring(1) : targetRef;
    }

}
