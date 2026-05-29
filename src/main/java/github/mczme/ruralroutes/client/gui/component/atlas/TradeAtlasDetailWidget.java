package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.AtlasNodeStatus;
import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.core.atlas.TradeRouteSegment;
import github.mczme.ruralroutes.core.atlas.TradeRouteStop;
import github.mczme.ruralroutes.network.packet.TradeRouteActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class TradeAtlasDetailWidget extends AbstractWidget {

    private static final int CONTENT_PAD = 8;
    private static final int LINE_HEIGHT = 12;
    private static final int CONTROL_GAP = 4;

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeAtlasNode> onSetTarget;
    private final Runnable onCancelPendingClue;
    private final Runnable onClearTarget;
    private final Runnable onCenterSelected;
    private final Button setTargetButton;
    private final Button cancelPendingClueButton;
    private final Button clearTargetButton;
    private final Button centerButton;
    private final Button renameRouteButton;
    private final Button cycleRouteStatusButton;
    private final Button toggleRouteVisibleButton;
    private final Button removeStopButton;
    private final Button applyRoleButton;
    private final Button applyNoteButton;
    private final Button cycleSegmentDirectionButton;
    private final Button selectNodeButton;
    private final Button selectRouteButton;
    private final List<Button> allButtons;
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
            Runnable onCenterSelected) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.detail"));
        this.state = state;
        this.viewState = viewState;
        this.onSetTarget = onSetTarget;
        this.onCancelPendingClue = onCancelPendingClue;
        this.onClearTarget = onClearTarget;
        this.onCenterSelected = onCenterSelected;

        int controlX = getX() + CONTENT_PAD;
        int controlWidth = getWidth() - CONTENT_PAD * 2;
        setTargetButton = button("gui.ruralroutes.trade_atlas.action.set_target",
            pressed -> viewState.selectedNode(state).ifPresent(onSetTarget));
        cancelPendingClueButton = button("gui.ruralroutes.trade_atlas.action.cancel_pending_clue",
            pressed -> onCancelPendingClue.run());
        clearTargetButton = button("gui.ruralroutes.trade_atlas.action.clear_target",
            pressed -> onClearTarget.run());
        centerButton = button("gui.ruralroutes.trade_atlas.action.center_selected",
            pressed -> onCenterSelected.run());
        renameRouteButton = button("gui.ruralroutes.trade_atlas.route.action.rename",
            pressed -> renameSelectedRoute());
        cycleRouteStatusButton = button("gui.ruralroutes.trade_atlas.route.action.status",
            pressed -> viewState.selectedRoute(state).ifPresent(route ->
                PacketDistributor.sendToServer(TradeRouteActionPayload.cycleRouteStatus(route.id()))));
        toggleRouteVisibleButton = button("gui.ruralroutes.trade_atlas.action.hide_route",
            pressed -> toggleSelectedRouteVisible());
        removeStopButton = button("gui.ruralroutes.trade_atlas.route.action.remove_stop",
            pressed -> removeSelectedStop());
        applyRoleButton = button("gui.ruralroutes.trade_atlas.route.action.apply_role",
            pressed -> updateSelectedStopRole());
        applyNoteButton = button("gui.ruralroutes.trade_atlas.route.action.apply_note",
            pressed -> updateSelectedStopNote());
        cycleSegmentDirectionButton = button("gui.ruralroutes.trade_atlas.route.action.direction",
            pressed -> cycleSelectedSegmentDirection());
        selectNodeButton = button("gui.ruralroutes.trade_atlas.action.select_node",
            pressed -> selectSelectedStopNode());
        selectRouteButton = button("gui.ruralroutes.trade_atlas.action.select_route",
            pressed -> viewState.selectedRoute(state).ifPresent(viewState::selectRoute));
        allButtons = List.of(setTargetButton, cancelPendingClueButton, clearTargetButton, centerButton,
            renameRouteButton, cycleRouteStatusButton, toggleRouteVisibleButton, removeStopButton, applyRoleButton,
            applyNoteButton, cycleSegmentDirectionButton, selectNodeButton, selectRouteButton);

        routeNameField = new EditBox(Minecraft.getInstance().font, controlX, getY(), controlWidth,
            TradeAtlasUi.BUTTON_HEIGHT, Component.translatable("gui.ruralroutes.trade_atlas.route.name"));
        routeNameField.setMaxLength(32);
        stopRoleField = new EditBox(Minecraft.getInstance().font, controlX, getY(), controlWidth,
            TradeAtlasUi.BUTTON_HEIGHT, Component.translatable("gui.ruralroutes.trade_atlas.route.stop_role"));
        stopRoleField.setMaxLength(32);
        stopNoteField = new EditBox(Minecraft.getInstance().font, controlX, getY(), controlWidth,
            TradeAtlasUi.BUTTON_HEIGHT, Component.translatable("gui.ruralroutes.trade_atlas.route.stop_note"));
        stopNoteField.setMaxLength(48);
    }

    private static Button button(String translationKey, Button.OnPress onPress) {
        return Button.builder(Component.translatable(translationKey), onPress)
            .bounds(0, 0, 0, TradeAtlasUi.BUTTON_HEIGHT)
            .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        prepareControls();
        TradeAtlasUi.renderPanel(guiGraphics, font, getX(), getY(), getWidth(), getHeight(), getMessage());
        guiGraphics.enableScissor(getX() + 1, getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 1,
            getX() + getWidth() - 1, getY() + getHeight() - 1);

        switch (viewState.selectionType()) {
            case NODE -> renderNodeDetail(guiGraphics, font);
            case ROUTE -> renderRouteDetail(guiGraphics, font, mouseX, mouseY, partialTick);
            case ROUTE_STOP -> renderRouteStopDetail(guiGraphics, font, mouseX, mouseY, partialTick);
            case ROUTE_SEGMENT -> renderRouteSegmentDetail(guiGraphics, font);
            case NONE -> renderEmptyDetail(guiGraphics, font);
        }

        for (Button button : allButtons) {
            if (button.visible) {
                button.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
        guiGraphics.disableScissor();
    }

    private void renderEmptyDetail(GuiGraphics guiGraphics, Font font) {
        int y = contentTop();
        drawTitle(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.none");
        y += LINE_HEIGHT + 4;
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.current_target",
            state.currentTarget().map(TradeAtlasUi::buildShortNodeLabel).orElse("-"), TradeAtlasUi.TEXT_PRIMARY, 2);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.visible_routes",
            String.valueOf(state.visibleRoutes().size()), TradeAtlasUi.TEXT_GOOD, 1);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.pending_clue",
            yesNo(state.hasPendingClue()), state.hasPendingClue() ? TradeAtlasUi.TEXT_WARN : TradeAtlasUi.TEXT_DIM, 1);
        y += LINE_HEIGHT + 8;
        drawTitle(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.interactions");
        y += LINE_HEIGHT + 4;
        y = drawWrappedHintLine(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.hint.click_select");
        y = drawWrappedHintLine(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.hint.double_route");
        y = drawWrappedHintLine(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.hint.single_route_node");
        y = drawWrappedHintLine(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.hint.multi_route_marker");
        y = drawWrappedHintLine(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.hint.blank_drag");
        drawWrappedHintLine(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.hint.visible_only");
    }

    private void renderNodeDetail(GuiGraphics guiGraphics, Font font) {
        Optional<TradeAtlasNode> selected = viewState.selectedNode(state);
        if (selected.isEmpty()) {
            renderEmptyDetail(guiGraphics, font);
            return;
        }

        TradeAtlasNode node = selected.get();
        int y = contentTop();
        drawTitle(guiGraphics, font, y, TradeAtlasUi.buildShortNodeLabel(node), TradeAtlasUi.TEXT_ACCENT);
        y += LINE_HEIGHT + 4;
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.status",
            Component.translatable(node.status().translationKey()).getString(), TradeAtlasUi.colorForStatus(node.status()), 1);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.target",
            yesNo(isCurrentTarget(node)), isCurrentTarget(node) ? TradeAtlasUi.TARGET_COLOR : TradeAtlasUi.TEXT_DIM, 1);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.related_routes",
            String.valueOf(countVisibleRoutesReferencingNode(node.id())), TradeAtlasUi.TEXT_GOOD, 1);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.position",
            TradeAtlasUi.formatPosition(node.position()), TradeAtlasUi.TEXT_PRIMARY, 2);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.style",
            Component.translatable(node.style().translationKey()).getString(), TradeAtlasUi.styleColor(node.style()), 1);
        String theme = node.themeName().map(ResourceLocation::toString).orElse("-");
        drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.theme", theme,
            node.themeName().isPresent() ? TradeAtlasUi.TEXT_GOOD : TradeAtlasUi.TEXT_DIM, 2);

        if (node.status() == AtlasNodeStatus.INVALID) {
            guiGraphics.drawString(font,
                ellipsize(font, Component.translatable("gui.ruralroutes.trade_atlas.detail.invalid_hint").getString()),
                getX() + CONTENT_PAD,
                controlsTop(4) - LINE_HEIGHT,
                TradeAtlasUi.TEXT_BAD,
                false);
        }
    }

    private void renderRouteDetail(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, float partialTick) {
        Optional<TradeRoute> selected = viewState.selectedRoute(state);
        if (selected.isEmpty()) {
            renderEmptyDetail(guiGraphics, font);
            return;
        }

        TradeRoute route = selected.get();
        int y = contentTop();
        drawTitle(guiGraphics, font, y, route.name(), route.color());
        y += LINE_HEIGHT + 4;
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.route.detail.status",
            Component.translatable(route.status().translationKey()).getString(), route.color(), 1);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.visible",
            yesNo(state.isRouteVisible(route.id())), state.isRouteVisible(route.id()) ? TradeAtlasUi.TEXT_GOOD : TradeAtlasUi.TEXT_DIM, 1);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.route.stops",
            String.valueOf(route.stops().size()), TradeAtlasUi.TEXT_PRIMARY, 1);
        drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.route.segments",
            String.valueOf(route.segments().size()), TradeAtlasUi.TEXT_PRIMARY, 1);
        routeNameField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderRouteStopDetail(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, float partialTick) {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteStop> stop = viewState.selectedRouteStop(state);
        if (route.isEmpty() || stop.isEmpty()) {
            renderEmptyDetail(guiGraphics, font);
            return;
        }

        TradeAtlasNode node = state.findNodeById(stop.get().nodeId()).orElse(null);
        int y = contentTop();
        drawTitle(guiGraphics, font, y, node == null ? "?" : TradeAtlasUi.buildShortNodeLabel(node),
            TradeAtlasUi.TEXT_ACCENT);
        y += LINE_HEIGHT + 4;
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.route",
            route.get().name(), route.get().color(), 2);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.node",
            node == null ? "?" : TradeAtlasUi.buildShortNodeLabel(node), TradeAtlasUi.TEXT_PRIMARY, 2);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.role",
            blankDash(stop.get().role()), TradeAtlasUi.TEXT_MUTED, 2);
        drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.note",
            blankDash(stop.get().note()), TradeAtlasUi.TEXT_MUTED, 2);
        stopRoleField.render(guiGraphics, mouseX, mouseY, partialTick);
        stopNoteField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderRouteSegmentDetail(GuiGraphics guiGraphics, Font font) {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteSegment> segment = viewState.selectedRouteSegment(state);
        if (route.isEmpty() || segment.isEmpty()) {
            renderEmptyDetail(guiGraphics, font);
            return;
        }

        int y = contentTop();
        drawTitle(guiGraphics, font, y, route.get().name(), route.get().color());
        y += LINE_HEIGHT + 4;
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.route",
            route.get().name(), route.get().color(), 2);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.from",
            segmentEndpointLabel(route.get(), segment.get().fromStopId()), TradeAtlasUi.TEXT_PRIMARY, 2);
        y = drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.to",
            segmentEndpointLabel(route.get(), segment.get().toStopId()), TradeAtlasUi.TEXT_PRIMARY, 2);
        drawWrappedField(guiGraphics, font, y, "gui.ruralroutes.trade_atlas.detail.direction",
            Component.translatable(segment.get().direction().translationKey()).getString(), TradeAtlasUi.TEXT_ACCENT, 1);
    }

    private void prepareControls() {
        hideControls();
        syncFields();
        switch (viewState.selectionType()) {
            case NODE -> layoutNodeControls();
            case ROUTE -> layoutRouteControls();
            case ROUTE_STOP -> layoutRouteStopControls();
            case ROUTE_SEGMENT -> layoutRouteSegmentControls();
            case NONE -> {
            }
        }
    }

    private void layoutNodeControls() {
        Optional<TradeAtlasNode> selected = viewState.selectedNode(state);
        boolean hasSelected = selected.isPresent();
        boolean selectedIsTarget = hasSelected && isCurrentTarget(selected.get());
        setTargetButton.visible = true;
        setTargetButton.active = hasSelected && !selectedIsTarget;
        setTargetButton.setMessage(Component.translatable(selectedIsTarget
            ? "gui.ruralroutes.trade_atlas.action.current_target"
            : "gui.ruralroutes.trade_atlas.action.set_target"));
        cancelPendingClueButton.visible = true;
        cancelPendingClueButton.active = state.hasPendingClue();
        clearTargetButton.visible = true;
        clearTargetButton.active = state.currentTargetNodeId().isPresent();
        centerButton.visible = true;
        centerButton.active = hasSelected;
        layoutVerticalButtons(List.of(setTargetButton, cancelPendingClueButton, clearTargetButton, centerButton));
    }

    private void layoutRouteControls() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        if (route.isEmpty()) {
            return;
        }
        int x = getX() + CONTENT_PAD;
        int fieldY = Math.max(contentTop() + LINE_HEIGHT * 5,
            Math.min(contentTop() + LINE_HEIGHT * 8 + 10,
                controlsTop(3) - TradeAtlasUi.BUTTON_HEIGHT - CONTROL_GAP));
        int renameWidth = 32;
        routeNameField.visible = true;
        routeNameField.active = true;
        routeNameField.setX(x);
        routeNameField.setY(fieldY);
        routeNameField.setWidth(getWidth() - CONTENT_PAD * 2 - renameWidth - CONTROL_GAP);
        renameRouteButton.visible = true;
        renameRouteButton.active = true;
        renameRouteButton.setX(x + routeNameField.getWidth() + CONTROL_GAP);
        renameRouteButton.setY(fieldY);
        renameRouteButton.setWidth(renameWidth);
        cycleRouteStatusButton.visible = true;
        cycleRouteStatusButton.active = true;
        toggleRouteVisibleButton.visible = true;
        toggleRouteVisibleButton.active = true;
        toggleRouteVisibleButton.setMessage(Component.translatable(state.isRouteVisible(route.get().id())
            ? "gui.ruralroutes.trade_atlas.action.hide_route"
            : "gui.ruralroutes.trade_atlas.action.show_route"));
        centerButton.visible = true;
        centerButton.active = true;
        layoutVerticalButtons(List.of(toggleRouteVisibleButton, cycleRouteStatusButton, centerButton));
    }

    private void layoutRouteStopControls() {
        Optional<TradeRoute> route = viewState.selectedRoute(state);
        Optional<TradeRouteStop> stop = viewState.selectedRouteStop(state);
        if (route.isEmpty() || stop.isEmpty()) {
            return;
        }

        int x = getX() + CONTENT_PAD;
        int applyWidth = 34;
        int fieldWidth = getWidth() - CONTENT_PAD * 2 - applyWidth - CONTROL_GAP;
        int halfButtonY = controlsTop(2) - TradeAtlasUi.BUTTON_HEIGHT - CONTROL_GAP;
        int roleY = Math.max(contentTop() + LINE_HEIGHT * 5,
            Math.min(contentTop() + LINE_HEIGHT * 8 + 8,
                halfButtonY - TradeAtlasUi.BUTTON_HEIGHT * 2 - CONTROL_GAP * 2));
        stopRoleField.visible = true;
        stopRoleField.active = true;
        stopRoleField.setX(x);
        stopRoleField.setY(roleY);
        stopRoleField.setWidth(fieldWidth);
        applyRoleButton.visible = true;
        applyRoleButton.active = true;
        applyRoleButton.setX(x + fieldWidth + CONTROL_GAP);
        applyRoleButton.setY(roleY);
        applyRoleButton.setWidth(applyWidth);

        int noteY = roleY + TradeAtlasUi.BUTTON_HEIGHT + CONTROL_GAP;
        stopNoteField.visible = true;
        stopNoteField.active = true;
        stopNoteField.setX(x);
        stopNoteField.setY(noteY);
        stopNoteField.setWidth(fieldWidth);
        applyNoteButton.visible = true;
        applyNoteButton.active = true;
        applyNoteButton.setX(x + fieldWidth + CONTROL_GAP);
        applyNoteButton.setY(noteY);
        applyNoteButton.setWidth(applyWidth);

        removeStopButton.visible = true;
        removeStopButton.active = route.get().stops().size() > 2;
        centerButton.visible = true;
        centerButton.active = true;
        layoutVerticalButtons(List.of(removeStopButton, centerButton));
        layoutHalfButtons(selectNodeButton, selectRouteButton, halfButtonY);
        selectNodeButton.visible = true;
        selectRouteButton.visible = true;
        selectNodeButton.active = state.findNodeById(stop.get().nodeId()).isPresent();
        selectRouteButton.active = true;
    }

    private void layoutRouteSegmentControls() {
        cycleSegmentDirectionButton.visible = true;
        cycleSegmentDirectionButton.active = viewState.selectedRouteSegment(state).isPresent();
        selectRouteButton.visible = true;
        selectRouteButton.active = viewState.selectedRoute(state).isPresent();
        centerButton.visible = true;
        centerButton.active = viewState.selectedRouteSegment(state).isPresent();
        layoutVerticalButtons(List.of(cycleSegmentDirectionButton, selectRouteButton, centerButton));
    }

    private void layoutVerticalButtons(List<Button> buttons) {
        int x = getX() + CONTENT_PAD;
        int width = getWidth() - CONTENT_PAD * 2;
        int y = controlsTop(buttons.size());
        for (Button button : buttons) {
            button.setX(x);
            button.setY(y);
            button.setWidth(width);
            y += TradeAtlasUi.BUTTON_HEIGHT + CONTROL_GAP;
        }
    }

    private void layoutHalfButtons(Button left, Button right, int y) {
        int x = getX() + CONTENT_PAD;
        int width = (getWidth() - CONTENT_PAD * 2 - CONTROL_GAP) / 2;
        left.setX(x);
        left.setY(y);
        left.setWidth(width);
        right.setX(x + width + CONTROL_GAP);
        right.setY(y);
        right.setWidth(width);
    }

    private int controlsTop(int count) {
        return getY() + getHeight() - CONTROL_GAP - count * TradeAtlasUi.BUTTON_HEIGHT
            - Math.max(0, count - 1) * CONTROL_GAP;
    }

    private void hideControls() {
        for (Button button : allButtons) {
            button.visible = false;
            button.active = false;
        }
        routeNameField.visible = false;
        stopRoleField.visible = false;
        stopNoteField.visible = false;
    }

    private void drawTitle(GuiGraphics guiGraphics, Font font, int y, String text, int color) {
        guiGraphics.drawString(font, ellipsize(font, text), getX() + CONTENT_PAD, y, color, false);
    }

    private void drawTitle(GuiGraphics guiGraphics, Font font, int y, String translationKey) {
        drawTitle(guiGraphics, font, y, Component.translatable(translationKey).getString(), TradeAtlasUi.TEXT_ACCENT);
    }

    private int drawWrappedField(GuiGraphics guiGraphics, Font font, int y, String labelKey, String value,
            int valueColor, int maxValueLines) {
        int x = getX() + CONTENT_PAD;
        int width = getWidth() - CONTENT_PAD * 2;
        guiGraphics.drawString(font, Component.translatable(labelKey), x, y, TradeAtlasUi.TEXT_DIM, false);
        y += font.lineHeight + 1;
        return drawWrappedText(guiGraphics, font, Component.literal(value), x, y, width, valueColor, maxValueLines) + 3;
    }

    private int drawWrappedHintLine(GuiGraphics guiGraphics, Font font, int y, String translationKey) {
        return drawWrappedText(guiGraphics, font, Component.translatable(translationKey), getX() + CONTENT_PAD, y,
            getWidth() - CONTENT_PAD * 2, TradeAtlasUi.TEXT_MUTED, 1) + 2;
    }

    private int drawWrappedText(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int width,
            int color, int maxLines) {
        List<FormattedCharSequence> lines = font.split(text, width);
        int count = Math.min(maxLines, lines.size());
        for (int i = 0; i < count; i++) {
            guiGraphics.drawString(font, lines.get(i), x, y, color, false);
            y += font.lineHeight + 1;
        }
        return y;
    }

    private String ellipsize(Font font, String text) {
        return TradeAtlasUi.ellipsize(font, text, getWidth() - CONTENT_PAD * 2);
    }

    private int contentTop() {
        return getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 8;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        prepareControls();
        if (routeNameField.visible && routeNameField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (stopRoleField.visible && stopRoleField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (stopNoteField.visible && stopNoteField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (Button actionButton : allButtons) {
            if (actionButton.visible && actionButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        prepareControls();
        return (routeNameField.visible && routeNameField.keyPressed(keyCode, scanCode, modifiers))
            || (stopRoleField.visible && stopRoleField.keyPressed(keyCode, scanCode, modifiers))
            || (stopNoteField.visible && stopNoteField.keyPressed(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        prepareControls();
        return (routeNameField.visible && routeNameField.charTyped(codePoint, modifiers))
            || (stopRoleField.visible && stopRoleField.charTyped(codePoint, modifiers))
            || (stopNoteField.visible && stopNoteField.charTyped(codePoint, modifiers));
    }

    private void syncFields() {
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

    private void renameSelectedRoute() {
        viewState.selectedRoute(state).ifPresent(route ->
            PacketDistributor.sendToServer(TradeRouteActionPayload.renameRoute(route.id(), routeNameField.getValue())));
    }

    private void toggleSelectedRouteVisible() {
        viewState.selectedRoute(state).ifPresent(route ->
            PacketDistributor.sendToServer(TradeRouteActionPayload.toggleRouteVisible(route.id())));
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

    private void selectSelectedStopNode() {
        viewState.selectedRouteStop(state)
            .flatMap(stop -> state.findNodeById(stop.nodeId()))
            .ifPresent(viewState::selectNode);
    }

    private String segmentEndpointLabel(TradeRoute route, UUID stopId) {
        return route.findStop(stopId)
            .flatMap(stop -> state.findNodeById(stop.nodeId()))
            .map(TradeAtlasUi::buildShortNodeLabel)
            .orElse("?");
    }

    private int countVisibleRoutesReferencingNode(UUID nodeId) {
        int count = 0;
        for (TradeRoute route : state.visibleRoutes()) {
            boolean containsNode = route.stops().stream().anyMatch(stop -> stop.nodeId().equals(nodeId));
            if (containsNode) {
                count++;
            }
        }
        return count;
    }

    private boolean isCurrentTarget(TradeAtlasNode node) {
        return state.currentTargetNodeId().isPresent() && state.currentTargetNodeId().get().equals(node.id());
    }

    private String yesNo(boolean value) {
        return Component.translatable(value
            ? "gui.ruralroutes.trade_atlas.detail.yes"
            : "gui.ruralroutes.trade_atlas.detail.no").getString();
    }

    private static String blankDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
