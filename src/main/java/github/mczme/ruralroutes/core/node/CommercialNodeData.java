package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 商业节点数据，存储在区块中。
 *
 * 运行时保持两层表达：
 * - 展示层使用 NodeTradeEntry，支持带组件物品的稳定展示和同步
 * - 库存使用 NodeStockEntry 保存物品栈原型和库存数量，TradeItemKey 只作为稳定地址
 */
public record CommercialNodeData(
    UUID tradeNodeId,
    ResourceLocation themeName,
    List<NodeTradeEntry> sellItems,
    List<NodeTradeEntry> buyItems,
    List<NodeTradeEntry> specialties,
    Map<TradeItemKey, NodeStockEntry> stocks,
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
            Codec.unboundedMap(TradeItemKey.CODEC, NodeStockEntry.CODEC)
                .fieldOf("stocks")
                .forGetter(CommercialNodeData::stocks),
            Codec.LONG
                .fieldOf("refresh_timestamp")
                .forGetter(CommercialNodeData::refreshTimestamp)
        ).apply(instance, CommercialNodeData::new)
    );

    public CommercialNodeData {
        tradeNodeId = Objects.requireNonNull(tradeNodeId, "tradeNodeId");
        themeName = Objects.requireNonNull(themeName, "themeName");
        sellItems = List.copyOf(Objects.requireNonNull(sellItems, "sellItems"));
        buyItems = List.copyOf(Objects.requireNonNull(buyItems, "buyItems"));
        specialties = List.copyOf(Objects.requireNonNull(specialties, "specialties"));
        stocks = normalizeStocks(Objects.requireNonNull(stocks, "stocks"));
    }

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
            Map<TradeItemKey, NodeStockEntry> stocks, long timestamp) {
        return new CommercialNodeData(tradeNodeId, themeName,
            List.copyOf(sellItems), List.copyOf(buyItems), List.copyOf(specialties),
            Map.copyOf(stocks), timestamp);
    }

    /** 获取指定库存键对应的库存项 */
    public NodeStockEntry getStockEntry(TradeItemKey itemKey) {
        if (itemKey == null) {
            return null;
        }
        NodeStockEntry entry = stocks.get(itemKey);
        if (entry != null) {
            return entry;
        }
        return itemKey.hasComponents() ? stocks.get(TradeItemKey.of(itemKey.itemId())) : null;
    }

    /** 获取指定物品栈对应的库存项 */
    public NodeStockEntry getStockEntry(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : getStockEntry(TradeItemKey.from(stack));
    }

    /** 获取指定索引项对应的库存项 */
    public NodeStockEntry getStockEntry(NodeTradeEntry entry) {
        return entry == null ? null : getStockEntry(entry.stockKey());
    }

    /** 获取指定物品的库存 */
    public NodeStockEntry getStock(TradeItemKey itemKey) {
        return getStockEntry(itemKey);
    }

    /** 获取指定物品栈的库存 */
    public NodeStockEntry getStock(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : getStock(TradeItemKey.from(stack));
    }

    /** 获取指定索引项对应的库存数量 */
    public NodeStockEntry getStock(NodeTradeEntry entry) {
        return getStockEntry(entry);
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
    public CommercialNodeData withStock(TradeItemKey itemKey, NodeStockEntry newStock) {
        Map<TradeItemKey, NodeStockEntry> newStocks = new java.util.HashMap<>(stocks);
        newStocks.put(itemKey, newStock);
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

    private static Map<TradeItemKey, NodeStockEntry> normalizeStocks(Map<TradeItemKey, NodeStockEntry> stocks) {
        Map<TradeItemKey, NodeStockEntry> normalized = new java.util.HashMap<>();
        for (Map.Entry<TradeItemKey, NodeStockEntry> entry : stocks.entrySet()) {
            NodeStockEntry stockEntry = Objects.requireNonNull(entry.getValue(), "stock entry");
            normalized.put(stockEntry.stockKey(), stockEntry);
        }
        return Map.copyOf(normalized);
    }

}
