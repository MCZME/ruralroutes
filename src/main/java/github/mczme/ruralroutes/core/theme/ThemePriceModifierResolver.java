package github.mczme.ruralroutes.core.theme;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主题价格修正解析器
 * 缓存主题下物品的价格修正，避免每个槽位反复遍历 price_modifiers。
 * 主题修正只应用首个命中的规则；市场事件的叠加在其他模块单独处理。
 * 缓存在主题 reload 和标签 reload 时失效。
 */
public final class ThemePriceModifierResolver {

    private ThemePriceModifierResolver() {}

    /** 默认修正值 */
    private static final ThemeTemplate.PriceModifier DEFAULT_MODIFIER =
        ThemeTemplate.PriceModifier.of(TradeTargetRef.item("minecraft:air"), 1.0f, 1.0f);

    /** 缓存 key: themeId.toString() + "|" + itemId.toString() */
    private static final Map<String, ThemeTemplate.PriceModifier> CACHE =
        new ConcurrentHashMap<>();

    /**
     * 解析物品在主题下的价格修正
     *
     * 解析规则：
     * 1. 精确物品修正优先（如 "minecraft:bread"）
     * 2. 标签修正其次（如 "#ruralroutes:pool/crop"）
     * 3. 多标签命中时使用主题文件中第一个匹配项，后续规则不再生效
     * 4. 无匹配时返回默认修正（sell=1.0, buy=1.0）
     *
     * @param template 主题模板
     * @param stack 物品栈
     * @return 价格修正（永不为 null）
     */
    public static ThemeTemplate.PriceModifier resolve(
            ThemeTemplate template,
            ItemStack stack) {
        return resolve(template, stack, Optional.empty());
    }

    public static ThemeTemplate.PriceModifier resolve(
            ThemeTemplate template,
            ItemStack stack,
            Optional<String> sourceKey) {
        if (stack.isEmpty()) {
            return DEFAULT_MODIFIER;
        }

        TradeItemKey itemKey = TradeItemKey.from(stack);
        // 构建缓存 key
        String cacheKey = template.name() + "|" + itemKey.canonicalKey() + "|" + sourceKey.orElse("");

        // 尝试从缓存获取
        return CACHE.computeIfAbsent(cacheKey, k -> doResolve(template, stack, itemKey, sourceKey));
    }

    public static ThemeTemplate.PriceModifier resolve(
            ThemeTemplate template,
            TradeItemKey itemKey,
            Optional<String> sourceKey) {
        if (itemKey == null) {
            return DEFAULT_MODIFIER;
        }

        String cacheKey = template.name() + "|" + itemKey.canonicalKey() + "|" + sourceKey.orElse("");
        return CACHE.computeIfAbsent(cacheKey, k -> doResolve(template, itemKey, sourceKey));
    }

    /**
     * 实际解析逻辑（无缓存）
     */
    private static ThemeTemplate.PriceModifier doResolve(
            ThemeTemplate template,
            ItemStack stack,
            TradeItemKey itemKey,
            Optional<String> sourceKey) {

        Optional<java.util.List<ThemeTemplate.PriceModifier>> modifiersOpt = template.priceModifiers();
        if (modifiersOpt.isEmpty()) {
            return DEFAULT_MODIFIER;
        }

        ThemeTemplate.PriceModifier best = null;
        int bestScore = -1;
        for (ThemeTemplate.PriceModifier modifier : modifiersOpt.get()) {
            int score = modifier.targetRef().matchSpecificity(stack, sourceKey, itemKey);
            if (score > bestScore) {
                bestScore = score;
                best = modifier;
            }
        }

        if (best != null) {
            RuralRoutes.LOGGER.debug("Price modifier match: {} -> {}", best.targetRef().asString(), best);
            return best;
        }
        return DEFAULT_MODIFIER;
    }

    private static ThemeTemplate.PriceModifier doResolve(
            ThemeTemplate template,
            TradeItemKey itemKey,
            Optional<String> sourceKey) {
        Optional<java.util.List<ThemeTemplate.PriceModifier>> modifiersOpt = template.priceModifiers();
        if (modifiersOpt.isEmpty()) {
            return DEFAULT_MODIFIER;
        }

        ThemeTemplate.PriceModifier best = null;
        int bestScore = -1;
        for (ThemeTemplate.PriceModifier modifier : modifiersOpt.get()) {
            int score = modifier.targetRef().matchSpecificity(null, sourceKey, itemKey);
            if (score > bestScore) {
                bestScore = score;
                best = modifier;
            }
        }

        return best != null ? best : DEFAULT_MODIFIER;
    }

    /**
     * 清空缓存
     * 应在主题 reload 和标签 reload 时调用
     */
    public static void invalidate() {
        int size = CACHE.size();
        CACHE.clear();
        if (size > 0) {
            RuralRoutes.LOGGER.debug("ThemePriceModifierResolver cache cleared, {} entries", size);
        }
    }
}
