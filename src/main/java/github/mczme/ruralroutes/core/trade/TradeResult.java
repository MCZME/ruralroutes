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
     */
    public static TradeResult success(int totalValue) {
        return new TradeResult(Reason.SUCCESS, List.of(), totalValue);
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
        VALUE_MISMATCH("trade.fail.value_mismatch"),
        PLAYER_INSUFFICIENT("trade.fail.player_insufficient"),
        VILLAGE_INSUFFICIENT("trade.fail.village_insufficient"),
        INVALID_REQUEST("trade.fail.invalid_request");

        private final String translationKey;

        Reason(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }
}
