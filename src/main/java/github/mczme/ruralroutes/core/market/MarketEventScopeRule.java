package github.mczme.ruralroutes.core.market;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 市场事件作用域规则
 *
 * 定义市场事件规则如何展开为具体的 MarketEvent。
 * 支持两种选择器：ALL（展开到所有可用目标）和 LIST（仅展开到指定目标列表）。
 */
public record MarketEventScopeRule(
        MarketScopeType type,
        ScopeSelector selector,
        Optional<List<ResourceLocation>> targets
) {
    /**
     * 选择器类型
     */
    public enum ScopeSelector {
        ALL,    // 展开到该类型下的所有可用目标
        LIST;   // 仅展开到 targets 列表中指定的目标

        public static final Codec<ScopeSelector> CODEC = Codec.STRING.flatXmap(
                name -> {
                    try {
                        return DataResult.success(valueOf(name.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException e) {
                        return DataResult.error(() -> "Invalid selector: " + name);
                    }
                },
                selector -> DataResult.success(selector.name().toLowerCase(Locale.ROOT))
        );
    }

    public static final Codec<MarketEventScopeRule> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MarketScopeType.CODEC.fieldOf("type").forGetter(MarketEventScopeRule::type),
                    ScopeSelector.CODEC.fieldOf("selector").forGetter(MarketEventScopeRule::selector),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("targets").forGetter(MarketEventScopeRule::targets)
            ).apply(instance, MarketEventScopeRule::new)
    );

    /**
     * 创建全局作用域规则
     */
    public static MarketEventScopeRule global() {
        return new MarketEventScopeRule(MarketScopeType.GLOBAL, ScopeSelector.ALL, Optional.empty());
    }

    /**
     * 创建展开到所有目标的作用域规则
     */
    public static MarketEventScopeRule all(MarketScopeType type) {
        return new MarketEventScopeRule(type, ScopeSelector.ALL, Optional.empty());
    }

    /**
     * 创建展开到指定目标列表的作用域规则
     */
    public static MarketEventScopeRule list(MarketScopeType type, List<ResourceLocation> targets) {
        return new MarketEventScopeRule(type, ScopeSelector.LIST, Optional.of(targets));
    }

    /**
     * 检查是否有有效的目标列表（仅当 selector 为 LIST 时需要）
     */
    public boolean hasValidTargets() {
        if (selector == ScopeSelector.LIST) {
            return targets.map(list -> !list.isEmpty()).orElse(false);
        }
        return true;
    }
}