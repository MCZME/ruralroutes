package github.mczme.ruralroutes.core.node;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockEntryTest {

    @Test
    void fullAndEmptyFactoriesReflectBoundaries() {
        StockEntry full = StockEntry.full(12);
        StockEntry empty = StockEntry.empty(12);

        assertEquals(12, full.current());
        assertEquals(12, full.max());
        assertTrue(full.isFull());
        assertFalse(full.isEmpty());

        assertEquals(0, empty.current());
        assertEquals(12, empty.max());
        assertTrue(empty.isEmpty());
        assertFalse(empty.isFull());
    }

    @Test
    void increaseAndDecreaseClampToBounds() {
        StockEntry base = new StockEntry(5, 10);

        assertEquals(0, base.decrease(99).current());
        assertEquals(10, base.increase(99).current());
    }
}
