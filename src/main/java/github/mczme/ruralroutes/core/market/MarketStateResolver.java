package github.mczme.ruralroutes.core.market;

import github.mczme.ruralroutes.Config;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return resolvePriceAdjustment(marketState, context, stack, Optional.empty());
    }

    public static MarketPriceAdjustment resolvePriceAdjustment(
            MarketState marketState,
            MarketContext context,
            ItemStack stack,
            Optional<String> sourceKey) {

        if (marketState == null || marketState.isEmpty() || stack == null || stack.isEmpty()) {
            return MarketPriceAdjustment.NONE;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        TradeItemKey itemKey = TradeItemKey.from(stack);
        List<MarketEvent> matchedEvents = collectMatchedEvents(marketState, context, itemId, itemKey, sourceKey.orElse(null));

        float maxDelta = Config.MARKET_MAX_DELTA.get().floatValue();
        return MarketPriceAdjustment.of(matchedEvents, maxDelta);
    }

    public static MarketPriceAdjustment resolvePriceAdjustment(
            MarketState marketState,
            MarketContext context,
            TradeItemKey itemKey,
            Optional<String> sourceKey) {

        if (marketState == null || marketState.isEmpty() || itemKey == null) {
            return MarketPriceAdjustment.NONE;
        }

        List<MarketEvent> matchedEvents = collectMatchedEvents(
            marketState,
            context,
            itemKey.itemId(),
            itemKey,
            sourceKey.orElse(null)
        );

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
        return resolveStockAdjustment(marketState, context, itemId, Optional.empty());
    }

    public static MarketStockAdjustment resolveStockAdjustment(
            MarketState marketState,
            MarketContext context,
            ItemStack stack,
            Optional<String> sourceKey) {

        if (marketState == null || marketState.isEmpty() || stack == null || stack.isEmpty()) {
            return MarketStockAdjustment.NONE;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        TradeItemKey itemKey = TradeItemKey.from(stack);
        List<MarketEvent> matchedEvents = collectMatchedEvents(marketState, context, itemId, itemKey, sourceKey.orElse(null));
        float maxDelta = Config.MARKET_MAX_STOCK_DELTA.get().floatValue();
        return MarketStockAdjustment.of(matchedEvents, maxDelta);
    }

    public static MarketStockAdjustment resolveStockAdjustment(
            MarketState marketState,
            MarketContext context,
            TradeItemKey itemKey,
            Optional<String> sourceKey) {

        if (marketState == null || marketState.isEmpty() || itemKey == null) {
            return MarketStockAdjustment.NONE;
        }

        List<MarketEvent> matchedEvents = collectMatchedEvents(
            marketState,
            context,
            itemKey.itemId(),
            itemKey,
            sourceKey.orElse(null)
        );
        float maxDelta = Config.MARKET_MAX_STOCK_DELTA.get().floatValue();
        return MarketStockAdjustment.of(matchedEvents, maxDelta);
    }

    public static MarketStockAdjustment resolveStockAdjustment(
            MarketState marketState,
            MarketContext context,
            ResourceLocation itemId,
            Optional<String> sourceKey) {

        if (marketState == null || marketState.isEmpty() || itemId == null) {
            return MarketStockAdjustment.NONE;
        }

        TradeItemKey itemKey = TradeItemKey.of(itemId);
        List<MarketEvent> matchedEvents = collectMatchedEvents(marketState, context, itemId, itemKey, sourceKey.orElse(null));
        float maxDelta = Config.MARKET_MAX_STOCK_DELTA.get().floatValue();
        return MarketStockAdjustment.of(matchedEvents, maxDelta);
    }

    private static boolean matchesItem(MarketEvent event, ResourceLocation itemId, TradeItemKey itemKey, String sourceKey) {
        TradeTargetRef targetRef = event.targetRef();
        if (targetRef == null) {
            return false;
        }

        if (targetRef.isSourceKey()) {
            return sourceKey != null && sourceKey.equals(targetRef.sourceKey().orElseThrow());
        }

        if (targetRef.hasComponents()) {
            return itemKey != null && itemKey.matchesExactItem(
                targetRef.itemId().orElseThrow(),
                targetRef.components().orElse(java.util.Map.of())
            );
        }

        if (targetRef.isItem()) {
            return itemId != null && itemId.toString().equals(targetRef.itemId().orElseThrow());
        }

        if (targetRef.isTag()) {
            return github.mczme.ruralroutes.core.util.TagLookupCache.matchesItem(
                itemId,
                "#" + targetRef.tagId().orElseThrow()
            );
        }

        return false;
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
        return getEventsForItem(marketState, stack, Optional.empty());
    }

    public static List<MarketEvent> getEventsForItem(MarketState marketState, ItemStack stack, Optional<String> sourceKey) {
        if (marketState == null || marketState.isEmpty() || stack == null || stack.isEmpty()) {
            return List.of();
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return collectItemEvents(marketState, itemId, TradeItemKey.from(stack), sourceKey.orElse(null));
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
            ResourceLocation itemId,
            TradeItemKey itemKey,
            String sourceKey) {

        List<MarketEvent> matchedEvents = new ArrayList<>();
        for (MarketEvent event : marketState.events()) {
            if (matchesItem(event, itemId, itemKey, sourceKey) && matchesScope(event, context)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    private static List<MarketEvent> collectItemEvents(MarketState marketState, ResourceLocation itemId, TradeItemKey itemKey, String sourceKey) {
        List<MarketEvent> result = new ArrayList<>();
        for (MarketEvent event : marketState.events()) {
            if (matchesItem(event, itemId, itemKey, sourceKey)) {
                result.add(event);
            }
        }
        return result;
    }
}
