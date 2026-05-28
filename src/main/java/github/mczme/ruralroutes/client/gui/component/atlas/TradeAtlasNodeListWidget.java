package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;

public final class TradeAtlasNodeListWidget extends AbstractWidget {

    private static final int MAX_VISIBLE_ROWS = 8;

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeAtlasNode> onSelect;

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

        int rowX = getX() + 8;
        int rowY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 7;
        int rowWidth = getWidth() - 16;

        if (state.nodes().isEmpty()) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.empty"),
                getX() + getWidth() / 2,
                rowY + 26,
                TradeAtlasUi.TEXT_DIM);
            return;
        }

        int visibleRows = Math.min(MAX_VISIBLE_ROWS, state.nodes().size());
        for (int i = 0; i < visibleRows; i++) {
            TradeAtlasNode node = state.nodes().get(i);
            int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
            boolean hovered = mouseX >= rowX && mouseX < rowX + rowWidth
                && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT;
            renderNodeRow(guiGraphics, rowX, rowTop, rowWidth, node, hovered);
        }
    }

    private void renderNodeRow(GuiGraphics guiGraphics, int x, int y, int width, TradeAtlasNode node, boolean hovered) {
        var font = Minecraft.getInstance().font;
        int bg = viewState.isSelected(node.id()) || hovered ? TradeAtlasUi.ROW_SELECTED_BG : TradeAtlasUi.ROW_BG;
        guiGraphics.fill(x, y, x + width, y + TradeAtlasUi.ROW_HEIGHT, bg);
        guiGraphics.renderOutline(x, y, width, TradeAtlasUi.ROW_HEIGHT,
            viewState.isSelected(node.id()) ? TradeAtlasUi.TEXT_ACCENT : TradeAtlasUi.ROW_BORDER);

        guiGraphics.fill(x + 4, y + 5, x + 10, y + 11, TradeAtlasUi.styleColor(node.style()));
        guiGraphics.fill(x + 12, y + 5, x + 18, y + 11, TradeAtlasUi.colorForStatus(node.status()));

        String label = TradeAtlasUi.buildNodeLabel(node);
        if (font.width(label) > width - 34) {
            label = TradeAtlasUi.ellipsize(font, label, width - 34);
        }
        guiGraphics.drawString(font, label, x + 22, y + 5, TradeAtlasUi.TEXT_PRIMARY, false);

        if (isCurrentTarget(node.id())) {
            guiGraphics.drawString(font, "*", x + width - 10, y + 5, TradeAtlasUi.TARGET_COLOR, false);
        }
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
        int rowX = getX() + 8;
        int rowY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 7;
        int rowWidth = getWidth() - 16;
        int visibleRows = Math.min(MAX_VISIBLE_ROWS, state.nodes().size());

        for (int i = 0; i < visibleRows; i++) {
            int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
            if (mouseX >= rowX && mouseX < rowX + rowWidth
                    && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT) {
                return Optional.of(state.nodes().get(i));
            }
        }
        return Optional.empty();
    }

    private boolean isCurrentTarget(java.util.UUID nodeId) {
        return state.currentTargetNodeId().isPresent() && state.currentTargetNodeId().get().equals(nodeId);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
