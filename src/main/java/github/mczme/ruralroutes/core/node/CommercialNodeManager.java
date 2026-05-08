package github.mczme.ruralroutes.core.node;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import github.mczme.ruralroutes.register.RRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
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
        List<ResourceLocation> sellItems = generateSellItems(template);
        List<ResourceLocation> buyItems = generateBuyItems(template);
        List<ResourceLocation> specialties = generateSpecialties(template);
        Map<ResourceLocation, StockEntry> stocks = initializeStocks(template);

        // 将特产加入出售列表和库存
        addSpecialtiesToSellItems(sellItems, specialties);
        addSpecialtiesToStocks(stocks, template, specialties);

        long timestamp = level.getGameTime();

        CommercialNodeData data = CommercialNodeData.create(tradeNodeId, themeName, sellItems, buyItems, specialties, stocks, timestamp);

        // 存储到区块
        ChunkAccess chunk = level.getChunk(pos);
        chunk.setData(RRAttachments.COMMERCIAL_NODE.get(), data);

        RuralRoutes.LOGGER.debug("Created commercial node {} with theme {} at {}",
            tradeNodeId, themeName, pos);

        return data;
    }

    /**
     * 生成特产列表
     * @param template 主题模板
     * @return 特产ID列表（主题特产 + 随机特产）
     */
    private static List<ResourceLocation> generateSpecialties(ThemeTemplate template) {
        List<ResourceLocation> specialties = new ArrayList<>();

        // 1. 主题特产
        if (template.themeSpecialties().isPresent()) {
            List<ResourceLocation> themeSpecialties = template.themeSpecialties().get();
            for (ResourceLocation specialtyId : themeSpecialties) {
                if (!specialties.contains(specialtyId)) {
                    specialties.add(specialtyId);
                }
            }
        }

        // 2. 随机特产
        Set<Item> poolItems = TagLookupCache.getItems("#ruralroutes:pool/specialty");
        if (!poolItems.isEmpty()) {
            List<Item> poolList = new ArrayList<>(poolItems);
            Collections.shuffle(poolList);

            // 随机抽取 1-3 种
            int count = 1 + new Random().nextInt(3);
            int added = 0;
            for (Item item : poolList) {
                if (added >= count) break;

                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (!specialties.contains(itemId)) {
                    specialties.add(itemId);
                    added++;
                }
            }
        }

        return specialties;
    }

    /**
     * 将特产加入出售列表
     */
    private static void addSpecialtiesToSellItems(List<ResourceLocation> sellItems, List<ResourceLocation> specialties) {
        for (ResourceLocation specialtyId : specialties) {
            if (!sellItems.contains(specialtyId)) {
                sellItems.add(specialtyId);
            }
        }
    }

    /**
     * 将特产加入库存（作为出售物品，初始满库存）
     */
    private static void addSpecialtiesToStocks(Map<ResourceLocation, StockEntry> stocks,
            ThemeTemplate template, List<ResourceLocation> specialties) {

        // 默认库存范围
        int defaultMin = 8;
        int defaultMax = 16;

        if (template.stock().isPresent()) {
            ThemeTemplate.StockConfig stockConfig = template.stock().get();
            if (stockConfig.defaultRange().isPresent()) {
                ThemeTemplate.StockRange range = stockConfig.defaultRange().get();
                defaultMin = range.min();
                defaultMax = range.max();
            }
        }

        for (ResourceLocation specialtyId : specialties) {
            if (!stocks.containsKey(specialtyId)) {
                int max = defaultMin + (int)(Math.random() * (defaultMax - defaultMin + 1));
                stocks.put(specialtyId, StockEntry.full(max));
            }
        }
    }

    /**
     * 从主题模板生成出售物品列表
     * 将模板中的 ItemReference 转换为 ResourceLocation 列表
     */
    private static List<ResourceLocation> generateSellItems(ThemeTemplate template) {
        List<ResourceLocation> sellItems = new ArrayList<>();

        for (ThemeTemplate.ItemReference itemRef : template.sellItems()) {
            Set<Item> items = TagLookupCache.getItems(itemRef.id());
            for (Item item : items) {
                sellItems.add(BuiltInRegistries.ITEM.getKey(item));
            }
        }

        return sellItems;
    }

    /**
     * 从主题模板生成收购物品列表
     * 将模板中的 ItemReference 转换为 ResourceLocation 列表
     */
    private static List<ResourceLocation> generateBuyItems(ThemeTemplate template) {
        List<ResourceLocation> buyItems = new ArrayList<>();

        for (ThemeTemplate.ItemReference itemRef : template.buyItems()) {
            Set<Item> items = TagLookupCache.getItems(itemRef.id());
            for (Item item : items) {
                buyItems.add(BuiltInRegistries.ITEM.getKey(item));
            }
        }

        return buyItems;
    }

    /**
     * 从主题模板初始化库存
     * 处理标签展开：标签类型的物品引用会展开为具体物品列表
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
        for (ThemeTemplate.ItemReference itemRef : template.sellItems()) {
            addItemsToStocks(stocks, template, itemRef, defaultMin, defaultMax, true);
        }

        // 添加收购物品（玩家卖给村庄）
        for (ThemeTemplate.ItemReference itemRef : template.buyItems()) {
            addItemsToStocks(stocks, template, itemRef, defaultMin, defaultMax, false);
        }

        return stocks;
    }

    /**
     * 将物品引用中的物品添加到库存映射
     * 如果是标签类型，展开为具体物品列表
     * @param isSellItem true=出售物品（初始满库存），false=收购物品（叠加库存上限）
     *
     * 叠加逻辑：
     * - 出售物品：库存满（current = max）
     * - 收购物品：
     *   - 如果物品不存在：库存空（current = 0, max = 配置值）
     *   - 如果物品已存在（同时在出售清单中）：current 不变，max 增加
     */
    private static void addItemsToStocks(Map<ResourceLocation, StockEntry> stocks,
            ThemeTemplate template, ThemeTemplate.ItemReference itemRef,
            int defaultMin, int defaultMax, boolean isSellItem) {

        Set<Item> items = TagLookupCache.getItems(itemRef.id());
        for (Item item : items) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            processStockEntry(stocks, template, itemRef.id(), itemId, defaultMin, defaultMax, isSellItem);
        }
    }

    /**
     * 处理单个物品的库存条目
     */
    private static void processStockEntry(Map<ResourceLocation, StockEntry> stocks,
            ThemeTemplate template, String itemRefId, ResourceLocation itemId,
            int defaultMin, int defaultMax, boolean isSellItem) {

        int max = getStockMax(template, itemRefId, itemId, defaultMin, defaultMax);

        if (isSellItem) {
            // 出售物品：库存满
            stocks.put(itemId, StockEntry.full(max));
        } else {
            // 收购物品
            StockEntry existing = stocks.get(itemId);
            if (existing == null) {
                // 不存在：库存空
                stocks.put(itemId, StockEntry.empty(max));
            } else {
                // 已存在（同时在出售清单中）：current 不变，max 增加
                stocks.put(itemId, new StockEntry(existing.current(), existing.max() + max));
            }
        }
    }

    /**
     * 获取物品的库存上限
     * @param itemRefId 物品引用ID（可能带#前缀的标签ID或精确物品ID）
     * @param itemId 实际物品ID（不带#前缀）
     */
    private static int getStockMax(ThemeTemplate template, String itemRefId, ResourceLocation itemId,
            int defaultMin, int defaultMax) {

        if (template.stock().isPresent()) {
            ThemeTemplate.StockConfig stockConfig = template.stock().get();
            if (stockConfig.specific().isPresent()) {
                Map<String, ThemeTemplate.StockRange> specific = stockConfig.specific().get();

                // 优先匹配精确物品ID
                String itemKey = itemId.toString();
                if (specific.containsKey(itemKey)) {
                    ThemeTemplate.StockRange range = specific.get(itemKey);
                    return randomInRange(range);
                }

                // 其次匹配标签ID（带#前缀）
                String tagKeyWithPrefix = itemRefId.startsWith("#") ? itemRefId : "#" + itemRefId;
                if (specific.containsKey(tagKeyWithPrefix)) {
                    ThemeTemplate.StockRange range = specific.get(tagKeyWithPrefix);
                    return randomInRange(range);
                }

                // 最后匹配标签ID（不带#前缀）
                String tagKeyNoPrefix = itemRefId.startsWith("#") ? itemRefId.substring(1) : itemRefId;
                if (specific.containsKey(tagKeyNoPrefix)) {
                    ThemeTemplate.StockRange range = specific.get(tagKeyNoPrefix);
                    return randomInRange(range);
                }
            }
        }

        // 使用默认范围随机
        return defaultMin + (int)(Math.random() * (defaultMax - defaultMin + 1));
    }

    /**
     * 在范围内取随机值
     */
    private static int randomInRange(ThemeTemplate.StockRange range) {
        return range.min() + (int)(Math.random() * (range.max() - range.min() + 1));
    }

    /**
     * 更新区块中的商业节点数据
     * @param level 世界
     * @param pos 方块坐标
     * @param newData 新的商业节点数据
     */
    public static void updateNodeData(Level level, BlockPos pos, CommercialNodeData newData) {
        ChunkAccess chunk = level.getChunk(pos);
        chunk.setData(RRAttachments.COMMERCIAL_NODE.get(), newData);
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

    // ===== 贸易周期相关 =====

    /**
     * 检查并刷新节点数据（如果周期已过期或需要刷新）
     * @param level 世界
     * @param pos 贸易站位置
     * @param nodeData 节点数据
     * @return 更新后的节点数据（如果刷新了则返回新数据，否则返回原数据）
     */
    public static CommercialNodeData checkAndRefreshCycle(ServerLevel level, BlockPos pos, CommercialNodeData nodeData) {
        CycleManager cycleManager = CycleManager.get(level);

        if (cycleManager.needsRefresh(nodeData.refreshTimestamp())) {
            // 确保市场状态与当前周期同步
            cycleManager.getOrInitMarketState();
            cycleManager.markRefreshed();
            RuralRoutes.LOGGER.debug("Refreshing commercial node {} at cycle {}",
                nodeData.tradeNodeId(), cycleManager.getCurrentCycle());
            return refreshNodeData(level, pos, nodeData, level.getGameTime());
        }

        return nodeData;
    }

    /**
     * 刷新节点数据
     * 恢复库存到基准值，重新生成特产，更新刷新时间戳
     */
    private static CommercialNodeData refreshNodeData(ServerLevel level, BlockPos pos,
            CommercialNodeData oldData, long currentGameTime) {

        ThemeTemplate template = ThemeManager.INSTANCE.getTheme(oldData.themeName());
        if (template == null) {
            RuralRoutes.LOGGER.warn("Cannot refresh node: theme {} not found", oldData.themeName());
            return oldData;
        }

        // 重新初始化库存（全量恢复）
        Map<ResourceLocation, StockEntry> newStocks = initializeStocks(template);

        // 重新生成特产
        List<ResourceLocation> newSpecialties = generateSpecialties(template);

        // 重新生成出售列表（包含新特产）
        List<ResourceLocation> newSellItems = generateSellItems(template);
        addSpecialtiesToSellItems(newSellItems, newSpecialties);
        addSpecialtiesToStocks(newStocks, template, newSpecialties);

        // 创建新数据，更新时间戳
        CommercialNodeData newData = CommercialNodeData.create(
            oldData.tradeNodeId(),
            oldData.themeName(),
            newSellItems,
            oldData.buyItems(),
            newSpecialties,
            newStocks,
            currentGameTime
        );

        // 存储到区块
        ChunkAccess chunk = level.getChunk(pos);
        chunk.setData(RRAttachments.COMMERCIAL_NODE.get(), newData);

        RuralRoutes.LOGGER.debug("Refreshed node {} with {} stock entries and {} specialties",
                newData.tradeNodeId(), newStocks.size(), newSpecialties.size());

        return newData;
    }
}