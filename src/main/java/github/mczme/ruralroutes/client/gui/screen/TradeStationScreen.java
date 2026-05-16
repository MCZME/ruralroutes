package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.client.gui.GuiTextStyles;
import github.mczme.ruralroutes.client.gui.component.CoinExchangeWidget;
import github.mczme.ruralroutes.client.gui.component.ScrollableSectionWidget;
import github.mczme.ruralroutes.client.gui.component.TradeAreaWidget;
import github.mczme.ruralroutes.client.gui.component.TradeOfferCardWidget;
import github.mczme.ruralroutes.core.trade.CoinExchangeContract;
import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import github.mczme.ruralroutes.network.packet.CoinExchangeRequestPayload;
import github.mczme.ruralroutes.network.packet.TradeRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    private static final ResourceLocation GUI_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "textures/gui/trade_station.png");

    private static final int GUI_WIDTH = 384;
    private static final int GUI_HEIGHT = 256;
    private static final int TITLE_CENTER_X = GUI_WIDTH / 2;
    private static final int TITLE_Y = 16;
    private static final int TITLE_COLOR = 0xFF46461E;
    private static final int TRADE_SECTION_HEIGHT = ScrollableSectionWidget.getRequiredHeight();

    private static final int SELL_X = 35;
    private static final int SELL_Y = 24;
    private static final int SELL_WIDTH = 326;
    private static final int SELL_HEIGHT = TRADE_SECTION_HEIGHT;

    private static final int BUY_X = 35;
    private static final int BUY_Y = 90;
    private static final int BUY_WIDTH = 326;
    private static final int BUY_HEIGHT = TRADE_SECTION_HEIGHT;

    private static final int TRADE_X = 36;
    private static final int TRADE_Y = 163;
    private static final int TRADE_WIDTH = 228;
    private static final int TRADE_HEIGHT = 79;

    private static final int COIN_X = 268;
    private static final int COIN_Y = 163;
    private static final int COIN_WIDTH = 102;
    private static final int COIN_HEIGHT = 80;
    private static final int FEEDBACK_TEXT_COLOR = 0xFFF6EBD0;
    private static final int FEEDBACK_SHADOW_COLOR = 0x30000000;
    private static final int FEEDBACK_SUCCESS_BG = 0xD05F6A2F;
    private static final int FEEDBACK_SUCCESS_BORDER = 0xFFB7C87E;
    private static final int FEEDBACK_WARNING_BG = 0xD08D5B27;
    private static final int FEEDBACK_WARNING_BORDER = 0xFFE3B16B;
    private static final int FEEDBACK_ERROR_BG = 0xD0833A32;
    private static final int FEEDBACK_ERROR_BORDER = 0xFFE2A08B;
    private static final int FEEDBACK_HIGHLIGHT = 0x24FFF4D4;

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

        // 出售区
        sellSection = new ScrollableSectionWidget(
            leftPos + SELL_X, topPos + SELL_Y, SELL_WIDTH, SELL_HEIGHT,
            0xFFC4C07D, 0x00000000,
            Component.translatable("gui.ruralroutes.trade_station.sell"));
        sellSection.setSlots(menu.getSellSlots(), (x, y) -> new TradeOfferCardWidget(x, y));
        sellSection.setOnCardClickGeneric(card -> onSellSlotClicked(card));

        // 收购区
        buySection = new ScrollableSectionWidget(
            leftPos + BUY_X, topPos + BUY_Y, BUY_WIDTH, BUY_HEIGHT,
            0xFFC89573, 0x00000000,
            Component.translatable("gui.ruralroutes.trade_station.buy"));
        buySection.setSlots(menu.getBuySlots(), (x, y) -> new TradeOfferCardWidget(x, y));
        buySection.setOnCardClickGeneric(card -> onBuySlotClicked(card));

        // 交易区
        tradeArea = new TradeAreaWidget(leftPos + TRADE_X, topPos + TRADE_Y, TRADE_WIDTH, TRADE_HEIGHT);
        tradeArea.init(btn -> onConfirmClick());
        tradeArea.setOnPendingSlotRemove(this::onPendingSlotRemoved);
        tradeArea.setPendingSlots(menu.getPendingSlots(), menu.isBuyTrade());

        // 货币交换区
        coinExchange = new CoinExchangeWidget(leftPos + COIN_X, topPos + COIN_Y, COIN_WIDTH, COIN_HEIGHT);
        coinExchange.init(this::onCoinExchangeClick);
        coinExchange.setPlayerWalletSupplier(menu::getPlayerCurrencyWallet);
        coinExchange.setVillageWallet(menu.getVillageCurrencyWallet());

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
        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        guiGraphics.drawCenteredString(font,
            GuiTextStyles.uniform(this.title),
            leftPos + TITLE_CENTER_X, topPos + TITLE_Y, TITLE_COLOR);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // 渲染出售区
        sellSection.render(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染收购区
        buySection.render(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染交易区和货币交换区
        tradeArea.render(guiGraphics, mouseX, mouseY, partialTick);
        coinExchange.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTradeFeedback(guiGraphics);

        // 渲染工具提示
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderTradeFeedback(GuiGraphics guiGraphics) {
        TradeStationMenu.ClientTradeFeedback feedback = menu.getActiveTradeFeedback();
        if (feedback == null) {
            return;
        }

        int bannerWidth = Math.min(tradeArea.getWidth() - 12, Math.max(110, font.width(feedback.message()) + 20));
        int bannerHeight = 14;
        int bannerX = tradeArea.getX() + (tradeArea.getWidth() - bannerWidth) / 2;
        int bannerY = tradeArea.getY() + 6;

        int backgroundColor = switch (feedback.type()) {
            case SUCCESS -> FEEDBACK_SUCCESS_BG;
            case WARNING -> FEEDBACK_WARNING_BG;
            case ERROR -> FEEDBACK_ERROR_BG;
        };
        int borderColor = switch (feedback.type()) {
            case SUCCESS -> FEEDBACK_SUCCESS_BORDER;
            case WARNING -> FEEDBACK_WARNING_BORDER;
            case ERROR -> FEEDBACK_ERROR_BORDER;
        };

        guiGraphics.fill(bannerX + 1, bannerY + 1, bannerX + bannerWidth + 1, bannerY + bannerHeight + 1, FEEDBACK_SHADOW_COLOR);
        guiGraphics.fill(bannerX, bannerY, bannerX + bannerWidth, bannerY + bannerHeight, backgroundColor);
        guiGraphics.fill(bannerX + 1, bannerY + 1, bannerX + bannerWidth - 1, bannerY + 2, FEEDBACK_HIGHLIGHT);
        guiGraphics.renderOutline(bannerX, bannerY, bannerWidth, bannerHeight, borderColor);

        int textX = bannerX + (bannerWidth - font.width(feedback.message())) / 2;
        int textY = bannerY + (bannerHeight - font.lineHeight) / 2;
        guiGraphics.drawString(font, GuiTextStyles.uniform(feedback.message()), textX, textY, FEEDBACK_TEXT_COLOR, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        List<Component> coinTooltip = coinExchange.getTooltip(x, y);
        if (coinTooltip != null) {
            guiGraphics.renderTooltip(font, coinTooltip.stream().map(Component::getVisualOrderText).toList(), x, y);
            return;
        }

        // 检查交易区悬停的卡片
        TradeAreaWidget.HoveredCardInfo tradeCardInfo = tradeArea.getHoveredCardInfo(x, y);
        if (tradeCardInfo != null) {
            renderTradeAreaCardTooltip(guiGraphics, tradeCardInfo, x, y);
            return;
        }

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

    private void renderTradeAreaCardTooltip(GuiGraphics guiGraphics, TradeAreaWidget.HoveredCardInfo info, int x, int y) {
        ItemStack stack = info.stack();
        if (stack == null || stack.isEmpty()) return;

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(stack.getDisplayName());
        tooltip.add(Component.literal("×" + info.count())
            .withStyle(style -> style.withColor(0xAAAAAA)));

        guiGraphics.renderTooltip(font, tooltip.stream().map(Component::getVisualOrderText).toList(), x, y);
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

    private void onCoinExchangeClick(CoinExchangeContract.ExchangeType exchangeType, Boolean exchangeAll) {
        PacketDistributor.sendToServer(new CoinExchangeRequestPayload(
            menu.containerId,
            exchangeType,
            Boolean.TRUE.equals(exchangeAll)
        ));
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
        coinExchange.setPlayerWalletSupplier(menu::getPlayerCurrencyWallet);
        coinExchange.setVillageWallet(menu.getVillageCurrencyWallet());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
