package github.mczme.ruralroutes.core.market;

import github.mczme.ruralroutes.Config;
import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.theme.ResolvedTheme;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 市场状态生成器
 *
 * 静态工具类，负责在贸易周期刷新时生成新的市场状态。
 * 从规则目录中按权重抽取规则，展开为具体的市场事件。
 */
public final class MarketStateGenerator {

    private MarketStateGenerator() {}

    /**
     * 生成新的市场状态
     *
     * @param cycleIndex 当前周期索引
     * @param random 随机数生成器
     * @return 新的市场状态
     */
    public static MarketState generate(long cycleIndex, Random random) {
        // 检查市场系统是否启用
        if (!Config.MARKET_ENABLED.get()) {
            RuralRoutes.LOGGER.debug("Market system disabled, returning empty state for cycle {}", cycleIndex);
            return MarketState.empty(cycleIndex);
        }

        // 检查是否有可用规则
        if (!MarketEventRuleCatalog.INSTANCE.hasRules()) {
            RuralRoutes.LOGGER.warn("No market rules loaded, returning empty state for cycle {}", cycleIndex);
            return MarketState.empty(cycleIndex);
        }

        // 获取可用群系和主题
        List<ResourceLocation> availableBiomes = getAvailableBiomes();
        List<ResourceLocation> availableThemes = getAvailableThemes();

        // 随机选择规则
        int pickCount = Config.MARKET_RULE_PICK_COUNT.get();
        List<MarketEventRule> selectedRules = MarketEventRuleCatalog.INSTANCE
                .selectRandomRules(random, pickCount);

        if (selectedRules.isEmpty()) {
            return MarketState.empty(cycleIndex);
        }

        // 生成事件
        List<MarketEvent> events = new ArrayList<>();
        Set<String> eventKeys = new HashSet<>(); // 用于去重
        int maxAttempts = pickCount * 8;
        int attempts = 0;

        for (MarketEventRule rule : selectedRules) {
            if (attempts >= maxAttempts) break;
            attempts++;

            List<MarketEvent> generatedEvents = generateEventsFromRule(
                    rule, availableBiomes, availableThemes, eventKeys);
            events.addAll(generatedEvents);
        }

        RuralRoutes.LOGGER.info("Generated {} market events for cycle {}", events.size(), cycleIndex);

        return new MarketState(cycleIndex, List.copyOf(events));
    }

    /**
     * 使用周期索引作为种子生成确定性随机数
     * 同一周期索引总是生成相同的市场状态
     */
    public static MarketState generateDeterministic(long cycleIndex) {
        Random random = new Random(cycleIndex ^ 0x5A5A5A5AL);
        return generate(cycleIndex, random);
    }

    /**
     * 从规则生成事件
     */
    private static List<MarketEvent> generateEventsFromRule(
            MarketEventRule rule,
            List<ResourceLocation> availableBiomes,
            List<ResourceLocation> availableThemes,
            Set<String> eventKeys) {

        List<MarketEvent> events = new ArrayList<>();

        for (MarketEventScopeRule scopeRule : rule.scopes()) {
            List<ResourceLocation> scopeTargets = resolveScopeTargets(
                    scopeRule, availableBiomes, availableThemes);

            for (ResourceLocation scopeTarget : scopeTargets) {
                String eventKey = buildEventKey(rule.id(), rule.targetRef(),
                        scopeRule.type(), scopeTarget);

                // 去重检查
                if (eventKeys.contains(eventKey)) {
                    continue;
                }
                eventKeys.add(eventKey);

                MarketEvent event = new MarketEvent(
                        rule.id(),
                        rule.targetRef(),
                        scopeRule.type(),
                        scopeRule.type() == MarketScopeType.GLOBAL
                                ? java.util.Optional.empty()
                                : java.util.Optional.of(scopeTarget),
                        rule.delta(),
                        rule.stock(),
                        rule.rumorFamily(),
                        rule.rumorTargetKey()
                );
                events.add(event);
            }
        }

        return events;
    }

    /**
     * 解析作用域目标列表
     */
    private static List<ResourceLocation> resolveScopeTargets(
            MarketEventScopeRule scopeRule,
            List<ResourceLocation> availableBiomes,
            List<ResourceLocation> availableThemes) {

        return switch (scopeRule.type()) {
            case GLOBAL -> List.of(ResourceLocation.parse("minecraft:empty")); // 全局作用域使用占位符
            case BIOME -> switch (scopeRule.selector()) {
                case ALL -> availableBiomes;
                case LIST -> scopeRule.targets().orElse(List.of());
            };
            case THEME -> switch (scopeRule.selector()) {
                case ALL -> availableThemes;
                case LIST -> scopeRule.targets().orElse(List.of());
            };
        };
    }

    /**
     * 构建事件唯一键
     */
    private static String buildEventKey(ResourceLocation ruleId, String targetRef,
                                         MarketScopeType scopeType, ResourceLocation scopeTarget) {
        return ruleId + "|" + targetRef + "|" + scopeType + "|" +
                (scopeTarget == null ? "" : scopeTarget);
    }

    /**
     * 获取所有可用群系（从已加载主题中提取）
     */
    private static List<ResourceLocation> getAvailableBiomes() {
        Map<ResourceLocation, ResolvedTheme> themes = ThemeManager.INSTANCE.getAllThemes();
        return themes.values().stream()
                .map(ResolvedTheme::biome)
                .distinct()
                .toList();
    }

    /**
     * 获取所有可用主题
     */
    private static List<ResourceLocation> getAvailableThemes() {
        return new ArrayList<>(ThemeManager.INSTANCE.getAllThemes().keySet());
    }
}
