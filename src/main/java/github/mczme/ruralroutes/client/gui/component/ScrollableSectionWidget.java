package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.menu.container.TradeDisplayContainer;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 可横向滚动的物品清单组件
 * 布局：奇数位置在上排，偶数位置在下排
 */
public class ScrollableSectionWidget extends AbstractWidget {

    private static final int CARD_SIZE = 18;
    private static final int CARD_SPACING = 2;
    private static final int ROWS = 2;
    private static final int PADDING = 4;
    private static final int SCROLLBAR_HEIGHT = 4;
    private static final int SCROLLBAR_MARGIN = 4;

    private final List<ItemCardWidget> cards = new ArrayList<>();
    private final List<ItemCardWidget> visibleCards = new ArrayList<>();
    private List<TradeSlot> slots = new ArrayList<>();
    private Consumer<ItemCardWidget> onCardClick;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private boolean dragging = false;

    private final int titleColor;
    private final int bgColor;

    public ScrollableSectionWidget(int x, int y, int width, int height, int titleColor, int bgColor, Component title) {
        super(x, y, width, height, title);
        this.titleColor = titleColor;
        this.bgColor = bgColor;
    }

    public ScrollableSectionWidget setOnCardClick(Consumer<ItemCardWidget> onCardClick) {
        this.onCardClick = onCardClick;
        return this;
    }

    /**
     * 设置 TradeSlot 列表
     */
    public void setSlots(List<TradeSlot> slots) {
        this.slots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        cards.clear();

        for (int i = 0; i < this.slots.size(); i++) {
            TradeSlot slot = this.slots.get(i);
            ItemCardWidget card = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
            card.setTradeSlot(slot);
            card.setOnClick(c -> {
                if (onCardClick != null) {
                    onCardClick.accept(c);
                }
            });
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

    public void setItems(List<ItemStack> items) {
        cards.clear();
        TradeDisplayContainer container = new TradeDisplayContainer(items.size());

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            TradeSlot slot = new TradeSlot(container, i, 0, 0);
            slot.setDisplayStack(stack.copy());
            slot.setBaseStock(stack.getCount());

            ItemCardWidget card = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
            card.setTradeSlot(slot);
            card.setOnClick(c -> {
                if (onCardClick != null) {
                    onCardClick.accept(c);
                }
            });
            cards.add(card);
        }
        updateLayout();
    }

    public void addItem(ItemStack stack) {
        TradeDisplayContainer container = new TradeDisplayContainer(cards.size() + 1);
        TradeSlot slot = new TradeSlot(container, cards.size(), 0, 0);
        slot.setDisplayStack(stack.copy());
        slot.setBaseStock(stack.getCount());

        ItemCardWidget card = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
        card.setTradeSlot(slot);
        card.setOnClick(c -> {
            if (onCardClick != null) {
                onCardClick.accept(c);
            }
        });
        cards.add(card);
        updateLayout();
    }

    public void clearItems() {
        cards.clear();
        scrollOffset = 0;
        updateLayout();
    }

    public List<ItemCardWidget> getCards() {
        return cards;
    }

    /**
     * 更新指定索引卡片的数量
     */
    public void updateCardCount(int index, int newCount) {
        if (index >= 0 && index < cards.size()) {
            TradeSlot slot = cards.get(index).getTradeSlot();
            if (slot != null) {
                slot.setBaseStock(newCount);
            }
        }
    }

    private void updateLayout() {
        maxScroll = Math.max(0, calculateContentWidth() - (getWidth() - PADDING * 2));
    }

    private int calculateContentWidth() {
        // 计算需要的列数：每2个物品占一列
        int cols = (cards.size() + 1) / 2;
        return cols * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), bgColor);

        // 绘制标题
        guiGraphics.drawString(Minecraft.getInstance().font, getMessage(), getX() + PADDING, getY() + PADDING, titleColor);

        // 裁剪区域
        int contentX = getX() + PADDING;
        int contentY = getY() + PADDING + 12;
        int contentWidth = getWidth() - PADDING * 2;
        int contentHeight = ROWS * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;

        enableScissor(guiGraphics, contentX, contentY, contentX + contentWidth, contentY + contentHeight);

        visibleCards.clear();

        // 计算可见卡片位置
        for (int i = 0; i < cards.size(); i++) {
            ItemCardWidget card = cards.get(i);

            // 布局：奇数位置(索引偶数)在上排，偶数位置(索引奇数)在下排
            int col = i / 2;
            int row = (i % 2 == 0) ? 0 : 1;

            int cardX = contentX + col * (CARD_SIZE + CARD_SPACING) - scrollOffset;
            int cardY = contentY + row * (CARD_SIZE + CARD_SPACING);

            card.setX(cardX);
            card.setY(cardY);

            // 只渲染在可见区域内的卡片
            if (cardX + CARD_SIZE >= contentX && cardX < contentX + contentWidth) {
                visibleCards.add(card);
                card.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        disableScissor(guiGraphics);

        // 绘制滚动条
        renderScrollbar(guiGraphics);
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        if (maxScroll <= 0) return;

        int barWidth = Math.max(20, (getWidth() - PADDING * 2) * (getWidth() - PADDING * 2) / (calculateContentWidth() + getWidth() - PADDING * 2));
        int barX = getX() + PADDING + (int) ((getWidth() - PADDING * 2 - barWidth) * scrollOffset / (float) maxScroll);
        int barY = getY() + getHeight() - SCROLLBAR_HEIGHT - SCROLLBAR_MARGIN;

        fill(guiGraphics, barX, barY, barWidth, SCROLLBAR_HEIGHT, 0x80AAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isValidClickButton(button)) return false;

        // 先检查鼠标是否在组件范围内
        if (!isMouseOver(mouseX, mouseY)) return false;

        // 检查是否点击滚动条
        if (maxScroll > 0 && mouseY >= getY() + getHeight() - SCROLLBAR_HEIGHT - SCROLLBAR_MARGIN) {
            dragging = true;
            return true;
        }

        // 检查是否点击卡片
        for (ItemCardWidget card : visibleCards) {
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
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (int) dragX));
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}
