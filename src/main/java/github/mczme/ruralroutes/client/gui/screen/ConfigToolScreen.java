package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.item.ConfigToolItem;
import github.mczme.ruralroutes.menu.ConfigToolMenu;
import github.mczme.ruralroutes.network.ConfigToolApplyPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 配置工具 GUI 屏幕
 * 贸易站：可编辑主题、复制节点信息
 * 展示柜/传闻板：可粘贴节点信息
 */
public class ConfigToolScreen extends AbstractContainerScreen<ConfigToolMenu> {

    private static final int GUI_WIDTH = 336;
    private static final int GUI_HEIGHT = 224;
    private static final int HEADER_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 34;
    private static final int PANEL_PADDING = 8;
    private static final int INFO_PANEL_WIDTH = 126;
    private static final int PANEL_HEADER_HEIGHT = 18;
    private static final int THEME_ENTRY_HEIGHT = 24;
    private static final int THEME_ENTRY_SPACING = 4;
    private static final int MAX_VISIBLE = 5;

    private static final int BOARD_BG = 0xDD4A3728;
    private static final int BOARD_BORDER = 0xFF8B7355;
    private static final int HEADER_BG = 0xAA2C2119;
    private static final int FOOTER_BG = 0x88443322;
    private static final int PANEL_BG = 0xAA2B211A;
    private static final int PANEL_HEADER_BG = 0x994C3928;
    private static final int PANEL_BORDER = 0xCC8B7355;
    private static final int DIVIDER_COLOR = 0x557F6752;
    private static final int TEXT_PRIMARY = 0xFFF4E9D8;
    private static final int TEXT_MUTED = 0xFFBDA992;
    private static final int TEXT_ACCENT = 0xFFE6C27A;
    private static final int TEXT_GOOD = 0xFF9DD77E;
    private static final int TEXT_WARN = 0xFFE18A74;
    private static final int THEME_BG = 0x66413024;
    private static final int THEME_HOVER_BG = 0x88624A31;
    private static final int THEME_SELECTED_BG = 0xAA7A5A36;
    private static final int THEME_BORDER = 0x995F4A34;
    private static final int THEME_CURRENT_MARK = 0xFF83C36A;
    private static final int THEME_SELECTED_MARK = 0xFFF4CB69;
    private static final int SCROLLBAR_TRACK = 0x55312418;
    private static final int SCROLLBAR_THUMB = 0xCCB08A5C;

    private int scrollOffset = 0;

