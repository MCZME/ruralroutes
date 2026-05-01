package github.mczme.ruralroutes.core.trade;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 交易请求
 * 包含玩家付出的物品组和玩家获得的物品组
 */
public record TradeRequest(
    List<ItemStack> giveItems,
    List<ItemStack> takeItems
) {
    /**
     * 验证请求是否有效（非空且无空 ItemStack）
     */
    public boolean isValid() {
        if (giveItems.isEmpty() && takeItems.isEmpty()) {
            return false;
        }
        return giveItems.stream().noneMatch(ItemStack::isEmpty)
            && takeItems.stream().noneMatch(ItemStack::isEmpty);
    }
}
