package github.mczme.ruralroutes.core.node;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockEntryTest {

    @Test
    void fullAndEmptyFactoriesReflectBoundaries() {
        NodeStockEntry full = NodeStockEntry.full(new ItemStack(Items.APPLE), 12);
        NodeStockEntry empty = NodeStockEntry.empty(new ItemStack(Items.APPLE), 12);

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
        NodeStockEntry base = new NodeStockEntry(new ItemStack(Items.APPLE), 5, 10);

        assertEquals(0, base.decrease(99).current());
        assertEquals(10, base.increase(99).current());
    }
}
