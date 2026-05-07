package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.client.gui.component.CoinExchangeWidget;
import github.mczme.ruralroutes.client.gui.component.ScrollableSectionWidget;
import github.mczme.ruralroutes.client.gui.component.TradeAreaWidget;
import github.mczme.ruralroutes.client.gui.component.TradeOfferCardWidget;
import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import github.mczme.ruralroutes.network.packet.TradeRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * 贸易站 GUI 屏幕
 *
 * 使用 ScrollableSectionWidget 组件展示交易物品
 */
public class TradeStationScreen extends AbstractContainerScreen<TradeStationMenu> {

    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;
    private static final int MARGIN = 8;
    private static final int SECTION_SPACING = 4;

    private static final int CARD_HEIGHT = 26;  // 压缩卡片高度
    private static final int CARD_SPACING = 2;  // 压缩间距
    private static final int ROWS = 2;
    private static final int SECTION_HEIGHT = ROWS * CARD_HEIGHT + (ROWS - 1) * CARD_SPACING + 14;  // 标题区域也压缩

    private ScrollableSectionWidget sellSection;
    private ScrollableSectionWidget buySection;
    private TradeAreaWidget tradeArea;
    private CoinExchangeWidget coinExchange;

    public TradeStationScreen(TradeStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    public TradeStationMenu getMenu() {
        return menu;
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        int currentY = topPos;
        int mainWidth = GUI_WIDTH - MARGIN * 2;

        // 出售区
        sellSection = new ScrollableSectionWidget(
            leftPos + MARGIN, currentY, mainWidth, SECTION_HEIGHT,
            0x55FF55, 0x40333333,
            Component.translatable("gui.ruralroutes.trade_station.sell"));
        sellSection.setSlots(menu.getSellSlots(), (x, y) -> new TradeOfferCardWidget(x, y));
        sellSection.setOnCardClickGeneric(card -> onSellSlotClicked(card));

        currentY += sellSection.getHeight() + SECTION_SPACING;

        // 收购区
        buySection = new ScrollableSectionWidget(
            leftPos + MARGIN, currentY, mainWidth, SECTION_HEIGHT,
            0xFF5555, 0x40333333,
            Component.translatable("gui.ruralroutes.trade_station.buy"));
        buySection.setSlots(menu.getBuySlots(), (x, y) -> new TradeOfferCardWidget(x, y));
        buySection.setOnCardClickGeneric(card -> onBuySlotClicked(card));

        currentY += buySection.getHeight() + SECTION_SPACING;

        // 交易区 - 自动计算剩余高度
        int tradeWidth = mainWidth * 7 / 10 - SECTION_SPACING / 2;
        int tradeHeight = GUI_HEIGHT - MARGIN - (currentY - topPos);
        tradeArea = new TradeAreaWidget(leftPos + MARGIN, currentY, tradeWidth, tradeHeight);
        tradeArea.init(btn -> onConfirmClick());
        tradeArea.setOnPendingSlotRemove(this::onPendingSlotRemoved);
        tradeArea.setPendingSlots(menu.getPendingSlots(), menu.isBuyTrade());

        // 铸币区
        int coinWidth = mainWidth * 3 / 10 - SECTION_SPACING / 2;
        int coinX = leftPos + MARGIN + tradeWidth + SECTION_SPACING;
        coinExchange = new CoinExchangeWidget(coinX, currentY, coinWidth, tradeHeight);
        coinExchange.init();

        // 初始化交易区显示
        updateTradeAreaDisplay();
    }

    /**
     * 更新交易区显示
     */
    private void updateTradeAreaDisplay() {
        List<PendingTradeSlot> pendingSlots = menu.getPendingSlots();
        boolean isBuyTrade = menu.isBuyTrade();

        tradeArea.setPendingSlots(pendingSlots, isBuyTrade);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 绘制背景
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xCC222222);

        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 5, 0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // 渲染出售区
        sellSection.render(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染收购区
        buySection.render(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染交易区和铸币区
        tradeArea.render(guiGraphics, mouseX, mouseY, partialTick);
        coinExchange.render(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染工具提示
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        // 检查出售区悬停的卡片
        for (AbstractWidget card : sellSection.getCards()) {
            if (card.isHovered() && card instanceof TradeOfferCardWidget offerCard) {
                renderOfferCardTooltip(guiGraphics, offerCard, x, y);
                return;
            }
        }

        // 检查收购区悬停的卡片
        for (AbstractWidget card : buySection.getCards()) {
            if (card.isHovered() && card instanceof TradeOfferCardWidget offerCard) {
                renderOfferCardTooltip(guiGraphics, offerCard, x, y);
                return;
            }
        }
    }

    /**
     * 渲染交易卡片 tooltip
     * 主物品图标区域使用原版tooltip，其他区域使用自定义tooltip
     */
    private void renderOfferCardTooltip(GuiGraphics guiGraphics, TradeOfferCardWidget card, int x, int y) {
        TradeSlot slot = card.getTradeSlot();
        if (slot == null) return;

        ItemStack displayStack = slot.getDisplayStack();
        if (displayStack.isEmpty()) return;

        // 检查鼠标是否在主物品图标区域
        if (card.isMouseOverItemIcon(x, y)) {
            // 使用原版物品tooltip（包含附魔、描述等）
            guiGraphics.renderTooltip(font, displayStack, x, y);
        } else {
            // 使用自定义tooltip（库存、价格等）
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(displayStack.getDisplayName());

            // 库存说明
            int stockCount = slot.getStockCount();
            String stockKey = slot.isBuy() ? "gui.ruralroutes.trade_card.tooltip.stock" : "gui.ruralroutes.trade_card.tooltip.can_buy";
            tooltip.add(Component.translatable(stockKey, stockCount)
                .withStyle(style -> style.withColor(stockCount > 0 ? 0xFFFFFF : 0xFF5555)));

            TradeContractType type = slot.getTradeType();

            if (type == TradeContractType.CURRENCY_BASKET_DYNAMIC) {
                tooltip.add(Component.translatable("gui.ruralroutes.trade_card.tooltip.price")
                    .withStyle(style -> style.withColor(0xFFD700)));
                List<ItemStack> priceStacks = slot.getPriceStacks();
                for (ItemStack stack : priceStacks) {
                    tooltip.add(Component.literal("  " + stack.getCount() + "x " + stack.getDisplayName().getString())
                        .withStyle(style -> style.withColor(0xAAAAAA)));
                }
            } else {
                tooltip.add(Component.translatable("gui.ruralroutes.trade_card.tooltip.need")
                    .withStyle(style -> style.withColor(0xFFAA00)));
                List<ItemStack> inputStacks = slot.getInputStacks();
                for (ItemStack stack : inputStacks) {
                    tooltip.add(Component.literal("  " + stack.getCount() + "x " + stack.getDisplayName().getString())
                        .withStyle(style -> style.withColor(0xAAAAAA)));
                }
            }

            guiGraphics.renderTooltip(font, tooltip.stream().map(Component::getVisualOrderText).toList(), x, y);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (sellSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (buySection.mouseClicked(mouseX, mouseY, button)) return true;
        if (tradeArea.mouseClicked(mouseX, mouseY, button)) return true;
        if (coinExchange.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        sellSection.mouseReleased(mouseX, mouseY, button);
        buySection.mouseReleased(mouseX, mouseY, button);
        tradeArea.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (sellSection.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        if (buySection.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        if (tradeArea.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (sellSection.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        if (buySection.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        if (tradeArea.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * 出售槽位点击
     */
    private void onSellSlotClicked(AbstractWidget card) {
        if (card instanceof TradeOfferCardWidget offerCard) {
            TradeSlot slot = offerCard.getTradeSlot();
            if (slot == null) return;
            int index = menu.getSellSlots().indexOf(slot);
            if (index >= 0) {
                PacketDistributor.sendToServer(new TradeRequestPayload(menu.containerId, TradeRequestPayload.ADD_BUY, index));
            }
        }
    }

    /**
     * 收购槽位点击
     */
    private void onBuySlotClicked(AbstractWidget card) {
        if (card instanceof TradeOfferCardWidget offerCard) {
            TradeSlot slot = offerCard.getTradeSlot();
            if (slot == null) return;
            int index = menu.getBuySlots().indexOf(slot);
            if (index >= 0) {
                PacketDistributor.sendToServer(new TradeRequestPayload(menu.containerId, TradeRequestPayload.ADD_SELL, index));
            }
        }
    }

    /**
     * 点击确认按钮
     */
    private void onConfirmClick() {
        if (!menu.getPendingSlots().isEmpty()) {
            PacketDistributor.sendToServer(new TradeRequestPayload(menu.containerId, TradeRequestPayload.CONFIRM, 0));
        }
    }

    /**
     * 移除暂存槽位
     */
    private void onPendingSlotRemoved(PendingTradeSlot slot) {
        if (slot != null && slot.hasSource()) {
            int requestType = slot.isSourceIsBuy() ? TradeRequestPayload.REMOVE_BUY : TradeRequestPayload.REMOVE_SELL;
            PacketDistributor.sendToServer(new TradeRequestPayload(menu.containerId, requestType, slot.getSourceSlotIndex()));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateTradeAreaDisplay();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