    public ConfigToolScreen(ConfigToolMenu menu, Inventory ignored, Component title) {
        super(menu, ignored, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        if (menu.canEditTheme()) {
            buildTradeStationButtons();
        } else {
            buildLinkButtons();
        }
    }

    private void buildTradeStationButtons() {
        int gap = 6;
        int totalWidth = GUI_WIDTH - PANEL_PADDING * 2;
        int buttonWidth = (totalWidth - gap * 2) / 3;
        int buttonY = topPos + GUI_HEIGHT - FOOTER_HEIGHT + 7;
        int startX = leftPos + PANEL_PADDING;

        addRenderableWidget(Button.builder(
            Component.translatable("gui.ruralroutes.config_tool.cancel"),
            b -> this.onClose()
        ).bounds(startX, buttonY, buttonWidth, 20).build());

        Button copyButton = Button.builder(
            Component.translatable("gui.ruralroutes.config_tool.copy_action"),
            b -> copyNodeInfo()
        ).bounds(startX + buttonWidth + gap, buttonY, buttonWidth, 20).build();
        copyButton.active = hasCurrentNodeInfo();
        addRenderableWidget(copyButton);

        Button applyButton = Button.builder(
            Component.translatable("gui.ruralroutes.config_tool.apply"),
            b -> applySelectedTheme()
        ).bounds(startX + (buttonWidth + gap) * 2, buttonY, buttonWidth, 20).build();
        applyButton.active = canApplyTheme();
        addRenderableWidget(applyButton);
    }

    private void buildLinkButtons() {
        int gap = 6;
        int totalWidth = GUI_WIDTH - PANEL_PADDING * 2;
        int buttonWidth = (totalWidth - gap) / 2;
        int buttonY = topPos + GUI_HEIGHT - FOOTER_HEIGHT + 7;
        int startX = leftPos + PANEL_PADDING;

        addRenderableWidget(Button.builder(
            Component.translatable("gui.ruralroutes.config_tool.close"),
            b -> this.onClose()
        ).bounds(startX, buttonY, buttonWidth, 20).build());

        Button pasteButton = Button.builder(
            Component.translatable("gui.ruralroutes.config_tool.paste_action"),
            b -> pasteNodeInfo()
        ).bounds(startX + buttonWidth + gap, buttonY, buttonWidth, 20).build();
        pasteButton.active = menu.hasCopiedNodeInfo();
        addRenderableWidget(pasteButton);
    }

    private void applySelectedTheme() {
        ResourceLocation selected = menu.getSelectedTheme();
        if (selected == null) {
            return;
        }
        PacketDistributor.sendToServer(ConfigToolApplyPayload.setTheme(menu.getBlockPos(), selected));
        this.onClose();
    }

    private void copyNodeInfo() {
        UUID nodeId = menu.getCurrentTradeNodeId();
        BlockPos stationPos = menu.getCurrentStationPos();
        if (nodeId == null || stationPos == null || this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack mainHand = this.minecraft.player.getMainHandItem();
        if (mainHand.getItem() instanceof ConfigToolItem) {
            ConfigToolItem.setCopiedNodeInfo(mainHand, nodeId, stationPos);
        }
        PacketDistributor.sendToServer(
            ConfigToolApplyPayload.copyNodeInfo(menu.getBlockPos(), nodeId, stationPos)
        );
        this.onClose();
    }

    private void pasteNodeInfo() {
        if (!menu.hasCopiedNodeInfo()) {
            return;
        }
        PacketDistributor.sendToServer(
            ConfigToolApplyPayload.pasteNodeInfo(
                menu.getBlockPos(),
                menu.getCopiedNodeId(),
                menu.getCopiedStationPos()
            )
        );
        this.onClose();
    }

    private boolean canApplyTheme() {
        ResourceLocation selected = menu.getSelectedTheme();
        ResourceLocation current = menu.getCurrentTheme();
        return selected != null && !selected.equals(current);
    }

    private boolean hasCurrentNodeInfo() {
        return menu.getCurrentTradeNodeId() != null && menu.getCurrentStationPos() != null;
    }

    private void selectTheme(ResourceLocation theme) {
        menu.selectTheme(theme);
        this.rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (menu.canEditTheme()) {
            renderThemeTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderBoard(guiGraphics);
        renderInfoPanel(guiGraphics);
        if (menu.canEditTheme()) {
            renderThemePanel(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderBoard(GuiGraphics guiGraphics) {
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, BOARD_BG);
        guiGraphics.renderOutline(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, BOARD_BORDER);

        guiGraphics.fill(leftPos + 1, topPos + 1, leftPos + GUI_WIDTH - 1, topPos + HEADER_HEIGHT, HEADER_BG);
        guiGraphics.fill(leftPos + 1, topPos + GUI_HEIGHT - FOOTER_HEIGHT,
            leftPos + GUI_WIDTH - 1, topPos + GUI_HEIGHT - 1, FOOTER_BG);

        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 6, TEXT_PRIMARY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 配置工具完全使用自定义标题和面板，不显示容器默认标签。
    }

    private void renderInfoPanel(GuiGraphics guiGraphics) {
        int panelX = leftPos + PANEL_PADDING;
        int panelY = topPos + HEADER_HEIGHT + PANEL_PADDING;
        int panelWidth = menu.canEditTheme() ? INFO_PANEL_WIDTH : GUI_WIDTH - PANEL_PADDING * 2;
        int panelHeight = getContentHeight();

        renderPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight,
            Component.translatable("gui.ruralroutes.config_tool.section.block_info"));

        int contentX = panelX + 6;
        int contentWidth = panelWidth - 12;
        int lineY = panelY + PANEL_HEADER_HEIGHT + 6;

        lineY = drawWrappedText(guiGraphics, getBlockTypeLine(), contentX, lineY, contentWidth, TEXT_PRIMARY);
        lineY += 2;
        lineY = drawWrappedText(guiGraphics, getNodeLine(), contentX, lineY, contentWidth,
            menu.getCurrentTradeNodeId() != null ? TEXT_GOOD : TEXT_WARN);

        if (menu.canEditTheme()) {
            lineY += 8;
            lineY = drawSectionTitle(guiGraphics,
                Component.translatable("gui.ruralroutes.config_tool.section.current_config"),
                contentX, lineY, contentWidth);
            lineY = drawWrappedText(guiGraphics, getCurrentThemeLine(), contentX, lineY, contentWidth, TEXT_MUTED);
            lineY += 2;
            ResourceLocation selected = menu.getSelectedTheme();
            if (selected != null) {
                lineY = drawWrappedText(guiGraphics,
                    Component.translatable("gui.ruralroutes.config_tool.selected", selected.toString()),
                    contentX, lineY, contentWidth, TEXT_ACCENT);
            } else {
                lineY = drawWrappedText(guiGraphics,
                    Component.translatable("gui.ruralroutes.config_tool.no_selection"),
                    contentX, lineY, contentWidth, TEXT_MUTED);
            }
        }

        lineY += 8;
        lineY = drawSectionTitle(guiGraphics,
            Component.translatable("gui.ruralroutes.config_tool.section.clipboard"),
            contentX, lineY, contentWidth);

        if (menu.hasCopiedNodeInfo()) {
            String shortId = shortenUuid(menu.getCopiedNodeId());
            lineY = drawWrappedText(guiGraphics,
                Component.translatable("gui.ruralroutes.config_tool.copied_node_info", shortId),
                contentX, lineY, contentWidth, TEXT_GOOD);
            lineY += 2;
            lineY = drawWrappedText(guiGraphics,
                Component.translatable("gui.ruralroutes.config_tool.clipboard_source",
                    menu.getCopiedStationPos().toShortString()),
                contentX, lineY, contentWidth, TEXT_MUTED);
        } else {
            lineY = drawWrappedText(guiGraphics,
                Component.translatable("gui.ruralroutes.config_tool.clipboard_empty"),
                contentX, lineY, contentWidth, TEXT_MUTED);
        }

        if (!menu.canEditTheme()) {
            lineY += 8;
            lineY = drawSectionTitle(guiGraphics,
                Component.translatable("gui.ruralroutes.config_tool.section.link_target"),
                contentX, lineY, contentWidth);
            lineY = drawWrappedText(guiGraphics,
                Component.translatable(menu.hasCopiedNodeInfo()
                    ? "gui.ruralroutes.config_tool.paste_ready"
                    : "gui.ruralroutes.config_tool.paste_missing"),
                contentX, lineY, contentWidth, menu.hasCopiedNodeInfo() ? TEXT_ACCENT : TEXT_WARN);
        }
    }

    private void renderThemePanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int panelX = getThemePanelX();
        int panelY = topPos + HEADER_HEIGHT + PANEL_PADDING;
        int panelWidth = getThemePanelWidth();
        int panelHeight = getContentHeight();
        renderPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight,
            Component.translatable("gui.ruralroutes.config_tool.section.available_themes"));

        List<ResourceLocation> themes = menu.getAvailableThemes();
        if (themes.isEmpty()) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.config_tool.theme_list_empty"),
                panelX + panelWidth / 2, panelY + panelHeight / 2 - 4, TEXT_MUTED);
            return;
        }

        int rowX = panelX + 6;
        int rowY = panelY + PANEL_HEADER_HEIGHT + 6;
        int rowWidth = getThemeRowWidth();

        ResourceLocation currentTheme = menu.getCurrentTheme();
        ResourceLocation selectedTheme = menu.getSelectedTheme();
        int visibleCount = Math.min(MAX_VISIBLE, themes.size() - scrollOffset);
        for (int i = 0; i < visibleCount; i++) {
            ResourceLocation theme = themes.get(scrollOffset + i);
            int entryY = rowY + i * (THEME_ENTRY_HEIGHT + THEME_ENTRY_SPACING);
            renderThemeEntry(guiGraphics, theme, rowX, entryY, rowWidth,
                mouseX, mouseY, theme.equals(currentTheme), theme.equals(selectedTheme));
        }

        renderScrollbar(guiGraphics, panelX + panelWidth - 8, rowY,
            MAX_VISIBLE * THEME_ENTRY_HEIGHT + (MAX_VISIBLE - 1) * THEME_ENTRY_SPACING, themes.size());
    }

