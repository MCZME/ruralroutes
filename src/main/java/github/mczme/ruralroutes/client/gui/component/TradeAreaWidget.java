package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.menu.container.TradeDisplayContainer;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
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
import java.util.function.BiConsumer;

/**
 * 交易区组件 - 左右布局，显示可点击的卡片和确认按钮
 * 支持多物品暂存
 */
public class TradeAreaWidget extends AbstractWidget {

    private static final int BG_COLOR = 0x40333333;
    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int GIVE_AREA_COLOR = 0x40FFAA00;
    private static final int RECEIVE_AREA_COLOR = 0x4000AAFF;
    private static final int PADDING = 4;
    private static final int CARD_SIZE = 18;
    private static final int CARD_SPACING = 2;
    private static final int MAX_VISIBLE_CARDS = 4;

    private Button confirmButton;
    private List<ItemCardWidget> giveCards = new ArrayList<>();  // 付出物品卡片列表
    private ItemCardWidget receiveCard;  // 获得卡片（显示货币总计）
    private PendingTradeSlot receiveSlot;  // 货币显示用的虚拟槽位

    private BiConsumer<Integer, ItemCardWidget> onGiveCardClick;  // 点击付出卡片的回调（带索引）
    private int totalPrice = 0;
    private boolean isBuyTrade = true;

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

        // 创建货币显示用的虚拟槽位
        receiveSlot = new PendingTradeSlot(new TradeDisplayContainer(1), 0, 0, 0);
        receiveSlot.setItemId(RRItems.COPPER_COIN.getId());
        receiveSlot.setIsBuy(true);

        // 创建获得卡片（仅显示，不可点击）
        receiveCard = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
        receiveCard.setTradeSlot(receiveSlot);
    }

    /**
     * 设置点击付出卡片的回调（用于移除条目）
     * @param onClick 回调函数，参数为条目索引和卡片
     */
    public void setOnGiveCardClick(BiConsumer<Integer, ItemCardWidget> onClick) {
        this.onGiveCardClick = onClick;
    }

    /**
     * 设置暂存区槽位
     * @param slots 槽位列表
     * @param isBuyTrade 是否为购买交易
     */
    public void setPendingSlots(List<PendingTradeSlot> slots, boolean isBuyTrade) {
        this.isBuyTrade = isBuyTrade;
        this.giveCards.clear();

        // 创建新卡片列表
        for (int i = 0; i < slots.size(); i++) {
            PendingTradeSlot slot = slots.get(i);
            ItemCardWidget card = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
            card.setTradeSlot(slot);  // 直接使用 TradeSlot

            // 设置点击回调（移除条目）
            final int slotIndex = i;
            card.setOnClick(c -> {
                if (onGiveCardClick != null) {
                    onGiveCardClick.accept(slotIndex, c);
                }
            });

            giveCards.add(card);
        }
    }

    /**
     * 设置总价（显示在获得卡片）
     */
    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
        if (receiveSlot != null) {
            receiveSlot.setDisplayStack(new ItemStack(RRItems.COPPER_COIN.get(), totalPrice));
            receiveSlot.setBaseStock(totalPrice);
        }
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * 清空交易区内容
     */
    public void clearContent() {
        giveCards.clear();
        totalPrice = 0;
        if (receiveSlot != null) {
            receiveSlot.clear();
        }
    }

    /**
     * 是否有交易内容
     */
    public boolean hasContent() {
        return !giveCards.isEmpty();
    }

    /**
     * 获取条目数量
     */
    public int getEntryCount() {
        return giveCards.size();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), BG_COLOR);

        var font = Minecraft.getInstance().font;
        int sideWidth = getWidth() * 2 / 5;
        int titleHeight = 12;
        int areaHeight = getHeight() - PADDING * 3 - titleHeight - 20; // 为按钮留空间
        int contentY = getY() + PADDING;

        // "你付出" 区域（左侧）
        String giveLabel = isBuyTrade ? "gui.ruralroutes.trade_station.you_pay" : "gui.ruralroutes.trade_station.you_give";
        guiGraphics.drawString(font,
            Component.translatable(giveLabel),
            getX() + PADDING, contentY, LABEL_COLOR);
        int areaY = contentY + titleHeight;
        fill(guiGraphics, getX() + PADDING, areaY, sideWidth - PADDING * 2, areaHeight, GIVE_AREA_COLOR);

        // 计算卡片位置并渲染付出卡片
        int giveAreaX = getX() + PADDING;
        int giveAreaWidth = sideWidth - PADDING * 2;
        int giveAreaContentWidth = giveCards.size() * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
        int giveStartX = giveAreaX + Math.max(0, (giveAreaWidth - giveAreaContentWidth) / 2);
        int giveItemY = areaY + (areaHeight - CARD_SIZE) / 2;

        for (int i = 0; i < giveCards.size() && i < MAX_VISIBLE_CARDS; i++) {
            ItemCardWidget card = giveCards.get(i);
            int cardX = giveStartX + i * (CARD_SIZE + CARD_SPACING);
            card.setX(cardX);
            card.setY(giveItemY);
            card.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 如果超过最大可见数量，显示省略号
        if (giveCards.size() > MAX_VISIBLE_CARDS) {
            String moreText = "+" + (giveCards.size() - MAX_VISIBLE_CARDS);
            int moreX = giveStartX + MAX_VISIBLE_CARDS * (CARD_SIZE + CARD_SPACING);
            guiGraphics.drawString(font, moreText, moreX, giveItemY + 5, LABEL_COLOR);
        }

        // 中间箭头
        int centerX = getX() + getWidth() / 2;
        guiGraphics.drawCenteredString(font, "->", centerX, areaY + areaHeight / 2 - 10, LABEL_COLOR);

        // "你获得" 区域（右侧）
        int receiveX = getX() + getWidth() - sideWidth;
        String receiveLabel = isBuyTrade ? "gui.ruralroutes.trade_station.you_receive" : "gui.ruralroutes.trade_station.you_get_paid";
        guiGraphics.drawString(font,
            Component.translatable(receiveLabel),
            receiveX, contentY, LABEL_COLOR);
        fill(guiGraphics, receiveX, areaY, sideWidth - PADDING * 2, areaHeight, RECEIVE_AREA_COLOR);

        // 计算卡片位置并渲染获得卡片
        int receiveItemX = receiveX + (sideWidth - PADDING * 2 - CARD_SIZE) / 2;
        int receiveItemY = areaY + (areaHeight - CARD_SIZE) / 2;
        if (receiveCard != null) {
            receiveCard.setX(receiveItemX);
            receiveCard.setY(receiveItemY);
            receiveCard.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 显示总价文本
        if (totalPrice > 0) {
            String priceText = totalPrice + " 铜";
            int priceX = receiveItemX + (CARD_SIZE - font.width(priceText)) / 2;
            int priceY = receiveItemY + CARD_SIZE + 2;
            guiGraphics.drawString(font, priceText, priceX, priceY, 0xFFD700);
        }

        // 渲染确认按钮
        if (confirmButton != null) {
            confirmButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查付出卡片点击（移除条目）
        for (int i = 0; i < giveCards.size() && i < MAX_VISIBLE_CARDS; i++) {
            ItemCardWidget card = giveCards.get(i);
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