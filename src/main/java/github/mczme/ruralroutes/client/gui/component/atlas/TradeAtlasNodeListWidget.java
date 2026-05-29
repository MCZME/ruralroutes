package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class TradeAtlasNodeListWidget extends AbstractWidget {

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeAtlasNode> onSelect;
    private int scrollOffset;

    public TradeAtlasNodeListWidget(int x, int y, int width, int height,
            TradeAtlasState state, TradeAtlasViewState viewState, Consumer<TradeAtlasNode> onSelect) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.nodes"));
        this.state = state;
        this.viewState = viewState;
        this.onSelect = onSelect;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        TradeAtlasUi.renderPanel(guiGraphics, font, getX(), getY(), getWidth(), getHeight(), getMessage());

        int rowX = getX() + 6;
        int rowY = listTop();
        clampScrollOffset();
        int rowWidth = listRowWidth();

        if (state.nodes().isEmpty()) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.empty"),
                getX() + getWidth() / 2,
                rowY + 26,
                TradeAtlasUi.TEXT_DIM);
            return;
        }

        int visibleRows = Math.min(maxVisibleRows(), state.nodes().size());
        for (int i = 0; i < visibleRows; i++) {
            TradeAtlasNode node = state.nodes().get(scrollOffset + i);
            int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
            boolean hovered = mouseX >= rowX && mouseX < rowX + rowWidth
                && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT;
            renderNodeRow(guiGraphics, rowX, rowTop, rowWidth, node, hovered);
        }
        renderScrollbar(guiGraphics, rowY, visibleRows);
    }

    private void renderNodeRow(GuiGraphics guiGraphics, int x, int y, int width, TradeAtlasNode node, boolean hovered) {
        var font = Minecraft.getInstance().font;
        int bg = viewState.isSelected(node.id()) || hovered ? TradeAtlasUi.ROW_SELECTED_BG : TradeAtlasUi.ROW_BG;
        guiGraphics.fill(x, y, x + width, y + TradeAtlasUi.ROW_HEIGHT, bg);
        guiGraphics.renderOutline(x, y, width, TradeAtlasUi.ROW_HEIGHT,
            viewState.isSelected(node.id()) ? TradeAtlasUi.TEXT_ACCENT : TradeAtlasUi.ROW_BORDER);

        guiGraphics.fill(x + 4, y + 4, x + 10, y + 10, TradeAtlasUi.styleColor(node.style()));
        guiGraphics.fill(x + 12, y + 4, x + 18, y + 10, TradeAtlasUi.colorForStatus(node.status()));

        String label = Component.translatable(node.style().translationKey()).getString()
            + " · "
            + Component.translatable(node.status().translationKey()).getString();
        if (font.width(label) > width - 42) {
            label = TradeAtlasUi.ellipsize(font, label, width - 42);
        }
        guiGraphics.drawString(font, label, x + 22, y + 3, TradeAtlasUi.TEXT_PRIMARY, false);

        if (isCurrentTarget(node.id())) {
            guiGraphics.drawString(font, "*", x + width - 10, y + 3, TradeAtlasUi.TARGET_COLOR, false);
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int rowY, int visibleRows) {
        if (state.nodes().size() <= visibleRows || visibleRows <= 0) {
            return;
        }
        int trackX = getX() + getWidth() - 6;
        int trackY = rowY;
        int trackHeight = visibleRows * TradeAtlasUi.ROW_HEIGHT + Math.max(0, visibleRows - 1) * TradeAtlasUi.ROW_GAP;
        int thumbHeight = Math.max(10, trackHeight * visibleRows / state.nodes().size());
        int maxScroll = Math.max(1, state.nodes().size() - visibleRows);
        int thumbY = trackY + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
        guiGraphics.fill(trackX, trackY, trackX + 2, trackY + trackHeight, 0x663A424C);
        guiGraphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, TradeAtlasUi.TEXT_DIM);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        Optional<TradeAtlasNode> node = findNodeAt(mouseX, mouseY);
        if (node.isPresent()) {
            onSelect.accept(node.get());
            return true;
        }
        return false;
    }

    private Optional<TradeAtlasNode> findNodeAt(double mouseX, double mouseY) {
        int rowX = getX() + 6;
        int rowY = listTop();
        int rowWidth = listRowWidth();
        int visibleRows = Math.min(maxVisibleRows(), state.nodes().size());

        for (int i = 0; i < visibleRows; i++) {
            int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
            if (mouseX >= rowX && mouseX < rowX + rowWidth
                    && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT) {
                int nodeIndex = scrollOffset + i;
                if (nodeIndex < state.nodes().size()) {
                    return Optional.of(state.nodes().get(nodeIndex));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY) || state.nodes().size() <= maxVisibleRows()) {
            return false;
        }
        scrollOffset -= (int) Math.signum(scrollY);
        clampScrollOffset();
        return true;
    }

    public void renderHoveredTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Optional<TradeAtlasNode> hovered = findNodeAt(mouseX, mouseY);
        if (hovered.isEmpty()) {
            return;
        }
        var font = Minecraft.getInstance().font;
        TradeAtlasNode node = hovered.get();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(TradeAtlasUi.buildNodeLabel(node)));
        lines.add(Component.literal(TradeAtlasUi.formatPosition(node.position())));
        lines.add(Component.literal(node.dimensionId().toString()));
        node.themeName()
            .map(ResourceLocation::toString)
            .ifPresent(theme -> lines.add(Component.literal(theme)));
        if (isCurrentTarget(node.id())) {
            lines.add(Component.translatable("gui.ruralroutes.trade_atlas.action.current_target"));
        }
        guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }

    private boolean isCurrentTarget(java.util.UUID nodeId) {
        return state.currentTargetNodeId().isPresent() && state.currentTargetNodeId().get().equals(nodeId);
    }

    private int maxVisibleRows() {
        int reservedHeight = TradeAtlasUi.PANEL_HEADER_HEIGHT + 4;
        int rowStride = TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP;
        return Math.max(1, (getHeight() - reservedHeight) / rowStride);
    }

    private int listRowWidth() {
        return getWidth() - (state.nodes().size() > maxVisibleRows() ? 18 : 12);
    }

    private int listTop() {
        return getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 4;
    }

    private void clampScrollOffset() {
        int maxOffset = Math.max(0, state.nodes().size() - maxVisibleRows());
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
