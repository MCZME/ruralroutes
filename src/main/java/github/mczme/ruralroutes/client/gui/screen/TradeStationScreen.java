package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.client.gui.component.CoinExchangeWidget;
import github.mczme.ruralroutes.client.gui.component.TradeAreaWidget;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import github.mczme.ruralroutes.menu.slot.TradeSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 贸易站 GUI 屏幕
 *
 * 使用 Slot 系统展示交易物品，支持横向滚动
 */
public class TradeStationScreen extends AbstractContainerScreen<TradeStationMenu> {

    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;
    private static final int MARGIN = 10;
    private static final int SECTION_SPACING = 8;

    // 滚动相关常量
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 2;
    private static final int ROWS = 2;

    private TradeAreaWidget tradeArea;
    private CoinExchangeWidget coinExchange;

    // 本地滚动偏移
    private int sellScrollOffset = 0;
    private int buyScrollOffset = 0;
    private int maxSellScroll = 0;
    private int maxBuyScroll = 0;

    // 拖拽状态
    private boolean draggingSellScrollbar = false;
    private boolean draggingBuyScrollbar = false;

    // 区域位置
    private int sellAreaX, sellAreaY, sellAreaWidth, sellAreaHeight;
    private int buyAreaX, buyAreaY, buyAreaWidth, buyAreaHeight;

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

        // 出售区位置
        sellAreaX = leftPos + MARGIN;
        sellAreaY = currentY + 12; // 标题高度
        sellAreaWidth = mainWidth;
        sellAreaHeight = ROWS * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;
        currentY += 12 + sellAreaHeight + 4 + SECTION_SPACING;

        // 收购区位置
        buyAreaX = leftPos + MARGIN;
        buyAreaY = currentY + 12;
        buyAreaWidth = mainWidth;
        buyAreaHeight = ROWS * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;
        currentY += 12 + buyAreaHeight + 4 + SECTION_SPACING;

        // 交易区
        int tradeWidth = mainWidth * 7 / 10 - SECTION_SPACING / 2;
        tradeArea = new TradeAreaWidget(leftPos + MARGIN, currentY, tradeWidth, 110);
        tradeArea.init(btn -> onConfirmClick());
        tradeArea.setOnGiveCardClick(this::onTradeAreaCardClick);

        // 铸币快捷操作
        int coinWidth = mainWidth * 3 / 10 - SECTION_SPACING / 2;
        int coinX = leftPos + MARGIN + tradeWidth + SECTION_SPACING;
        coinExchange = new CoinExchangeWidget(coinX, currentY, coinWidth, 110);
        coinExchange.init();

