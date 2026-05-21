package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.advancement.RRPlayerProgressState;
import github.mczme.ruralroutes.advancement.trigger.OpenTradeStationTrigger.TradeStationEvent;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.core.trade.CoinExchangeContract;
import github.mczme.ruralroutes.core.trade.TradeContractExecutor;
import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.core.trade.TradePricingService;
import github.mczme.ruralroutes.core.trade.TradeResult;
import github.mczme.ruralroutes.core.trade.TradeSide;
import github.mczme.ruralroutes.core.trade.TradeItemKey;
import github.mczme.ruralroutes.core.trade.CurrencyBasketComposer;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import github.mczme.ruralroutes.menu.container.TradeDisplayContainer;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import github.mczme.ruralroutes.network.packet.CoinExchangeStatePayload;
import github.mczme.ruralroutes.network.packet.TradeFeedbackPayload;
import github.mczme.ruralroutes.network.packet.PendingTradeSyncPayload;
import github.mczme.ruralroutes.network.packet.TradeRequestPayload;
import github.mczme.ruralroutes.network.packet.TradeSlotSyncPayload;
import github.mczme.ruralroutes.register.RRItemTags;
import github.mczme.ruralroutes.register.RRItems;
import github.mczme.ruralroutes.register.RRMenuTypes;
import github.mczme.ruralroutes.register.RRAttachments;
import github.mczme.ruralroutes.register.RRCriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 贸易站 GUI 菜单
 *
 * 槽位布局：
 * - 出售槽位 (sellSlots): 玩家可从村庄购买物品（村庄出售）
 * - 收购槽位 (buySlots): 玩家可向村庄出售物品（村庄收购）
 * - 暂存区：支持多物品暂存
 *
 * 使用 TradeSlot 实现不可交互的展示槽位
 * 槽位数量动态创建，基于 CommercialNodeData 中的 sellItems/buyItems 列表
 */
