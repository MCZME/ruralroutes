package github.mczme.ruralroutes.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.network.packet.OpenNodeDataViewerPayload.TargetBlockType;
import github.mczme.ruralroutes.network.packet.OpenNodeDataViewerPayload.ViewStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 节点数据查看器界面。
 * 使用网格化卡片布局展示节点快照，不依赖 Menu。
 */
public class NodeDataViewerScreen extends Screen {

    private static final int GUI_WIDTH = 392;
    private static final int GUI_HEIGHT = 244;
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 18;
    private static final int PANEL_PADDING = 10;
    private static final int CONTENT_INSET = 6;
    private static final int COLUMN_GAP = 8;
    private static final int PANEL_GAP = 8;
    private static final int PANEL_HEADER_HEIGHT = 18;
    private static final int PANEL_INNER_PADDING = 6;
    private static final int INFO_TILE_HEIGHT = 24;
    private static final int ITEM_ROW_HEIGHT = 26;
    private static final int ITEM_ROW_GAP = 4;
    private static final int SCROLL_STEP = 16;
    private static final int ICON_SIZE = 16;

    private static final int BACKDROP_TOP = 0xE10A0D10;
    private static final int BACKDROP_BOTTOM = 0xF2040609;
    private static final int PANEL_BG = 0xE021252B;
    private static final int PANEL_BORDER = 0xFF6E7782;
    private static final int HEADER_BG = 0xCC11161C;
    private static final int CONTENT_BG = 0x99313843;
    private static final int CARD_BG = 0xCC2D3641;
    private static final int CARD_HEADER_BG = 0xCC46515E;
    private static final int TILE_BG = 0x9938424F;
    private static final int TILE_BORDER = 0x66556372;
    private static final int ROW_BG = 0x7F182028;
    private static final int ROW_BORDER = 0x663E4956;
    private static final int TEXT_PRIMARY = 0xFFF4F5F7;
    private static final int TEXT_MUTED = 0xFFBCC5D0;
    private static final int TEXT_DIM = 0xFF8D97A3;
    private static final int TEXT_ACCENT = 0xFF8BD3FF;
    private static final int TEXT_WARN = 0xFFFFC27A;
    private static final int TEXT_STOCK = 0xFFA4F3A1;
    private static final int SCROLLBAR_TRACK = 0x66252D37;
    private static final int SCROLLBAR_THUMB = 0xCC7FAFD0;

    private final TargetBlockType targetBlockType;
    private final ViewStatus viewStatus;
    @Nullable
    private final CommercialNodeData nodeData;
    private final List<HoverEntry> hoverEntries = new ArrayList<>();

    private int leftPos;
    private int topPos;
    private int scrollOffset;
    private int maxScroll;
    private int totalContentHeight;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;

    public NodeDataViewerScreen(TargetBlockType targetBlockType, ViewStatus viewStatus,
            @Nullable CommercialNodeData nodeData) {
        super(Component.translatable("gui.ruralroutes.node_data_viewer.title"));
        this.targetBlockType = targetBlockType;
        this.nodeData = nodeData;
        this.viewStatus = nodeData == null && viewStatus == ViewStatus.HAS_DATA
            ? ViewStatus.MISSING_NODE_DATA
            : viewStatus;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
        this.scrollOffset = 0;
        this.draggingScrollbar = false;
        this.scrollbarDragOffset = 0;
        rebuildLayout();
    }

    private void rebuildLayout() {
        if (!hasNodeData()) {
            totalContentHeight = 0;
            maxScroll = 0;
            return;
        }

        int panelAreaWidth = getPanelAreaWidth();
        int summaryHeight = getSummaryPanelHeight();
        int columnWidth = (panelAreaWidth - COLUMN_GAP) / 2;

        int leftHeight = getListPanelHeight(nodeData.sellItems().size())
            + PANEL_GAP
            + getListPanelHeight(nodeData.specialties().size());
        int rightHeight = getListPanelHeight(nodeData.buyItems().size())
            + PANEL_GAP
            + getStockPanelHeight(nodeData.stocks().size());

        totalContentHeight = CONTENT_INSET * 2
            + summaryHeight
            + PANEL_GAP
            + Math.max(leftHeight, rightHeight);

        if (columnWidth <= 0) {
            totalContentHeight = 0;
        }

        maxScroll = Math.max(0, totalContentHeight - getContentHeight());
        scrollOffset = clamp(scrollOffset, 0, maxScroll);
    }

