package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 交易区组件 - 两栏布局
 * - 左侧：玩家想要的（获得）- 包含买入的物品 + 卖出获得的货币
 * - 右侧：玩家支付的（付出）- 包含卖出的物品 + 买入支付的货币/输入物品
 * - 底部：确认按钮
 */
public class TradeAreaWidget extends AbstractWidget {

    private static final int BG_COLOR = 0x40333333;
    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int WANT_AREA_COLOR = 0x4000AAFF;    // 蓝色 - 玩家获得
    private static final int PAY_AREA_COLOR = 0x40FFAA00;     // 橙色 - 玩家付出
    private static final int CARD_BG_COLOR = 0x40FFFFFF;
    private static final int CARD_HOVER_COLOR = 0x80FFFFFF;
    private static final int STOCK_COLOR = 0xFFFFFF;
    private static final int OUT_OF_STOCK_COLOR = 0xFF5555;
    private static final int PADDING = 4;
    private static final int CARD_SIZE = 18;
    private static final int CARD_SPACING = 2;
    private static final int MAX_VISIBLE_CARDS = 4;
    private static final int SETTLEMENT_HEIGHT = 24;

    private Button confirmButton;

    // 暂存槽位数据
    private List<PendingTradeSlot> wantItemSlots = new ArrayList<>();
    private List<PendingTradeSlot> payItemSlots = new ArrayList<>();

    // 缓存的卡片列表
    private List<CardInfo> cachedWantCards = new ArrayList<>();
    private List<CardInfo> cachedPayCards = new ArrayList<>();

    // 回调
    private Consumer<PendingTradeSlot> onRemoveSlot;

