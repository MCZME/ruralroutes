package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Collections;
import java.util.LinkedHashMap;
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
public class CommercialNodeData {
    private final UUID tradeNodeId;
    private final ResourceLocation themeName;
    private final List<NodeTradeEntry> sellItems;
    private final List<NodeTradeEntry> buyItems;
    private final Map<TradeItemKey, NodeStockEntry> stocks;
    private long refreshTimestamp;

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
            Codec.unboundedMap(TradeItemKey.CODEC, NodeStockEntry.CODEC)
                .fieldOf("stocks")
                .forGetter(CommercialNodeData::stocks),
            Codec.LONG
                .fieldOf("refresh_timestamp")
                .forGetter(CommercialNodeData::refreshTimestamp)
        ).apply(instance, CommercialNodeData::new)
    );

    public CommercialNodeData(UUID tradeNodeId, ResourceLocation themeName,
            List<NodeTradeEntry> sellItems, List<NodeTradeEntry> buyItems,
            Map<TradeItemKey, NodeStockEntry> stocks, long refreshTimestamp) {
        this.tradeNodeId = Objects.requireNonNull(tradeNodeId, "tradeNodeId");
        this.themeName = Objects.requireNonNull(themeName, "themeName");
        this.sellItems = List.copyOf(Objects.requireNonNull(sellItems, "sellItems"));
        this.buyItems = List.copyOf(Objects.requireNonNull(buyItems, "buyItems"));
        this.stocks = normalizeStocks(Objects.requireNonNull(stocks, "stocks"));
        this.refreshTimestamp = refreshTimestamp;
    }

    /** 创建空的默认实例（用于 Attachment 默认值） */
    public static CommercialNodeData empty() {
        return new CommercialNodeData(
            UUID.randomUUID(),
            ResourceLocation.parse("minecraft:empty"),
            List.of(),
            List.of(),
            Map.of(),
            0L
        );
    }

    /** 创建新节点数据 */
    public static CommercialNodeData create(UUID tradeNodeId, ResourceLocation themeName,
            List<NodeTradeEntry> sellItems, List<NodeTradeEntry> buyItems,
            Map<TradeItemKey, NodeStockEntry> stocks, long timestamp) {
        return new CommercialNodeData(tradeNodeId, themeName, sellItems, buyItems, stocks, timestamp);
    }

    public UUID tradeNodeId() {
        return tradeNodeId;
    }

    public ResourceLocation themeName() {
        return themeName;
    }

    public List<NodeTradeEntry> sellItems() {
        return sellItems;
    }

    public List<NodeTradeEntry> buyItems() {
        return buyItems;
    }

    public Map<TradeItemKey, NodeStockEntry> stocks() {
        return Collections.unmodifiableMap(stocks);
    }

    public long refreshTimestamp() {
        return refreshTimestamp;
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

    /** 原地更新单个物品库存 */
    public void putStock(NodeStockEntry newStock) {
        NodeStockEntry normalized = Objects.requireNonNull(newStock, "newStock");
        stocks.put(normalized.stockKey(), normalized);
    }

    /** 原地替换全部库存 */
    public void replaceStocks(Map<TradeItemKey, NodeStockEntry> newStocks) {
        stocks.clear();
        stocks.putAll(normalizeStocks(Objects.requireNonNull(newStocks, "newStocks")));
    }

    /** 原地更新刷新时间戳 */
    public void setRefreshTimestamp(long newTimestamp) {
        refreshTimestamp = newTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommercialNodeData that)) {
            return false;
        }
        return refreshTimestamp == that.refreshTimestamp
            && tradeNodeId.equals(that.tradeNodeId)
            && themeName.equals(that.themeName)
            && sellItems.equals(that.sellItems)
            && buyItems.equals(that.buyItems)
            && stocks.equals(that.stocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeNodeId, themeName, sellItems, buyItems, stocks, refreshTimestamp);
    }

    @Override
    public String toString() {
        return "CommercialNodeData["
            + "tradeNodeId=" + tradeNodeId
            + ", themeName=" + themeName
            + ", sellItems=" + sellItems
            + ", buyItems=" + buyItems
            + ", stocks=" + stocks
            + ", refreshTimestamp=" + refreshTimestamp
            + "]";
    }

    private static Map<TradeItemKey, NodeStockEntry> normalizeStocks(Map<TradeItemKey, NodeStockEntry> stocks) {
        Map<TradeItemKey, NodeStockEntry> normalized = new LinkedHashMap<>();
        for (Map.Entry<TradeItemKey, NodeStockEntry> entry : stocks.entrySet()) {
            NodeStockEntry stockEntry = Objects.requireNonNull(entry.getValue(), "stock entry");
            normalized.put(stockEntry.stockKey(), stockEntry);
        }
        return normalized;
    }

}
