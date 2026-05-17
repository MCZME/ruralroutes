package github.mczme.ruralroutes.core.market;

import java.util.ArrayList;
import java.util.List;

/**
 * 库存调整结果。
 *
 * 将命中的市场事件解析为出售现货与收购容量两条独立的库存因子。
 */
public record MarketStockAdjustment(
        float sellFactor,
        float buyFactor,
        float sellDelta,
        float buyDelta,
        List<MarketEvent> matchedEvents
) {
    public static final MarketStockAdjustment NONE = new MarketStockAdjustment(
            1.0f, 1.0f, 0.0f, 0.0f, List.of());

    public static MarketStockAdjustment of(List<MarketEvent> matchedEvents, float maxDelta) {
        if (matchedEvents.isEmpty()) {
            return NONE;
        }

        float totalSellDelta = 0.0f;
        float totalBuyDelta = 0.0f;
        List<MarketEvent> effectiveEvents = new ArrayList<>();

        for (MarketEvent event : matchedEvents) {
            MarketStockModifier stock = event.stock().orElse(MarketStockModifier.NONE);
            if (!stock.hasEffect()) {
                continue;
            }
            totalSellDelta += stock.sell();
            totalBuyDelta += stock.buy();
            effectiveEvents.add(event);
        }

        if (effectiveEvents.isEmpty()) {
            return NONE;
        }

        totalSellDelta = clampDelta(totalSellDelta, maxDelta);
        totalBuyDelta = clampDelta(totalBuyDelta, maxDelta);

        return new MarketStockAdjustment(
                1.0f + totalSellDelta,
                1.0f + totalBuyDelta,
                totalSellDelta,
                totalBuyDelta,
                List.copyOf(effectiveEvents)
        );
    }

    public boolean hasAdjustment() {
        return !matchedEvents.isEmpty();
    }

    public int applySellBase(int baseValue) {
        return scale(baseValue, sellFactor);
    }

    public int applyBuyBase(int baseValue) {
        return scale(baseValue, buyFactor);
    }

    private static float clampDelta(float value, float maxDelta) {
        return Math.max(-maxDelta, Math.min(maxDelta, value));
    }

    private static int scale(int baseValue, float factor) {
        if (baseValue <= 0) {
            return 0;
        }
        return Math.max(0, Math.round(baseValue * factor));
    }
}
