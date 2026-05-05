package github.mczme.ruralroutes.core.theme;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主题价格修正解析器
 * 缓存主题下物品的价格修正，避免每个槽位反复遍历 price_modifiers。
 * 缓存在主题 reload 和标签 reload 时失效。
 */
public final class ThemePriceModifierResolver {

    private ThemePriceModifierResolver() {}

    /** 默认修正值 */
    private static final ThemeTemplate.PriceModifier DEFAULT_MODIFIER =
        new ThemeTemplate.PriceModifier(1.0f, 1.0f);

    /** 缓存 key: themeId.toString() + "|" + itemId.toString() */
    private static final Map<String, ThemeTemplate.PriceModifier> CACHE =
        new ConcurrentHashMap<>();

    /**
     * 解析物品在主题下的价格修正
     *
     * 解析规则：
     * 1. 精确物品修正优先（如 "minecraft:bread"）
     * 2. 标签修正其次（如 "#ruralroutes:pool/crop"）
     * 3. 多标签命中时使用主题文件中第一个匹配项
     * 4. 无匹配时返回默认修正（sell=1.0, buy=1.0）
     *
     * @param template 主题模板
     * @param stack 物品栈
     * @return 价格修正（永不为 null）
     */
    public static ThemeTemplate.PriceModifier resolve(
            ThemeTemplate template,
            ItemStack stack) {

        if (stack.isEmpty()) {
            return DEFAULT_MODIFIER;
        }

        ResourceLocation themeId = template.name();
        ResourceLocation itemId = stack.getItemHolder()
            .unwrapKey()
            .map(key -> key.location())
            .orElse(null);

        if (itemId == null) {
            return DEFAULT_MODIFIER;
        }

        // 构建缓存 key
        String cacheKey = themeId.toString() + "|" + itemId.toString();

        // 尝试从缓存获取
        return CACHE.computeIfAbsent(cacheKey, k -> doResolve(template, stack, itemId));
    }

    /**
     * 实际解析逻辑（无缓存）
     */
    private static ThemeTemplate.PriceModifier doResolve(
            ThemeTemplate template,
            ItemStack stack,
            ResourceLocation itemId) {

        Optional<Map<String, ThemeTemplate.PriceModifier>> modifiersOpt = template.priceModifiers();
        if (modifiersOpt.isEmpty()) {
            return DEFAULT_MODIFIER;
        }

        Map<String, ThemeTemplate.PriceModifier> modifiers = modifiersOpt.get();
        String itemIdStr = itemId.toString();

        // 1. 尝试精确匹配
        ThemeTemplate.PriceModifier exactMatch = modifiers.get(itemIdStr);
        if (exactMatch != null) {
            RuralRoutes.LOGGER.debug("Exact price modifier match: {} -> {}", itemIdStr, exactMatch);
            return exactMatch;
        }

        // 2. 遍历标签匹配（使用第一个匹配项）
        // price_modifiers 使用 LinkedHashMap 保证 JSON 顺序
        for (Map.Entry<String, ThemeTemplate.PriceModifier> entry : modifiers.entrySet()) {
            String ref = entry.getKey();
            if (ref.startsWith("#") && TagLookupCache.matchesItem(stack, ref)) {
                RuralRoutes.LOGGER.debug("Tag price modifier match: {} -> {}", ref, entry.getValue());
                return entry.getValue();
            }
        }

        // 3. 无匹配，返回默认
        return DEFAULT_MODIFIER;
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