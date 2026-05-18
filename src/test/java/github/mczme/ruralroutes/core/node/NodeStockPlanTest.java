package github.mczme.ruralroutes.core.node;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeStockPlanTest {

    @Test
    void convertsSellAndBuyBasesToSharedStockEntry() {
        // current 代表当前可卖现货，max 代表现货 + 可回收容量，这里固定共享库存语义。
        StockEntry stockEntry = NodeStockPlan.EMPTY
                .addSell(8)
                .addBuy(5)
                .toStockEntry();

        assertEquals(8, stockEntry.current());
        assertEquals(13, stockEntry.max());
    }

    @Test
    void emptyPlanProducesEmptyStockEntry() {
        StockEntry stockEntry = NodeStockPlan.EMPTY.toStockEntry();

        assertEquals(0, stockEntry.current());
        assertEquals(0, stockEntry.max());
        assertTrue(stockEntry.isEmpty());
    }
}
