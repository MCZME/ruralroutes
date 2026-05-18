package github.mczme.ruralroutes.core.trade;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradePaymentPlanTest {

    @Test
    void emptyPlanReportsEmpty() {
        assertTrue(TradePaymentPlan.empty().isEmpty());
    }

    @Test
    void mergeCombinesAllPaymentListsInOrder() {
        // merge 会被多段交易拼接复用，顺序错误会直接污染 GUI 展示和服务端执行计划。
        TradePaymentPlan left = new TradePaymentPlan(
                List.of(new ItemStack(Items.APPLE, 2)),
                List.of(new ItemStack(Items.BREAD, 1)),
                List.of(),
                List.of(new ItemStack(Items.EMERALD, 3))
        );
        TradePaymentPlan right = new TradePaymentPlan(
                List.of(new ItemStack(Items.CARROT, 4)),
                List.of(),
                List.of(new ItemStack(Items.IRON_INGOT, 2)),
                List.of()
        );

        TradePaymentPlan merged = left.merge(right);

        assertFalse(merged.isEmpty());
        assertEquals(2, merged.playerInputs().size());
        assertEquals(2, merged.playerInputs().get(0).getCount());
        assertEquals(4, merged.playerInputs().get(1).getCount());
        assertEquals(1, merged.playerOutputs().size());
        assertEquals(1, merged.villageInputs().size());
        assertEquals(1, merged.villageOutputs().size());
    }
}