public class TradeStationMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final Player player;
    private TradeDisplayContainer sellContainer;
    private TradeDisplayContainer buyContainer;
    private final List<TradeSlot> sellSlots;
    private final List<TradeSlot> buySlots;

    // 多物品暂存区
    private List<PendingTradeSlot> pendingSlots = new ArrayList<>();
    private boolean isBuyTrade = true; // true=购买交易，false=出售交易

    // 当前周期索引（用于跨周期检测）
    private long currentCycleIndex;
    private ClientTradeFeedback activeTradeFeedback;
    private long tradeFeedbackExpireAtMs;
    private ClientCurrencyWallet playerCurrencyWallet = ClientCurrencyWallet.EMPTY;
    private ClientCurrencyWallet villageCurrencyWallet = ClientCurrencyWallet.EMPTY;


    // 常量
    public static final int SLOT_SIZE = 18;
    public static final int SLOT_SPACING = 2;

    public TradeStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos(), data.readVarInt(), data.readVarInt());
    }

    public TradeStationMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        this(containerId, playerInventory, blockPos, 0, 0);
    }

    /**
     * 完整构造函数
     * @param sellSlotCount 出售槽位数量（客户端从服务端接收）
     * @param buySlotCount 收购槽位数量（客户端从服务端接收）
     */
    public TradeStationMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
            int sellSlotCount, int buySlotCount) {
        super(RRMenuTypes.TRADE_STATION.get(), containerId);
        this.blockPos = blockPos;
        this.player = playerInventory.player;

        // 初始化槽位列表
        this.sellSlots = new ArrayList<>();
        this.buySlots = new ArrayList<>();

        // 从区块数据查询并初始化
        Level level = playerInventory.player.level();
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(level, blockPos);

        // 只有服务端才从节点数据初始化，客户端等待服务端同步
        if (player instanceof ServerPlayer && nodeData != null) {
            initializeFromNodeData(nodeData);
        } else {
            // 客户端：使用服务端传递的槽位数量创建空槽位
            initializeEmptySlots(sellSlotCount, buySlotCount);
        }
    }

    /**
     * 从商业节点数据初始化槽位
     */
    private void initializeFromNodeData(CommercialNodeData nodeData) {
        sellSlots.clear();
        buySlots.clear();
        slots.removeIf(slot -> slot instanceof TradeSlot);

        List<CommercialNodeData.NodeTradeEntry> sellItems = nodeData.sellItems();
        List<CommercialNodeData.NodeTradeEntry> buyItems = nodeData.buyItems();
        Map<TradeItemKey, StockEntry> stocks = nodeData.stocks();

        // 获取主题模板
        ThemeTemplate theme = ThemeManager.INSTANCE.getTheme(nodeData.themeName());

        // 计算过滤货币后的实际槽位数量
        int sellSlotCount = countNonCurrencyItems(toItemIds(sellItems));
        int buySlotCount = countNonCurrencyItems(toItemIds(buyItems));

        // 创建容器
        this.sellContainer = new TradeDisplayContainer(Math.max(1, sellSlotCount));
        this.buyContainer = new TradeDisplayContainer(Math.max(1, buySlotCount));

        // 创建出售槽位（村庄卖给玩家），过滤货币物品
        int sellStartX = 10;
        int sellStartY = 22;
        int sellSlotIndex = 0;
        for (int i = 0; i < sellItems.size(); i++) {
            CommercialNodeData.NodeTradeEntry entryData = sellItems.get(i);
            ResourceLocation itemId = entryData.itemId();
            ItemStack displayStack = entryData.displayStackOrDefault();

            // 跳过货币物品
            if (isCurrencyItem(displayStack)) {
                continue;
            }

            StockEntry entry = stocks.get(entryData.tradeItemKey());
            int stockCount = entry != null ? entry.current() : 0;

            int col = sellSlotIndex / 2;
            int row = sellSlotIndex % 2;
            int x = sellStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = sellStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(sellContainer, sellSlotIndex, x, y);
            slot.setItemId(itemId);
            slot.setDisplayStack(displayStack);
            slot.setBaseStock(stockCount);
            slot.setPrice(calculateSellPrice(itemId, entryData.sourceKey(), displayStack));
            slot.setIsBuy(true); // 此槽位用于玩家购买

            // 查找匹配的契约
            TradeContractMatch match = findMatchingContract(theme, entryData, TradeSide.SELL_TO_PLAYER);
            if (match != null) {
                slot.setTradeType(match.tradeType);
                slot.setPriceStacks(match.priceStacks);
                slot.setInputStacks(match.inputStacks);
            } else {
                // 默认货币篮
                List<ItemStack> priceStacks = calculatePriceStacks(itemId, entryData.sourceKey(), displayStack, TradeSide.SELL_TO_PLAYER);
                slot.setPriceStacks(priceStacks);
                slot.setTradeType(TradeContractType.CURRENCY_BASKET_DYNAMIC);
            }

            sellSlots.add(slot);
            addSlot(slot);
            sellSlotIndex++;
        }

        // 创建收购槽位（村庄收购玩家物品），过滤货币物品
        int buyStartX = 10;
        int buyStartY = 62;
        int buySlotIndex = 0;
        for (int i = 0; i < buyItems.size(); i++) {
            CommercialNodeData.NodeTradeEntry entryData = buyItems.get(i);
            ResourceLocation itemId = entryData.itemId();
            ItemStack displayStack = entryData.displayStackOrDefault();

            // 跳过货币物品
            if (isCurrencyItem(displayStack)) {
                continue;
            }

            StockEntry entry = stocks.get(entryData.tradeItemKey());
            int currentStock = entry != null ? entry.current() : 0;
            int maxStock = entry != null ? entry.max() : 0;

            int col = buySlotIndex / 2;
            int row = buySlotIndex % 2;
            int x = buyStartX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = buyStartY + row * (SLOT_SIZE + SLOT_SPACING);

            TradeSlot slot = new TradeSlot(buyContainer, buySlotIndex, x, y);
            slot.setItemId(itemId);
            slot.setDisplayStack(displayStack);
            slot.setBaseStock(currentStock);  // 已收购数量
            slot.setMaxStock(maxStock);       // 收购上限
            slot.setPrice(calculateBuyPrice(itemId, entryData.sourceKey(), displayStack));
            slot.setIsBuy(false); // 此槽位用于玩家出售

            // 查找匹配的契约
            TradeContractMatch match = findMatchingContract(theme, entryData, TradeSide.BUY_FROM_PLAYER);
            if (match != null) {
                slot.setTradeType(match.tradeType);
                slot.setPriceStacks(match.priceStacks);
                slot.setInputStacks(match.inputStacks);
            } else {
                // 默认货币篮
                List<ItemStack> priceStacks = calculatePriceStacks(itemId, entryData.sourceKey(), displayStack, TradeSide.BUY_FROM_PLAYER);
                slot.setPriceStacks(priceStacks);
                slot.setTradeType(TradeContractType.CURRENCY_BASKET_DYNAMIC);
            }

            buySlots.add(slot);
            addSlot(slot);
            buySlotIndex++;
        }

        // 初始化周期索引（服务端）
        if (player instanceof ServerPlayer serverPlayer) {
            CycleManager cycleManager = CycleManager.get(serverPlayer.serverLevel());
            this.currentCycleIndex = cycleManager.getCycleIndex(serverPlayer.serverLevel());
        }
    }

    /**
     * 契约匹配结果
     */
    private record TradeContractMatch(
        TradeContractType tradeType,
        List<ItemStack> priceStacks,
        List<ItemStack> inputStacks
    ) {}

    /**
     * 查找匹配的契约条目
     * @param theme 主题模板
     * @param itemId 物品ID
     * @param side 交易方向
     * @return 匹配结果，如果没有匹配则返回 null
     */
    private TradeContractMatch findMatchingContract(ThemeTemplate theme, CommercialNodeData.NodeTradeEntry entryData, TradeSide side) {
        if (theme == null || theme.tradeContracts().isEmpty()) {
            return null;
        }

        ResourceLocation itemId = entryData.itemId();
        String itemIdStr = itemId.toString();
        ItemStack contractStack = entryData.displayStackOrDefault();

        for (ThemeTemplate.TradeContractEntry entry : theme.tradeContracts().get()) {
            if (entry instanceof ThemeTemplate.FixedTradeEntry fixedEntry) {
                // 固定交换：检查 outputs 是否包含该物品（村庄出售给玩家）
                for (ThemeTemplate.OutputEntry output : fixedEntry.outputs()) {
                    if (output.item().equals(itemIdStr)) {
                        // 找到匹配的固定交换
                        List<ItemStack> inputStacks = new ArrayList<>();
                        for (ThemeTemplate.InputEntry input : fixedEntry.inputs()) {
                            ItemStack stack = createItemStack(ResourceLocation.parse(input.item()));
                            stack.setCount(input.count());
                            inputStacks.add(stack);
                        }
                        return new TradeContractMatch(TradeContractType.FIXED, List.of(), inputStacks);
                    }
                }
            } else if (entry instanceof ThemeTemplate.CurrencyBasketEntry basketEntry) {
                // 动态货币篮：检查 side 和 items
                if (basketEntry.side() == side) {
                    for (String itemPattern : basketEntry.items()) {
                        if (itemPattern.equals("*") || TagLookupCache.matchesItem(contractStack, itemPattern)) {
                            int price = calculatePriceForContract(itemId, entryData.sourceKey(), contractStack, side);
                            List<ItemStack> priceStacks = CurrencyBasketComposer.compose(
                                price,
                                basketEntry.acceptedCurrencies(),
                                basketEntry.composition(),
                                side
                            );
                            return new TradeContractMatch(TradeContractType.CURRENCY_BASKET_DYNAMIC, priceStacks, List.of());
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 计算契约价格（用于货币篮）
     */
    private int calculatePriceForContract(ResourceLocation itemId, String sourceKey, ItemStack displayStack, TradeSide side) {
        if (side == TradeSide.SELL_TO_PLAYER) {
            return calculateSellPrice(itemId, sourceKey, displayStack);
        } else {
            return calculateBuyPrice(itemId, sourceKey, displayStack);
        }
    }

    /**
     * 检查物品是否为货币（静态方法，供外部调用）
     */
    public static boolean isCurrencyItem(ItemStack stack) {
        return stack.is(RRItemTags.CURRENCY) || stack.is(RRItemTags.CURRENCY_BASE);
    }

    /**
     * 计算非货币物品数量（静态方法，供外部调用）
     */
    public static int countNonCurrencyItems(List<ResourceLocation> itemIds) {
        int count = 0;
        for (ResourceLocation itemId : itemIds) {
            ItemStack stack = createItemStack(itemId);
            if (!isCurrencyItem(stack)) {
                count++;
            }
        }
        return count;
    }

    private static List<ResourceLocation> toItemIds(List<CommercialNodeData.NodeTradeEntry> entries) {
        List<ResourceLocation> itemIds = new ArrayList<>(entries.size());
        for (CommercialNodeData.NodeTradeEntry entry : entries) {
            itemIds.add(entry.itemId());
        }
        return itemIds;
    }

    /**
     * 初始化空槽位（客户端使用服务端传递的槽位数量）
     */
    private void initializeEmptySlots(int sellSlotCount, int buySlotCount) {
        this.sellContainer = new TradeDisplayContainer(Math.max(1, sellSlotCount));
        this.buyContainer = new TradeDisplayContainer(Math.max(1, buySlotCount));

        int sellStartX = 10;
        int sellStartY = 22;
        for (int i = 0; i < sellSlotCount; i++) {
            TradeSlot slot = createTradeSlot(sellContainer, i, sellStartX, sellStartY, true);
            sellSlots.add(slot);
            addSlot(slot);
        }

        int buyStartX = 10;
        int buyStartY = 62;
        for (int i = 0; i < buySlotCount; i++) {
            TradeSlot slot = createTradeSlot(buyContainer, i, buyStartX, buyStartY, false);
            buySlots.add(slot);
            addSlot(slot);
        }
    }

    /**
     * 从资源位置创建物品堆
     */
    private static ItemStack createItemStack(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    /**
     * 计算出售价格（村庄卖给玩家）
     * 使用统一定价服务
     */
    public int calculateSellPrice(ResourceLocation itemId, String sourceKey, ItemStack displayStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        ItemStack stack = displayStack != null && !displayStack.isEmpty() ? displayStack.copy() : createItemStack(itemId);
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            return 0;
        }

        return TradePricingService.calculateFinalPrice(
            serverPlayer.serverLevel(),
            nodeData,
            stack,
            TradeSide.SELL_TO_PLAYER,
            java.util.Optional.ofNullable(sourceKey)
        );
    }

    /**
     * 计算收购价格（玩家卖给村庄）
     * 使用统一定价服务
     */
    public int calculateBuyPrice(ResourceLocation itemId, String sourceKey, ItemStack displayStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        ItemStack stack = displayStack != null && !displayStack.isEmpty() ? displayStack.copy() : createItemStack(itemId);
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            return 0;
        }

        return TradePricingService.calculateFinalPrice(
            serverPlayer.serverLevel(),
            nodeData,
            stack,
            TradeSide.BUY_FROM_PLAYER,
            java.util.Optional.ofNullable(sourceKey)
        );
    }

    /**
     * 计算货币篮报价（使用默认配置）
     * 仅在 findMatchingContract 未找到匹配时调用
     */
    public List<ItemStack> calculatePriceStacks(ResourceLocation itemId, String sourceKey, ItemStack displayStack, TradeSide side) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return List.of();
        }

        ItemStack stack = displayStack != null && !displayStack.isEmpty() ? displayStack.copy() : createItemStack(itemId);
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            return List.of();
        }

        int price = TradePricingService.calculateFinalPrice(
            serverPlayer.serverLevel(),
            nodeData,
            stack,
            side,
            java.util.Optional.ofNullable(sourceKey)
        );

        // 使用默认货币篮配置：仅基础货币
        List<String> acceptedCurrencies = List.of("#ruralroutes:currency_base");
        ThemeTemplate.CompositionStrategy strategy = ThemeTemplate.CompositionStrategy.LARGEST_FIRST;

        return CurrencyBasketComposer.compose(price, acceptedCurrencies, strategy, side);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(blockPos) instanceof TradeStationBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 不支持快速移动
        return ItemStack.EMPTY;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * 获取出售槽位列表
     */
    public List<TradeSlot> getSellSlots() {
        return sellSlots;
    }

    /**
     * 获取收购槽位列表
     */
    public List<TradeSlot> getBuySlots() {
        return buySlots;
    }

    /**
     * 获取暂存区槽位列表
     */
    public List<PendingTradeSlot> getPendingSlots() {
        return pendingSlots;
    }

    /**
     * 是否为购买交易
     */
    public boolean isBuyTrade() {
        return isBuyTrade;
    }

    /**
     * 暂存区是否有待处理交易
     */
    public boolean hasPendingTrade() {
        return !pendingSlots.isEmpty();
    }

    /**
     * 添加物品到暂存区
     * @param isBuy true=购买交易（点击出售槽位），false=出售交易（点击收购槽位）
     * @param slotIndex 来源槽位索引
     */
    public void addTradeEntry(boolean isBuy, int slotIndex, int requestedAmount) {
        // 获取来源槽位
        List<TradeSlot> sourceSlots = isBuy ? sellSlots : buySlots;
        if (slotIndex < 0 || slotIndex >= sourceSlots.size()) return;

        TradeSlot sourceSlot = sourceSlots.get(slotIndex);
        // 收购区（玩家卖给村庄）只检查 displayStack，出售区检查完整 isEmpty
        if (isBuy) {
            if (sourceSlot.isEmpty()) return;
        } else {
            if (sourceSlot.getDisplayStack().isEmpty()) return;
        }

        int toAdd = requestedAmount == TradeRequestPayload.ALL_AMOUNT
            ? sourceSlot.getStockCount()
            : Math.max(0, requestedAmount);
        int actualAdd = sourceSlot.addPending(toAdd, isBuy);
        if (actualAdd <= 0) return;

        ResourceLocation itemId = sourceSlot.getItemId();

        // 查找是否已有该物品的 PendingTradeSlot
        PendingTradeSlot existingSlot = null;
        for (PendingTradeSlot pending : pendingSlots) {
            if (pending.getItemId() != null && pending.getItemId().equals(itemId)
                && pending.getSourceSlotIndex() == slotIndex) {
                existingSlot = pending;
                break;
            }
        }

        if (existingSlot != null) {
            // 累加数量：增加 baseStock
            existingSlot.setBaseStock(existingSlot.getBaseStock() + actualAdd);
        } else {
            // 新建 PendingTradeSlot
            PendingTradeSlot newSlot = new PendingTradeSlot(
                new TradeDisplayContainer(1), pendingSlots.size(), 0, 0);
            newSlot.setItemId(itemId);
            newSlot.setDisplayStack(sourceSlot.getDisplayStack().copy());
            newSlot.setBaseStock(actualAdd);
            newSlot.setPrice(sourceSlot.getPrice());
            newSlot.setIsBuy(isBuy);
            newSlot.setSource(slotIndex, isBuy);
            // 复制契约信息
            newSlot.setTradeType(sourceSlot.getTradeType());
            newSlot.setPriceStacks(sourceSlot.getPriceStacks());
            newSlot.setInputStacks(sourceSlot.getInputStacks());
            pendingSlots.add(newSlot);
        }

        // 6. 同步到客户端
        syncPendingTradeToClient();
    }

    /**
     * 移除暂存区条目（点击暂存区物品）
     */
    public void removeTradeEntry(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= pendingSlots.size()) return;

        PendingTradeSlot pending = pendingSlots.get(slotIndex);

        if (pending.hasSource()) {
            List<TradeSlot> sourceSlots = pending.isBuy() ? sellSlots : buySlots;
            int srcIdx = pending.getSourceSlotIndex();
            if (srcIdx >= 0 && srcIdx < sourceSlots.size()) {
                TradeSlot source = sourceSlots.get(srcIdx);
                source.setPendingCount(Math.max(0, source.getPendingCount() - pending.getBaseStock()));
            }
        }

        pendingSlots.remove(slotIndex);

        if (pendingSlots.isEmpty()) {
            isBuyTrade = true;
        }

        syncPendingTradeToClient();
    }

    private void removeTradeEntryByType(boolean isBuy, int sourceSlotIndex, int amount, boolean clearAll) {
        for (int i = 0; i < pendingSlots.size(); i++) {
            PendingTradeSlot pending = pendingSlots.get(i);
            if (pending.isBuy() != isBuy || pending.getSourceSlotIndex() != sourceSlotIndex) {
                continue;
            }

            List<TradeSlot> sourceSlots = isBuy ? sellSlots : buySlots;
            int removeAmount = clearAll ? pending.getBaseStock() : Math.min(Math.max(1, amount), pending.getBaseStock());

            if (sourceSlotIndex >= 0 && sourceSlotIndex < sourceSlots.size()) {
                TradeSlot source = sourceSlots.get(sourceSlotIndex);
                source.setPendingCount(Math.max(0, source.getPendingCount() - removeAmount));
            }

            int remaining = pending.getBaseStock() - removeAmount;
            if (remaining > 0 && !clearAll) {
                pending.setBaseStock(remaining);
            } else {
                pendingSlots.remove(i);
            }

            if (pendingSlots.isEmpty()) {
                isBuyTrade = true;
            }

            syncPendingTradeToClient();
            return;
        }
    }

    /**
     * 清空暂存区
     */
    public void clearPendingTrade() {
        for (PendingTradeSlot pending : pendingSlots) {
            if (pending.hasSource()) {
                List<TradeSlot> sourceSlots = pending.isBuy() ? sellSlots : buySlots;
                int srcIdx = pending.getSourceSlotIndex();
                if (srcIdx >= 0 && srcIdx < sourceSlots.size()) {
                    TradeSlot source = sourceSlots.get(srcIdx);
                    source.setPendingCount(Math.max(0, source.getPendingCount() - pending.getBaseStock()));
                }
            }
        }

        pendingSlots.clear();
        isBuyTrade = true;

        syncPendingTradeToClient();
    }

    /**
     * 处理交易请求
     * @param requestType 请求类型：0=ADD_BUY, 1=ADD_SELL, 2=REMOVE_ENTRY, 3=CLEAR, 4=REMOVE_BUY, 5=REMOVE_SELL, 6=CONFIRM
     * @param slotIndex 槽位索引或条目索引
     */
    public void handleTradeRequest(int requestType, int slotIndex, int amount) {
        switch (requestType) {
            case 0 -> addTradeEntry(true, slotIndex, amount);   // ADD_BUY
            case 1 -> addTradeEntry(false, slotIndex, amount);  // ADD_SELL
            case 2 -> removeTradeEntry(slotIndex);       // REMOVE_ENTRY (legacy)
            case 3 -> clearPendingTrade();               // CLEAR
            case 4 -> removeTradeEntryByType(true, slotIndex, amount, false);   // REMOVE_BUY
            case 5 -> removeTradeEntryByType(false, slotIndex, amount, false);  // REMOVE_SELL
            case 6 -> executeTrade();                    // CONFIRM
            case 7 -> removeTradeEntryByType(true, slotIndex, amount, true);    // CLEAR_BUY
            case 8 -> removeTradeEntryByType(false, slotIndex, amount, true);   // CLEAR_SELL
        }
    }

    /**
     * 执行交易
     * 由服务端在收到 CONFIRM 请求时调用
     * 直接从暂存区读取数据，服务端权威计算价格
     */
    public void executeTrade() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (pendingSlots.isEmpty()) return;

        // 获取商业节点数据
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            sendTradeFeedback(serverPlayer, "gui.ruralroutes.trade_station.error.no_data", TradeFeedbackPayload.FeedbackType.ERROR);
            return;
        }

        // 执行交易（直接从 pendingSlots 读取）
        TradeResult result = TradeContractExecutor.INSTANCE.executeMixedTrade(
            serverPlayer.serverLevel(),
            nodeData,
            serverPlayer,
            pendingSlots,
            currentCycleIndex,
            blockPos
        );

        boolean completedFixedTrade = pendingSlots.stream()
            .anyMatch(slot -> slot.getTradeType() == TradeContractType.FIXED);
        boolean purchasedSpecialty = pendingSlots.stream()
            .anyMatch(slot -> slot.isBuy()
                && slot.getItemId() != null
                && nodeData.specialtyIds().contains(slot.getItemId()));
        Set<String> purchasedSpecialtyIds = pendingSlots.stream()
            .filter(TradeSlot::isBuy)
            .map(TradeSlot::getItemId)
            .filter(itemId -> itemId != null && nodeData.specialtyIds().contains(itemId))
            .map(ResourceLocation::toString)
            .collect(java.util.stream.Collectors.toSet());

        if (result.isSuccess()) {
            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(serverPlayer, TradeStationEvent.FIRST_TRADE);
            if (completedFixedTrade) {
                RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(serverPlayer, TradeStationEvent.FIXED_TRADE);
            }
            if (purchasedSpecialty) {
                RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(serverPlayer, TradeStationEvent.BUY_SPECIALTY);
            }
            updatePlayerProgress(serverPlayer, result.totalValueExchanged(), purchasedSpecialtyIds);

            // 交易成功：��空暂存区，刷新库存显示
            pendingSlots.clear();
            isBuyTrade = true;

            // 重置所有槽位的 pendingCount
            for (TradeSlot slot : sellSlots) {
                slot.setPendingCount(0);
            }
            for (TradeSlot slot : buySlots) {
                slot.setPendingCount(0);
            }

            // 重新加载节点数据（已被 TradeEngine 更新）
            CommercialNodeData updatedData = CommercialNodeManager.getNodeData(player.level(), blockPos);
            if (updatedData != null) {
                updateSlotsFromNodeData(updatedData);
            }

            // 同步到客户端
            syncSlotDataToClient(serverPlayer);
            syncPendingTradeToClient();
            syncCoinExchangeStateToClient(serverPlayer);

            sendTradeFeedback(serverPlayer, "gui.ruralroutes.trade_station.success", TradeFeedbackPayload.FeedbackType.SUCCESS);
        } else if (result.reason() == TradeResult.Reason.CYCLE_CHANGED) {
            // 周期变化：刷新 GUI 并通知玩家
            CycleManager cycleManager = CycleManager.get(serverPlayer.serverLevel());
            this.currentCycleIndex = cycleManager.getCycleIndex(serverPlayer.serverLevel());

            // 清空旧暂存区，避免继续持有上一周期的报价和清单引用
            pendingSlots.clear();
            isBuyTrade = true;

            // 刷新节点数据
            CommercialNodeData updatedData = CommercialNodeManager.getNodeData(player.level(), blockPos);
            if (updatedData != null) {
                // 周期刷新后，清单和槽位数量都可能变化，因此直接按节点数据重建
                initializeFromNodeData(updatedData);
            }

            syncSlotDataToClient(serverPlayer);
            syncPendingTradeToClient();
            syncCoinExchangeStateToClient(serverPlayer);
            sendTradeFeedback(serverPlayer, "gui.ruralroutes.trade_station.error.cycle_changed", TradeFeedbackPayload.FeedbackType.WARNING);
        } else {
            // 其他交易失败：发送失败原因
            sendTradeFeedback(serverPlayer, result.reason().getTranslationKey(), TradeFeedbackPayload.FeedbackType.ERROR);
        }
    }

    /**
     * 执行货币交换
     * 由服务端在收到货币交换请求时调用。
     */
    public void executeCoinExchange(CoinExchangeContract.ExchangeType exchangeType, boolean exchangeAll) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        if (nodeData == null) {
            sendTradeFeedback(serverPlayer, "gui.ruralroutes.trade_station.error.no_data", TradeFeedbackPayload.FeedbackType.ERROR);
            return;
        }

        int inputPerTrade = exchangeType.getInputCount();
        int outputPerTrade = exchangeType.getOutputCount();
        if (inputPerTrade <= 0 || outputPerTrade <= 0) {
            sendTradeFeedback(serverPlayer, TradeResult.Reason.INVALID_REQUEST.getTranslationKey(), TradeFeedbackPayload.FeedbackType.ERROR);
            return;
        }

        int playerCurrencyCount = countItemInInventory(serverPlayer, exchangeType.getInputItem());
        int villageCurrencyCount = getVillageCurrencyStock(nodeData, exchangeType.getOutputItem());
        int maxByPlayer = playerCurrencyCount / inputPerTrade;
        int maxByVillage = villageCurrencyCount / outputPerTrade;

        if (maxByPlayer <= 0) {
            syncCoinExchangeStateToClient(serverPlayer);
            sendTradeFeedback(serverPlayer,
                "gui.ruralroutes.trade_station.coin_exchange.fail.player_insufficient",
                TradeFeedbackPayload.FeedbackType.ERROR);
            return;
        }
        if (maxByVillage <= 0) {
            syncCoinExchangeStateToClient(serverPlayer);
            sendTradeFeedback(serverPlayer,
                "gui.ruralroutes.trade_station.coin_exchange.fail.village_insufficient",
                TradeFeedbackPayload.FeedbackType.ERROR);
            return;
        }

        int requestedTrades = exchangeAll ? Math.min(maxByPlayer, maxByVillage) : 1;
        CoinExchangeContract contract = new CoinExchangeContract(exchangeType);
        int executedTrades = 0;

        for (int i = 0; i < requestedTrades; i++) {
            CommercialNodeData currentData = CommercialNodeManager.getNodeData(player.level(), blockPos);
            if (currentData == null) {
                break;
            }

            TradeResult result = TradeContractExecutor.INSTANCE.executeContract(
                serverPlayer.serverLevel(),
                currentData,
                serverPlayer,
                contract,
                blockPos
            );

            if (!result.isSuccess()) {
                break;
            }
            executedTrades++;
        }

        syncCoinExchangeStateToClient(serverPlayer);

        if (executedTrades > 0) {
            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(serverPlayer, TradeStationEvent.COIN_EXCHANGE);
            sendTradeFeedback(serverPlayer,
                "gui.ruralroutes.trade_station.coin_exchange.success",
                TradeFeedbackPayload.FeedbackType.SUCCESS);
        } else {
            sendTradeFeedback(serverPlayer,
                "gui.ruralroutes.trade_station.coin_exchange.fail.village_insufficient",
                TradeFeedbackPayload.FeedbackType.ERROR);
        }
    }

    private void sendTradeFeedback(ServerPlayer player, String translationKey, TradeFeedbackPayload.FeedbackType feedbackType) {
        PacketDistributor.sendToPlayer(player, new TradeFeedbackPayload(containerId, translationKey, feedbackType));
    }

    private void updatePlayerProgress(ServerPlayer player, int totalValueExchanged, Set<String> purchasedSpecialtyIds) {
        RRPlayerProgressState state = player.getData(RRAttachments.PLAYER_PROGRESS.get());

        int newTradeCount = state.successfulTradeCount() + 1;
        if (newTradeCount >= 10) {
            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(player, TradeStationEvent.TRADE_10_TIMES);
        }
        if (newTradeCount >= 100) {
            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(player, TradeStationEvent.TRADE_100_TIMES);
        }

        Set<String> specialties = new HashSet<>(state.purchasedSpecialties());
        specialties.addAll(purchasedSpecialtyIds);
        if (specialties.size() >= 3) {
            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(player, TradeStationEvent.COLLECTOR);
        }

        if (Math.abs(totalValueExchanged) >= 300) {
            RRCriteriaTriggers.OPEN_TRADE_STATION.get().trigger(player, TradeStationEvent.BIG_SPENDER);
        }

        player.setData(
            RRAttachments.PLAYER_PROGRESS.get(),
            state.withSuccessfulTradeCount(newTradeCount).withPurchasedSpecialties(specialties)
        );
    }

    public void syncCoinExchangeStateToClient(ServerPlayer player) {
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(player.level(), blockPos);
        ClientCurrencyWallet playerWallet = extractPlayerCurrencyWallet(player);
        ClientCurrencyWallet villageWallet = extractVillageCurrencyWallet(nodeData);
        PacketDistributor.sendToPlayer(player, new CoinExchangeStatePayload(
            containerId,
            playerWallet.copperCount(),
            playerWallet.ironCount(),
            playerWallet.goldCount(),
            villageWallet.copperCount(),
            villageWallet.ironCount(),
            villageWallet.goldCount()
        ));
    }

    private ClientCurrencyWallet extractPlayerCurrencyWallet(Player player) {
        if (player == null) {
            return ClientCurrencyWallet.EMPTY;
        }

        return new ClientCurrencyWallet(
            countItemInInventory(player, RRItems.COPPER_COIN.get()),
            countItemInInventory(player, RRItems.IRON_COIN.get()),
            countItemInInventory(player, RRItems.GOLD_COIN.get())
        );
    }

    private ClientCurrencyWallet extractVillageCurrencyWallet(CommercialNodeData nodeData) {
        if (nodeData == null) {
            return ClientCurrencyWallet.EMPTY;
        }

        return new ClientCurrencyWallet(
            getVillageCurrencyStock(nodeData, RRItems.COPPER_COIN.get()),
            getVillageCurrencyStock(nodeData, RRItems.IRON_COIN.get()),
            getVillageCurrencyStock(nodeData, RRItems.GOLD_COIN.get())
        );
    }

    private int getVillageCurrencyStock(CommercialNodeData nodeData, Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        StockEntry stockEntry = nodeData.stocks().get(TradeItemKey.of(itemId));
        return stockEntry != null ? stockEntry.current() : 0;
    }

    private int countItemInInventory(Player player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * 从更新后的节点数据更新槽位库存
     */
    private void updateSlotsFromNodeData(CommercialNodeData nodeData) {
        Map<TradeItemKey, StockEntry> stocks = nodeData.stocks();

        for (int i = 0; i < sellSlots.size() && i < nodeData.sellItems().size(); i++) {
            TradeSlot slot = sellSlots.get(i);
            CommercialNodeData.NodeTradeEntry entryData = nodeData.sellItems().get(i);
            StockEntry entry = stocks.get(entryData.tradeItemKey());
            if (entry != null) {
                slot.setBaseStock(entry.current());
            }
        }

        for (int i = 0; i < buySlots.size() && i < nodeData.buyItems().size(); i++) {
            TradeSlot slot = buySlots.get(i);
            CommercialNodeData.NodeTradeEntry entryData = nodeData.buyItems().get(i);
            StockEntry entry = stocks.get(entryData.tradeItemKey());
            if (entry != null) {
                slot.setBaseStock(entry.current());
            }
        }
    }

    /**
     * 创建贸易槽位
     */
    private TradeSlot createTradeSlot(TradeDisplayContainer container, int index,
            int startX, int startY, boolean isBuy) {
        int col = index / 2;
        int row = index % 2;
        int x = startX + col * (SLOT_SIZE + SLOT_SPACING);
        int y = startY + row * (SLOT_SIZE + SLOT_SPACING);
        TradeSlot slot = new TradeSlot(container, index, x, y);
        slot.setIsBuy(isBuy);
        return slot;
    }

    /**
     * 应用槽位同步数据
     */
    private void applySlotData(TradeSlot slot, TradeSlotSyncPayload.SlotData data) {
        slot.setDisplayStack(data.displayStack());
        slot.setBaseStock(data.stockCount());
        slot.setMaxStock(data.maxStock());
        slot.setPrice(data.basePrice());
        slot.setPriceStacks(data.priceStacks());
        slot.setInputStacks(data.inputStacks());
        slot.setTradeType(data.tradeType());
    }

    /**
     * 根据类型移除暂存区条目
     * @param isBuy true=移除买入条目，false=移除卖出条目
     * @param sourceSlotIndex 来源槽位索引
     */
    /**
     * 接收从服务端同步的槽位数据（客户端调用）
     */
    public void receiveSlotData(int newSellSlotCount, int newBuySlotCount,
            List<TradeSlotSyncPayload.SlotData> sellData, List<TradeSlotSyncPayload.SlotData> buyData) {

        if (sellSlots.size() != newSellSlotCount || buySlots.size() != newBuySlotCount) {
            rebuildSlotsFromSyncData(newSellSlotCount, newBuySlotCount, sellData, buyData);
            return;
        }

        for (TradeSlotSyncPayload.SlotData data : sellData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < sellSlots.size()) {
                applySlotData(sellSlots.get(data.slotIndex()), data);
            }
        }

        for (TradeSlotSyncPayload.SlotData data : buyData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < buySlots.size()) {
                applySlotData(buySlots.get(data.slotIndex()), data);
            }
        }
    }

    /**
     * 根据同步数据重建槽位（客户端调用）
     */
    private void rebuildSlotsFromSyncData(int newSellSlotCount, int newBuySlotCount,
            List<TradeSlotSyncPayload.SlotData> sellData, List<TradeSlotSyncPayload.SlotData> buyData) {

        sellSlots.clear();
        buySlots.clear();
        slots.removeIf(slot -> slot instanceof TradeSlot);

        this.sellContainer = new TradeDisplayContainer(Math.max(1, newSellSlotCount));
        this.buyContainer = new TradeDisplayContainer(Math.max(1, newBuySlotCount));

        int sellStartX = 10;
        int sellStartY = 22;
        for (int i = 0; i < newSellSlotCount; i++) {
            TradeSlot slot = createTradeSlot(sellContainer, i, sellStartX, sellStartY, true);
            sellSlots.add(slot);
            addSlot(slot);
        }

        int buyStartX = 10;
        int buyStartY = 62;
        for (int i = 0; i < newBuySlotCount; i++) {
            TradeSlot slot = createTradeSlot(buyContainer, i, buyStartX, buyStartY, false);
            buySlots.add(slot);
            addSlot(slot);
        }

        for (TradeSlotSyncPayload.SlotData data : sellData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < sellSlots.size()) {
                applySlotData(sellSlots.get(data.slotIndex()), data);
            }
        }

        for (TradeSlotSyncPayload.SlotData data : buyData) {
            if (data.slotIndex() >= 0 && data.slotIndex() < buySlots.size()) {
                applySlotData(buySlots.get(data.slotIndex()), data);
            }
        }
    }

    /**
     * 同步槽位数据到客户端（服务端调用）
     */
    public void syncSlotDataToClient(ServerPlayer player) {
        List<TradeSlotSyncPayload.SlotData> sellData = new ArrayList<>();
        for (int i = 0; i < sellSlots.size(); i++) {
            TradeSlot slot = sellSlots.get(i);
            sellData.add(new TradeSlotSyncPayload.SlotData(
                i,
                slot.getDisplayStack(),
                slot.getBaseStock(),
                slot.getMaxStock(),
                slot.getPrice(),
                slot.getPriceStacks(),
                slot.getInputStacks(),
                true,
                slot.getTradeType()
            ));
        }

        List<TradeSlotSyncPayload.SlotData> buyData = new ArrayList<>();
        for (int i = 0; i < buySlots.size(); i++) {
            TradeSlot slot = buySlots.get(i);
            buyData.add(new TradeSlotSyncPayload.SlotData(
                i,
                slot.getDisplayStack(),
                slot.getBaseStock(),
                slot.getMaxStock(),
                slot.getPrice(),
                slot.getPriceStacks(),
                slot.getInputStacks(),
                false,
                slot.getTradeType()
            ));
        }

        PacketDistributor.sendToPlayer(player,
            new TradeSlotSyncPayload(containerId, sellSlots.size(), buySlots.size(), sellData, buyData));
    }

    /**
     * 同步暂存区数据到客户端（服务端调用）
     */
    private void syncPendingTradeToClient() {
        if (!(getPlayer() instanceof ServerPlayer serverPlayer)) return;

        // 构建槽位数据列表
        List<PendingTradeSyncPayload.PendingSlotData> slotDataList = new ArrayList<>();
        for (PendingTradeSlot slot : pendingSlots) {
            slotDataList.add(new PendingTradeSyncPayload.PendingSlotData(
                slot.getItemId(),
                slot.getDisplayStack(),
                slot.getBaseStock(),
                slot.getPrice(),
                slot.isBuy(),
                slot.getSourceSlotIndex(),
                slot.getTradeType(),
                slot.getPriceStacks(),
                slot.getInputStacks()
            ));
        }

        // 构建 pendingCountMap（发送所有槽位的 pendingCount，包括 0）
        Map<Integer, Integer> pendingCountMap = new HashMap<>();
        for (int i = 0; i < sellSlots.size(); i++) {
            pendingCountMap.put(i, sellSlots.get(i).getPendingCount());
        }
        for (int i = 0; i < buySlots.size(); i++) {
            pendingCountMap.put(sellSlots.size() + i, buySlots.get(i).getPendingCount());
        }

        PacketDistributor.sendToPlayer(serverPlayer,
            new PendingTradeSyncPayload(containerId, isBuyTrade, slotDataList, pendingCountMap));
    }

    /**
     * 接收暂存区同步数据（客户端调用）
     */
    public void receivePendingTradeData(PendingTradeSyncPayload payload) {
        // 更新暂存区槽位
        pendingSlots.clear();
        for (PendingTradeSyncPayload.PendingSlotData data : payload.pendingSlots()) {
            PendingTradeSlot slot = new PendingTradeSlot(
                new TradeDisplayContainer(1), pendingSlots.size(), 0, 0);
            slot.setItemId(data.itemId());
            slot.setDisplayStack(data.displayStack().copy());
            slot.setBaseStock(data.stockCount());
            slot.setPrice(data.price());
            slot.setIsBuy(data.isBuy());
            slot.setSource(data.sourceSlotIndex(), data.isBuy());
            slot.setTradeType(data.tradeType());
            slot.setPriceStacks(data.priceStacks());
            slot.setInputStacks(data.inputStacks());
            pendingSlots.add(slot);
        }

        // 更新交易类型
        isBuyTrade = payload.isBuyTrade();

        // 更新来源槽位的暂存计数
        Map<Integer, Integer> pendingCountMap = payload.pendingCountMap();
        for (Map.Entry<Integer, Integer> mapEntry : pendingCountMap.entrySet()) {
            int slotIndex = mapEntry.getKey();
            int pendingCount = mapEntry.getValue();

            if (slotIndex < sellSlots.size()) {
                sellSlots.get(slotIndex).setPendingCount(pendingCount);
            } else if (slotIndex < sellSlots.size() + buySlots.size()) {
                int buyIndex = slotIndex - sellSlots.size();
                if (buyIndex < buySlots.size()) {
                    buySlots.get(buyIndex).setPendingCount(pendingCount);
                }
            }
        }
    }

    /**
     * 接收交易反馈（客户端调用）
     */
    public void receiveTradeFeedback(TradeFeedbackPayload payload) {
        this.activeTradeFeedback = new ClientTradeFeedback(
            Component.translatable(payload.translationKey()),
            payload.feedbackType()
        );
        this.tradeFeedbackExpireAtMs = System.currentTimeMillis() + 2800L;
    }

    /**
     * 接收村庄货币库存同步（客户端调用）
     */
    public void receiveCoinExchangeState(CoinExchangeStatePayload payload) {
        this.playerCurrencyWallet = new ClientCurrencyWallet(
            payload.playerCopperCount(),
            payload.playerIronCount(),
            payload.playerGoldCount()
        );
        this.villageCurrencyWallet = new ClientCurrencyWallet(
            payload.villageCopperCount(),
            payload.villageIronCount(),
            payload.villageGoldCount()
        );
    }

    public ClientTradeFeedback getActiveTradeFeedback() {
        if (activeTradeFeedback == null) {
            return null;
        }
        if (System.currentTimeMillis() > tradeFeedbackExpireAtMs) {
            activeTradeFeedback = null;
            return null;
        }
        return activeTradeFeedback;
    }

    public record ClientTradeFeedback(Component message, TradeFeedbackPayload.FeedbackType type) {
    }

    public ClientCurrencyWallet getPlayerCurrencyWallet() {
        return playerCurrencyWallet;
    }

    public ClientCurrencyWallet getVillageCurrencyWallet() {
        return villageCurrencyWallet;
    }

    public record ClientCurrencyWallet(int copperCount, int ironCount, int goldCount) {
        public static final ClientCurrencyWallet EMPTY = new ClientCurrencyWallet(0, 0, 0);

        public int count(Item item) {
            if (item == RRItems.COPPER_COIN.get()) {
                return copperCount;
            }
            if (item == RRItems.IRON_COIN.get()) {
                return ironCount;
            }
            if (item == RRItems.GOLD_COIN.get()) {
                return goldCount;
            }
            return 0;
        }
    }

    /**
     * 获取玩家
     */
    private Player getPlayer() {
        return player;
    }
}
