package github.mczme.ruralroutes.core.trade;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 交易结果
 */
public record TradeResult(
    boolean success,
    FailReason failReason,
    List<ItemStack> shortfall,
    int totalValueExchanged
) {
    /**
     * 创建成功结果
     */
    public static TradeResult success(int totalValue) {
        return new TradeResult(true, null, List.of(), totalValue);
    }

    /**
     * 创建失败结果
     */
    public static TradeResult fail(FailReason reason, List<ItemStack> shortfall) {
        return new TradeResult(false, reason, shortfall, 0);
    }

    /**
     * 失败原因枚举
     */
    public enum FailReason {
        VALUE_MISMATCH("trade.fail.value_mismatch"),
        PLAYER_INSUFFICIENT("trade.fail.player_insufficient"),
        VILLAGE_INSUFFICIENT("trade.fail.village_insufficient"),
        PLAYER_NO_SPACE("trade.fail.player_no_space"),
        INVALID_REQUEST("trade.fail.invalid_request");

        private final String translationKey;

        FailReason(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }
}
