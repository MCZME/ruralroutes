package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.client.gui.GuiTextStyles;
import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * 交易报价卡片组件
 * 两行布局，支持货币篮和以物易物展示
 *
 * 布局结构：
 * 第一行：物品图标 + 库存
 * 第二行：货币篮/输入物品 + 价格/个
 */
public class TradeOfferCardWidget extends AbstractWidget {

    // 尺寸常量
    public static final int CARD_WIDTH = 65;    // 进一步压缩宽度
    public static final int CARD_HEIGHT = 26;   // 压缩高度
    private static final int ITEM_ICON_SIZE = 14;  // 缩小物品图标
    private static final int CURRENCY_ICON_SIZE = 10;
    private static final int PADDING = 2;       // 最小内边距
    private static final int LINE_HEIGHT = 13;  // 压缩行高

    // 颜色常量
    private static final int DEFAULT_BG = 0x90E9D4A7;
    private static final int HOVER_BG = 0xB0F4E4BC;
    private static final int SELECTED_BG = 0x90D7BF8B;
    private static final int SELECTED_BORDER = 0xFF8A5C2E;
    private static final int STOCK_COLOR = 0xFF4A3827;
    private static final int OUT_OF_STOCK_COLOR = 0xFF9B3A2E;
    private static final int PRICE_LABEL_COLOR = 0xFF6C5843;

    private TradeSlot tradeSlot;
    private Consumer<TradeOfferCardWidget> onClick;
    private boolean isSelected = false;

    public TradeOfferCardWidget(int x, int y) {
        super(x, y, CARD_WIDTH, CARD_HEIGHT, Component.empty());
        this.tradeSlot = null;
    }

    public TradeOfferCardWidget(int x, int y, TradeSlot slot) {
        this(x, y);
        this.tradeSlot = slot;
    }

    /**
     * 设置 TradeSlot
     */
    public void setTradeSlot(TradeSlot slot) {
        this.tradeSlot = slot;
    }

    /**
     * 获取 TradeSlot
     */
    public TradeSlot getTradeSlot() {
        return tradeSlot;
    }

    /**
     * 设置选中状态
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    /**
     * 是否选中
     */
    public boolean isSelected() {
        return isSelected;
    }

