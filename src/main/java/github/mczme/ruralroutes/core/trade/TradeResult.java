package github.mczme.ruralroutes.core.trade;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 统一交易结果
 * 用于混合交易和单一契约执行
 */
public record TradeResult(
    Reason reason,
    List<ItemStack> outputs,         // 玩家获得的物品
    List<ItemStack> consumed,        // 玩家消耗的物品
    List<ItemStack> shortfall,       // 失败时的不足物品
    int totalValueExchanged          // 交易总价值（净货币差额）
) {
    public enum Reason {
        SUCCESS("trade.success"),
        PLAYER_INSUFFICIENT("trade.fail.player_insufficient"),
        VILLAGE_INSUFFICIENT("trade.fail.village_insufficient"),
        INVALID_INPUT("trade.fail.invalid_request"),
        INVALID_REQUEST("trade.fail.invalid_request"),
        CYCLE_CHANGED("trade.fail.cycle_changed");

        private final String translationKey;

        Reason(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    // ===== 成功结果工厂方法 =====

    /**
     * 创建契约执行成功结果
     */
    public static TradeResult success(List<ItemStack> outputs, List<ItemStack> consumed) {
        return new TradeResult(Reason.SUCCESS, outputs, consumed, List.of(), 0);
    }

    /**
     * 创建混合交易成功结果
     */
    public static TradeResult success(int sellValueTotal, int buyValueTotal) {
        int netValue = sellValueTotal - buyValueTotal;
        return new TradeResult(Reason.SUCCESS, List.of(), List.of(), List.of(), netValue);
    }

    // ===== 失败结果工厂方法 =====

    /**
     * 创建失败结果（带不足物品）
     */
    public static TradeResult fail(Reason reason, List<ItemStack> shortfall) {
        return new TradeResult(reason, List.of(), List.of(), shortfall, 0);
    }

    /**
     * 创建失败结果（无不足物品）
     */
    public static TradeResult fail(Reason reason) {
        return new TradeResult(reason, List.of(), List.of(), List.of(), 0);
    }

    // ===== 便捷方法 =====

    public boolean isSuccess() {
        return reason == Reason.SUCCESS;
    }

    /**
     * 获取用于 GUI 显示的翻译键
     */
    public String getTranslationKey() {
        return reason.getTranslationKey();
    }
}
