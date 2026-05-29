package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.network.packet.TradeRouteActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class TradeRouteListWidget extends AbstractWidget {

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeRoute> onSelectRoute;
    private final Button newRouteButton;
    private final Button addStopButton;
    private final Button deleteButton;
    private final List<Button> buttons;
    private int scrollOffset;

    public TradeRouteListWidget(int x, int y, int width, int height,
            TradeAtlasState state, TradeAtlasViewState viewState, Consumer<TradeRoute> onSelectRoute) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.routes"));
        this.state = state;
        this.viewState = viewState;
        this.onSelectRoute = onSelectRoute;

        int buttonX = getX() + 6;
        int fullButtonWidth = getWidth() - 12;
        int smallButtonWidth = (getWidth() - 20) / 3;
        int buttonY = actionButtonTop();
        newRouteButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.new"),
            pressed -> toggleRouteDraft()
        ).bounds(buttonX, buttonY, smallButtonWidth, TradeAtlasUi.BUTTON_HEIGHT).build();
        addStopButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.add_stop"),
            pressed -> addSelectedNodeAsStop()
        ).bounds(buttonX + smallButtonWidth + 4, buttonY, smallButtonWidth, TradeAtlasUi.BUTTON_HEIGHT).build();
        deleteButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.delete"),
            pressed -> viewState.selectedRoute(state).ifPresent(route ->
                PacketDistributor.sendToServer(TradeRouteActionPayload.deleteRoute(route.id())))
        ).bounds(buttonX + (smallButtonWidth + 4) * 2, buttonY, fullButtonWidth - smallButtonWidth * 2 - 8,
            TradeAtlasUi.BUTTON_HEIGHT).build();
        buttons = List.of(newRouteButton, addStopButton, deleteButton);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        layoutButtons();
        updateButtonStates();
        clampScrollOffset();
        TradeAtlasUi.renderPanel(guiGraphics, font, getX(), getY(), getWidth(), getHeight(), getMessage());

        int rowX = getX() + 6;
        int rowY = listTop();
        int rowWidth = listRowWidth();

        int visibleRows = 0;
        if (state.routes().isEmpty()) {
            guiGraphics.drawString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.route.empty"),
                rowX,
                rowY + 4,
                TradeAtlasUi.TEXT_DIM,
                false);
        } else {
            visibleRows = Math.min(maxVisibleRows(), state.routes().size());
            for (int i = 0; i < visibleRows; i++) {
                TradeRoute route = state.routes().get(scrollOffset + i);
                int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
                boolean hovered = mouseX >= rowX && mouseX < rowX + rowWidth
                    && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT;
                renderRouteRow(guiGraphics, rowX, rowTop, rowWidth, route, hovered);
            }
        }
        renderScrollbar(guiGraphics, rowY, visibleRows);

        int hintY = actionButtonTop() - 11;
        int rowBottom = rowY + visibleRows * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
        if (state.routes().isEmpty() || hintY >= rowBottom + 2) {
            renderDraftHint(guiGraphics, rowX, hintY);
        }
        for (Button button : buttons) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderRouteRow(GuiGraphics guiGraphics, int x, int y, int width, TradeRoute route, boolean hovered) {
        var font = Minecraft.getInstance().font;
        int bg = viewState.isRouteSelected(route.id()) || hovered ? TradeAtlasUi.ROW_SELECTED_BG : TradeAtlasUi.ROW_BG;
        guiGraphics.fill(x, y, x + width, y + TradeAtlasUi.ROW_HEIGHT, bg);
        guiGraphics.renderOutline(x, y, width, TradeAtlasUi.ROW_HEIGHT,
            viewState.isRouteSelected(route.id()) ? route.color() : TradeAtlasUi.ROW_BORDER);

        guiGraphics.fill(x + 4, y + 4, x + 10, y + 10, route.color());
        guiGraphics.drawString(font, state.isRouteVisible(route.id()) ? "●" : "-", x + 13, y + 3,
            state.isRouteVisible(route.id()) ? TradeAtlasUi.TEXT_GOOD : TradeAtlasUi.TEXT_DIM, false);
        String status = Component.translatable(route.status().translationKey()).getString();
        int statusWidth = Math.min(font.width(status), 28);
        String label = TradeAtlasUi.ellipsize(font, route.name(), width - 38 - statusWidth);
        guiGraphics.drawString(font, label, x + 25, y + 3, TradeAtlasUi.TEXT_PRIMARY, false);
        guiGraphics.drawString(font, TradeAtlasUi.ellipsize(font, status, statusWidth),
            x + width - 6 - statusWidth, y + 3, route.color(), false);
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int rowY, int visibleRows) {
        if (state.routes().size() <= visibleRows || visibleRows <= 0) {
            return;
        }
        int trackX = getX() + getWidth() - 6;
        int trackY = rowY;
        int trackHeight = visibleRows * TradeAtlasUi.ROW_HEIGHT + Math.max(0, visibleRows - 1) * TradeAtlasUi.ROW_GAP;
        int thumbHeight = Math.max(10, trackHeight * visibleRows / state.routes().size());
        int maxScroll = Math.max(1, state.routes().size() - visibleRows);
        int thumbY = trackY + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
        guiGraphics.fill(trackX, trackY, trackX + 2, trackY + trackHeight, 0x663A424C);
        guiGraphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, TradeAtlasUi.TEXT_DIM);
    }

    private void renderDraftHint(GuiGraphics guiGraphics, int x, int y) {
        var font = Minecraft.getInstance().font;
        boolean drafting = viewState.routeDraftStartNodeId().isPresent();
        String hint = viewState.routeDraftStartNodeId()
            .flatMap(state::findNodeById)
            .map(node -> Component.translatable("gui.ruralroutes.trade_atlas.route.pick_endpoint_hint",
                TradeAtlasUi.buildShortNodeLabel(node)).getString())
            .orElse(Component.translatable("gui.ruralroutes.trade_atlas.route.new_route_hint").getString());
        guiGraphics.drawString(font, TradeAtlasUi.ellipsize(font, hint, getWidth() - 12),
            x, y, drafting ? TradeAtlasUi.TEXT_WARN : TradeAtlasUi.TEXT_DIM, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        Optional<TradeRoute> route = findRouteAt(mouseX, mouseY);
        if (route.isPresent()) {
            if (mouseX < getX() + 32) {
                PacketDistributor.sendToServer(TradeRouteActionPayload.toggleRouteVisible(route.get().id()));
            } else {
                onSelectRoute.accept(route.get());
            }
            return true;
        }

        for (Button actionButton : buttons) {
            if (actionButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    private Optional<TradeRoute> findRouteAt(double mouseX, double mouseY) {
        int rowX = getX() + 6;
        int rowY = listTop();
        int rowWidth = listRowWidth();
        int visibleRows = Math.min(maxVisibleRows(), state.routes().size());

        for (int i = 0; i < visibleRows; i++) {
            int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
            if (mouseX >= rowX && mouseX < rowX + rowWidth
                    && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT) {
                int routeIndex = scrollOffset + i;
                if (routeIndex < state.routes().size()) {
                    return Optional.of(state.routes().get(routeIndex));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY) || state.routes().size() <= maxVisibleRows()) {
            return false;
        }
        scrollOffset -= (int) Math.signum(scrollY);
        clampScrollOffset();
        return true;
    }

    public void renderHoveredTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Optional<TradeRoute> hovered = findRouteAt(mouseX, mouseY);
        if (hovered.isEmpty()) {
            return;
        }
        var font = Minecraft.getInstance().font;
        TradeRoute route = hovered.get();
        guiGraphics.renderTooltip(font,
            List.of(
                Component.literal(route.name()),
                Component.translatable(route.status().translationKey()),
                Component.translatable("gui.ruralroutes.trade_atlas.detail.visible").append(": ")
                    .append(Component.translatable(state.isRouteVisible(route.id())
                        ? "gui.ruralroutes.trade_atlas.detail.yes"
                        : "gui.ruralroutes.trade_atlas.detail.no")),
                Component.translatable("gui.ruralroutes.trade_atlas.route.stops").append(": ")
                    .append(String.valueOf(route.stops().size())),
                Component.translatable("gui.ruralroutes.trade_atlas.route.segments").append(": ")
                    .append(String.valueOf(route.segments().size()))
            ),
            Optional.empty(),
            mouseX,
            mouseY);
    }

    private void toggleRouteDraft() {
        if (viewState.routeDraftStartNodeId().isPresent()) {
            viewState.clearRouteDraftStartNode();
            return;
        }
        viewState.selectedNode(state).ifPresent(viewState::setRouteDraftStartNode);
    }

    private void addSelectedNodeAsStop() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeAtlasNode> node = viewState.selectedNode(state);
        if (route.isEmpty() || node.isEmpty()) {
            return;
        }
        UUID anchorStopId = viewState.selectedRouteStop(state)
            .map(stop -> stop.id())
            .orElse(null);
        PacketDistributor.sendToServer(TradeRouteActionPayload.addStop(route.get().id(), node.get().id(), anchorStopId));
    }

    private void updateButtonStates() {
        Optional<TradeAtlasNode> selectedNode = viewState.selectedNode(state);
        Optional<UUID> startNodeId = viewState.routeDraftStartNodeId();
        Optional<TradeRoute> selectedRoute = viewState.selectedRoute(state);

        boolean drafting = startNodeId.isPresent();
        newRouteButton.active = drafting || selectedNode.isPresent();
        newRouteButton.setMessage(Component.translatable(drafting
            ? "gui.ruralroutes.trade_atlas.route.action.cancel_new"
            : "gui.ruralroutes.trade_atlas.route.action.new"));
        addStopButton.active = selectedRoute.isPresent() && selectedNode.isPresent() && !drafting;
        deleteButton.active = selectedRoute.isPresent() && !drafting;
    }

    private void layoutButtons() {
        int buttonX = getX() + 6;
        int fullButtonWidth = getWidth() - 12;
        int smallButtonWidth = (getWidth() - 20) / 3;
        int buttonY = actionButtonTop();
        newRouteButton.setX(buttonX);
        newRouteButton.setY(buttonY);
        newRouteButton.setWidth(smallButtonWidth);
        addStopButton.setX(buttonX + smallButtonWidth + 4);
        addStopButton.setY(buttonY);
        addStopButton.setWidth(smallButtonWidth);
        deleteButton.setX(buttonX + (smallButtonWidth + 4) * 2);
        deleteButton.setY(buttonY);
        deleteButton.setWidth(fullButtonWidth - smallButtonWidth * 2 - 8);
    }

    private int maxVisibleRows() {
        int actionTop = actionButtonTop();
        int listTop = listTop();
        int rowStride = TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP;
        return Math.max(1, (actionTop - listTop) / rowStride);
    }

    private int actionButtonTop() {
        return getY() + getHeight() - TradeAtlasUi.BUTTON_HEIGHT - 4;
    }

    private int listRowWidth() {
        return getWidth() - (state.routes().size() > maxVisibleRows() ? 18 : 12);
    }

    private int listTop() {
        return getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 4;
    }

    private void clampScrollOffset() {
        int maxOffset = Math.max(0, state.routes().size() - maxVisibleRows());
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