    public TradeOfferCardWidget setOnClick(Consumer<TradeOfferCardWidget> onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 确定背景颜色
        int bgColor;
        if (isSelected) {
            bgColor = SELECTED_BG;
        } else if (isHovered()) {
            bgColor = HOVER_BG;
        } else {
            bgColor = DEFAULT_BG;
        }

        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), bgColor);

        if (isSelected) {
            guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), SELECTED_BORDER);
        }

        if (tradeSlot == null) return;

        // 第一行：物品 + 库存
        renderFirstLine(guiGraphics);

        // 第二行：货币篮/输入物品 + 价格标签
        renderSecondLine(guiGraphics);
    }

    /**
     * 渲染第一行：物品图标 + 库存数量
     */
    private void renderFirstLine(GuiGraphics guiGraphics) {
        ItemStack displayStack = tradeSlot.getDisplayStack();
        int stockCount = tradeSlot.getStockCount();

        // 物品图标 (14x14 缩放)
        int itemX = getX() + PADDING;
        int itemY = getY() + PADDING;
        if (!displayStack.isEmpty()) {
            renderItemIcon(guiGraphics, displayStack, itemX, itemY);
        }

        // 库存数量（使用本地化）
        String stockKey = tradeSlot.isBuy() ? "gui.ruralroutes.trade_card.stock" : "gui.ruralroutes.trade_card.can_buy";
        Component stockText = Component.translatable(stockKey, stockCount);
        int stockColor = stockCount > 0 ? STOCK_COLOR : OUT_OF_STOCK_COLOR;

        int textX = itemX + ITEM_ICON_SIZE + PADDING;
        int textY = itemY + (ITEM_ICON_SIZE - 8) / 2;
        guiGraphics.drawString(Minecraft.getInstance().font, GuiTextStyles.uniform(stockText), textX, textY, stockColor, false);
    }

    /**
     * 检查鼠标是否在主物品图标区域
     */
    public boolean isMouseOverItemIcon(int mouseX, int mouseY) {
        int itemX = getX() + PADDING;
        int itemY = getY() + PADDING;
        return mouseX >= itemX && mouseX < itemX + ITEM_ICON_SIZE &&
               mouseY >= itemY && mouseY < itemY + ITEM_ICON_SIZE;
    }

    /**
     * 渲染物品图标（使用通用缩放方法）
     */
    private void renderItemIcon(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        renderItemScaled(guiGraphics, stack, x, y, ITEM_ICON_SIZE);
    }

    /**
     * 渲染第二行：货币篮/输入物品 + 价格标签
     */
    private void renderSecondLine(GuiGraphics guiGraphics) {
        var font = Minecraft.getInstance().font;
        int lineY = getY() + PADDING + LINE_HEIGHT;
        int currentX = getX() + PADDING;

        TradeContractType type = tradeSlot.getTradeType();

        if (type == TradeContractType.CURRENCY_BASKET_DYNAMIC) {
            currentX = renderCurrencyBasket(guiGraphics, font, currentX, lineY);
            guiGraphics.drawString(font, GuiTextStyles.uniform(Component.translatable("gui.ruralroutes.trade_card.price_per")),
                currentX + 2, lineY + 1, PRICE_LABEL_COLOR);
        } else {
            renderInputItems(guiGraphics, font, currentX, lineY);
        }
    }

    /**
     * 渲染货币篮图标串
     */
    private int renderCurrencyBasket(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int startX, int y) {
        int x = startX;
        List<ItemStack> priceStacks = tradeSlot.getPriceStacks();

        int maxCurrencies = 2;
        int shown = 0;
        int hiddenCount = 0;

        for (ItemStack stack : priceStacks) {
            if (shown >= maxCurrencies) { hiddenCount++; continue; }
            if (stack.isEmpty()) continue;

            String countText = String.valueOf(stack.getCount());
            guiGraphics.drawString(font, GuiTextStyles.uniformLiteral(countText), x, y + 1, STOCK_COLOR, false);
            x += font.width(countText) + 1;

            renderItemScaled(guiGraphics, stack, x, y, CURRENCY_ICON_SIZE);
            x += CURRENCY_ICON_SIZE + 2;
            shown++;
        }

        if (hiddenCount > 0) {
            String moreText = "+" + hiddenCount;
            guiGraphics.drawString(font, GuiTextStyles.uniformLiteral(moreText), x, y + 1, PRICE_LABEL_COLOR, false);
        }
        return x;
    }

    /**
     * 渲染以物易物输入物品
     */
    private void renderInputItems(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int startX, int y) {
        int x = startX;
        List<ItemStack> inputStacks = tradeSlot.getInputStacks();

        int maxItems = 2;
        int shown = 0;
        int hiddenCount = 0;

        for (ItemStack stack : inputStacks) {
            if (shown >= maxItems) { hiddenCount++; continue; }
            if (stack.isEmpty()) continue;

            renderItemScaled(guiGraphics, stack, x, y, CURRENCY_ICON_SIZE);

            String countText = String.valueOf(stack.getCount());
            guiGraphics.drawString(font, GuiTextStyles.uniformLiteral(countText), x + CURRENCY_ICON_SIZE + 1, y + 1, STOCK_COLOR, false);

            x += CURRENCY_ICON_SIZE + font.width(countText) + 2;
            shown++;
        }

        if (hiddenCount > 0) {
            String moreText = "+" + hiddenCount;
            guiGraphics.drawString(font, GuiTextStyles.uniformLiteral(moreText), x, y + 1, PRICE_LABEL_COLOR, false);
        }
    }

    /**
     * 渲染缩放物品图标
     */
    private void renderItemScaled(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int targetSize) {
        if (stack.isEmpty()) return;

        float scale = targetSize / 16.0f;
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0f);
        guiGraphics.renderFakeItem(stack, 0, 0);
        poseStack.popPose();
    }

    /**
     * 填充矩形背景
     */
    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button) && isMouseOver(mouseX, mouseY)) {
            if (onClick != null) {
                onClick.accept(this);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        ItemStack displayStack = tradeSlot != null ? tradeSlot.getDisplayStack() : ItemStack.EMPTY;
        narration.add(NarratedElementType.TITLE,
            displayStack.isEmpty() ? Component.translatable("gui.ruralroutes.trade_card.empty_slot") : displayStack.getDisplayName());
    }
}
