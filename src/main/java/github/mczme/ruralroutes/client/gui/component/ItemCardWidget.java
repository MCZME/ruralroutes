package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.menu.slot.TradeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * 可点击的物品卡片组件
 */
public class ItemCardWidget extends AbstractWidget {

    private static final int DEFAULT_COLOR = 0x40FFFFFF;
    private static final int HOVER_COLOR = 0x80FFFFFF;
    private static final int PRICE_COLOR = 0xFFD700; // 金色

    private TradeSlot tradeSlot;
    private Consumer<ItemCardWidget> onClick;

    public ItemCardWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.tradeSlot = null;
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

    public ItemCardWidget setOnClick(Consumer<ItemCardWidget> onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 确定背景颜色
        int bgColor = isHovered() ? HOVER_COLOR : DEFAULT_COLOR;

        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), bgColor);

        if (tradeSlot == null) return;

        ItemStack displayStack = tradeSlot.getDisplayStack();
        int stockCount = tradeSlot.getStockCount();
        int price = tradeSlot.getPrice();

        // 绘制物品图标
        if (!displayStack.isEmpty()) {
            guiGraphics.renderItem(displayStack, getX() + 1, getY() + 1);
        }

        // 在右下角绘制库存数量
        if (stockCount > 0) {
            String stockText = String.valueOf(stockCount);
            int stockX = getX() + getWidth() - Minecraft.getInstance().font.width(stockText) - 1;
            int stockY = getY() + getHeight() - 8;
            guiGraphics.drawString(Minecraft.getInstance().font, stockText, stockX, stockY, 0xFFFFFF);
        }

        // 在物品下方绘制价格（金色）
        if (price > 0) {
            String priceText = price + " 铜";
            int priceX = getX() + (getWidth() - Minecraft.getInstance().font.width(priceText)) / 2;
            int priceY = getY() + getHeight() + 1;
            guiGraphics.drawString(Minecraft.getInstance().font, priceText, priceX, priceY, PRICE_COLOR);
        }
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
            displayStack.isEmpty() ? Component.literal("空槽位") : displayStack.getDisplayName());
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}
