package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.AtlasNodeStatus;
import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.core.atlas.TradeRouteSegment;
import github.mczme.ruralroutes.core.atlas.TradeRouteStop;
import github.mczme.ruralroutes.network.packet.TradeRouteActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class TradeAtlasDetailWidget extends AbstractWidget {

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeAtlasNode> onSetTarget;
    private final Runnable onCancelPendingClue;
    private final Runnable onClearTarget;
    private final Runnable onCenterSelected;
    private final Runnable onToggleLocate;
    private final List<Button> nodeButtons;
    private final List<Button> routeButtons;
    private final Button setTargetButton;
    private final Button cancelPendingClueButton;
    private final Button clearTargetButton;
    private final Button centerButton;
    private final Button locateToggleButton;
    private final Button renameRouteButton;
    private final Button cycleRouteStatusButton;
    private final Button removeStopButton;
    private final Button applyRoleButton;
    private final Button applyNoteButton;
    private final Button cycleSegmentDirectionButton;
    private final EditBox routeNameField;
    private final EditBox stopRoleField;
    private final EditBox stopNoteField;
    private UUID syncedRouteId;
    private UUID syncedStopId;

    public TradeAtlasDetailWidget(int x, int y, int width, int height,
            TradeAtlasState state,
            TradeAtlasViewState viewState,
            Consumer<TradeAtlasNode> onSetTarget,
            Runnable onCancelPendingClue,
            Runnable onClearTarget,
            Runnable onCenterSelected,
            Runnable onToggleLocate) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.detail"));
        this.state = state;
        this.viewState = viewState;
        this.onSetTarget = onSetTarget;
        this.onCancelPendingClue = onCancelPendingClue;
        this.onClearTarget = onClearTarget;
        this.onCenterSelected = onCenterSelected;
        this.onToggleLocate = onToggleLocate;

        int buttonX = getX() + 8;
        int buttonWidth = getWidth() - 16;
        int buttonY = getY() + getHeight() - 108;

        setTargetButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.set_target"),
            pressed -> viewState.selectedNode(state).ifPresent(onSetTarget)
        ).bounds(buttonX, buttonY, buttonWidth, 18).build();
        cancelPendingClueButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.cancel_pending_clue"),
            pressed -> onCancelPendingClue.run()
        ).bounds(buttonX, buttonY + 22, buttonWidth, 18).build();
        clearTargetButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.clear_target"),
            pressed -> onClearTarget.run()
        ).bounds(buttonX, buttonY + 44, buttonWidth, 18).build();
        centerButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.center_selected"),
            pressed -> onCenterSelected.run()
        ).bounds(buttonX, buttonY + 66, buttonWidth, 18).build();
        locateToggleButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.locate"),
            pressed -> onToggleLocate.run()
        ).bounds(buttonX, buttonY + 88, buttonWidth, 18).build();
        nodeButtons = List.of(setTargetButton, cancelPendingClueButton, clearTargetButton, centerButton, locateToggleButton);

        routeNameField = new EditBox(Minecraft.getInstance().font, getX() + 8,
            getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 28, buttonWidth - 42, 16,
            Component.translatable("gui.ruralroutes.trade_atlas.route.name"));
        routeNameField.setMaxLength(32);
        renameRouteButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.rename"),
            pressed -> viewState.selectedRoute(state).ifPresent(route ->
                PacketDistributor.sendToServer(TradeRouteActionPayload.renameRoute(route.id(), routeNameField.getValue())))
        ).bounds(getX() + getWidth() - 42, getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 27, 34, 18).build();
        cycleRouteStatusButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.status"),
            pressed -> viewState.selectedRoute(state).ifPresent(route ->
                PacketDistributor.sendToServer(TradeRouteActionPayload.cycleRouteStatus(route.id())))
        ).bounds(buttonX, getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 48, buttonWidth, 18).build();
        stopRoleField = new EditBox(Minecraft.getInstance().font, buttonX,
            getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 122, buttonWidth, 16,
            Component.translatable("gui.ruralroutes.trade_atlas.route.stop_role"));
        stopRoleField.setMaxLength(32);
        applyRoleButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.apply_role"),
            pressed -> updateSelectedStopRole()
        ).bounds(buttonX, getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 140, buttonWidth, 18).build();
        stopNoteField = new EditBox(Minecraft.getInstance().font, buttonX,
            getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 162, buttonWidth, 16,
            Component.translatable("gui.ruralroutes.trade_atlas.route.stop_note"));
        stopNoteField.setMaxLength(48);
        applyNoteButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.apply_note"),
            pressed -> updateSelectedStopNote()
        ).bounds(buttonX, getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 180, buttonWidth, 18).build();
        removeStopButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.remove_stop"),
            pressed -> removeSelectedStop()
        ).bounds(buttonX, getY() + getHeight() - 42, buttonWidth, 18).build();
        cycleSegmentDirectionButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.route.action.direction"),
            pressed -> cycleSelectedSegmentDirection()
        ).bounds(buttonX, getY() + getHeight() - 20, buttonWidth, 18).build();
        routeButtons = List.of(renameRouteButton, cycleRouteStatusButton, applyRoleButton, applyNoteButton,
            removeStopButton, cycleSegmentDirectionButton);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        TradeAtlasUi.renderPanel(guiGraphics, font, getX(), getY(), getWidth(), getHeight(), getMessage());
        if (viewState.detailMode() == TradeAtlasViewState.DetailMode.ROUTE) {
            syncRouteFields();
            updateRouteButtonStates();
            renderRouteDetail(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            updateNodeButtonStates();
            renderNodeDetail(guiGraphics);
            for (Button button : nodeButtons) {
                button.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private void renderNodeDetail(GuiGraphics guiGraphics) {
        var font = Minecraft.getInstance().font;
        Optional<TradeAtlasNode> selected = viewState.selectedNode(state);
        if (selected.isEmpty()) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.detail.empty"),
                getX() + getWidth() / 2,
                getY() + 48,
                TradeAtlasUi.TEXT_DIM);
            return;
        }

        TradeAtlasNode node = selected.get();
        int cursorY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 8;
        TradeAtlasUi.drawDetailLine(guiGraphics, font, getX() + 8, cursorY,
            Component.translatable("gui.ruralroutes.trade_atlas.detail.status"),
            Component.translatable(node.status().translationKey()),
            TradeAtlasUi.colorForStatus(node.status()));
        cursorY += 18;
        TradeAtlasUi.drawDetailLine(guiGraphics, font, getX() + 8, cursorY,
            Component.translatable("gui.ruralroutes.trade_atlas.detail.style"),
            Component.translatable(node.style().translationKey()),
            TradeAtlasUi.styleColor(node.style()));
        cursorY += 18;
        TradeAtlasUi.drawDetailLine(guiGraphics, font, getX() + 8, cursorY,
            Component.translatable("gui.ruralroutes.trade_atlas.detail.position"),
            Component.literal(TradeAtlasUi.formatPosition(node.position())),
            TradeAtlasUi.TEXT_PRIMARY);
        cursorY += 18;
        String dimension = TradeAtlasUi.ellipsize(font, node.dimensionId().toString(), getWidth() - 16);
        TradeAtlasUi.drawDetailLine(guiGraphics, font, getX() + 8, cursorY,
            Component.translatable("gui.ruralroutes.trade_atlas.detail.dimension"),
            Component.literal(dimension),
            TradeAtlasUi.TEXT_MUTED);
        cursorY += 18;
        TradeAtlasUi.drawDetailLine(guiGraphics, font, getX() + 8, cursorY,
            Component.translatable("gui.ruralroutes.trade_atlas.detail.target"),
            Component.translatable(isCurrentTarget(node)
                ? "gui.ruralroutes.trade_atlas.detail.yes"
                : "gui.ruralroutes.trade_atlas.detail.no"),
            isCurrentTarget(node) ? TradeAtlasUi.TARGET_COLOR : TradeAtlasUi.TEXT_DIM);
        cursorY += 18;
        String theme = node.themeName().map(ResourceLocation::toString).orElse("-");
        TradeAtlasUi.drawDetailLine(guiGraphics, font, getX() + 8, cursorY,
            Component.translatable("gui.ruralroutes.trade_atlas.detail.theme"),
            Component.literal(theme),
            node.themeName().isPresent() ? TradeAtlasUi.TEXT_GOOD : TradeAtlasUi.TEXT_DIM);

        if (node.status() == AtlasNodeStatus.INVALID) {
            guiGraphics.drawString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.detail.invalid_hint"),
                getX() + 8,
                getY() + getHeight() - 100,
                TradeAtlasUi.TEXT_BAD,
                false);
        }
    }

    private void renderRouteDetail(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        Optional<TradeRoute> selected = viewState.selectedRoute(state);
        if (selected.isEmpty()) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.route.empty"),
                getX() + getWidth() / 2,
                getY() + 48,
                TradeAtlasUi.TEXT_DIM);
            return;
        }

        TradeRoute route = selected.get();
        int cursorY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 8;
        TradeAtlasUi.drawDetailLine(guiGraphics, font, getX() + 8, cursorY,
            Component.translatable("gui.ruralroutes.trade_atlas.route.detail.status"),
            Component.translatable(route.status().translationKey()),
            route.color());
        routeNameField.render(guiGraphics, mouseX, mouseY, partialTick);
        renameRouteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        cycleRouteStatusButton.render(guiGraphics, mouseX, mouseY, partialTick);

        renderStops(guiGraphics, route);
        stopRoleField.render(guiGraphics, mouseX, mouseY, partialTick);
        applyRoleButton.render(guiGraphics, mouseX, mouseY, partialTick);
        stopNoteField.render(guiGraphics, mouseX, mouseY, partialTick);
        applyNoteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSegments(guiGraphics, route);
        removeStopButton.render(guiGraphics, mouseX, mouseY, partialTick);
        cycleSegmentDirectionButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderStops(GuiGraphics guiGraphics, TradeRoute route) {
        var font = Minecraft.getInstance().font;
        int x = getX() + 8;
        int y = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 70;
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_atlas.route.stops"),
            x,
            y,
            TradeAtlasUi.TEXT_DIM,
            false);
        int visibleStops = Math.min(3, route.stops().size());
        for (int i = 0; i < visibleStops; i++) {
            TradeRouteStop stop = route.stops().get(i);
            int rowY = y + 12 + i * 14;
            int bg = viewState.isRouteStopSelected(stop.id()) ? TradeAtlasUi.ROW_SELECTED_BG : TradeAtlasUi.ROW_BG;
            guiGraphics.fill(x, rowY, getX() + getWidth() - 8, rowY + 12, bg);
            TradeAtlasNode node = state.findNodeById(stop.nodeId()).orElse(null);
            String label = (i + 1) + ". " + (node == null ? "?" : TradeAtlasUi.buildShortNodeLabel(node));
            if (!stop.role().isBlank()) {
                label += " / " + stop.role();
            }
            guiGraphics.drawString(font, TradeAtlasUi.ellipsize(font, label, getWidth() - 22),
                x + 3, rowY + 2, TradeAtlasUi.TEXT_PRIMARY, false);
        }
    }

    private void renderSegments(GuiGraphics guiGraphics, TradeRoute route) {
        var font = Minecraft.getInstance().font;
        int x = getX() + 8;
        int y = getY() + getHeight() - 66;
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_atlas.route.segments"),
            x,
            y,
            TradeAtlasUi.TEXT_DIM,
            false);
        int visibleSegments = Math.min(1, route.segments().size());
        for (int i = 0; i < visibleSegments; i++) {
            TradeRouteSegment segment = route.segments().get(i);
            int rowY = y + 12 + i * 14;
            int bg = viewState.isRouteSegmentSelected(segment.id()) ? TradeAtlasUi.ROW_SELECTED_BG : TradeAtlasUi.ROW_BG;
            guiGraphics.fill(x, rowY, getX() + getWidth() - 8, rowY + 12, bg);
            String label = Component.translatable(segment.direction().translationKey()).getString();
            guiGraphics.drawString(font, TradeAtlasUi.ellipsize(font, label, getWidth() - 22),
                x + 3, rowY + 2, TradeAtlasUi.TEXT_PRIMARY, false);
        }
    }

    private void updateNodeButtonStates() {
        Optional<TradeAtlasNode> selected = viewState.selectedNode(state);
        boolean hasSelected = selected.isPresent();
        boolean selectedIsTarget = hasSelected && isCurrentTarget(selected.get());

        setTargetButton.active = hasSelected && !selectedIsTarget;
        setTargetButton.setMessage(Component.translatable(selectedIsTarget
            ? "gui.ruralroutes.trade_atlas.action.current_target"
            : "gui.ruralroutes.trade_atlas.action.set_target"));
        cancelPendingClueButton.active = state.hasPendingClue();
        clearTargetButton.active = state.currentTargetNodeId().isPresent();
        centerButton.active = hasSelected;
        locateToggleButton.active = !state.locating();
        locateToggleButton.setMessage(Component.translatable(viewState.isLocateSelectionOpen()
            ? "gui.ruralroutes.trade_atlas.action.close_locate"
            : "gui.ruralroutes.trade_atlas.action.locate"));
    }

    private void updateRouteButtonStates() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteStop> stop = viewState.selectedRouteStop(state);
        Optional<TradeRouteSegment> segment = viewState.selectedRouteSegment(state);

        boolean hasRoute = route.isPresent();
        routeNameField.active = hasRoute;
        renameRouteButton.active = hasRoute;
        cycleRouteStatusButton.active = hasRoute;
        stopRoleField.active = stop.isPresent();
        stopNoteField.active = stop.isPresent();
        applyRoleButton.active = stop.isPresent();
        applyNoteButton.active = stop.isPresent();
        removeStopButton.active = hasRoute && stop.isPresent() && route.get().stops().size() > 2;
        cycleSegmentDirectionButton.active = segment.isPresent();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (viewState.detailMode() == TradeAtlasViewState.DetailMode.ROUTE) {
            if (selectRouteDetailRow(mouseX, mouseY)) {
                syncRouteFields();
                return true;
            }
            if (routeNameField.mouseClicked(mouseX, mouseY, button)
                    || stopRoleField.mouseClicked(mouseX, mouseY, button)
                    || stopNoteField.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            for (Button actionButton : routeButtons) {
                if (actionButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }

        for (Button actionButton : nodeButtons) {
            if (actionButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (viewState.detailMode() != TradeAtlasViewState.DetailMode.ROUTE) {
            return false;
        }
        return routeNameField.keyPressed(keyCode, scanCode, modifiers)
            || stopRoleField.keyPressed(keyCode, scanCode, modifiers)
            || stopNoteField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (viewState.detailMode() != TradeAtlasViewState.DetailMode.ROUTE) {
            return false;
        }
        return routeNameField.charTyped(codePoint, modifiers)
            || stopRoleField.charTyped(codePoint, modifiers)
            || stopNoteField.charTyped(codePoint, modifiers);
    }

    private boolean selectRouteDetailRow(double mouseX, double mouseY) {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        if (route.isEmpty()) {
            return false;
        }

        int stopX = getX() + 8;
        int stopY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 82;
        int rowWidth = getWidth() - 16;
        int visibleStops = Math.min(3, route.get().stops().size());
        for (int i = 0; i < visibleStops; i++) {
            int rowY = stopY + i * 14;
            if (mouseX >= stopX && mouseX < stopX + rowWidth && mouseY >= rowY && mouseY < rowY + 12) {
                viewState.selectRouteStop(route.get(), route.get().stops().get(i));
                return true;
            }
        }

        int segmentY = getY() + getHeight() - 54;
        int visibleSegments = Math.min(1, route.get().segments().size());
        for (int i = 0; i < visibleSegments; i++) {
            int rowY = segmentY + i * 14;
            if (mouseX >= stopX && mouseX < stopX + rowWidth && mouseY >= rowY && mouseY < rowY + 12) {
                viewState.selectRouteSegment(route.get(), route.get().segments().get(i));
                return true;
            }
        }
        return false;
    }

    private void syncRouteFields() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteStop> stop = viewState.selectedRouteStop(state);
        UUID routeId = route.map(TradeRoute::id).orElse(null);
        UUID stopId = stop.map(TradeRouteStop::id).orElse(null);
        if (!java.util.Objects.equals(routeId, syncedRouteId)) {
            routeNameField.setValue(route.map(TradeRoute::name).orElse(""));
            syncedRouteId = routeId;
        }
        if (!java.util.Objects.equals(stopId, syncedStopId)) {
            stopRoleField.setValue(stop.map(TradeRouteStop::role).orElse(""));
            stopNoteField.setValue(stop.map(TradeRouteStop::note).orElse(""));
            syncedStopId = stopId;
        }
    }

    private void updateSelectedStopRole() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteStop> stop = viewState.selectedRouteStop(state);
        if (route.isEmpty() || stop.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(TradeRouteActionPayload.updateStopRole(
            route.get().id(), stop.get().id(), stopRoleField.getValue()));
    }

    private void updateSelectedStopNote() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteStop> stop = viewState.selectedRouteStop(state);
        if (route.isEmpty() || stop.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(TradeRouteActionPayload.updateStopNote(
            route.get().id(), stop.get().id(), stopNoteField.getValue()));
    }

    private void removeSelectedStop() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteStop> stop = viewState.selectedRouteStop(state);
        if (route.isEmpty() || stop.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(TradeRouteActionPayload.removeStop(route.get().id(), stop.get().id()));
    }

    private void cycleSelectedSegmentDirection() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteSegment> segment = viewState.selectedRouteSegment(state);
        if (route.isEmpty() || segment.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(TradeRouteActionPayload.cycleSegmentDirection(
            route.get().id(), segment.get().id()));
    }

    private boolean isCurrentTarget(TradeAtlasNode node) {
        return state.currentTargetNodeId().isPresent() && state.currentTargetNodeId().get().equals(node.id());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
