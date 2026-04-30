package github.mczme.ruralroutes.core.node;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.register.RRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.entity.ai.village.poi.PoiManager;

import java.util.*;

/**
 * 商业节点管理器
 * 处理商业节点的创建、访问和校验
 */
public class CommercialNodeManager {

    private CommercialNodeManager() {}

    /**
     * 获取区块中的商业节点数据
     * @param level 世界
     * @param chunkPos 区块坐标
     * @return 商业节点数据，不存在则返回 null
     */
    public static CommercialNodeData getNodeData(Level level, ChunkPos chunkPos) {
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        if (chunk.hasData(RRAttachments.COMMERCIAL_NODE.get())) {
            return chunk.getData(RRAttachments.COMMERCIAL_NODE.get());
        }
        return null;
    }

    /**
     * 获取区块中的商业节点数据
     * @param level 世界
     * @param pos 方块坐标
     * @return 商业节点数据，不存在则返回 null
     */
    public static CommercialNodeData getNodeData(Level level, BlockPos pos) {
        return getNodeData(level, new ChunkPos(pos));
    }

    /**
     * 检查区块是否已有商业节点数据
     */
    public static boolean hasNodeData(Level level, ChunkPos chunkPos) {
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        return chunk.hasData(RRAttachments.COMMERCIAL_NODE.get());
    }

    /**
     * 检查区块是否已有商业节点数据
     */
    public static boolean hasNodeData(Level level, BlockPos pos) {
        return hasNodeData(level, new ChunkPos(pos));
    }

    /**
     * 检查贸易站附近是否有村庄
     * 使用 PoiManager.sectionsToVillage 检测村庄距离
     * @param level 世界
     * @param pos 贸易站位置
     * @return 是否有村庄
     */
    public static boolean hasVillageNearby(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        PoiManager poiManager = serverLevel.getPoiManager();
        SectionPos sectionPos = SectionPos.of(pos);

        // sectionsToVillage 返回到村庄的距离（区块段为单位）
        // MAX_VILLAGE_DISTANCE = 6，距离 <= 6 表示在村庄范围内
        int distanceToVillage = poiManager.sectionsToVillage(sectionPos);
        return distanceToVillage <= PoiManager.MAX_VILLAGE_DISTANCE;
    }

    /**
     * 创建商业节点数据
     * @param level 世界
     * @param pos 贸易站位置
     * @param themeName 主题名称
     * @return 创建的商业节点数据，创建失败返回 null
     */
    public static CommercialNodeData createNodeData(Level level, BlockPos pos, ResourceLocation themeName) {
        ThemeTemplate template = ThemeManager.INSTANCE.getTheme(themeName);
        if (template == null) {
            RuralRoutes.LOGGER.warn("Cannot create commercial node: theme {} not found", themeName);
            return null;
        }

        UUID tradeNodeId = UUID.randomUUID();
        Map<ResourceLocation, StockEntry> stocks = initializeStocks(template);
        List<ResourceLocation> specialties = generateSpecialties(template);
        long timestamp = level.getGameTime();

        CommercialNodeData data = CommercialNodeData.create(tradeNodeId, themeName, stocks, specialties, timestamp);

        // 存储到区块
        ChunkAccess chunk = level.getChunk(pos);
        chunk.setData(RRAttachments.COMMERCIAL_NODE.get(), data);

        RuralRoutes.LOGGER.debug("Created commercial node {} with theme {} at {}",
            tradeNodeId, themeName, pos);

        return data;
    }

    /**
     * 从主题模板生成特产列表
     * 特产 = 主题特产（必定出现）+ 随机特产（从全局池随机抽取）
     */
    private static List<ResourceLocation> generateSpecialties(ThemeTemplate template) {
        List<ResourceLocation> specialties = new ArrayList<>();

        // 添加主题特产（必定出现）
        if (template.themeSpecialties().isPresent()) {
            List<ResourceLocation> themeSpecialties = template.themeSpecialties().get();
            specialties.addAll(themeSpecialties);
        }

        // TODO: 第二阶段实现从全局特产池随机抽取 0-N 种随机特产

        return specialties;
    }

    /**
     * 从主题模板初始化库存
     */
    private static Map<ResourceLocation, StockEntry> initializeStocks(ThemeTemplate template) {
        Map<ResourceLocation, StockEntry> stocks = new HashMap<>();

        // 默认库存范围
        int defaultMin = 8;
        int defaultMax = 16;

        if (template.stock().isPresent()) {
            ThemeTemplate.StockConfig stockConfig = template.stock().get();

            // 获取默认范围
            if (stockConfig.defaultRange().isPresent()) {
                ThemeTemplate.StockRange range = stockConfig.defaultRange().get();
                defaultMin = range.min();
                defaultMax = range.max();
            }
        }

        // 添加出售物品（村庄卖给玩家）
        for (ThemeTemplate.ItemReference item : template.sellItems()) {
            ResourceLocation itemId = ResourceLocation.parse(item.id().startsWith("#") ?
                item.id().substring(1) : item.id());
            int max = getStockMax(template, item.id(), defaultMin, defaultMax);
            stocks.put(itemId, StockEntry.full(max));
        }

        // 添加收购物品（玩家卖给村庄）
        for (ThemeTemplate.ItemReference item : template.buyItems()) {
            ResourceLocation itemId = ResourceLocation.parse(item.id().startsWith("#") ?
                item.id().substring(1) : item.id());
            int max = getStockMax(template, item.id(), defaultMin, defaultMax);
            // 收购物品初始库存为0，上限为max
            stocks.put(itemId, StockEntry.empty(max));
        }

        return stocks;
    }

    /**
     * 获取物品的库存上限
     */
    private static int getStockMax(ThemeTemplate template, String itemId, int defaultMin, int defaultMax) {
        if (template.stock().isPresent()) {
            ThemeTemplate.StockConfig stockConfig = template.stock().get();
            if (stockConfig.specific().isPresent()) {
                Map<String, ThemeTemplate.StockRange> specific = stockConfig.specific().get();
                if (specific.containsKey(itemId)) {
                    ThemeTemplate.StockRange range = specific.get(itemId);
                    // 返回范围内的随机值
                    return range.min() + (int)(Math.random() * (range.max() - range.min() + 1));
                }
            }
        }
        // 使用默认范围随机
        return defaultMin + (int)(Math.random() * (defaultMax - defaultMin + 1));
    }

    /**
     * 校验贸易站与区块数据的一致性
     * @return true 表示校验通过
     */
    public static boolean validateTradeStation(TradeStationBlockEntity station, CommercialNodeData nodeData) {
        if (station == null || nodeData == null) {
            return false;
        }

        // 校验主题名称
        ResourceLocation stationTheme = station.getVillageTheme();
        if (stationTheme == null || !stationTheme.equals(nodeData.themeName())) {
            return false;
        }

        // 校验节点ID
        UUID stationId = station.getTradeNodeId();
        if (stationId != null && !stationId.equals(nodeData.tradeNodeId())) {
            return false;
        }

        return true;
    }
}