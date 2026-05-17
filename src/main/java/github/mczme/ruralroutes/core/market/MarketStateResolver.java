package github.mczme.ruralroutes.core.market;

import github.mczme.ruralroutes.Config;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 市场状态解析器
 *
 * 静态工具类，用于解析市场事件并计算价格调整。
 * 封装目标命中、范围命中、叠加和上限保护规则。
 */
public final class MarketStateResolver {

    private MarketStateResolver() {}

    /**
     * 解析物品的价格调整
     *
     * @param marketState 当前市场状态
     * @param context 市场上下文
     * @param stack 要查询的物品栈
     * @return 价格调整结果
     */
    public static MarketPriceAdjustment resolvePriceAdjustment(
            MarketState marketState,
            MarketContext context,
            ItemStack stack) {

        if (marketState == null || marketState.isEmpty()) {
            return MarketPriceAdjustment.NONE;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<MarketEvent> matchedEvents = collectMatchedEvents(marketState, context, itemId);

        float maxDelta = Config.MARKET_MAX_DELTA.get().floatValue();
        return MarketPriceAdjustment.of(matchedEvents, maxDelta);
    }

    /**
     * 解析物品的库存调整
     *
     * @param marketState 当前市场状态
     * @param context 市场上下文
     * @param itemId 物品注册表 ID
     * @return 库存调整结果
     */
    public static MarketStockAdjustment resolveStockAdjustment(
            MarketState marketState,
            MarketContext context,
            ResourceLocation itemId) {

        if (marketState == null || marketState.isEmpty() || itemId == null) {
            return MarketStockAdjustment.NONE;
        }

        List<MarketEvent> matchedEvents = collectMatchedEvents(marketState, context, itemId);
        float maxDelta = Config.MARKET_MAX_STOCK_DELTA.get().floatValue();
        return MarketStockAdjustment.of(matchedEvents, maxDelta);
    }

    /**
     * 检查事件是否匹配物品 ID
     */
    private static boolean matchesItem(MarketEvent event, ResourceLocation itemId) {
        return TagLookupCache.matchesItem(itemId, event.targetRef());
    }

    /**
     * 检查事件是否匹配作用域
     *
     * @param event 市场事件
     * @param context 市场上下文
     * @return 如果事件作用域匹配上下文则返回 true
     */
    private static boolean matchesScope(MarketEvent event, MarketContext context) {
        return switch (event.scopeType()) {
            case GLOBAL -> true;
            case BIOME -> event.scopeTarget()
                    .map(target -> context.matchesBiome(target))
                    .orElse(false);
            case THEME -> event.scopeTarget()
                    .map(target -> context.matchesTheme(target))
                    .orElse(false);
        };
    }

    /**
     * 获取影响指定物品的所有事件
     *
     * @param marketState 市场状态
     * @param stack 物品栈
     * @return 影响该物品的事件列表
     */
    public static List<MarketEvent> getEventsForItem(MarketState marketState, ItemStack stack) {
        if (marketState == null || marketState.isEmpty()) {
            return List.of();
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return collectItemEvents(marketState, itemId);
    }

    /**
     * 获取指定作用域类型下的所有事件
     *
     * @param marketState 市场状态
     * @param scopeType 作用域类型
     * @return 该作用域类型的事件列表
     */
    public static List<MarketEvent> getEventsByScopeType(MarketState marketState, MarketScopeType scopeType) {
        if (marketState == null) {
            return List.of();
        }
        return marketState.getEventsByScopeType(scopeType);
    }

    private static List<MarketEvent> collectMatchedEvents(
            MarketState marketState,
            MarketContext context,
            ResourceLocation itemId) {

        List<MarketEvent> matchedEvents = new ArrayList<>();
        for (MarketEvent event : marketState.events()) {
            if (matchesItem(event, itemId) && matchesScope(event, context)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    private static List<MarketEvent> collectItemEvents(MarketState marketState, ResourceLocation itemId) {
        List<MarketEvent> result = new ArrayList<>();
        for (MarketEvent event : marketState.events()) {
            if (matchesItem(event, itemId)) {
                result.add(event);
            }
        }
        return result;
    }
}