    public TradeAreaWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void init(Button.OnPress confirmAction) {
        int buttonWidth = Math.min(50, getWidth() / 4);
        int buttonHeight = 18;
        int centerX = getX() + getWidth() / 2;
        int buttonX = centerX - buttonWidth / 2;
        int buttonY = getY() + getHeight() - PADDING - buttonHeight;

        confirmButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_station.confirm"),
            confirmAction
        ).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build();
    }

    /**
     * 设置移除暂存槽位的回调
     */
    public void setOnPendingSlotRemove(Consumer<PendingTradeSlot> callback) {
        this.onRemoveSlot = callback;
    }

    /**
     * 设置暂存区槽位
     */
    public void setPendingSlots(List<PendingTradeSlot> slots, boolean isBuyTrade) {
        this.wantItemSlots.clear();
        this.payItemSlots.clear();

        for (PendingTradeSlot slot : slots) {
            if (slot.isBuy()) {
                wantItemSlots.add(slot);
            } else {
                payItemSlots.add(slot);
            }
        }

        rebuildCardCache();
    }

    /**
     * 重建卡片缓存
     */
    private void rebuildCardCache() {
        cachedWantCards = buildWantCards();
        cachedPayCards = buildPayCards();
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * 清空交易区内容
     */
    public void clearContent() {
        wantItemSlots.clear();
        payItemSlots.clear();
        cachedWantCards.clear();
        cachedPayCards.clear();
    }

    /**
     * 是否有交易内容
     */
    public boolean hasContent() {
        return !wantItemSlots.isEmpty() || !payItemSlots.isEmpty();
    }

    /**
     * 获取条目数量
     */
    public int getEntryCount() {
        return wantItemSlots.size() + payItemSlots.size();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), BG_COLOR);

        var font = Minecraft.getInstance().font;
        int sideWidth = getWidth() / 2 - PADDING * 2;
        int titleHeight = 12;
        int areaHeight = getHeight() - PADDING * 3 - titleHeight - SETTLEMENT_HEIGHT;
        int contentY = getY() + PADDING;

        // ========== 左侧：玩家想要的（获得）==========
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.want_area"),
            getX() + PADDING, contentY, LABEL_COLOR);

        int wantAreaY = contentY + titleHeight;
        int wantAreaX = getX() + PADDING;
        fill(guiGraphics, wantAreaX, wantAreaY, sideWidth, areaHeight, WANT_AREA_COLOR);

        // 使用缓存的左侧卡片
        int wantContentWidth = cachedWantCards.size() * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
        int wantStartX = wantAreaX + Math.max(0, (sideWidth - wantContentWidth) / 2);
        int wantItemY = wantAreaY + (areaHeight - CARD_SIZE) / 2;

        for (int i = 0; i < cachedWantCards.size() && i < MAX_VISIBLE_CARDS; i++) {
            CardInfo card = cachedWantCards.get(i);
            int cardX = wantStartX + i * (CARD_SIZE + CARD_SPACING);
            boolean isHovered = isCardHovered(cardX, wantItemY, mouseX, mouseY);
            renderItemCard(guiGraphics, card.stack, card.count, cardX, wantItemY, isHovered);
        }

        if (cachedWantCards.size() > MAX_VISIBLE_CARDS) {
            String moreText = "+" + (cachedWantCards.size() - MAX_VISIBLE_CARDS);
            int moreX = wantStartX + MAX_VISIBLE_CARDS * (CARD_SIZE + CARD_SPACING);
            guiGraphics.drawString(font, moreText, moreX, wantItemY + 5, LABEL_COLOR);
        }

        // ========== 右侧：玩家支付的（付出）==========
        int payAreaX = getX() + getWidth() / 2 + PADDING;
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.pay_area"),
            payAreaX, contentY, LABEL_COLOR);

        int payAreaY = contentY + titleHeight;
        fill(guiGraphics, payAreaX, payAreaY, sideWidth, areaHeight, PAY_AREA_COLOR);

        // 使用缓存的右侧卡片
        int payContentWidth = cachedPayCards.size() * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
        int payStartX = payAreaX + Math.max(0, (sideWidth - payContentWidth) / 2);
        int payItemY = payAreaY + (areaHeight - CARD_SIZE) / 2;

        for (int i = 0; i < cachedPayCards.size() && i < MAX_VISIBLE_CARDS; i++) {
            CardInfo card = cachedPayCards.get(i);
            int cardX = payStartX + i * (CARD_SIZE + CARD_SPACING);
            boolean isHovered = isCardHovered(cardX, payItemY, mouseX, mouseY);
            renderItemCard(guiGraphics, card.stack, card.count, cardX, payItemY, isHovered);
        }

        if (cachedPayCards.size() > MAX_VISIBLE_CARDS) {
            String moreText = "+" + (cachedPayCards.size() - MAX_VISIBLE_CARDS);
            int moreX = payStartX + MAX_VISIBLE_CARDS * (CARD_SIZE + CARD_SPACING);
            guiGraphics.drawString(font, moreText, moreX, payItemY + 5, LABEL_COLOR);
        }

        // 渲染确认按钮
        if (confirmButton != null) {
            confirmButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    /**
     * 卡片信息
     */
    private record CardInfo(ItemStack stack, int count) {}

    /**
     * 构建左侧（玩家获得）卡片列表
     */
    private List<CardInfo> buildWantCards() {
        List<CardInfo> cards = new ArrayList<>();

        // 买入的物品
        for (PendingTradeSlot slot : wantItemSlots) {
            cards.add(new CardInfo(slot.getDisplayStack(), slot.getBaseStock()));
        }

        // 卖出获得的货币（仅货币篮类型）
        for (PendingTradeSlot slot : payItemSlots) {
            if (slot.getTradeType() == TradeContractType.CURRENCY_BASKET_DYNAMIC) {
                int multiplier = slot.getBaseStock();
                for (ItemStack priceStack : slot.getPriceStacks()) {
                    ItemStack scaled = priceStack.copy();
                    scaled.setCount(priceStack.getCount() * multiplier);
                    cards.add(new CardInfo(scaled, scaled.getCount()));
                }
            }
        }

        return cards;
    }

    /**
     * 构建右侧（玩家支付）卡片列表
     */
    private List<CardInfo> buildPayCards() {
        List<CardInfo> cards = new ArrayList<>();

        // 卖出的物品
        for (PendingTradeSlot slot : payItemSlots) {
            cards.add(new CardInfo(slot.getDisplayStack(), slot.getBaseStock()));
        }

        // 买入支付的货币/输入物品
        for (PendingTradeSlot slot : wantItemSlots) {
            int multiplier = slot.getBaseStock();
            TradeContractType tradeType = slot.getTradeType();

            if (tradeType == TradeContractType.FIXED) {
                // 固定交换：按数量缩放输入物品
                for (ItemStack inputStack : slot.getInputStacks()) {
                    ItemStack scaled = inputStack.copy();
                    scaled.setCount(inputStack.getCount() * multiplier);
                    cards.add(new CardInfo(scaled, scaled.getCount()));
                }
            } else {
                // 货币篮（包括默认）：按数量缩放价格
                for (ItemStack priceStack : slot.getPriceStacks()) {
                    ItemStack scaled = priceStack.copy();
                    scaled.setCount(priceStack.getCount() * multiplier);
                    cards.add(new CardInfo(scaled, scaled.getCount()));
                }
            }
        }

        return cards;
    }

    /**
     * 渲染物品卡片
     */
    private void renderItemCard(GuiGraphics guiGraphics, ItemStack stack, int count, int x, int y, boolean hovered) {
        int bgColor = hovered ? CARD_HOVER_COLOR : CARD_BG_COLOR;
        fill(guiGraphics, x, y, CARD_SIZE, CARD_SIZE, bgColor);

        if (stack != null && !stack.isEmpty()) {
            guiGraphics.renderItem(stack, x + 1, y + 1);
        }

        String countText = String.valueOf(count);
        int countX = x + CARD_SIZE - Minecraft.getInstance().font.width(countText) - 1;
        int countY = y + CARD_SIZE - 8;
        int countColor = count > 0 ? STOCK_COLOR : OUT_OF_STOCK_COLOR;
        guiGraphics.drawString(Minecraft.getInstance().font, countText, countX, countY, countColor);
    }

    /**
     * 检查卡片是否悬停
     */
    private boolean isCardHovered(int cardX, int cardY, int mouseX, int mouseY) {
        return mouseX >= cardX && mouseX < cardX + CARD_SIZE &&
               mouseY >= cardY && mouseY < cardY + CARD_SIZE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int sideWidth = getWidth() / 2 - PADDING * 2;
        int titleHeight = 12;
        int areaHeight = getHeight() - PADDING * 3 - titleHeight - SETTLEMENT_HEIGHT;
        int contentY = getY() + PADDING;

        int wantAreaY = contentY + titleHeight;
        int wantAreaX = getX() + PADDING;
        int wantItemY = wantAreaY + (areaHeight - CARD_SIZE) / 2;

        int wantContentWidth = cachedWantCards.size() * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
        int wantStartX = wantAreaX + Math.max(0, (sideWidth - wantContentWidth) / 2);

        for (int i = 0; i < wantItemSlots.size() && i < MAX_VISIBLE_CARDS; i++) {
            int cardX = wantStartX + i * (CARD_SIZE + CARD_SPACING);
            if (isCardHovered(cardX, wantItemY, (int) mouseX, (int) mouseY)) {
                if (onRemoveSlot != null) {
                    onRemoveSlot.accept(wantItemSlots.get(i));
                }
                return true;
            }
        }

        int payAreaX = getX() + getWidth() / 2 + PADDING;
        int payAreaY = contentY + titleHeight;
        int payItemY = payAreaY + (areaHeight - CARD_SIZE) / 2;

        int payContentWidth = cachedPayCards.size() * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
        int payStartX = payAreaX + Math.max(0, (sideWidth - payContentWidth) / 2);

        for (int i = 0; i < payItemSlots.size() && i < MAX_VISIBLE_CARDS; i++) {
            int cardX = payStartX + i * (CARD_SIZE + CARD_SPACING);
            if (isCardHovered(cardX, payItemY, (int) mouseX, (int) mouseY)) {
                if (onRemoveSlot != null) {
                    onRemoveSlot.accept(payItemSlots.get(i));
                }
                return true;
            }
        }

        if (confirmButton != null && confirmButton.isMouseOver(mouseX, mouseY)) {
            return confirmButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable("gui.ruralroutes.trade_station.trade_area"));
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}
