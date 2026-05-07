package github.mczme.ruralroutes.core.trade;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 交易支付计划
 * 明确的输入/输出物品集合
 */
public record TradePaymentPlan(
    List<ItemStack> playerInputs,
    List<ItemStack> playerOutputs,
    List<ItemStack> villageInputs,
    List<ItemStack> villageOutputs
) {
    public static TradePaymentPlan empty() {
        return new TradePaymentPlan(List.of(), List.of(), List.of(), List.of());
    }

    public boolean isEmpty() {
        return playerInputs.isEmpty() && playerOutputs.isEmpty()
            && villageInputs.isEmpty() && villageOutputs.isEmpty();
    }

    /** 合并两个支付计划 */
    public TradePaymentPlan merge(TradePaymentPlan other) {
        return new TradePaymentPlan(
            mergeLists(this.playerInputs, other.playerInputs),
            mergeLists(this.playerOutputs, other.playerOutputs),
            mergeLists(this.villageInputs, other.villageInputs),
            mergeLists(this.villageOutputs, other.villageOutputs)
        );
    }

    private static List<ItemStack> mergeLists(List<ItemStack> a, List<ItemStack> b) {
        java.util.ArrayList<ItemStack> result = new java.util.ArrayList<>(a);
        result.addAll(b);
        return List.copyOf(result);
    }
}