    private boolean hasNodeData() {
        return viewStatus == ViewStatus.HAS_DATA && nodeData != null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderFrame(guiGraphics);

        hoverEntries.clear();
        if (hasNodeData()) {
            renderGridContent(guiGraphics, mouseX, mouseY);
        } else {
            renderEmptyState(guiGraphics);
        }

        renderFooter(guiGraphics);
        renderHoveredTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderFrame(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, BACKDROP_TOP, BACKDROP_BOTTOM);
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, PANEL_BG);
        guiGraphics.renderOutline(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, PANEL_BORDER);

        guiGraphics.fill(leftPos + 1, topPos + 1, leftPos + GUI_WIDTH - 1, topPos + HEADER_HEIGHT, HEADER_BG);
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 8, TEXT_PRIMARY);
        guiGraphics.drawCenteredString(font, Component.translatable(targetBlockType.translationKey()),
            leftPos + GUI_WIDTH / 2, topPos + 19, TEXT_ACCENT);

        guiGraphics.fill(getContentX(), getContentY(),
            getContentX() + getContentWidth(), getContentY() + getContentHeight(), CONTENT_BG);
        guiGraphics.renderOutline(getContentX(), getContentY(), getContentWidth(), getContentHeight(), ROW_BORDER);
    }

    private void renderGridContent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int areaX = getContentX() + CONTENT_INSET;
        int areaY = getContentY() + CONTENT_INSET - scrollOffset;
        int areaWidth = getPanelAreaWidth();
        int columnWidth = (areaWidth - COLUMN_GAP) / 2;
        int summaryHeight = getSummaryPanelHeight();

        guiGraphics.enableScissor(getContentX(), getContentY(),
            getContentX() + getContentWidth(), getContentY() + getContentHeight());

        renderSummaryPanel(guiGraphics, areaX, areaY, areaWidth);

        int columnsTop = areaY + summaryHeight + PANEL_GAP;
        int leftX = areaX;
        int rightX = areaX + columnWidth + COLUMN_GAP;

        int leftY = columnsTop;
        leftY = renderItemPanel(guiGraphics, leftX, leftY, columnWidth,
            Component.translatable("gui.ruralroutes.node_data_viewer.section.sell_items"),
            nodeData.sellItems(), false);
        leftY += PANEL_GAP;
        renderItemPanel(guiGraphics, leftX, leftY, columnWidth,
            Component.translatable("gui.ruralroutes.node_data_viewer.section.specialties"),
            nodeData.specialties(), false);

        int rightY = columnsTop;
        rightY = renderItemPanel(guiGraphics, rightX, rightY, columnWidth,
            Component.translatable("gui.ruralroutes.node_data_viewer.section.buy_items"),
            nodeData.buyItems(), false);
        rightY += PANEL_GAP;
        renderStockPanel(guiGraphics, rightX, rightY, columnWidth);

        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics);
    }

    private void renderSummaryPanel(GuiGraphics guiGraphics, int x, int y, int width) {
        int height = getSummaryPanelHeight();
        renderCardFrame(guiGraphics, x, y, width, height,
            Component.translatable("gui.ruralroutes.node_data_viewer.section.summary"));

        int contentX = x + PANEL_INNER_PADDING;
        int contentY = y + PANEL_HEADER_HEIGHT + PANEL_INNER_PADDING;
        int contentWidth = width - PANEL_INNER_PADDING * 2;
        int tileGap = 6;
        int halfWidth = (contentWidth - tileGap) / 2;

        renderInfoTile(guiGraphics, contentX, contentY, contentWidth, INFO_TILE_HEIGHT,
            Component.translatable("gui.ruralroutes.node_data_viewer.summary.trade_node_id"),
            nodeData.tradeNodeId().toString());

        int secondRowY = contentY + INFO_TILE_HEIGHT + 6;
        renderInfoTile(guiGraphics, contentX, secondRowY, halfWidth, INFO_TILE_HEIGHT,
            Component.translatable("gui.ruralroutes.node_data_viewer.summary.theme"),
            nodeData.themeName().toString());
        renderInfoTile(guiGraphics, contentX + halfWidth + tileGap, secondRowY, halfWidth, INFO_TILE_HEIGHT,
            Component.translatable("gui.ruralroutes.node_data_viewer.summary.refresh_timestamp"),
            Long.toString(nodeData.refreshTimestamp()));
    }

    private int renderItemPanel(GuiGraphics guiGraphics, int x, int y, int width, Component title,
            List<CommercialNodeData.NodeTradeEntry> itemEntries, boolean showStock) {
        int height = getListPanelHeight(itemEntries.size());
        renderCardFrame(guiGraphics, x, y, width, height, title);

        int rowX = x + PANEL_INNER_PADDING;
        int rowY = y + PANEL_HEADER_HEIGHT + PANEL_INNER_PADDING;
        int rowWidth = width - PANEL_INNER_PADDING * 2;

        if (itemEntries.isEmpty()) {
            renderEmptyRow(guiGraphics, rowX, rowY, rowWidth, Component.translatable("gui.ruralroutes.node_data_viewer.empty_list"));
            return y + height;
        }

        for (CommercialNodeData.NodeTradeEntry entry : itemEntries) {
            ResourceLocation itemId = entry.itemId();
            ItemStack stack = entry.displayStackOrDefault();
            renderItemRow(guiGraphics, rowX, rowY, rowWidth,
                getItemName(itemId, stack).getString(), itemId.toString(), stack, null, showStock);
            rowY += ITEM_ROW_HEIGHT + ITEM_ROW_GAP;
        }

        return y + height;
    }

    private void renderStockPanel(GuiGraphics guiGraphics, int x, int y, int width) {
        List<Map.Entry<ResourceLocation, StockEntry>> stocks = nodeData.stocks().entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
            .toList();

        int height = getStockPanelHeight(stocks.size());
        renderCardFrame(guiGraphics, x, y, width, height,
            Component.translatable("gui.ruralroutes.node_data_viewer.section.stocks"));

        int rowX = x + PANEL_INNER_PADDING;
        int rowY = y + PANEL_HEADER_HEIGHT + PANEL_INNER_PADDING;
        int rowWidth = width - PANEL_INNER_PADDING * 2;

        if (stocks.isEmpty()) {
            renderEmptyRow(guiGraphics, rowX, rowY, rowWidth, Component.translatable("gui.ruralroutes.node_data_viewer.empty_list"));
            return;
        }

        for (Map.Entry<ResourceLocation, StockEntry> stock : stocks) {
            ItemStack stack = createItemStack(stock.getKey());
            String stockText = Component.translatable("gui.ruralroutes.node_data_viewer.stock_pair",
                stock.getValue().current(), stock.getValue().max()).getString();
            renderItemRow(guiGraphics, rowX, rowY, rowWidth,
                getItemName(stock.getKey(), stack).getString(), stock.getKey().toString(), stack, stockText, true);
            rowY += ITEM_ROW_HEIGHT + ITEM_ROW_GAP;
        }
    }

    private void renderCardFrame(GuiGraphics guiGraphics, int x, int y, int width, int height, Component title) {
        guiGraphics.fill(x, y, x + width, y + height, CARD_BG);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + PANEL_HEADER_HEIGHT, CARD_HEADER_BG);
        guiGraphics.renderOutline(x, y, width, height, PANEL_BORDER);
        guiGraphics.drawString(font, title, x + 6, y + 5, TEXT_PRIMARY, false);
    }

    private void renderInfoTile(GuiGraphics guiGraphics, int x, int y, int width, int height,
            Component label, String value) {
        guiGraphics.fill(x, y, x + width, y + height, TILE_BG);
        guiGraphics.renderOutline(x, y, width, height, TILE_BORDER);
        guiGraphics.drawString(font, label, x + 6, y + 4, TEXT_DIM, false);
        guiGraphics.drawString(font, ellipsize(value, width - 12), x + 6, y + 14, TEXT_PRIMARY, false);
    }

    private void renderItemRow(GuiGraphics guiGraphics, int x, int y, int width,
            String primaryText, String secondaryText, ItemStack icon, @Nullable String stockText, boolean showStock) {
        guiGraphics.fill(x, y, x + width, y + ITEM_ROW_HEIGHT, ROW_BG);
        guiGraphics.renderOutline(x, y, width, ITEM_ROW_HEIGHT, ROW_BORDER);

        int iconX = x + 6;
        int iconY = y + 5;
        if (!icon.isEmpty()) {
            guiGraphics.renderItem(icon, iconX, iconY);
            hoverEntries.add(new HoverEntry(icon, iconX, iconY, ICON_SIZE, ICON_SIZE));
        }

        int textX = x + 28;
        int reserveWidth = showStock && stockText != null ? font.width(stockText) + 14 : 10;
        guiGraphics.drawString(font, ellipsize(primaryText, width - (textX - x) - reserveWidth),
            textX, y + 4, TEXT_PRIMARY, false);
        guiGraphics.drawString(font, ellipsize(secondaryText, width - (textX - x) - 10),
            textX, y + 14, TEXT_MUTED, false);

        if (showStock && stockText != null) {
            int stockX = x + width - 6 - font.width(stockText);
            guiGraphics.drawString(font, stockText, stockX, y + 4, TEXT_STOCK, false);
        }
    }

    private void renderEmptyRow(GuiGraphics guiGraphics, int x, int y, int width, Component text) {
        guiGraphics.fill(x, y, x + width, y + ITEM_ROW_HEIGHT, ROW_BG);
        guiGraphics.renderOutline(x, y, width, ITEM_ROW_HEIGHT, ROW_BORDER);
        guiGraphics.drawCenteredString(font, text, x + width / 2, y + 9, TEXT_DIM);
    }

    private void renderEmptyState(GuiGraphics guiGraphics) {
        int emptyWidth = getContentWidth() - 40;
        int emptyHeight = 72;
        int emptyX = getContentX() + (getContentWidth() - emptyWidth) / 2;
        int emptyY = getContentY() + (getContentHeight() - emptyHeight) / 2 - 6;

        renderCardFrame(guiGraphics, emptyX, emptyY, emptyWidth, emptyHeight,
            Component.translatable("gui.ruralroutes.node_data_viewer.section.summary"));
        guiGraphics.drawCenteredString(font,
            Component.translatable("gui.ruralroutes.node_data_viewer.empty"),
            emptyX + emptyWidth / 2, emptyY + 28, TEXT_WARN);

        String messageKey = viewStatus.messageKey();
        if (messageKey != null) {
            guiGraphics.drawCenteredString(font, Component.translatable(messageKey),
                emptyX + emptyWidth / 2, emptyY + 44, TEXT_MUTED);
        }
    }

    private void renderFooter(GuiGraphics guiGraphics) {
        guiGraphics.drawCenteredString(font,
            Component.translatable("gui.ruralroutes.node_data_viewer.footer"),
            leftPos + GUI_WIDTH / 2, topPos + GUI_HEIGHT - 12, TEXT_DIM);
    }

    private void renderHoveredTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (HoverEntry hoverEntry : hoverEntries) {
            if (hoverEntry.contains(mouseX, mouseY) && !hoverEntry.stack().isEmpty()) {
                guiGraphics.renderTooltip(font, hoverEntry.stack(), mouseX, mouseY);
                return;
            }
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        if (maxScroll <= 0) {
            return;
        }

        ScrollbarMetrics metrics = getScrollbarMetrics();
        int trackX = metrics.trackX();
        int trackY = metrics.trackY();
        int trackHeight = metrics.trackHeight();
        guiGraphics.fill(trackX, trackY, trackX + 3, trackY + trackHeight, SCROLLBAR_TRACK);

        guiGraphics.fill(trackX, metrics.thumbY(), trackX + 3, metrics.thumbY() + metrics.thumbHeight(), SCROLLBAR_THUMB);
    }

    private int getSummaryPanelHeight() {
        return PANEL_HEADER_HEIGHT + PANEL_INNER_PADDING * 2 + INFO_TILE_HEIGHT * 2 + 6;
    }

    private int getListPanelHeight(int itemCount) {
        return PANEL_HEADER_HEIGHT + PANEL_INNER_PADDING * 2
            + Math.max(1, itemCount) * ITEM_ROW_HEIGHT
            + Math.max(0, Math.max(1, itemCount) - 1) * ITEM_ROW_GAP;
    }

    private int getStockPanelHeight(int itemCount) {
        return getListPanelHeight(itemCount);
    }

    private ItemStack createItemStack(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private Component getItemName(ResourceLocation itemId, ItemStack stack) {
        if (!stack.isEmpty()) {
            return stack.getHoverName();
        }
        return Component.translatable("gui.ruralroutes.node_data_viewer.missing_item", itemId.toString());
    }

    private int getContentX() {
        return leftPos + PANEL_PADDING;
    }

    private int getContentY() {
        return topPos + HEADER_HEIGHT + PANEL_PADDING;
    }

    private int getContentWidth() {
        return GUI_WIDTH - PANEL_PADDING * 2;
    }

    private int getContentHeight() {
        return GUI_HEIGHT - HEADER_HEIGHT - FOOTER_HEIGHT - PANEL_PADDING * 2;
    }

    private int getPanelAreaWidth() {
        return getContentWidth() - CONTENT_INSET * 2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!hasNodeData() || maxScroll <= 0 || !isMouseOverContent(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int direction = scrollY > 0 ? -SCROLL_STEP : SCROLL_STEP;
        scrollOffset = clamp(scrollOffset + direction, 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hasNodeData() && maxScroll > 0) {
            ScrollbarMetrics metrics = getScrollbarMetrics();
            if (metrics.containsThumb(mouseX, mouseY)) {
                draggingScrollbar = true;
                scrollbarDragOffset = (int) mouseY - metrics.thumbY();
                return true;
            }
            if (metrics.containsTrack(mouseX, mouseY)) {
                draggingScrollbar = true;
                scrollbarDragOffset = metrics.thumbHeight() / 2;
                updateScrollFromMouse((int) mouseY, metrics);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingScrollbar && hasNodeData() && maxScroll > 0) {
            updateScrollFromMouse((int) mouseY, getScrollbarMetrics());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isMouseOverContent(double mouseX, double mouseY) {
        return mouseX >= getContentX() && mouseX < getContentX() + getContentWidth()
            && mouseY >= getContentY() && mouseY < getContentY() + getContentHeight();
    }

    private void updateScrollFromMouse(int mouseY, ScrollbarMetrics metrics) {
        int thumbTravel = metrics.trackHeight() - metrics.thumbHeight();
        if (thumbTravel <= 0) {
            scrollOffset = 0;
            return;
        }

        int clampedThumbY = clamp(mouseY - scrollbarDragOffset, metrics.trackY(), metrics.trackY() + thumbTravel);
        int relativeY = clampedThumbY - metrics.trackY();
        scrollOffset = relativeY * maxScroll / thumbTravel;
    }

    private ScrollbarMetrics getScrollbarMetrics() {
        int trackX = getContentX() + getContentWidth() - 6;
        int trackY = getContentY() + 4;
        int trackHeight = getContentHeight() - 8;
        int thumbHeight = Math.max(18, trackHeight * getContentHeight() / totalContentHeight);
        int thumbTravel = trackHeight - thumbHeight;
        int thumbY = maxScroll <= 0 || thumbTravel <= 0
            ? trackY
            : trackY + (thumbTravel * scrollOffset / maxScroll);
        return new ScrollbarMetrics(trackX, trackY, trackHeight, thumbY, thumbHeight);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key inputKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.minecraft != null && this.minecraft.options.keyInventory.isActiveAndMatches(inputKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private String ellipsize(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (font.width(builder.toString() + ch) + ellipsisWidth > maxWidth) {
                break;
            }
            builder.append(ch);
        }
        return builder + ellipsis;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record HoverEntry(ItemStack stack, int x, int y, int width, int height) {
        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
        }
    }

    private record ScrollbarMetrics(int trackX, int trackY, int trackHeight, int thumbY, int thumbHeight) {
        private boolean containsTrack(double mouseX, double mouseY) {
            return mouseX >= trackX && mouseX < trackX + 3
                && mouseY >= trackY && mouseY < trackY + trackHeight;
        }

        private boolean containsThumb(double mouseX, double mouseY) {
            return mouseX >= trackX && mouseX < trackX + 3
                && mouseY >= thumbY && mouseY < thumbY + thumbHeight;
        }
    }
}
