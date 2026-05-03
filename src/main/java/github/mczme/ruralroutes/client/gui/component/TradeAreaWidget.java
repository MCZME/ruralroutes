package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import github.mczme.ruralroutes.register.RRItems;
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
 * - 左侧：玩家想要的（获得）- 包含买入的物品卡片 + 卖出获得的货币卡片
 * - 右侧：玩家支付的（付出）- 包含卖出的物品卡片 + 买入支付的货币卡片
 * - 底部：确认按钮
 */
public class TradeAreaWidget extends AbstractWidget {

    private static final int BG_COLOR = 0x40333333;
    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int WANT_AREA_COLOR = 0x4000AAFF;    // 蓝色 - 玩家获得
    private static final int PAY_AREA_COLOR = 0x40FFAA00;     // 橙色 - 玩家付出
    private static final int PADDING = 4;
    private static final int CARD_SIZE = 18;
    private static final int CARD_SPACING = 2;
    private static final int MAX_VISIBLE_CARDS = 4;
    private static final int SETTLEMENT_HEIGHT = 24;

    private Button confirmButton;

    // 物品卡片（可点击取消）
    private List<ItemCardWidget> wantItemCards = new ArrayList<>();  // 买入的物品（玩家获得）
    private List<ItemCardWidget> payItemCards = new ArrayList<>();   // 卖出的物品（玩家付出）

    // 货币卡片（不可点击）
    private ItemCardWidget wantCoinCard;  // 卖出获得的货币总额
    private ItemCardWidget payCoinCard;   // 买入支付的货币总额

    // 货币总额
    private int wantCoinValue = 0;  // 玩家获得的货币
    private int payCoinValue = 0;   // 玩家支付的货币

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
     * 根据 slot.isBuy() 分配物品卡片和货币卡片
     */
    public void setPendingSlots(List<PendingTradeSlot> slots, boolean isBuyTrade) {
        this.wantItemCards.clear();
        this.payItemCards.clear();
        this.wantCoinCard = null;
        this.payCoinCard = null;
        this.wantCoinValue = 0;
        this.payCoinValue = 0;

        for (PendingTradeSlot slot : slots) {
            int value = slot.getPrice() * slot.getBaseStock();

            if (slot.isBuy()) {
                // 买入：物品在左侧，货币在右侧
                ItemCardWidget itemCard = createClickableItemCard(slot);
                wantItemCards.add(itemCard);
                payCoinValue += value;
            } else {
                // 卖出：物品在右侧，货币在左侧
                ItemCardWidget itemCard = createClickablePayItemCard(slot);
                payItemCards.add(itemCard);
                wantCoinValue += value;
            }
        }

        // 创建货币卡片（仅当总额 > 0）
        if (wantCoinValue > 0) {
            wantCoinCard = createCoinCard(wantCoinValue);
        }
        if (payCoinValue > 0) {
            payCoinCard = createCoinCard(payCoinValue);
        }
    }

    /**
     * 创建可点击的物品卡片（左侧买入物品）
     */
    private ItemCardWidget createClickableItemCard(PendingTradeSlot slot) {
        ItemCardWidget card = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
        card.setTradeSlot(slot);
        card.setOnClick(c -> {
            if (onRemoveSlot != null) {
                onRemoveSlot.accept(slot);
            }
        });
        return card;
    }

    /**
     * 创建可点击的物品卡片（右侧卖出物品）
     */
    private ItemCardWidget createClickablePayItemCard(PendingTradeSlot slot) {
        ItemCardWidget card = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
        card.setTradeSlot(slot);
        card.setOnClick(c -> {
            if (onRemoveSlot != null) {
                onRemoveSlot.accept(slot);
            }
        });
        return card;
    }

    /**
     * 创建货币卡片（不可点击）
     */
    private ItemCardWidget createCoinCard(int value) {
        ItemCardWidget card = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
        TradeSlot coinSlot = new TradeSlot(null, -1, 0, 0);
        coinSlot.setDisplayStack(new ItemStack(RRItems.COPPER_COIN.get()));
        coinSlot.setBaseStock(value);
        coinSlot.setPrice(value);
        card.setTradeSlot(coinSlot);
        // 不设置点击回调，货币卡片不可点击
        return card;
    }

