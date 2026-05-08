package github.mczme.ruralroutes.core.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 商业节点数据，存储在区块中
 * 包含节点唯一标识、主题、出售/收购物品列表、库存、特产和刷新时间戳
 */
public record CommercialNodeData(
    UUID tradeNodeId,
    ResourceLocation themeName,
    List<ResourceLocation> sellItems,           // 出售物品列表（村庄卖给玩家）
    List<ResourceLocation> buyItems,            // 收购物品列表（玩家卖给村庄）
    List<ResourceLocation> specialties,         // 特产列表（主题特产 + 随机特产）
    Map<ResourceLocation, StockEntry> stocks,   // 库存数据（共用）
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
            ResourceLocation.CODEC.listOf()
                .fieldOf("sell_items")
                .forGetter(CommercialNodeData::sellItems),
            ResourceLocation.CODEC.listOf()
                .fieldOf("buy_items")
                .forGetter(CommercialNodeData::buyItems),
            ResourceLocation.CODEC.listOf()
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
            List<ResourceLocation> sellItems, List<ResourceLocation> buyItems,
            List<ResourceLocation> specialties,
            Map<ResourceLocation, StockEntry> stocks, long timestamp) {
        return new CommercialNodeData(tradeNodeId, themeName,
            List.copyOf(sellItems), List.copyOf(buyItems), List.copyOf(specialties),
            Map.copyOf(stocks), timestamp);
    }

    /** 获取指定物品的库存 */
    public StockEntry getStock(ResourceLocation itemId) {
        return stocks.get(itemId);
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
    public CommercialNodeData withSpecialties(List<ResourceLocation> newSpecialties) {
        return new CommercialNodeData(tradeNodeId, themeName, sellItems, buyItems, List.copyOf(newSpecialties), stocks, refreshTimestamp);
    }
}