package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommercialNodeDataTest {

    @Test
    void codecRoundTripsMutableData() {
        CommercialNodeData data = sampleData();

        var encoded = CommercialNodeData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        CommercialNodeData decoded = CommercialNodeData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(data, decoded);
        assertEquals(data.hashCode(), decoded.hashCode());
    }

    @Test
    void mutatorsUpdateStocksAndTimestampInPlace() {
        CommercialNodeData data = sampleData();
        TradeItemKey key = TradeItemKey.of(ResourceLocation.parse("minecraft:apple"));
        NodeStockEntry updatedStock = NodeStockEntry.full(new ItemStack(Items.APPLE), 9);

        data.putStock(updatedStock);
        data.setRefreshTimestamp(42L);

        assertEquals(9, data.getStock(key).current());
        assertEquals(42L, data.refreshTimestamp());
        assertTrue(data.stocks().containsKey(key));
    }

    @Test
    void replaceStocksCopiesInputMapAndKeepsOriginalDataMutable() {
        CommercialNodeData data = sampleData();
        Map<TradeItemKey, NodeStockEntry> replacement = Map.of(
            TradeItemKey.of(ResourceLocation.parse("minecraft:carrot")),
            NodeStockEntry.full(new ItemStack(Items.CARROT), 7)
        );

        data.replaceStocks(replacement);

        assertEquals(7, data.getStock(TradeItemKey.of(ResourceLocation.parse("minecraft:carrot"))).current());
        assertNotSame(replacement, data.stocks());
    }

    private static CommercialNodeData sampleData() {
        TradeItemKey appleKey = TradeItemKey.of(ResourceLocation.parse("minecraft:apple"));
        NodeStockEntry stock = NodeStockEntry.full(new ItemStack(Items.APPLE), 5);
        return CommercialNodeData.create(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            ResourceLocation.parse("ruralroutes:test"),
            List.of(NodeTradeEntry.of("sell/apple", appleKey)),
            List.of(NodeTradeEntry.of("buy/apple", appleKey)),
            Map.of(appleKey, stock),
            11L
        );
    }
}