        updateMaxScroll();
    }

    /**
     * 更新最大滚动偏移
     */
    private void updateMaxScroll() {
        int sellCols = (menu.getSellSlots().size() + 1) / 2;
        int buyCols = (menu.getBuySlots().size() + 1) / 2;

        int sellContentWidth = sellCols * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;
        int buyContentWidth = buyCols * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;

        maxSellScroll = Math.max(0, sellContentWidth - sellAreaWidth);
        maxBuyScroll = Math.max(0, buyContentWidth - buyAreaWidth);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 绘制背景
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xCC222222);

        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 5, 0xFFFFFF);

        int currentY = topPos;

        // 绘制出售区标题和背景
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.sell"),
            leftPos + MARGIN, currentY + 2, 0x55FF55);
        fill(guiGraphics, sellAreaX, sellAreaY, sellAreaWidth, sellAreaHeight, 0x40333333);
        currentY += 12 + sellAreaHeight + 4;

        // 绘制出售区滚动条
        renderScrollbar(guiGraphics, sellAreaX, sellAreaY + sellAreaHeight + 2, sellAreaWidth, maxSellScroll, sellScrollOffset);

        currentY += SECTION_SPACING;

        // 绘制收购区标题和背景
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.buy"),
            leftPos + MARGIN, currentY + 2, 0xFF5555);
        fill(guiGraphics, buyAreaX, buyAreaY, buyAreaWidth, buyAreaHeight, 0x40333333);

        // 绘制收购区滚动条
        renderScrollbar(guiGraphics, buyAreaX, buyAreaY + buyAreaHeight + 2, buyAreaWidth, maxBuyScroll, buyScrollOffset);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // 渲染槽位（带裁剪）
        renderSlotsWithClipping(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染交易区和铸币区
        tradeArea.render(guiGraphics, mouseX, mouseY, partialTick);
        coinExchange.render(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染工具提示
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 带裁剪渲染槽位
     */
    private void renderSlotsWithClipping(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染出售槽位
        guiGraphics.enableScissor(sellAreaX, sellAreaY, sellAreaX + sellAreaWidth, sellAreaY + sellAreaHeight);
        renderSlotSection(guiGraphics, menu.getSellSlots(), sellAreaX, sellAreaY, sellScrollOffset, mouseX, mouseY);
        guiGraphics.disableScissor();

        // 渲染收购槽位
        guiGraphics.enableScissor(buyAreaX, buyAreaY, buyAreaX + buyAreaWidth, buyAreaY + buyAreaHeight);
        renderSlotSection(guiGraphics, menu.getBuySlots(), buyAreaX, buyAreaY, buyScrollOffset, mouseX, mouseY);
        guiGraphics.disableScissor();
    }

    /**
     * 渲染单个区域的槽位
     */
    private void renderSlotSection(GuiGraphics guiGraphics, List<TradeSlot> slots, int areaX, int areaY, int scrollOffset, int mouseX, int mouseY) {
        for (int i = 0; i < slots.size(); i++) {
            TradeSlot slot = slots.get(i);
            if (slot == null) continue;

            // 计算槽位位置（奇数在上排，偶数在下排）
            int col = i / 2;
            int row = (i % 2 == 0) ? 0 : 1;

            int slotX = areaX + col * (SLOT_SIZE + SLOT_SPACING) - scrollOffset;
            int slotY = areaY + row * (SLOT_SIZE + SLOT_SPACING);

            // 检查是否在可见区域内
            if (slotX + SLOT_SIZE < areaX || slotX >= areaX + sellAreaWidth) {
                continue;
            }

            // 渲染槽位背景
            boolean isHovered = isMouseOverSlot(slotX, slotY, mouseX, mouseY);
            int bgColor = isHovered ? 0x80FFFFFF : 0x40FFFFFF;
            fill(guiGraphics, slotX, slotY, SLOT_SIZE, SLOT_SIZE, bgColor);

            // 渲染物品
            ItemStack displayStack = slot.getDisplayStack();
            if (!displayStack.isEmpty() && slot.getStockCount() > 0) {
                guiGraphics.renderItem(displayStack, slotX + 1, slotY + 1);
                guiGraphics.renderItemDecorations(font, displayStack, slotX + 1, slotY + 1);

                // 渲染价格标签
                String priceText = String.valueOf(slot.getPrice());
                guiGraphics.drawString(font, priceText, slotX + SLOT_SIZE - font.width(priceText) - 1, slotY + SLOT_SIZE - 8, 0xFFFFFF);
            }
        }
    }

    /**
     * 渲染滚动条
     */
    private void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int width, int maxScroll, int currentScroll) {
        if (maxScroll <= 0) return;

        int barHeight = 4;
        int barWidth = Math.max(20, width * width / (maxScroll + width));
        int barX = x + (int) ((width - barWidth) * currentScroll / (float) maxScroll);

        fill(guiGraphics, barX, y, barWidth, barHeight, 0x80AAAAAA);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        // 检查是否悬停在槽位上，显示详细信息
        TradeSlot hoveredSlot = getHoveredSlot(x, y);
        if (hoveredSlot != null && !hoveredSlot.getDisplayStack().isEmpty()) {
            guiGraphics.renderTooltip(font, hoveredSlot.getDisplayStack(), x, y);
        }
    }

    /**
     * 获取鼠标悬停的槽位
     */
    private TradeSlot getHoveredSlot(int mouseX, int mouseY) {
        // 检查出售区
        TradeSlot sellSlot = getHoveredSlotInSection(menu.getSellSlots(), sellAreaX, sellAreaY, sellScrollOffset, mouseX, mouseY);
        if (sellSlot != null) return sellSlot;

        // 检查收购区
        return getHoveredSlotInSection(menu.getBuySlots(), buyAreaX, buyAreaY, buyScrollOffset, mouseX, mouseY);
    }

    private TradeSlot getHoveredSlotInSection(List<TradeSlot> slots, int areaX, int areaY, int scrollOffset, int mouseX, int mouseY) {
        for (int i = 0; i < slots.size(); i++) {
            TradeSlot slot = slots.get(i);
            if (slot == null) continue;

            int col = i / 2;
            int row = (i % 2 == 0) ? 0 : 1;

            int slotX = areaX + col * (SLOT_SIZE + SLOT_SPACING) - scrollOffset;
            int slotY = areaY + row * (SLOT_SIZE + SLOT_SPACING);

            if (isMouseOverSlot(slotX, slotY, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private boolean isMouseOverSlot(int slotX, int slotY, int mouseX, int mouseY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE
            && mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查滚动条点击
        if (isOverScrollbar(sellAreaX, sellAreaY + sellAreaHeight + 2, sellAreaWidth, maxSellScroll, (int) mouseX, (int) mouseY)) {
            draggingSellScrollbar = true;
            return true;
        }
        if (isOverScrollbar(buyAreaX, buyAreaY + buyAreaHeight + 2, buyAreaWidth, maxBuyScroll, (int) mouseX, (int) mouseY)) {
            draggingBuyScrollbar = true;
            return true;
        }

        // 检查槽位点击
        TradeSlot clickedSlot = getHoveredSlot((int) mouseX, (int) mouseY);
        if (clickedSlot != null) {
            onSlotClicked(clickedSlot);
            return true;
        }

        if (tradeArea.mouseClicked(mouseX, mouseY, button)) return true;
        if (coinExchange.mouseClicked(mouseX, mouseY, button)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOverScrollbar(int x, int y, int width, int maxScroll, int mouseX, int mouseY) {
        if (maxScroll <= 0) return false;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 8;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSellScrollbar = false;
        draggingBuyScrollbar = false;

        tradeArea.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingSellScrollbar && maxSellScroll > 0) {
            sellScrollOffset = Math.max(0, Math.min(maxSellScroll, sellScrollOffset - (int) dragX));
            return true;
        }
        if (draggingBuyScrollbar && maxBuyScroll > 0) {
            buyScrollOffset = Math.max(0, Math.min(maxBuyScroll, buyScrollOffset - (int) dragX));
            return true;
        }

        if (tradeArea.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // 检查是否在出售区
        if (mouseX >= sellAreaX && mouseX <= sellAreaX + sellAreaWidth
            && mouseY >= sellAreaY && mouseY <= sellAreaY + sellAreaHeight) {
            if (maxSellScroll > 0) {
                sellScrollOffset = Math.max(0, Math.min(maxSellScroll, sellScrollOffset + (int) scrollY * 20));
                return true;
            }
        }

        // 检查是否在收购区
        if (mouseX >= buyAreaX && mouseX <= buyAreaX + buyAreaWidth
            && mouseY >= buyAreaY && mouseY <= buyAreaY + buyAreaHeight) {
            if (maxBuyScroll > 0) {
                buyScrollOffset = Math.max(0, Math.min(maxBuyScroll, buyScrollOffset + (int) scrollY * 20));
                return true;
            }
        }

        if (tradeArea.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * 槽位点击处理
     */
    private void onSlotClicked(TradeSlot slot) {
        if (slot.isEmpty()) return;

        // 判断是出售区还是收购区
        int sellIndex = menu.getSellSlots().indexOf(slot);
        int buyIndex = menu.getBuySlots().indexOf(slot);

        if (sellIndex >= 0) {
            onSellSlotClicked(slot, sellIndex);
        } else if (buyIndex >= 0) {
            onBuySlotClicked(slot, buyIndex);
        }
    }

    /**
     * 出售槽位点击（玩家购买）
     */
    private void onSellSlotClicked(TradeSlot slot, int index) {
        // TODO: 实现购买逻辑
    }

    /**
     * 收购槽位点击（玩家出售）
     */
    private void onBuySlotClicked(TradeSlot slot, int index) {
        // TODO: 实现出售逻辑
    }

    /**
     * 点击确认按钮
     */
    private void onConfirmClick() {
        // TODO: 后续实现确认逻辑
    }

    /**
     * 点击交易区卡片（取消）
     */
    private void onTradeAreaCardClick(Object card) {
        // TODO: 后续实现交易区取消逻辑
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // 每帧更新滚动范围，以适应动态槽位数量变化
        updateMaxScroll();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}