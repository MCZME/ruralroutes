package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.client.gui.GuiTextStyles;
import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.menu.slot.PendingTradeSlot;
import github.mczme.ruralroutes.register.RRItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 交易区组件 - 双行卡片布局 + 两侧独立滚动
 * - 左侧：玩家将获得的 - 包含买入的物品 + 卖出获得的货币（同种货币合并显示）
 * - 右侧：玩家需支付的 - 包含卖出的物品 + 买入支付的货币/输入物品（同种货币合并显示）
 * - 底部：删除按钮 + 确认按钮
 * - 删除模式：点击删除按钮进入，点击卡片级联删除所有关联条目
 * - 空状态：居中提示文字
 */
public class TradeAreaWidget extends AbstractWidget {

    private static final int LABEL_COLOR = 0xFF5C4330;
    private static final int AREA_BG_COLOR = 0x30291B0E;
    private static final int CARD_BG_COLOR = 0x90E9D4A7;
    private static final int CARD_HOVER_COLOR = 0xB0F4E4BC;
    private static final int DELETE_BORDER_COLOR = 0xFF9B3A2E;
    private static final int DELETE_OVERLAY_COLOR = 0x30FF0000;
    private static final int DELETE_HOVER_OVERLAY_COLOR = 0x50FF0000;
    private static final int OUT_OF_STOCK_COLOR = 0xFF9B3A2E;
    private static final int SCROLLBAR_COLOR = 0x907E5D36;
    private static final int EMPTY_TEXT_COLOR = 0xFF5C4330;

    private static final int PADDING = 6;
    private static final int CARD_SIZE = 18;
    private static final int CARD_SPACING = 2;
    private static final int ROWS = 2;
    private static final int CONTENT_OFFSET_Y = 6;
    private static final int LABEL_HEIGHT = 10;
    private static final int LABEL_TO_CARDS_GAP = 3;
    private static final int ROW_SPACING = 2;
    private static final int SCROLLBAR_HEIGHT = 2;
    private static final int SCROLLBAR_TO_BUTTON_GAP = 2;
    private static final int BUTTON_HEIGHT = 16;
    private static final int DELETE_BUTTON_WIDTH = 46;
    private static final int CONFIRM_BUTTON_WIDTH = 80;
    private static final int BUTTON_GAP = 6;
    private static final int SECTION_WIDTH = 96;
    private static final int SECTION_GAP = 24;
    private static final int BUTTON_DIVIDER_COLOR = 0x406C4D31;
    private static final int BUTTON_DIVIDER_HIGHLIGHT = 0x25EBDDBE;
    private static final int BUTTON_SHADOW_COLOR = 0x35000000;
    private static final int BUTTON_TEXT_COLOR = 0xFF2F2014;
    private static final int BUTTON_DISABLED_BG_COLOR = 0x70695842;
    private static final int BUTTON_DISABLED_BORDER_COLOR = 0x905C4D39;
    private static final int BUTTON_DISABLED_TEXT_COLOR = 0xB0998A70;
    private static final int BUTTON_TOP_HIGHLIGHT_COLOR = 0x35FFF4D5;
    private static final int BUTTON_BOTTOM_SHADE_COLOR = 0x28000000;
    private static final int DELETE_BUTTON_BG_COLOR = 0xC07C4A3C;
    private static final int DELETE_BUTTON_HOVER_COLOR = 0xD08F5A49;
    private static final int DELETE_BUTTON_ACTIVE_COLOR = 0xD0A13F34;
    private static final int DELETE_BUTTON_BORDER_COLOR = 0xFF6F2D24;
    private static final int DELETE_BUTTON_ACTIVE_BORDER_COLOR = 0xFFB44737;
    private static final int DELETE_BUTTON_ACCENT_COLOR = 0xFFD99B77;
    private static final int CONFIRM_BUTTON_BG_COLOR = 0xC08E7642;
    private static final int CONFIRM_BUTTON_HOVER_COLOR = 0xD0A58A51;
    private static final int CONFIRM_BUTTON_BORDER_COLOR = 0xFF70572D;
    private static final int CONFIRM_BUTTON_ACCENT_COLOR = 0xFFF0D38B;
    private Button confirmButton;
    private Button deleteButton;

