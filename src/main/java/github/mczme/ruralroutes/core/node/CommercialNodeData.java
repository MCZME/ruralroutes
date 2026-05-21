package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 商业节点数据，存储在区块中。
 *
 * 运行时保持两层表达：
 * - 展示层使用 NodeTradeEntry，支持带组件物品的稳定展示和同步
 * - 库存、价格修正和市场事件仍按基础物品 ID 分桶
 */
public record CommercialNodeData(
    UUID tradeNodeId,
    ResourceLocation themeName,
    List<NodeTradeEntry> sellItems,
    List<NodeTradeEntry> buyItems,
    List<NodeTradeEntry> specialties,
    Map<ResourceLocation, StockEntry> stocks,
    long refreshTimestamp
) {
    public static final Codec<CommercialNodeData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString)
                .fieldOf("trade_node_id")
                .forGetter(CommercialNodeData::tradeNodeId),
            ResourceLocation.CODEC
                .fieldOf("theme_name")
                .forGetter(CommercialNodeData::themeName),
            NodeTradeEntry.CODEC.listOf()
                .fieldOf("sell_items")
                .forGetter(CommercialNodeData::sellItems),
            NodeTradeEntry.CODEC.listOf()
                .fieldOf("buy_items")
                .forGetter(CommercialNodeData::buyItems),
            NodeTradeEntry.CODEC.listOf()
                .fieldOf("specialties")
                .forGetter(CommercialNodeData::specialties),
            Codec.unboundedMap(ResourceLocation.CODEC, StockEntry.CODEC)
                .fieldOf("stocks")
                .forGetter(CommercialNodeData::stocks),
            Codec.LONG
                .fieldOf("refresh_timestamp")
                .forGetter(CommercialNodeData::refreshTimestamp)
        ).apply(instance, CommercialNodeData::new)
    );

    /** 创建空的默认实例（用于 Attachment 默认值） */
    public static CommercialNodeData empty() {
        return new CommercialNodeData(
            UUID.randomUUID(),
            ResourceLocation.parse("minecraft:empty"),
            List.of(),
            List.of(),
            List.of(),
            Map.of(),
            0L
        );
    }

    /** 创建新节点数据 */
    public static CommercialNodeData create(UUID tradeNodeId, ResourceLocation themeName,
            List<NodeTradeEntry> sellItems, List<NodeTradeEntry> buyItems,
            List<NodeTradeEntry> specialties,
            Map<ResourceLocation, StockEntry> stocks, long timestamp) {
        return new CommercialNodeData(tradeNodeId, themeName,
            List.copyOf(sellItems), List.copyOf(buyItems), List.copyOf(specialties),
            Map.copyOf(stocks), timestamp);
    }

    /** 获取指定物品的库存 */
    public StockEntry getStock(ResourceLocation itemId) {
        return stocks.get(itemId);
    }

    public List<ResourceLocation> sellItemIds() {
        return sellItems.stream().map(NodeTradeEntry::itemId).toList();
    }

    public List<ResourceLocation> buyItemIds() {
        return buyItems.stream().map(NodeTradeEntry::itemId).toList();
    }

    public List<ResourceLocation> specialtyIds() {
        return specialties.stream().map(NodeTradeEntry::itemId).toList();
    }

    /** 更新单个物品库存 */
    public CommercialNodeData withStock(ResourceLocation itemId, StockEntry newStock) {
        Map<ResourceLocation, StockEntry> newStocks = new java.util.HashMap<>(stocks);
        newStocks.put(itemId, newStock);
        return new CommercialNodeData(tradeNodeId, themeName, sellItems, buyItems, specialties, Map.copyOf(newStocks), refreshTimestamp);
    }

    /** 更新刷新时间戳 */
    public CommercialNodeData withTimestamp(long newTimestamp) {
        return new CommercialNodeData(tradeNodeId, themeName, sellItems, buyItems, specialties, stocks, newTimestamp);
    }

    /** 更新特产列表 */
    public CommercialNodeData withSpecialties(List<NodeTradeEntry> newSpecialties) {
        return new CommercialNodeData(tradeNodeId, themeName, sellItems, buyItems, List.copyOf(newSpecialties), stocks, refreshTimestamp);
    }

    public record NodeTradeEntry(
        String sourceKey,
        ResourceLocation itemId,
        @Nullable ItemStack displayStack
    ) {
        public static final Codec<NodeTradeEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.STRING.fieldOf("source_key").forGetter(NodeTradeEntry::sourceKey),
                ResourceLocation.CODEC.fieldOf("item_id").forGetter(NodeTradeEntry::itemId),
                ItemStack.CODEC.optionalFieldOf("display_stack").forGetter(entry -> java.util.Optional.ofNullable(entry.displayStack))
            ).apply(instance, (sourceKey, itemId, displayStack) ->
                new NodeTradeEntry(sourceKey, itemId, displayStack.orElse(null)))
        );

        public NodeTradeEntry {
            sourceKey = sourceKey == null ? "" : sourceKey.strip();
        }

        public static NodeTradeEntry of(String sourceKey, ResourceLocation itemId, ItemStack displayStack) {
            return new NodeTradeEntry(sourceKey, itemId, displayStack == null ? null : displayStack.copy());
        }

        public static NodeTradeEntry of(String sourceKey, ResourceLocation itemId) {
            return new NodeTradeEntry(sourceKey, itemId, null);
        }

        public ItemStack displayStackOrDefault() {
            if (displayStack != null && !displayStack.isEmpty()) {
                return displayStack.copy();
            }
            ItemStack fallback = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
            return fallback.isEmpty() ? ItemStack.EMPTY : fallback;
        }

        public boolean hasDisplayStack() {
            return displayStack != null && !displayStack.isEmpty();
        }
    }
}
