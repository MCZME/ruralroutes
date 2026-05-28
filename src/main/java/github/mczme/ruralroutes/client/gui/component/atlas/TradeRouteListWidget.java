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

    private static final int MAX_VISIBLE_ROWS = 3;

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeRoute> onSelectRoute;
    private final Button markStartButton;
    private final Button createButton;
    private final Button addStopButton;
    private final Button deleteButton;
    private final List<Button> buttons;

    public TradeRouteListWidget(int x, int y, int width, int height,
            TradeAtlasState state, TradeAtlasViewState viewState, Consumer<TradeRoute> onSelectRoute) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.routes"));
        this.state = state;
        this.viewState = viewState;
        this.onSelectRoute = onSelectRoute;

        int buttonX = getX() + 8;
        int buttonWidth = (getWidth() - 20) / 2;
        int buttonY = getY() + getHeight() - 42;
        markStartButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.mark_start"),
            pressed -> viewState.selectedNode(state).ifPresent(viewState::setRouteDraftStartNode)
        ).bounds(buttonX, buttonY, buttonWidth, 18).build();
        createButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.create"),
            pressed -> createRouteFromSelection()
        ).bounds(buttonX + buttonWidth + 4, buttonY, buttonWidth, 18).build();
        addStopButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.add_stop"),
            pressed -> addSelectedNodeAsStop()
        ).bounds(buttonX, buttonY + 22, buttonWidth, 18).build();
        deleteButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.delete"),
            pressed -> viewState.selectedRoute(state).ifPresent(route ->
                PacketDistributor.sendToServer(TradeRouteActionPayload.deleteRoute(route.id())))
        ).bounds(buttonX + buttonWidth + 4, buttonY + 22, buttonWidth, 18).build();
        buttons = List.of(markStartButton, createButton, addStopButton, deleteButton);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        updateButtonStates();
        TradeAtlasUi.renderPanel(guiGraphics, font, getX(), getY(), getWidth(), getHeight(), getMessage());

        int rowX = getX() + 8;
        int rowY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 7;
        int rowWidth = getWidth() - 16;

        if (state.routes().isEmpty()) {
            guiGraphics.drawString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.route.empty"),
                rowX,
                rowY + 4,
                TradeAtlasUi.TEXT_DIM,
                false);
        } else {
            int visibleRows = Math.min(MAX_VISIBLE_ROWS, state.routes().size());
            for (int i = 0; i < visibleRows; i++) {
                TradeRoute route = state.routes().get(i);
                int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
                boolean hovered = mouseX >= rowX && mouseX < rowX + rowWidth
                    && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT;
                renderRouteRow(guiGraphics, rowX, rowTop, rowWidth, route, hovered);
            }
        }

        renderDraftHint(guiGraphics, rowX, getY() + getHeight() - 54);
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

        guiGraphics.fill(x + 4, y + 5, x + 10, y + 11, route.color());
        guiGraphics.drawString(font, state.isRouteVisible(route.id()) ? "V" : "-", x + 13, y + 5,
            state.isRouteVisible(route.id()) ? TradeAtlasUi.TEXT_GOOD : TradeAtlasUi.TEXT_DIM, false);
        String label = route.name() + " " + Component.translatable(route.status().translationKey()).getString();
        label = TradeAtlasUi.ellipsize(font, label, width - 34);
        guiGraphics.drawString(font, label, x + 25, y + 5, TradeAtlasUi.TEXT_PRIMARY, false);
    }

    private void renderDraftHint(GuiGraphics guiGraphics, int x, int y) {
        var font = Minecraft.getInstance().font;
        String hint = viewState.routeDraftStartNodeId()
            .flatMap(state::findNodeById)
            .map(node -> Component.translatable("gui.ruralroutes.trade_atlas.route.start_hint",
                TradeAtlasUi.buildShortNodeLabel(node)).getString())
            .orElse(Component.translatable("gui.ruralroutes.trade_atlas.route.no_start_hint").getString());
        guiGraphics.drawString(font, TradeAtlasUi.ellipsize(font, hint, getWidth() - 16),
            x, y, TradeAtlasUi.TEXT_DIM, false);
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
        int rowX = getX() + 8;
        int rowY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 7;
        int rowWidth = getWidth() - 16;
        int visibleRows = Math.min(MAX_VISIBLE_ROWS, state.routes().size());

        for (int i = 0; i < visibleRows; i++) {
            int rowTop = rowY + i * (TradeAtlasUi.ROW_HEIGHT + TradeAtlasUi.ROW_GAP);
            if (mouseX >= rowX && mouseX < rowX + rowWidth
                    && mouseY >= rowTop && mouseY < rowTop + TradeAtlasUi.ROW_HEIGHT) {
                return Optional.of(state.routes().get(i));
            }
        }
        return Optional.empty();
    }

    private void createRouteFromSelection() {
        Optional<UUID> startNodeId = viewState.routeDraftStartNodeId();
        Optional<TradeAtlasNode> selectedNode = viewState.selectedNode(state);
        if (startNodeId.isEmpty() || selectedNode.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(TradeRouteActionPayload.createRoute(startNodeId.get(), selectedNode.get().id()));
        viewState.clearRouteDraftStartNode();
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

        markStartButton.active = selectedNode.isPresent();
        createButton.active = startNodeId.isPresent() && selectedNode.isPresent();
        addStopButton.active = selectedRoute.isPresent() && selectedNode.isPresent();
        deleteButton.active = selectedRoute.isPresent();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
