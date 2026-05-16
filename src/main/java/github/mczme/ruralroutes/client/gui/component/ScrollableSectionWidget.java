package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.client.gui.GuiTextStyles;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 可横向滚动的物品清单组件
 * 支持两行卡片布局
 */
public class ScrollableSectionWidget extends AbstractWidget {

    // 卡片尺寸常量
    public static final int CARD_WIDTH = 65;
    public static final int CARD_HEIGHT = 26;
    private static final int CARD_SPACING = 2;
    private static final int ROWS = 2;

    private static final int TITLE_RIBBON_X = 0;
    private static final int TITLE_RIBBON_Y = 24;
    private static final int TITLE_RIBBON_WIDTH = 18;
    private static final int TITLE_VERTICAL_SPACING = 9;
    private static final int CONTENT_LEFT_INSET = 26;
    private static final int CONTENT_RIGHT_INSET = 12;
    private static final int CONTENT_OFFSET_Y = 17;
    private static final int SCROLLBAR_HEIGHT = 2;
    private static final int SCROLLBAR_MARGIN = 1;

    private final List<AbstractWidget> cards = new ArrayList<>();
    private final List<AbstractWidget> visibleCards = new ArrayList<>();
    private List<TradeSlot> slots = new ArrayList<>();
    private Consumer<AbstractWidget> onCardClick;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private boolean dragging = false;
    private int dragStartX = 0;
    private int dragStartScroll = 0;

    private final int titleColor;
    private final int bgColor;

    public ScrollableSectionWidget(int x, int y, int width, int height, int titleColor, int bgColor, Component title) {
        super(x, y, width, height, title);
        this.titleColor = titleColor;
        this.bgColor = bgColor;
    }

    /**
     * 设置卡片点击回调
     */
    public ScrollableSectionWidget setOnCardClickGeneric(Consumer<AbstractWidget> onCardClick) {
        this.onCardClick = onCardClick;
        return this;
    }

    /**
     * 设置 TradeSlot 列表（使用工厂函数创建卡片）
     */
    public void setSlots(List<TradeSlot> slots, BiFunction<Integer, Integer, AbstractWidget> cardFactory) {
        this.slots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        cards.clear();

        for (int i = 0; i < this.slots.size(); i++) {
            TradeSlot slot = this.slots.get(i);
            AbstractWidget card = cardFactory.apply(0, 0);
            if (card instanceof TradeOfferCardWidget offerCard) {
                offerCard.setTradeSlot(slot);
                offerCard.setOnClick(c -> {
                    if (onCardClick != null) {
                        onCardClick.accept(c);
                    }
                });
            }
            cards.add(card);
        }
        scrollOffset = 0;
        updateLayout();
    }

    /**
     * 获取 TradeSlot 列表
     */
    public List<TradeSlot> getSlots() {
        return slots;
    }

    /**
     * 清空卡片
     */
    public void clearItems() {
        cards.clear();
        scrollOffset = 0;
        updateLayout();
    }

    /**
     * 获取所有卡片
     */
    public List<AbstractWidget> getCards() {
        return cards;
    }

    /**
     * 当前布局下内容实际渲染所需的最小高度。
     * 以第二排卡片底部为准，不裁掉已渲染内容。
     */
    public static int getRequiredHeight() {
        return CONTENT_OFFSET_Y + ROWS * CARD_HEIGHT + (ROWS - 1) * CARD_SPACING;
    }

    /**
     * 更新指定索引卡片的数量
     */
    public void updateCardCount(int index, int newCount) {
        if (index >= 0 && index < cards.size()) {
            AbstractWidget card = cards.get(index);
            if (card instanceof TradeOfferCardWidget offerCard) {
                TradeSlot slot = offerCard.getTradeSlot();
                if (slot != null) {
                    slot.setBaseStock(newCount);
                }
            }
        }
    }

    private void updateLayout() {
        maxScroll = Math.max(0, calculateContentWidth() - getContentWidth());
    }