    private List<PendingTradeSlot> wantItemSlots = new ArrayList<>();
    private List<PendingTradeSlot> payItemSlots = new ArrayList<>();

    private List<CardInfo> cachedWantCards = new ArrayList<>();
    private List<CardInfo> cachedPayCards = new ArrayList<>();

    private int wantScrollOffset = 0;
    private int payScrollOffset = 0;
    private int wantMaxScroll = 0;
    private int payMaxScroll = 0;

    private boolean isDraggingWantScrollbar = false;
    private boolean isDraggingPayScrollbar = false;
    private int dragStartX = 0;
    private int dragStartScroll = 0;

    private boolean isDeleteMode = false;

    private BiConsumer<PendingTradeSlot, Boolean> onRemoveSlot;

    public TradeAreaWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    /**
     * 初始化组件，创建删除按钮和确认按钮
     */
    public void init(Button.OnPress confirmAction) {
        int buttonY = getY() + CONTENT_OFFSET_Y + LABEL_HEIGHT + LABEL_TO_CARDS_GAP
            + ROWS * CARD_SIZE + (ROWS - 1) * ROW_SPACING
            + SCROLLBAR_HEIGHT + SCROLLBAR_TO_BUTTON_GAP;

        int totalButtonWidth = DELETE_BUTTON_WIDTH + BUTTON_GAP + CONFIRM_BUTTON_WIDTH;
        int buttonsStartX = getX() + (getWidth() - totalButtonWidth) / 2;

        deleteButton = Button.builder(
            GuiTextStyles.uniform(Component.translatable("gui.ruralroutes.trade_station.delete")),
            btn -> {
                if (Screen.hasShiftDown() && hasContent() && onRemoveSlot != null) {
                    onRemoveSlot.accept(null, true);
                    return;
                }
                isDeleteMode = !isDeleteMode;
                updateDeleteButtonStyle();
            }
        ).bounds(buttonsStartX, buttonY, DELETE_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        confirmButton = Button.builder(
            GuiTextStyles.uniform(Component.translatable("gui.ruralroutes.trade_station.confirm")),
            confirmAction
        ).bounds(buttonsStartX + DELETE_BUTTON_WIDTH + BUTTON_GAP, buttonY, CONFIRM_BUTTON_WIDTH, BUTTON_HEIGHT).build();

        updateDeleteButtonStyle();
    }

    /**
     * 更新删除按钮的显示文本（普通/激活状态）
     */
    private void updateDeleteButtonStyle() {
        if (deleteButton == null) return;
        if (isDeleteMode) {
            deleteButton.setMessage(
                GuiTextStyles.uniform(Component.translatable("gui.ruralroutes.trade_station.delete_active"))
            );
        } else {
            deleteButton.setMessage(
                GuiTextStyles.uniform(Component.translatable("gui.ruralroutes.trade_station.delete"))
            );
        }
    }

    /**
     * 设置移除暂存槽位的回调
     */
    public void setOnPendingSlotRemove(BiConsumer<PendingTradeSlot, Boolean> callback) {
        this.onRemoveSlot = callback;
    }

    /**
     * 设置暂存区槽位，按买卖方向分流并重建卡片缓存
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
        if (!hasContent() && isDeleteMode) {
            isDeleteMode = false;
            updateDeleteButtonStyle();
        }
    }

    /**
     * 重建卡片缓存，更新滚动范围
     */
    private void rebuildCardCache() {
        cachedWantCards = buildWantCards();
        cachedPayCards = buildPayCards();
        updateMaxScroll();
        wantScrollOffset = Math.min(wantScrollOffset, wantMaxScroll);
        payScrollOffset = Math.min(payScrollOffset, payMaxScroll);
    }

    /**
     * 更新两侧最大滚动量
     */
    private void updateMaxScroll() {
        wantMaxScroll = Math.max(0, calculateContentWidth(cachedWantCards.size()) - SECTION_WIDTH);
        payMaxScroll = Math.max(0, calculateContentWidth(cachedPayCards.size()) - SECTION_WIDTH);
    }

    /**
     * 根据卡片数量计算内容总宽度
     */
    private int calculateContentWidth(int cardCount) {
        if (cardCount <= 0) return 0;
        int cols = (cardCount + ROWS - 1) / ROWS;
        return cols * (CARD_SIZE + CARD_SPACING) - CARD_SPACING;
    }

    /**
     * 获取确认按钮
     */
    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * 清空交易区内容，重置所有状态
     */
    public void clearContent() {
        wantItemSlots.clear();
        payItemSlots.clear();
        cachedWantCards.clear();
        cachedPayCards.clear();
        wantScrollOffset = 0;
        payScrollOffset = 0;
        wantMaxScroll = 0;
        payMaxScroll = 0;
        isDeleteMode = false;
        updateDeleteButtonStyle();
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

    /**
     * 是否处于删除模式
     */
    public boolean isDeleteMode() {
        return isDeleteMode;
    }

    /**
     * 获取鼠标悬停的卡片信息，供 Screen 层渲染 Tooltip 使用
     */
    public HoveredCardInfo getHoveredCardInfo(int mouseX, int mouseY) {
        CardInfo wantHit = findHoveredCard(cachedWantCards, wantScrollOffset, getWantAreaX(), getCardsAreaY(), mouseX, mouseY);
        if (wantHit != null) return new HoveredCardInfo(wantHit.stack, wantHit.count, isEditableCard(wantHit));

        CardInfo payHit = findHoveredCard(cachedPayCards, payScrollOffset, getPayAreaX(), getCardsAreaY(), mouseX, mouseY);
        if (payHit != null) return new HoveredCardInfo(payHit.stack, payHit.count, isEditableCard(payHit));

        return null;
    }

    /**
     * 获取左侧（获得）区域的 X 坐标
     */
    private int getWantAreaX() {
        return getX() + PADDING;
    }

    /**
     * 获取右侧（支付）区域的 X 坐标
     */
    private int getPayAreaX() {
        return getX() + PADDING + SECTION_WIDTH + SECTION_GAP;
    }

    /**
     * 获取卡片区域的 Y 坐标
     */
    private int getCardsAreaY() {
        return getY() + CONTENT_OFFSET_Y + LABEL_HEIGHT + LABEL_TO_CARDS_GAP;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        boolean hasEntries = hasContent();
        int cardsAreaY = getCardsAreaY();
        int cardsAreaHeight = ROWS * CARD_SIZE + (ROWS - 1) * ROW_SPACING;

        if (!hasEntries) {
            renderEmptyState(guiGraphics, font);
        } else {
            renderSection(guiGraphics, font, mouseX, mouseY,
                getWantAreaX(), cardsAreaY, cardsAreaHeight,
                cachedWantCards, wantScrollOffset, wantMaxScroll,
                "gui.ruralroutes.trade_station.want_area",
                true);

            renderSection(guiGraphics, font, mouseX, mouseY,
                getPayAreaX(), cardsAreaY, cardsAreaHeight,
                cachedPayCards, payScrollOffset, payMaxScroll,
                "gui.ruralroutes.trade_station.pay_area",
                false);
        }

        if (deleteButton != null) {
            deleteButton.active = hasEntries;
        }
        if (confirmButton != null) {
            confirmButton.active = hasEntries;
        }

        if (hasEntries) {
            renderButtonDivider(guiGraphics);

            if (deleteButton != null) {
                renderActionButton(guiGraphics, deleteButton, mouseX, mouseY, true);
            }
            if (confirmButton != null) {
                renderActionButton(guiGraphics, confirmButton, mouseX, mouseY, false);
            }
        }
    }

    /**
     * 渲染空状态提示
     */
    private void renderEmptyState(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font) {
        Component hint = GuiTextStyles.uniform(
            Component.translatable("gui.ruralroutes.trade_station.empty_hint"));
        int textWidth = font.width(hint);
        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2 - 10;
        guiGraphics.drawString(font, hint, centerX - textWidth / 2, centerY, EMPTY_TEXT_COLOR, false);
    }

    /**
     * 渲染单个区域（标签 + 背景 + 卡片 + 滚动条）
     */
    private void renderSection(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                               int mouseX, int mouseY,
                               int areaX, int cardsAreaY, int cardsAreaHeight,
                               List<CardInfo> cards, int scrollOffset, int maxScroll,
                               String labelKey, boolean isWantSide) {
        guiGraphics.drawString(font,
            GuiTextStyles.uniform(Component.translatable(labelKey)),
            areaX, cardsAreaY - LABEL_HEIGHT - LABEL_TO_CARDS_GAP, LABEL_COLOR, false);

        fill(guiGraphics, areaX, cardsAreaY, SECTION_WIDTH, cardsAreaHeight, AREA_BG_COLOR);

        enableScissor(guiGraphics, areaX, cardsAreaY, areaX + SECTION_WIDTH, cardsAreaY + cardsAreaHeight);

        for (int i = 0; i < cards.size(); i++) {
            CardInfo card = cards.get(i);
            int col = i / ROWS;
            int row = i % ROWS;
            int cardX = areaX + col * (CARD_SIZE + CARD_SPACING) - scrollOffset;
            int cardY = cardsAreaY + row * (CARD_SIZE + ROW_SPACING);

            if (cardX + CARD_SIZE < areaX || cardX > areaX + SECTION_WIDTH) continue;

            boolean isHovered = isCardHovered(cardX, cardY, mouseX, mouseY);
            renderItemCard(guiGraphics, card.stack, card.count, cardX, cardY, isHovered, isDeleteMode);
        }

        disableScissor(guiGraphics);

        renderScrollbar(guiGraphics, areaX, cardsAreaY + cardsAreaHeight + 1, maxScroll, scrollOffset);
    }

    /**
     * 渲染滚动条滑块
     */
    private void renderScrollbar(GuiGraphics guiGraphics, int trackX, int barY, int maxScroll, int scrollOffset) {
        if (maxScroll <= 0) return;

        int trackWidth = SECTION_WIDTH;
        int barWidth = Math.max(12, trackWidth * trackWidth / (maxScroll + trackWidth));
        int barX = trackX + (int) ((trackWidth - barWidth) * scrollOffset / (float) maxScroll);

        fill(guiGraphics, barX, barY, barWidth, SCROLLBAR_HEIGHT, SCROLLBAR_COLOR);
    }

    /**
     * 按钮上方的分隔线，让底部操作区与卡片区更有层次。
     */
    private void renderButtonDivider(GuiGraphics guiGraphics) {
        if (deleteButton == null && confirmButton == null) return;

        int dividerY = (deleteButton != null ? deleteButton.getY() : confirmButton.getY()) - 4;
        int dividerX = getX() + PADDING;
        int dividerWidth = getWidth() - PADDING * 2;
        fill(guiGraphics, dividerX, dividerY, dividerWidth, 1, BUTTON_DIVIDER_COLOR);
        fill(guiGraphics, dividerX + 6, dividerY + 1, Math.max(0, dividerWidth - 12), 1, BUTTON_DIVIDER_HIGHLIGHT);
    }

    /**
     * 自绘按钮，让操作区风格和交易卡片保持统一。
     */
    private void renderActionButton(GuiGraphics guiGraphics, Button button, int mouseX, int mouseY, boolean isDeleteButton) {
        var font = Minecraft.getInstance().font;
        int x = button.getX();
        int y = button.getY();
        int width = button.getWidth();
        int height = button.getHeight();
        boolean active = button.active;
        boolean hovered = active && button.isMouseOver(mouseX, mouseY);
        boolean toggled = isDeleteButton && isDeleteMode;

        int backgroundColor = resolveButtonBackground(active, hovered, toggled, isDeleteButton);
        int borderColor = resolveButtonBorder(active, toggled, isDeleteButton);
        int accentColor = isDeleteButton ? DELETE_BUTTON_ACCENT_COLOR : CONFIRM_BUTTON_ACCENT_COLOR;
        int textColor = active ? BUTTON_TEXT_COLOR : BUTTON_DISABLED_TEXT_COLOR;

        fill(guiGraphics, x + 1, y + 1, width, height, BUTTON_SHADOW_COLOR);
        fill(guiGraphics, x, y, width, height, backgroundColor);
        fill(guiGraphics, x + 1, y + 1, Math.max(0, width - 2), 1, BUTTON_TOP_HIGHLIGHT_COLOR);
        fill(guiGraphics, x + 1, y + height - 2, Math.max(0, width - 2), 1, BUTTON_BOTTOM_SHADE_COLOR);
        fill(guiGraphics, x + 1, y + 1, 2, Math.max(0, height - 2), active ? accentColor : BUTTON_DISABLED_BORDER_COLOR);

        if (hovered) {
            fill(guiGraphics, x + 1, y + 1, Math.max(0, width - 2), Math.max(0, height - 2), 0x12FFFFFF);
        }
        if (toggled) {
            fill(guiGraphics, x + width - 4, y + 1, 2, Math.max(0, height - 2), 0x55FFF1E4);
        }

        guiGraphics.renderOutline(x, y, width, height, borderColor);

        Component message = button.getMessage();
        int textX = x + (width - font.width(message)) / 2;
        int textY = y + (height - font.lineHeight) / 2;
        guiGraphics.drawString(font, message, textX, textY, textColor, false);
    }

    private int resolveButtonBackground(boolean active, boolean hovered, boolean toggled, boolean isDeleteButton) {
        if (!active) {
            return BUTTON_DISABLED_BG_COLOR;
        }
        if (isDeleteButton) {
            if (toggled) {
                return hovered ? brighten(DELETE_BUTTON_ACTIVE_COLOR, 0x080808) : DELETE_BUTTON_ACTIVE_COLOR;
            }
            return hovered ? DELETE_BUTTON_HOVER_COLOR : DELETE_BUTTON_BG_COLOR;
        }
        return hovered ? CONFIRM_BUTTON_HOVER_COLOR : CONFIRM_BUTTON_BG_COLOR;
    }

    private int resolveButtonBorder(boolean active, boolean toggled, boolean isDeleteButton) {
        if (!active) {
            return BUTTON_DISABLED_BORDER_COLOR;
        }
        if (isDeleteButton) {
            return toggled ? DELETE_BUTTON_ACTIVE_BORDER_COLOR : DELETE_BUTTON_BORDER_COLOR;
        }
        return CONFIRM_BUTTON_BORDER_COLOR;
    }

    private int brighten(int color, int amount) {
        int alpha = (color >>> 24) & 0xFF;
        int red = Math.min(255, ((color >>> 16) & 0xFF) + ((amount >>> 16) & 0xFF));
        int green = Math.min(255, ((color >>> 8) & 0xFF) + ((amount >>> 8) & 0xFF));
        int blue = Math.min(255, (color & 0xFF) + (amount & 0xFF));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * 卡片信息，包含物品、数量和来源暂存槽位列表
     * 合并后的货币卡片包含多个来源，删除时级联移除所有关联条目
     */
    private record CardInfo(ItemStack stack, int count, List<PendingTradeSlot> sourceSlots, boolean editable) {
        CardInfo(ItemStack stack, int count, PendingTradeSlot sourceSlot) {
            this(stack, count, List.of(sourceSlot), true);
        }
    }

    /**
     * 悬停卡片信息，供 Tooltip 渲染使用
     */
    public record HoveredCardInfo(ItemStack stack, int count, boolean editable) {}

    /**
     * 构建左侧（玩家获得）卡片列表
     * 包含买入的物品 + 卖出获得的货币（同种货币合并）
     */
    private List<CardInfo> buildWantCards() {
        List<CardInfo> cards = new ArrayList<>();

        for (PendingTradeSlot slot : wantItemSlots) {
            cards.add(new CardInfo(slot.getDisplayStack(), slot.getBaseStock(), slot));
        }

        List<CardInfo> currencyCards = new ArrayList<>();
        for (PendingTradeSlot slot : payItemSlots) {
            if (slot.getTradeType() == TradeContractType.CURRENCY_BASKET_DYNAMIC) {
                int multiplier = slot.getBaseStock();
                for (ItemStack priceStack : slot.getPriceStacks()) {
                    ItemStack scaled = priceStack.copy();
                    scaled.setCount(priceStack.getCount() * multiplier);
                    currencyCards.add(new CardInfo(scaled, scaled.getCount(), List.of(slot), false));
                }
            }
        }
        cards.addAll(mergeCurrencyCards(currencyCards));

        return cards;
    }

    /**
     * 构建右侧（玩家支付）卡片列表
     * 包含卖出的物品 + 买入支付的货币/输入物品（同种货币合并）
     */
    private List<CardInfo> buildPayCards() {
        List<CardInfo> cards = new ArrayList<>();

        for (PendingTradeSlot slot : payItemSlots) {
            cards.add(new CardInfo(slot.getDisplayStack(), slot.getBaseStock(), slot));
        }

        List<CardInfo> currencyCards = new ArrayList<>();
        for (PendingTradeSlot slot : wantItemSlots) {
            int multiplier = slot.getBaseStock();
            TradeContractType tradeType = slot.getTradeType();

            if (tradeType == TradeContractType.FIXED) {
                for (ItemStack inputStack : slot.getInputStacks()) {
                    ItemStack scaled = inputStack.copy();
                    scaled.setCount(inputStack.getCount() * multiplier);
                    currencyCards.add(new CardInfo(scaled, scaled.getCount(), List.of(slot), false));
                }
            } else {
                for (ItemStack priceStack : slot.getPriceStacks()) {
                    ItemStack scaled = priceStack.copy();
                    scaled.setCount(priceStack.getCount() * multiplier);
                    currencyCards.add(new CardInfo(scaled, scaled.getCount(), List.of(slot), false));
                }
            }
        }
        cards.addAll(mergeCurrencyCards(currencyCards));

        return cards;
    }

    /**
     * 合并同种货币卡片
     * 仅合并 #ruralroutes:currency 标签的物品，非货币物品保持独立
     * 合并后的卡片包含所有来源条目，删除时级联移除
     */
    private List<CardInfo> mergeCurrencyCards(List<CardInfo> currencyCards) {
        if (currencyCards.isEmpty()) return currencyCards;

        List<CardInfo> result = new ArrayList<>();
        Map<Item, List<CardInfo>> currencyGroups = new LinkedHashMap<>();

        for (CardInfo card : currencyCards) {
            if (card.stack.is(RRItemTags.CURRENCY)) {
                currencyGroups.computeIfAbsent(card.stack.getItem(), k -> new ArrayList<>()).add(card);
            } else {
                result.add(card);
            }
        }

        for (List<CardInfo> group : currencyGroups.values()) {
            if (group.size() == 1) {
                result.add(group.get(0));
            } else {
                ItemStack template = group.get(0).stack.copy();
                int totalCount = 0;
                List<PendingTradeSlot> allSources = new ArrayList<>();
                for (CardInfo card : group) {
                    totalCount += card.count;
                    allSources.addAll(card.sourceSlots);
                }
                template.setCount(totalCount);
                result.add(new CardInfo(template, totalCount, allSources, false));
            }
        }
        return result;
    }

    private boolean isEditableCard(CardInfo card) {
        return card != null && card.editable;
    }

    /**
     * 渲染物品卡片
     * 包含背景、物品图标、数量文字，删除模式下叠加红色遮罩和边框
     */
    private void renderItemCard(GuiGraphics guiGraphics, ItemStack stack, int count, int x, int y, boolean hovered, boolean deleteMode) {
        int bgColor = hovered ? CARD_HOVER_COLOR : CARD_BG_COLOR;
        fill(guiGraphics, x, y, CARD_SIZE, CARD_SIZE, bgColor);

        if (stack != null && !stack.isEmpty()) {
            guiGraphics.renderItem(stack, x + 1, y + 1);
        }

        if (count > 0) {
            String countText = String.valueOf(count);
            var font = Minecraft.getInstance().font;
            Component countComponent = GuiTextStyles.uniformLiteral(countText);
            int countX = x + CARD_SIZE - font.width(countComponent) - 1;
            int countY = y + CARD_SIZE - 8;
            int countColor = count > 0 ? 0xFFFFFFFF : OUT_OF_STOCK_COLOR;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
            guiGraphics.drawString(font, countComponent, countX, countY, countColor, false);
            guiGraphics.pose().popPose();
        }

        if (deleteMode) {
            int overlayColor = hovered ? DELETE_HOVER_OVERLAY_COLOR : DELETE_OVERLAY_COLOR;
            fill(guiGraphics, x, y, CARD_SIZE, CARD_SIZE, overlayColor);
            fill(guiGraphics, x, y, CARD_SIZE, 1, DELETE_BORDER_COLOR);
            fill(guiGraphics, x, y + CARD_SIZE - 1, CARD_SIZE, 1, DELETE_BORDER_COLOR);
            fill(guiGraphics, x, y, 1, CARD_SIZE, DELETE_BORDER_COLOR);
            fill(guiGraphics, x + CARD_SIZE - 1, y, 1, CARD_SIZE, DELETE_BORDER_COLOR);
        }
    }

    /**
     * 检查卡片是否被鼠标悬停
     */
    private boolean isCardHovered(int cardX, int cardY, int mouseX, int mouseY) {
        return mouseX >= cardX && mouseX < cardX + CARD_SIZE &&
               mouseY >= cardY && mouseY < cardY + CARD_SIZE;
    }

    /**
     * 在指定区域中查找鼠标悬停的卡片
     */
    private CardInfo findHoveredCard(List<CardInfo> cards, int scrollOffset, int areaX, int cardsAreaY, int mouseX, int mouseY) {
        int cardsAreaHeight = ROWS * CARD_SIZE + (ROWS - 1) * ROW_SPACING;
        if (mouseY < cardsAreaY || mouseY >= cardsAreaY + cardsAreaHeight) return null;
        if (mouseX < areaX || mouseX >= areaX + SECTION_WIDTH) return null;

        for (int i = 0; i < cards.size(); i++) {
            CardInfo card = cards.get(i);
            int col = i / ROWS;
            int row = i % ROWS;
            int cardX = areaX + col * (CARD_SIZE + CARD_SPACING) - scrollOffset;
            int cardY = cardsAreaY + row * (CARD_SIZE + ROW_SPACING);

            if (isCardHovered(cardX, cardY, mouseX, mouseY)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (deleteButton != null && deleteButton.isMouseOver(mouseX, mouseY)) {
            return deleteButton.mouseClicked(mouseX, mouseY, button);
        }

        if (confirmButton != null && confirmButton.isMouseOver(mouseX, mouseY)) {
            return confirmButton.mouseClicked(mouseX, mouseY, button);
        }

        if (tryScrollbarClick((int) mouseX, (int) mouseY)) {
            return true;
        }

        if (isDeleteMode) {
            PendingTradeSlot hitSlot = findClickedPendingSlot((int) mouseX, (int) mouseY);
            if (hitSlot != null) {
                if (onRemoveSlot != null) {
                    onRemoveSlot.accept(hitSlot, Screen.hasShiftDown());
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 尝试点击滚动条，成功则开始拖拽
     */
    private boolean tryScrollbarClick(int mouseX, int mouseY) {
        int cardsAreaY = getCardsAreaY();
        int cardsAreaHeight = ROWS * CARD_SIZE + (ROWS - 1) * ROW_SPACING;
        int scrollbarY = cardsAreaY + cardsAreaHeight + 1;

        if (mouseY < scrollbarY || mouseY >= scrollbarY + SCROLLBAR_HEIGHT + 2) return false;

        int wantAreaX = getWantAreaX();
        if (mouseX >= wantAreaX && mouseX < wantAreaX + SECTION_WIDTH && wantMaxScroll > 0) {
            isDraggingWantScrollbar = true;
            dragStartX = mouseX;
            dragStartScroll = wantScrollOffset;
            return true;
        }

        int payAreaX = getPayAreaX();
        if (mouseX >= payAreaX && mouseX < payAreaX + SECTION_WIDTH && payMaxScroll > 0) {
            isDraggingPayScrollbar = true;
            dragStartX = mouseX;
            dragStartScroll = payScrollOffset;
            return true;
        }

        return false;
    }

    /**
     * 查找鼠标点击位置对应的可编辑暂存槽位。
     * 派生货币卡片不可编辑，因此不会命中。
     */
    private PendingTradeSlot findClickedPendingSlot(int mouseX, int mouseY) {
        CardInfo wantHit = findHoveredCard(cachedWantCards, wantScrollOffset, getWantAreaX(), getCardsAreaY(), mouseX, mouseY);
        if (isEditableCard(wantHit)) return wantHit.sourceSlots.get(0);

        CardInfo payHit = findHoveredCard(cachedPayCards, payScrollOffset, getPayAreaX(), getCardsAreaY(), mouseX, mouseY);
        if (isEditableCard(payHit)) return payHit.sourceSlots.get(0);

        return null;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingWantScrollbar = false;
        isDraggingPayScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingWantScrollbar && wantMaxScroll > 0) {
            wantScrollOffset = calculateDragScroll(wantMaxScroll, (int) mouseX);
            return true;
        }
        if (isDraggingPayScrollbar && payMaxScroll > 0) {
            payScrollOffset = calculateDragScroll(payMaxScroll, (int) mouseX);
            return true;
        }
        return false;
    }

    /**
     * 根据鼠标位移计算拖拽后的滚动偏移
     */
    private int calculateDragScroll(int maxScroll, int currentMouseX) {
        int trackWidth = SECTION_WIDTH;
        int barWidth = Math.max(12, trackWidth * trackWidth / (maxScroll + trackWidth));
        float mouseDelta = currentMouseX - dragStartX;
        float scrollRatio = mouseDelta / (trackWidth - barWidth);
        int newScroll = dragStartScroll + (int) (scrollRatio * maxScroll);
        return Math.max(0, Math.min(maxScroll, newScroll));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int wantAreaX = getWantAreaX();
        int payAreaX = getPayAreaX();
        int cardsAreaY = getCardsAreaY();
        int cardsAreaHeight = ROWS * CARD_SIZE + (ROWS - 1) * ROW_SPACING;

        if (mouseY >= cardsAreaY && mouseY < cardsAreaY + cardsAreaHeight) {
            if (mouseX >= wantAreaX && mouseX < wantAreaX + SECTION_WIDTH && wantMaxScroll > 0) {
                wantScrollOffset = Math.max(0, Math.min(wantMaxScroll, wantScrollOffset - (int) scrollY * 20));
                return true;
            }
            if (mouseX >= payAreaX && mouseX < payAreaX + SECTION_WIDTH && payMaxScroll > 0) {
                payScrollOffset = Math.max(0, Math.min(payMaxScroll, payScrollOffset - (int) scrollY * 20));
                return true;
            }
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable("gui.ruralroutes.trade_station.trade_area"));
    }

    private void enableScissor(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        guiGraphics.enableScissor(x1, y1, x2, y2);
    }

    private void disableScissor(GuiGraphics guiGraphics) {
        guiGraphics.disableScissor();
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}
