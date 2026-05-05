package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.market.MarketPriceAdjustment;

/**
 * 贸易价格计算结果
 * 包含价格明细，用于结算、展示和调试
 */
public record TradePrice(
    int baseValue,
    float themeModifier,
    MarketPriceAdjustment marketAdjustment,
    int finalPrice
) {
    /**
     * 创建默认价格（无调整）
     * @param baseValue 基础价值
     * @return 仅包含基础价值的价格结果
     */
    public static TradePrice ofBase(int baseValue) {
        return new TradePrice(baseValue, 1.0f, MarketPriceAdjustment.NONE, baseValue);
    }

    /**
     * 检查是否有市场调整
     * @return 如果有市场事件影响价格则返回 true
     */
    public boolean hasMarketAdjustment() {
        return marketAdjustment.hasAdjustment();
    }

    /**
     * 检查是否有主题修正
     * @return 如果主题修正系数不为 1.0 则返回 true
     */
    public boolean hasThemeModifier() {
        return Math.abs(themeModifier - 1.0f) > 0.001f;
    }
}