    private int calculateContentWidth() {
        int cols = (cards.size() + ROWS - 1) / ROWS;
        return cols * (CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if ((bgColor >>> 24) != 0) {
            fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), bgColor);
        }

        renderVerticalTitle(guiGraphics);

        // 计算内容区域
        int contentX = getX() + CONTENT_LEFT_INSET;
        int contentY = getY() + CONTENT_OFFSET_Y;
        int contentWidth = getContentWidth();
        int contentHeight = ROWS * CARD_HEIGHT + (ROWS - 1) * CARD_SPACING;

        enableScissor(guiGraphics, contentX, contentY, contentX + contentWidth, contentY + contentHeight);

        visibleCards.clear();

        for (int i = 0; i < cards.size(); i++) {
            AbstractWidget card = cards.get(i);

            int col = i / ROWS;
            int row = i % ROWS;

            int cardX = contentX + col * (CARD_WIDTH + CARD_SPACING) - scrollOffset;
            int cardY = contentY + row * (CARD_HEIGHT + CARD_SPACING);

            card.setX(cardX);
            card.setY(cardY);

            if (cardX + CARD_WIDTH >= contentX && cardX < contentX + contentWidth) {
                visibleCards.add(card);
                card.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        disableScissor(guiGraphics);

        renderScrollbar(guiGraphics);
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        if (maxScroll <= 0) return;

        int trackWidth = getContentWidth();
        int barWidth = Math.max(20, trackWidth * trackWidth / (calculateContentWidth() + trackWidth));
        int barX = getX() + CONTENT_LEFT_INSET + (int) ((trackWidth - barWidth) * scrollOffset / (float) maxScroll);
        int barY = getY() + getHeight() - SCROLLBAR_HEIGHT - SCROLLBAR_MARGIN;

        fill(guiGraphics, barX, barY, barWidth, SCROLLBAR_HEIGHT, 0x907E5D36);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isValidClickButton(button)) return false;

        if (!isMouseOver(mouseX, mouseY)) return false;

        if (maxScroll > 0 && mouseY >= getY() + getHeight() - SCROLLBAR_HEIGHT - SCROLLBAR_MARGIN) {
            dragging = true;
            dragStartX = (int) mouseX;
            dragStartScroll = scrollOffset;
            return true;
        }

        for (AbstractWidget card : visibleCards) {
            if (card.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && maxScroll > 0) {
            int trackWidth = getContentWidth();
            int barWidth = Math.max(20, trackWidth * trackWidth / (calculateContentWidth() + trackWidth));

            float mouseDelta = (float) mouseX - dragStartX;
            float scrollRatio = mouseDelta / (trackWidth - barWidth);
            int newScroll = dragStartScroll + (int) (scrollRatio * maxScroll);

            scrollOffset = Math.max(0, Math.min(maxScroll, newScroll));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovered() && maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY * 20));
            return true;
        }
        return false;
    }

    private void enableScissor(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        guiGraphics.enableScissor(x1, y1, x2, y2);
    }

    private void disableScissor(GuiGraphics guiGraphics) {
        guiGraphics.disableScissor();
    }

    private int getContentWidth() {
        return getWidth() - CONTENT_LEFT_INSET - CONTENT_RIGHT_INSET;
    }

    private void renderVerticalTitle(GuiGraphics guiGraphics) {
        var font = Minecraft.getInstance().font;
        String text = getMessage().getString();
        if (text == null || text.isEmpty()) {
            return;
        }

        int baseX = getX() + TITLE_RIBBON_X;
        int baseY = getY() + TITLE_RIBBON_Y;

        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            int charX = baseX + (TITLE_RIBBON_WIDTH - font.width(ch)) / 2;
            int charY = baseY + i * TITLE_VERTICAL_SPACING;
            guiGraphics.drawString(font, GuiTextStyles.uniformLiteral(ch), charX, charY, titleColor, true);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}
