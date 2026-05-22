package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 节点库存索引条目。
 *
 * 用于 sellItems / buyItems / specialties 这类消费端有序视图。
 */
public record NodeTradeEntry(
    String sourceKey,
    TradeItemKey stockKey
) {
    public static final Codec<NodeTradeEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.fieldOf("source_key").forGetter(NodeTradeEntry::sourceKey),
            TradeItemKey.CODEC.fieldOf("stock_key").forGetter(NodeTradeEntry::stockKey)
        ).apply(instance, NodeTradeEntry::new)
    );

    public NodeTradeEntry {
        sourceKey = sourceKey == null ? "" : sourceKey.strip();
        stockKey = Objects.requireNonNull(stockKey, "stockKey");
    }

    public static NodeTradeEntry of(String sourceKey, TradeItemKey stockKey) {
        return new NodeTradeEntry(sourceKey, stockKey);
    }

    public static NodeTradeEntry of(String sourceKey, ResourceLocation itemId, ItemStack displayStack) {
        TradeItemKey stockKey = displayStack == null || displayStack.isEmpty()
            ? TradeItemKey.of(itemId)
            : TradeItemKey.from(displayStack);
        return new NodeTradeEntry(sourceKey, stockKey);
    }

    public static NodeTradeEntry of(String sourceKey, ResourceLocation itemId) {
        return new NodeTradeEntry(sourceKey, TradeItemKey.of(itemId));
    }

    public ResourceLocation itemId() {
        return stockKey.itemId();
    }

    public TradeItemKey tradeItemKey() {
        return stockKey;
    }

    public ItemStack displayStackOrDefault() {
        return stockKey.asItemStack();
    }

    public boolean hasDisplayStack() {
        return stockKey.hasComponents();
    }
}
