package github.mczme.ruralroutes.core.node;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.market.MarketContext;
import github.mczme.ruralroutes.core.market.MarketState;
import github.mczme.ruralroutes.core.market.MarketStockAdjustment;
import github.mczme.ruralroutes.core.market.MarketStateResolver;
import github.mczme.ruralroutes.core.theme.ResolvedTheme;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import github.mczme.ruralroutes.register.RRAttachments;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
     * 当前周期内实际入选的交易物品。
     * sourceRefId 保留来源键，便于 stock.specific 等规则继续按原引用或候选组匹配。
     */
    private record SelectedTradeItem(
        String sourceRefId,
        ResourceLocation itemId,
        ItemStack displayStack,
        TradeItemKey tradeItemKey
    ) {}

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
        ResolvedTheme template = ThemeManager.INSTANCE.getTheme(themeName);
        if (template == null) {
            RuralRoutes.LOGGER.warn("Cannot create commercial node: theme {} not found", themeName);
            return null;
        }

        UUID tradeNodeId = UUID.randomUUID();
        List<SelectedTradeItem> selectedSellItems = selectTradeItems(template.sellItems());
        List<SelectedTradeItem> selectedBuyItems = selectTradeItems(template.buyItems());
        List<NodeTradeEntry> sellItems = toTradeEntries(selectedSellItems);
        List<NodeTradeEntry> buyItems = toTradeEntries(selectedBuyItems);
        List<NodeTradeEntry> specialties = generateSpecialties(template);
        MarketState marketState = getCurrentMarketState(level);
        Map<TradeItemKey, NodeStockEntry> stocks = initializeStocks(
            template, selectedSellItems, selectedBuyItems, marketState);

        // 将特产加入出售列表和库存
        addSpecialtiesToSellItems(sellItems, specialties);
        addSpecialtiesToStocks(stocks, template, specialties, marketState);

        long timestamp = level instanceof ServerLevel serverLevel 
            ? CycleManager.getEffectiveTime(serverLevel) 
            : level.getGameTime();

        CommercialNodeData data = CommercialNodeData.create(
            tradeNodeId,
            themeName,
            sellItems,
            buyItems,
            specialties,
            stocks,
            timestamp
        );

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
    private static List<NodeTradeEntry> generateSpecialties(ResolvedTheme template) {
        List<NodeTradeEntry> specialties = new ArrayList<>();
        Set<ResourceLocation> seen = new LinkedHashSet<>();

        // 1. 主题特产
        if (template.themeSpecialtyItems().isPresent()) {
            List<ThemeTemplate.ItemReference> themeSpecialties = template.themeSpecialtyItems().get();
            for (ThemeTemplate.ItemReference specialtyRef : themeSpecialties) {
                if (!specialtyRef.isExactItem()) {
                    RuralRoutes.LOGGER.warn("Theme specialty must resolve to an exact item, got {}", specialtyRef.debugLabel());
                    continue;
                }
                ResourceLocation specialtyId = ResourceLocation.parse(specialtyRef.itemId());
                ItemStack displayStack = createItemStack(specialtyRef.itemEntries().get(0));
                if (seen.add(specialtyId)) {
                    specialties.add(NodeTradeEntry.of(
                        specialtyRef.sourceKey(),
                        specialtyId,
                        displayStack
                    ));
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
                if (seen.add(itemId)) {
                    specialties.add(NodeTradeEntry.of(itemId.toString(), itemId));
                    added++;
                }
            }
        }

        return specialties;
    }

    /**
     * 将特产加入出售列表
     */
    private static void addSpecialtiesToSellItems(List<NodeTradeEntry> sellItems,
            List<NodeTradeEntry> specialties) {
        for (NodeTradeEntry specialty : specialties) {
            boolean exists = sellItems.stream().anyMatch(entry -> entry.itemId().equals(specialty.itemId()));
            if (!exists) {
                sellItems.add(NodeTradeEntry.of(
                    specialty.sourceKey(),
                    specialty.stockKey()
                ));
            }
        }
    }

    /**
     * 将特产加入库存（作为出售物品，初始满库存）
     */
    private static void addSpecialtiesToStocks(Map<TradeItemKey, NodeStockEntry> stocks,
            ResolvedTheme template, List<NodeTradeEntry> specialties, MarketState marketState) {

        // 默认库存范围
        int defaultMin = 8;
        int defaultMax = 16;
        MarketContext marketContext = MarketContext.fromTheme(template);

        if (template.stock().isPresent()) {
            ThemeTemplate.StockConfig stockConfig = template.stock().get();
            if (stockConfig.defaultRange().isPresent()) {
                ThemeTemplate.StockRange range = stockConfig.defaultRange().get();
                defaultMin = range.min();
                defaultMax = range.max();
            }
        }

        for (NodeTradeEntry specialty : specialties) {
            ResourceLocation specialtyId = specialty.itemId();
            TradeItemKey stockKey = specialty.tradeItemKey();
            if (!stocks.containsKey(stockKey)) {
                int baseMax = getBaseStockMax(template, specialtyId.toString(), specialtyId, defaultMin, defaultMax, true);
                MarketStockAdjustment stockAdjustment = resolveStockAdjustment(
                    marketState,
                    marketContext,
                    specialty.tradeItemKey(),
                    Optional.of(specialty.sourceKey()));
                int sellBase = stockAdjustment.applySellBase(baseMax);
                stocks.put(stockKey, NodeStockEntry.full(specialty.displayStackOrDefault(), sellBase));
            }
        }
    }

    /**
     * 从主题模板候选中选出当前周期的实际交易物品。
     * 规则：
     * 1. 先展开引用为候选物品集合
     * 2. 若引用声明了 pick，则从单引用或候选组展开后的全集中随机抽取指定数量
     * 3. 不同引用命中同一物品时，保留先出现的那条引用
     */
    private static List<SelectedTradeItem> selectTradeItems(List<ThemeTemplate.ItemReference> itemRefs) {
        Map<String, SelectedTradeItem> selected = new LinkedHashMap<>();
        Random random = new Random();

        for (ThemeTemplate.ItemReference itemRef : itemRefs) {
            List<SelectedTradeItem> candidates = resolveItemCandidates(itemRef);
            if (candidates.isEmpty()) {
                continue;
            }

            List<SelectedTradeItem> chosen = chooseItems(itemRef, candidates, random);
            for (SelectedTradeItem item : chosen) {
                selected.putIfAbsent(item.tradeItemKey().canonicalKey(), item);
            }
        }

        return List.copyOf(selected.values());
    }

    private static List<SelectedTradeItem> resolveItemCandidates(ThemeTemplate.ItemReference itemRef) {
        Map<String, SelectedTradeItem> candidates = new LinkedHashMap<>();
        for (ThemeTemplate.ItemEntry itemEntry : itemRef.itemEntries()) {
            ItemStack displayStack = createItemStack(itemEntry);
            for (Item item : TagLookupCache.getItems(itemEntry.ref())) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (itemId != null) {
                    SelectedTradeItem selected = new SelectedTradeItem(itemRef.sourceKey(), itemId, displayStack.copy(), TradeItemKey.from(displayStack));
                    candidates.putIfAbsent(selected.tradeItemKey().canonicalKey(), selected);
                }
            }
        }

        if (candidates.isEmpty()) {
            RuralRoutes.LOGGER.warn("Theme item reference resolved to empty set: {}", itemRef.debugLabel());
        }

        return candidates.values().stream()
            .sorted(Comparator.comparing(item -> item.itemId().toString()))
            .toList();
    }

    private static List<SelectedTradeItem> chooseItems(ThemeTemplate.ItemReference itemRef,
            List<SelectedTradeItem> candidates, Random random) {

        if (itemRef.pick().isEmpty() || itemRef.pick().get() >= candidates.size()) {
            return candidates;
        }

        List<SelectedTradeItem> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, random);

        List<SelectedTradeItem> chosen = new ArrayList<>(shuffled.subList(0, itemRef.pick().get()));
        chosen.sort(Comparator.comparing(item -> item.itemId().toString()));
        return chosen;
    }

    private static List<NodeTradeEntry> toTradeEntries(List<SelectedTradeItem> selectedItems) {
        List<NodeTradeEntry> itemIds = new ArrayList<>(selectedItems.size());
        for (SelectedTradeItem selectedItem : selectedItems) {
            itemIds.add(NodeTradeEntry.of(
                selectedItem.sourceRefId(),
                selectedItem.itemId(),
                selectedItem.displayStack()
            ));
        }
        return itemIds;
    }

    /**
     * 从主题模板初始化库存。
     * 仅为当前周期实际入选的物品建立库存条目。
     */
    private static Map<TradeItemKey, NodeStockEntry> initializeStocks(ResolvedTheme template,
            List<SelectedTradeItem> selectedSellItems, List<SelectedTradeItem> selectedBuyItems,
            MarketState marketState) {
        Map<TradeItemKey, NodeStockPlan> plans = new HashMap<>();
        Map<TradeItemKey, ItemStack> stockStacks = new HashMap<>();

        // 默认库存范围
        int defaultMin = 8;
        int defaultMax = 16;
        MarketContext marketContext = MarketContext.fromTheme(template);

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
        for (SelectedTradeItem item : selectedSellItems) {
            stockStacks.putIfAbsent(item.tradeItemKey(), item.displayStack());
            processStockEntry(plans, template, marketState, marketContext,
                item.sourceRefId(), item.itemId(), item.tradeItemKey(), defaultMin, defaultMax, true);
        }

        // 添加收购物品（玩家卖给村庄）
        for (SelectedTradeItem item : selectedBuyItems) {
            stockStacks.putIfAbsent(item.tradeItemKey(), item.displayStack());
            processStockEntry(plans, template, marketState, marketContext,
                item.sourceRefId(), item.itemId(), item.tradeItemKey(), defaultMin, defaultMax, false);
        }

        Map<TradeItemKey, NodeStockEntry> stocks = new HashMap<>();
        for (Map.Entry<TradeItemKey, NodeStockPlan> entry : plans.entrySet()) {
            ItemStack stockStack = stockStacks.getOrDefault(entry.getKey(), entry.getKey().asItemStack());
            NodeStockEntry stockEntry = entry.getValue().toStockEntry(stockStack);
            if (stockEntry.max() > 0) {
                stocks.put(entry.getKey(), stockEntry);
            }
        }
        return stocks;
    }

    /**
     * 处理单个物品的库存条目
     */
    private static void processStockEntry(Map<TradeItemKey, NodeStockPlan> plans,
            ResolvedTheme template, MarketState marketState, MarketContext marketContext,
            String itemRefId, ResourceLocation itemId, TradeItemKey stockKey,
            int defaultMin, int defaultMax, boolean isSellItem) {

        int baseAmount = getBaseStockMax(template, itemRefId, itemId, defaultMin, defaultMax, isSellItem);
        MarketStockAdjustment stockAdjustment = resolveStockAdjustment(
            marketState,
            marketContext,
            stockKey,
            Optional.ofNullable(itemRefId));
        NodeStockPlan existing = plans.getOrDefault(stockKey, NodeStockPlan.EMPTY);

        if (isSellItem) {
            int sellBase = stockAdjustment.applySellBase(baseAmount);
            plans.put(stockKey, existing.addSell(sellBase));
        } else {
            int buyBase = stockAdjustment.applyBuyBase(baseAmount);
            plans.put(stockKey, existing.addBuy(buyBase));
        }
    }

    /**
     * 获取物品的库存上限
     * @param itemRefId 物品引用ID（可能带#前缀的标签ID或精确物品ID）
     * @param itemId 实际物品ID（不带#前缀）
     */
    private static int getBaseStockMax(ResolvedTheme template, String itemRefId, ResourceLocation itemId,
            int defaultMin, int defaultMax, boolean isSellItem) {

        if (template.stock().isPresent()) {
            ThemeTemplate.StockConfig stockConfig = template.stock().get();
            if (stockConfig.targetEntries().isPresent()) {
                ThemeTemplate.StockTarget target = stockConfig.resolveTarget(itemRefId, itemId).orElse(null);
                if (target != null) {
                    ThemeTemplate.StockRange range = target.shared().orElse(null);
                    if (range == null) {
                        range = isSellItem
                            ? target.sell().orElse(target.buy().orElse(null))
                            : target.buy().orElse(target.sell().orElse(null));
                    }
                    if (range != null) {
                        return randomInRange(range);
                    }
                }
            }
            if (stockConfig.specific().isPresent()) {
                Optional<ThemeTemplate.StockRange> resolved = stockConfig.resolveSpecific(itemRefId, itemId);
                if (resolved.isPresent()) {
                    return randomInRange(resolved.get());
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

    private static ItemStack createItemStack(ThemeTemplate.ItemEntry itemEntry) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemEntry.ref()));
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item);
        itemEntry.components().ifPresent(components -> applyComponents(stack, components));
        return stack;
    }

    private static void applyComponents(ItemStack stack, Map<String, String> components) {
        for (Map.Entry<String, String> entry : components.entrySet()) {
            ResourceLocation componentId = ResourceLocation.tryParse(entry.getKey());
            if (componentId == null) {
                continue;
            }

            DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(componentId).orElse(null);
            if (type == null) {
                continue;
            }

            try {
                JsonElement json = JsonParser.parseString(entry.getValue());
                Object value = type.codec().parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> RuralRoutes.LOGGER.warn(
                        "Failed to parse component {} for {}: {}",
                        componentId, stack.getItem(), err))
                    .orElse(null);
                if (value != null) {
                    setComponent(stack, type, value);
                }
            } catch (Exception e) {
                RuralRoutes.LOGGER.warn("Failed to apply component {} to {}: {}",
                    componentId, stack.getItem(), e.getMessage());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setComponent(ItemStack stack, DataComponentType type, Object value) {
        stack.set(type, value);
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
            cycleManager.getOrInitMarketState();
            cycleManager.markRefreshed();
            RuralRoutes.LOGGER.debug("Refreshing commercial node {} at cycle {}",
                nodeData.tradeNodeId(), cycleManager.getCurrentCycle());
            return refreshNodeData(level, pos, nodeData, CycleManager.getEffectiveTime(level));
        }

        return nodeData;
    }

    /**
     * 刷新节点数据
     * 恢复库存到基准值，重新生成特产，更新刷新时间戳
     */
    private static CommercialNodeData refreshNodeData(ServerLevel level, BlockPos pos,
            CommercialNodeData oldData, long currentTimestamp) {

        ResolvedTheme template = ThemeManager.INSTANCE.getTheme(oldData.themeName());
        if (template == null) {
            RuralRoutes.LOGGER.warn("Cannot refresh node: theme {} not found", oldData.themeName());
            return oldData;
        }

        List<SelectedTradeItem> selectedSellItems = selectTradeItems(template.sellItems());
        List<SelectedTradeItem> selectedBuyItems = selectTradeItems(template.buyItems());
        List<NodeTradeEntry> newSellItems = toTradeEntries(selectedSellItems);
        List<NodeTradeEntry> newBuyItems = toTradeEntries(selectedBuyItems);
        MarketState marketState = CycleManager.get(level).getOrInitMarketState();

        // 重新初始化库存（全量恢复）
        Map<TradeItemKey, NodeStockEntry> newStocks = initializeStocks(
            template, selectedSellItems, selectedBuyItems, marketState);

        // 重新生成特产
        List<NodeTradeEntry> newSpecialties = generateSpecialties(template);

        // 重新生成出售列表（包含新特产）
        addSpecialtiesToSellItems(newSellItems, newSpecialties);
        addSpecialtiesToStocks(newStocks, template, newSpecialties, marketState);

        // 创建新数据，更新时间戳
        CommercialNodeData newData = CommercialNodeData.create(
            oldData.tradeNodeId(),
            oldData.themeName(),
            newSellItems,
            newBuyItems,
            newSpecialties,
            newStocks,
            currentTimestamp
        );

        // 存储到区块
        ChunkAccess chunk = level.getChunk(pos);
        chunk.setData(RRAttachments.COMMERCIAL_NODE.get(), newData);

        RuralRoutes.LOGGER.debug("Refreshed node {} with {} stock entries and {} specialties",
                newData.tradeNodeId(), newStocks.size(), newSpecialties.size());

        return newData;
    }

    private static MarketState getCurrentMarketState(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            CycleManager cycleManager = CycleManager.get(serverLevel);
            cycleManager.updateCurrentCycle(serverLevel);
            return cycleManager.getOrInitMarketState();
        }
        return MarketState.empty(-1);
    }

    private static MarketStockAdjustment resolveStockAdjustment(
            MarketState marketState,
            MarketContext marketContext,
            TradeItemKey itemKey,
            Optional<String> sourceKey) {
        return MarketStateResolver.resolveStockAdjustment(marketState, marketContext, itemKey, sourceKey);
    }
}
