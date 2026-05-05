package github.mczme.ruralroutes.core.trade;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 交易结果
 */
public record TradeResult(
    Reason reason,
    List<ItemStack> shortfall,
    int totalValueExchanged
) {
    /**
     * 创建成功结果
     * @param sellValueTotal 玩家需支付的货币（购买物品总价）
     * @param buyValueTotal 玩家将获得的货币（出售物品总价）
     */
    public static TradeResult success(int sellValueTotal, int buyValueTotal) {
        int netValue = sellValueTotal - buyValueTotal;
        return new TradeResult(Reason.SUCCESS, List.of(), netValue);
    }

    /**
     * 创建失败结果
     */
    public static TradeResult fail(Reason reason, List<ItemStack> shortfall) {
        return new TradeResult(reason, shortfall, 0);
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return reason == Reason.SUCCESS;
    }

    /**
     * 交易原因枚举
     */
    public enum Reason {
        SUCCESS("trade.success"),
        PLAYER_INSUFFICIENT("trade.fail.player_insufficient"),
        VILLAGE_INSUFFICIENT("trade.fail.village_insufficient"),
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
}
