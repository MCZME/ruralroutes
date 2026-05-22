package github.mczme.ruralroutes.core.node;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeStockPlanTest {

    @Test
    void convertsSellAndBuyBasesToSharedStockEntry() {
        // current 代表当前可卖现货，max 代表现货 + 可回收容量，这里固定共享库存语义。
        NodeStockEntry stockEntry = NodeStockPlan.EMPTY
                .addSell(8)
                .addBuy(5)
                .toStockEntry(new ItemStack(Items.APPLE));

        assertEquals(8, stockEntry.current());
        assertEquals(13, stockEntry.max());
    }

    @Test
    void emptyPlanProducesEmptyStockEntry() {
        NodeStockEntry stockEntry = NodeStockPlan.EMPTY.toStockEntry(new ItemStack(Items.APPLE));

        assertEquals(0, stockEntry.current());
        assertEquals(0, stockEntry.max());
        assertTrue(stockEntry.isEmpty());
    }
}