    /**
     * 设置总价（兼容旧接口）
     */
    public void setTotalPrice(int totalPrice) {
        // 不再使用，保留兼容性
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * 清空交易区内容
     */
    public void clearContent() {
        wantItemCards.clear();
        payItemCards.clear();
        wantCoinCard = null;
        payCoinCard = null;
        wantCoinValue = 0;
        payCoinValue = 0;
    }

    /**
     * 是否有交易内容
     */
    public boolean hasContent() {
        return !wantItemCards.isEmpty() || !payItemCards.isEmpty() ||
               wantCoinCard != null || payCoinCard != null;
    }

    /**
     * 获取条目数量
     */
    public int getEntryCount() {
        return wantItemCards.size() + payItemCards.size();
    }

    /**
     * 获取净货币价值（正值=玩家获得货币，负值=玩家付出货币）
     */
    public int getNetValue() {
        return wantCoinValue - payCoinValue;
    }

    /**
     * 获取左侧所有卡片（用于渲染工具提示）
     */
    public List<ItemCardWidget> getWantCards() {
        List<ItemCardWidget> cards = new ArrayList<>(wantItemCards);
        if (wantCoinCard != null) {
            cards.add(wantCoinCard);
        }
        return cards;
    }

    /**
     * 获取右侧所有卡片（用于渲染工具提示）
     */
    public List<ItemCardWidget> getPayCards() {
        List<ItemCardWidget> cards = new ArrayList<>(payItemCards);
        if (payCoinCard != null) {
            cards.add(payCoinCard);
        }
        return cards;
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

        // 计算左侧卡片数量
        int wantCardCount = wantItemCards.size() + (wantCoinCard != null ? 1 : 0);
        int wantContentWidth = wantCardCount * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
        int wantStartX = wantAreaX + Math.max(0, (sideWidth - wantContentWidth) / 2);
        int wantItemY = wantAreaY + (areaHeight - CARD_SIZE) / 2;

        // 渲染左侧物品卡片
        int cardIndex = 0;
        for (int i = 0; i < wantItemCards.size() && cardIndex < MAX_VISIBLE_CARDS; i++, cardIndex++) {
            ItemCardWidget card = wantItemCards.get(i);
            int cardX = wantStartX + cardIndex * (CARD_SIZE + CARD_SPACING);
            card.setX(cardX);
            card.setY(wantItemY);
            card.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 渲染左侧货币卡片
        if (wantCoinCard != null && cardIndex < MAX_VISIBLE_CARDS) {
            int cardX = wantStartX + cardIndex * (CARD_SIZE + CARD_SPACING);
            wantCoinCard.setX(cardX);
            wantCoinCard.setY(wantItemY);
            wantCoinCard.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (wantCardCount > MAX_VISIBLE_CARDS) {
            String moreText = "+" + (wantCardCount - MAX_VISIBLE_CARDS);
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

        // 计算右侧卡片数量
        int payCardCount = payItemCards.size() + (payCoinCard != null ? 1 : 0);
        int payContentWidth = payCardCount * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
        int payStartX = payAreaX + Math.max(0, (sideWidth - payContentWidth) / 2);
        int payItemY = payAreaY + (areaHeight - CARD_SIZE) / 2;

        // 渲染右侧物品卡片
        cardIndex = 0;
        for (int i = 0; i < payItemCards.size() && cardIndex < MAX_VISIBLE_CARDS; i++, cardIndex++) {
            ItemCardWidget card = payItemCards.get(i);
            int cardX = payStartX + cardIndex * (CARD_SIZE + CARD_SPACING);
            card.setX(cardX);
            card.setY(payItemY);
            card.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 渲染右侧货币卡片
        if (payCoinCard != null && cardIndex < MAX_VISIBLE_CARDS) {
            int cardX = payStartX + cardIndex * (CARD_SIZE + CARD_SPACING);
            payCoinCard.setX(cardX);
            payCoinCard.setY(payItemY);
            payCoinCard.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (payCardCount > MAX_VISIBLE_CARDS) {
            String moreText = "+" + (payCardCount - MAX_VISIBLE_CARDS);
            int moreX = payStartX + MAX_VISIBLE_CARDS * (CARD_SIZE + CARD_SPACING);
            guiGraphics.drawString(font, moreText, moreX, payItemY + 5, LABEL_COLOR);
        }

        // 渲染确认按钮
        if (confirmButton != null) {
            confirmButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        // 左侧物品卡片可点击
        for (int i = 0; i < wantItemCards.size() && i < MAX_VISIBLE_CARDS; i++) {
            ItemCardWidget card = wantItemCards.get(i);
            if (card.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // 右侧物品卡片可点击
        for (int i = 0; i < payItemCards.size() && i < MAX_VISIBLE_CARDS; i++) {
            ItemCardWidget card = payItemCards.get(i);
            if (card.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // 检查确认按钮点击
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
