package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 商业节点数据，存储在区块中
 * 包含节点唯一标识、主题、库存、特产列表和刷新时间戳
 */
public record CommercialNodeData(
    UUID tradeNodeId,
    ResourceLocation themeName,
    Map<ResourceLocation, StockEntry> stocks,
    List<ResourceLocation> specialties,
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
            Codec.unboundedMap(ResourceLocation.CODEC, StockEntry.CODEC)
                .fieldOf("stocks")
                .forGetter(CommercialNodeData::stocks),
            ResourceLocation.CODEC.listOf()
                .fieldOf("specialties")
                .forGetter(CommercialNodeData::specialties),
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
            Map.of(),
            List.of(),
            0L
        );
    }

    /** 创建新节点数据 */
    public static CommercialNodeData create(UUID tradeNodeId, ResourceLocation themeName,
            Map<ResourceLocation, StockEntry> stocks, List<ResourceLocation> specialties, long timestamp) {
        return new CommercialNodeData(tradeNodeId, themeName, Map.copyOf(stocks), List.copyOf(specialties), timestamp);
    }

    /** 获取指定物品的库存 */
    public StockEntry getStock(ResourceLocation itemId) {
        return stocks.get(itemId);
    }

    /** 更新单个物品库存 */
    public CommercialNodeData withStock(ResourceLocation itemId, StockEntry newStock) {
        Map<ResourceLocation, StockEntry> newStocks = new java.util.HashMap<>(stocks);
        newStocks.put(itemId, newStock);
        return new CommercialNodeData(tradeNodeId, themeName, Map.copyOf(newStocks), specialties, refreshTimestamp);
    }

    /** 更新刷新时间戳 */
    public CommercialNodeData withTimestamp(long newTimestamp) {
        return new CommercialNodeData(tradeNodeId, themeName, stocks, specialties, newTimestamp);
    }
}