    private void renderThemeEntry(GuiGraphics guiGraphics, ResourceLocation theme, int x, int y, int width,
                                  int mouseX, int mouseY, boolean isCurrent, boolean isSelected) {
        boolean hovered = isMouseOverEntry(mouseX, mouseY, x, y, width, THEME_ENTRY_HEIGHT);
        int bgColor = isSelected ? THEME_SELECTED_BG : hovered ? THEME_HOVER_BG : THEME_BG;

        guiGraphics.fill(x, y, x + width, y + THEME_ENTRY_HEIGHT, bgColor);
        guiGraphics.renderOutline(x, y, width, THEME_ENTRY_HEIGHT, THEME_BORDER);

        int stripeColor = isSelected ? THEME_SELECTED_MARK : isCurrent ? THEME_CURRENT_MARK : THEME_BORDER;
        guiGraphics.fill(x, y, x + 3, y + THEME_ENTRY_HEIGHT, stripeColor);

        String primary = ellipsize(formatThemeTitle(theme), width - 18);
        String secondary = ellipsize(theme.toString(), width - 18);
        guiGraphics.drawString(font, primary, x + 8, y + 4, TEXT_PRIMARY);
        guiGraphics.drawString(font, secondary, x + 8, y + 14, isCurrent ? TEXT_GOOD : TEXT_MUTED);

        if (isCurrent) {
            guiGraphics.fill(x + width - 7, y + 4, x + width - 3, y + 8, THEME_CURRENT_MARK);
        }
        if (isSelected) {
            guiGraphics.fill(x + width - 7, y + THEME_ENTRY_HEIGHT - 8,
                x + width - 3, y + THEME_ENTRY_HEIGHT - 4, THEME_SELECTED_MARK);
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int height, int itemCount) {
        if (itemCount <= MAX_VISIBLE) {
            return;
        }

        guiGraphics.fill(x, y, x + 4, y + height, SCROLLBAR_TRACK);

        int maxScroll = itemCount - MAX_VISIBLE;
        int thumbHeight = Math.max(18, height * MAX_VISIBLE / itemCount);
        int thumbY = y + (height - thumbHeight) * scrollOffset / maxScroll;
        guiGraphics.fill(x, thumbY, x + 4, thumbY + thumbHeight, SCROLLBAR_THUMB);
    }

    private void renderThemeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ResourceLocation hoveredTheme = getHoveredTheme(mouseX, mouseY);
        if (hoveredTheme == null) {
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(hoveredTheme.toString()));

        ThemeTemplate template = ThemeManager.INSTANCE.getTheme(hoveredTheme);
        if (template != null) {
            tooltip.add(Component.translatable("gui.ruralroutes.config_tool.theme_biome", template.biome().toString()));
        }

        guiGraphics.renderTooltip(font,
            tooltip.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
    }

    private void renderPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, Component title) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BG);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + PANEL_HEADER_HEIGHT, PANEL_HEADER_BG);
        guiGraphics.renderOutline(x, y, width, height, PANEL_BORDER);
        guiGraphics.drawString(font, title, x + 6, y + 5, TEXT_ACCENT);
    }

    private int drawSectionTitle(GuiGraphics guiGraphics, Component title, int x, int y, int width) {
        guiGraphics.drawString(font, title, x, y, TEXT_ACCENT);
        int dividerY = y + font.lineHeight + 2;
        guiGraphics.fill(x, dividerY, x + width, dividerY + 1, DIVIDER_COLOR);
        return dividerY + 4;
    }

    private int drawWrappedText(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
        List<FormattedCharSequence> lines = font.split(text, width);
        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(font, line, x, y, color, false);
            y += font.lineHeight + 1;
        }
        return y;
    }

    private Component getBlockTypeLine() {
        return switch (menu.getBlockType()) {
            case TRADE_STATION -> Component.translatable("block.ruralroutes.trade_station");
            case DISPLAY_CASE -> Component.translatable("block.ruralroutes.display_case");
            case RUMOR_BOARD -> Component.translatable("block.ruralroutes.rumor_board");
            default -> Component.translatable("gui.ruralroutes.config_tool.unknown_block");
        };
    }

    private Component getNodeLine() {
        UUID nodeId = menu.getCurrentTradeNodeId();
        if (nodeId == null) {
            return Component.translatable("gui.ruralroutes.config_tool.no_node_id");
        }
        return Component.translatable("gui.ruralroutes.config_tool.node_id", shortenUuid(nodeId));
    }

    private Component getCurrentThemeLine() {
        ResourceLocation current = menu.getCurrentTheme();
        if (current == null) {
            return Component.translatable("gui.ruralroutes.config_tool.no_current_theme");
        }
        return Component.translatable("gui.ruralroutes.config_tool.current_theme", current.toString());
    }

    private int getContentHeight() {
        return GUI_HEIGHT - HEADER_HEIGHT - FOOTER_HEIGHT - PANEL_PADDING * 2;
    }

    private int getThemePanelX() {
        return leftPos + PANEL_PADDING * 2 + INFO_PANEL_WIDTH;
    }

    private int getThemePanelWidth() {
        return GUI_WIDTH - INFO_PANEL_WIDTH - PANEL_PADDING * 3;
    }

    private int getThemeRowWidth() {
        return getThemePanelWidth() - 16;
    }

    private ResourceLocation getHoveredTheme(double mouseX, double mouseY) {
        if (!menu.canEditTheme()) {
            return null;
        }

        List<ResourceLocation> themes = menu.getAvailableThemes();
        if (themes.isEmpty()) {
            return null;
        }

        int rowX = getThemePanelX() + 6;
        int rowY = topPos + HEADER_HEIGHT + PANEL_PADDING + PANEL_HEADER_HEIGHT + 6;
        int rowWidth = getThemeRowWidth();

        for (int i = 0; i < Math.min(MAX_VISIBLE, themes.size() - scrollOffset); i++) {
            int entryY = rowY + i * (THEME_ENTRY_HEIGHT + THEME_ENTRY_SPACING);
            if (isMouseOverEntry(mouseX, mouseY, rowX, entryY, rowWidth, THEME_ENTRY_HEIGHT)) {
                return themes.get(scrollOffset + i);
            }
        }
        return null;
    }

    private boolean isMouseOverEntry(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            ResourceLocation hoveredTheme = getHoveredTheme(mouseX, mouseY);
            if (hoveredTheme != null) {
                selectTheme(hoveredTheme);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.canEditTheme() && isMouseOverThemePanel(mouseX, mouseY)) {
            List<ResourceLocation> themes = menu.getAvailableThemes();
            int maxScroll = Math.max(0, themes.size() - MAX_VISIBLE);
            int direction = scrollY > 0 ? -1 : 1;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + direction));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isMouseOverThemePanel(double mouseX, double mouseY) {
        if (!menu.canEditTheme()) {
            return false;
        }
        int x = getThemePanelX();
        int y = topPos + HEADER_HEIGHT + PANEL_PADDING;
        int width = getThemePanelWidth();
        int height = getContentHeight();
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private String shortenUuid(UUID nodeId) {
        return nodeId.toString().substring(0, 8) + "...";
    }

    private String formatThemeTitle(ResourceLocation theme) {
        String[] parts = theme.getPath().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
