package github.mczme.ruralroutes.core.market;

import java.util.List;

/**
 * 价格调整结果
 *
 * 包含市场解析后的价格因子、总调整值和匹配的事件列表。
 * 用于价格计算、GUI 显示和调试。
 */
public record MarketPriceAdjustment(
        float factor,
        float delta,
        List<MarketEvent> matchedEvents
) {
    /**
     * 无调整结果（默认值）
     */
    public static final MarketPriceAdjustment NONE = new MarketPriceAdjustment(1.0f, 0.0f, List.of());

    /**
     * 根据匹配的事件创建调整结果
     * @param matchedEvents 匹配的市场事件列表
     * @return 包含计算后因子和调整值的结果
     */
    public static MarketPriceAdjustment of(List<MarketEvent> matchedEvents, float maxDelta) {
        if (matchedEvents.isEmpty()) {
            return NONE;
        }

        float totalDelta = matchedEvents.stream()
                .map(MarketEvent::delta)
                .reduce(0.0f, Float::sum);

        // 应用上限保护
        totalDelta = Math.max(-maxDelta, Math.min(maxDelta, totalDelta));

        float factor = 1.0f + totalDelta;

        return new MarketPriceAdjustment(factor, totalDelta, List.copyOf(matchedEvents));
    }

    /**
     * 检查是否有调整
     * @return 如果有价格调整则返回 true
     */
    public boolean hasAdjustment() {
        return !matchedEvents.isEmpty();
    }

    /**
     * 获取涨跌方向描述
     * @return "up" 表示上涨，"down" 表示下跌，"stable" 表示无变化
     */
    public String getDirectionKey() {
        if (delta > 0.001f) return "up";
        if (delta < -0.001f) return "down";
        return "stable";
    }

    /**
     * 获取百分比显示值
     * @return 可读的百分比字符串，如 "+15%" 或 "-10%"
     */
    public String getPercentDisplay() {
        int percent = (int) (delta * 100);
        if (percent > 0) {
            return "+" + percent + "%";
        } else if (percent < 0) {
            return percent + "%";
        } else {
            return "0%";
        }
    }
}