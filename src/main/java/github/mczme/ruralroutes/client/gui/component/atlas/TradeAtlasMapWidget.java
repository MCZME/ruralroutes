package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.AtlasNodeStatus;
import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.core.atlas.TradeRouteDirection;
import github.mczme.ruralroutes.core.atlas.TradeRouteSegment;
import github.mczme.ruralroutes.core.atlas.TradeRouteStop;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TradeAtlasMapWidget extends AbstractWidget {

    private static final int NODE_MARK_SIZE = 8;
    private static final int MAP_TOOLBAR_TOP = 6;
    private static final long DOUBLE_CLICK_WINDOW_MS = 260L;
    private static final double DOUBLE_CLICK_MAX_DISTANCE = 5.0D;
    private static final double SEGMENT_HIT_TOLERANCE = 4.0D;

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeAtlasNode> onSelectNode;
    private final Consumer<TradeRoute> onSelectRoute;
    private final BiConsumer<TradeRoute, TradeRouteStop> onSelectRouteStop;
    private final BiConsumer<TradeRoute, TradeRouteSegment> onSelectRouteSegment;
    private final Consumer<VillageStyle> onLocate;
    private final int toolbarX;
    private final int toolbarWidth;
    private final List<Button> mapButtons = new ArrayList<>();
    private final List<Button> locateStyleButtons = new ArrayList<>();
    private Button locateToggleButton;
    private Button centerSelectedButton;
    private Button centerPlayerButton;
    private Button centerTargetButton;
    private Button zoomOutButton;
    private Button zoomInButton;

    private boolean draggingMap;
    private double mapZoom = 1.0D;
    private double mapOffsetX;
    private double mapOffsetY;
    private long lastClickTime;
    private double lastClickX;
    private double lastClickY;
    private String lastClickKey = "";

    public TradeAtlasMapWidget(int x, int y, int width, int height,
            TradeAtlasState state, TradeAtlasViewState viewState,
            Consumer<TradeAtlasNode> onSelectNode,
            Consumer<TradeRoute> onSelectRoute,
            BiConsumer<TradeRoute, TradeRouteStop> onSelectRouteStop,
            BiConsumer<TradeRoute, TradeRouteSegment> onSelectRouteSegment,
            Consumer<VillageStyle> onLocate, int toolbarX, int toolbarWidth) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.map"));
        this.state = state;
        this.viewState = viewState;
        this.onSelectNode = onSelectNode;
        this.onSelectRoute = onSelectRoute;
        this.onSelectRouteStop = onSelectRouteStop;
        this.onSelectRouteSegment = onSelectRouteSegment;
        this.onLocate = onLocate;
        this.toolbarX = toolbarX;
        this.toolbarWidth = toolbarWidth;
        buildMapButtons();
        buildLocateButtons();
    }

    private void buildMapButtons() {
        boolean compact = toolbarWidth < 190;
        int gap = compact ? 2 : 4;
        int locateWidth = compact ? 24 : 34;
        int actionWidth = compact ? 20 : 28;
        int zoomWidth = compact ? 14 : 18;
        int toolbarContentWidth = locateWidth + actionWidth * 3 + zoomWidth * 2 + gap * 5;
        int buttonY = getY() + MAP_TOOLBAR_TOP;
        int cursorX = toolbarX + Math.max(0, (toolbarWidth - toolbarContentWidth) / 2);

        locateToggleButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.locate"),
            pressed -> viewState.toggleLocateSelection()
        ).bounds(cursorX, buttonY, locateWidth, TradeAtlasUi.BUTTON_HEIGHT).build();
        cursorX += locateWidth + gap;

        centerSelectedButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.map.center_selected"),
            pressed -> centerOnSelected()
        ).bounds(cursorX, buttonY, actionWidth, TradeAtlasUi.BUTTON_HEIGHT).build();
        cursorX += actionWidth + gap;

        centerPlayerButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.map.center_player"),
            pressed -> centerOnPlayer()
        ).bounds(cursorX, buttonY, actionWidth, TradeAtlasUi.BUTTON_HEIGHT).build();
        cursorX += actionWidth + gap;

        centerTargetButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.map.center_target"),
            pressed -> centerOnTarget()
        ).bounds(cursorX, buttonY, actionWidth, TradeAtlasUi.BUTTON_HEIGHT).build();
        cursorX += actionWidth + gap;

        zoomOutButton = Button.builder(
            Component.literal("-"),
            pressed -> mapZoom = Mth.clamp(mapZoom * 0.85D, 0.5D, 4.0D)
        ).bounds(cursorX, buttonY, zoomWidth, TradeAtlasUi.BUTTON_HEIGHT).build();
        cursorX += zoomWidth + gap;

        zoomInButton = Button.builder(
            Component.literal("+"),
            pressed -> mapZoom = Mth.clamp(mapZoom * 1.15D, 0.5D, 4.0D)
        ).bounds(cursorX, buttonY, zoomWidth, TradeAtlasUi.BUTTON_HEIGHT).build();

        mapButtons.addAll(List.of(locateToggleButton, centerSelectedButton, centerPlayerButton, centerTargetButton,
            zoomOutButton, zoomInButton));
    }

    private void buildLocateButtons() {
        int panelX = toolbarX + 4;
        int panelY = getY() + MAP_TOOLBAR_TOP + TradeAtlasUi.BUTTON_HEIGHT + 38;
        int gap = 4;
        int buttonWidth = (toolbarWidth - 16 - gap) / 2;

        List<VillageStyle> styles = TradeAtlasUi.villageStyles();
        for (int i = 0; i < styles.size(); i++) {
            VillageStyle style = styles.get(i);
            int col = i % 2;
            int row = i / 2;
            Button button = Button.builder(
                Component.translatable(style.translationKey()),
                pressed -> {
                    viewState.closeLocateSelection();
                    onLocate.accept(style);
                }
            ).bounds(panelX + col * (buttonWidth + gap), panelY + row * 18, buttonWidth,
                TradeAtlasUi.BUTTON_HEIGHT).build();
            locateStyleButtons.add(button);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderMapArea(guiGraphics);
        updateMapButtonStates();
        for (Button button : mapButtons) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (viewState.isLocateSelectionOpen()) {
            renderLocatePanel(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderMapArea(GuiGraphics guiGraphics) {
        var font = Minecraft.getInstance().font;
        int areaX = areaX();
        int areaY = areaY();
        int areaWidth = areaWidth();
        int areaHeight = areaHeight();

        guiGraphics.fill(areaX, areaY, areaX + areaWidth, areaY + areaHeight, TradeAtlasUi.MAP_BG);
        renderMapGrid(guiGraphics, areaX, areaY, areaWidth, areaHeight);

        if (state.nodes().isEmpty()) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.trade_atlas.empty"),
                areaX + areaWidth / 2,
                areaY + areaHeight / 2 - 4,
                TradeAtlasUi.TEXT_DIM);
            return;
        }

        MapMetrics metrics = calculateMapMetrics(areaWidth, areaHeight);
        guiGraphics.enableScissor(areaX, areaY, areaX + areaWidth, areaY + areaHeight);
        renderVisibleRoutes(guiGraphics, metrics, areaX, areaY, areaWidth, areaHeight);
        renderPlayerMarker(guiGraphics, metrics, areaX, areaY, areaWidth, areaHeight);
        for (TradeAtlasNode node : state.nodes()) {
            int nodeX = metrics.screenX(areaX, areaWidth, node, mapOffsetX);
            int nodeY = metrics.screenY(areaY, areaHeight, node, mapOffsetY);
            renderMapNode(guiGraphics, node, nodeX, nodeY);
        }
        renderRouteStopBadges(guiGraphics, metrics, areaX, areaY, areaWidth, areaHeight);
        guiGraphics.disableScissor();

        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_atlas.map.zoom", (int) Math.round(mapZoom * 100.0D)),
            areaX + 5,
            areaY + areaHeight - 12,
            TradeAtlasUi.TEXT_DIM,
            false);
    }

    private void renderMapGrid(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int centerX = x + width / 2 + (int) Math.round(mapOffsetX);
        int centerY = y + height / 2 + (int) Math.round(mapOffsetY);
        for (int gridX = x + 16; gridX < x + width; gridX += 24) {
            guiGraphics.fill(gridX, y + 1, gridX + 1, y + height - 1, TradeAtlasUi.MAP_GRID);
        }
        for (int gridY = y + 16; gridY < y + height; gridY += 24) {
            guiGraphics.fill(x + 1, gridY, x + width - 1, gridY + 1, TradeAtlasUi.MAP_GRID);
        }
        int clampedCenterX = Mth.clamp(centerX, x + 1, x + width - 2);
        int clampedCenterY = Mth.clamp(centerY, y + 1, y + height - 2);
        guiGraphics.fill(clampedCenterX, y + 1, clampedCenterX + 1, y + height - 1, TradeAtlasUi.MAP_AXIS);
        guiGraphics.fill(x + 1, clampedCenterY, x + width - 1, clampedCenterY + 1, TradeAtlasUi.MAP_AXIS);
    }

    private void renderMapNode(GuiGraphics guiGraphics, TradeAtlasNode node, int x, int y) {
        var font = Minecraft.getInstance().font;
        int half = NODE_MARK_SIZE / 2;
        int color = TradeAtlasUi.colorForStatus(node.status());
        int outline = viewState.isNodeSelected(node.id()) ? TradeAtlasUi.TEXT_ACCENT : TradeAtlasUi.ROW_BORDER;

        if (node.status() == AtlasNodeStatus.CLUE) {
            guiGraphics.renderOutline(x - half, y - half, NODE_MARK_SIZE, NODE_MARK_SIZE, color);
            guiGraphics.fill(x - 1, y - 1, x + 2, y + 2, color);
        } else {
            guiGraphics.fill(x - half, y - half, x + half, y + half, color);
            guiGraphics.renderOutline(x - half, y - half, NODE_MARK_SIZE, NODE_MARK_SIZE, outline);
        }

        if (node.status() == AtlasNodeStatus.RECORDED) {
            guiGraphics.fill(x - 2, y - half - 3, x + 3, y - half - 1, TradeAtlasUi.TEXT_GOOD);
        } else if (node.status() == AtlasNodeStatus.INVALID) {
            guiGraphics.drawString(font, "!", x - 2, y - half - 9, TradeAtlasUi.TEXT_BAD, false);
        }

        if (isCurrentTarget(node)) {
            guiGraphics.renderOutline(x - half - 3, y - half - 3, NODE_MARK_SIZE + 6, NODE_MARK_SIZE + 6,
                TradeAtlasUi.TARGET_COLOR);
        }
        if (viewState.routeDraftStartNodeId().filter(node.id()::equals).isPresent()) {
            guiGraphics.renderOutline(x - half - 5, y - half - 5, NODE_MARK_SIZE + 10, NODE_MARK_SIZE + 10,
                TradeAtlasUi.TEXT_WARN);
        }
    }

    private void renderVisibleRoutes(GuiGraphics guiGraphics, MapMetrics metrics, int areaX, int areaY,
            int areaWidth, int areaHeight) {
        List<RouteSegmentView> segmentViews = buildRouteSegmentViews(metrics, areaX, areaY, areaWidth, areaHeight);
        for (RouteSegmentView view : segmentViews) {
            drawRouteSegment(guiGraphics, view);
        }
    }

    private List<RouteSegmentView> buildRouteSegmentViews(MapMetrics metrics, int areaX, int areaY,
            int areaWidth, int areaHeight) {
        List<RouteSegmentView> views = new ArrayList<>();
        for (TradeRoute route : state.visibleRoutes()) {
            for (TradeRouteSegment segment : route.segments()) {
                TradeRouteStop fromStop = route.findStop(segment.fromStopId()).orElse(null);
                TradeRouteStop toStop = route.findStop(segment.toStopId()).orElse(null);
                if (fromStop == null || toStop == null) {
                    continue;
                }
                TradeAtlasNode fromNode = state.findNodeById(fromStop.nodeId()).orElse(null);
                TradeAtlasNode toNode = state.findNodeById(toStop.nodeId()).orElse(null);
                if (fromNode == null || toNode == null) {
                    continue;
                }
                StopAnchor fromAnchor = stopAnchor(route, fromStop, metrics, areaX, areaY, areaWidth, areaHeight);
                StopAnchor toAnchor = stopAnchor(route, toStop, metrics, areaX, areaY, areaWidth, areaHeight);
                views.add(new RouteSegmentView(route, segment, fromAnchor.x(), fromAnchor.y(), toAnchor.x(),
                    toAnchor.y(), overlapKey(fromNode.id(), toNode.id())));
            }
        }
        return offsetRouteSegmentViews(views);
    }

    private List<RouteSegmentView> offsetRouteSegmentViews(List<RouteSegmentView> views) {
        Map<String, Integer> totals = new HashMap<>();
        Map<String, Integer> seen = new HashMap<>();
        for (RouteSegmentView view : views) {
            totals.merge(view.overlapKey(), 1, Integer::sum);
        }

        List<RouteSegmentView> offsetViews = new ArrayList<>(views.size());
        for (RouteSegmentView view : views) {
            int index = seen.merge(view.overlapKey(), 1, Integer::sum) - 1;
            int count = totals.getOrDefault(view.overlapKey(), 1);
            double dx = view.toX() - view.fromX();
            double dy = view.toY() - view.fromY();
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length < 1.0D) {
                offsetViews.add(view);
                continue;
            }

            double offset = (index - (count - 1) / 2.0D) * 4.0D;
            int ox = (int) Math.round(-dy / length * offset);
            int oy = (int) Math.round(dx / length * offset);
            offsetViews.add(new RouteSegmentView(view.route(), view.segment(), view.fromX() + ox,
                view.fromY() + oy, view.toX() + ox, view.toY() + oy, view.overlapKey()));
        }
        return offsetViews;
    }

    private void drawRouteSegment(GuiGraphics guiGraphics, RouteSegmentView view) {
        double dx = view.toX() - view.fromX();
        double dy = view.toY() - view.fromY();
        double length = Math.sqrt(dx * dx + dy * dy);
        int color = routeSegmentColor(view);
        if (length < 1.0D) {
            drawRouteLoop(guiGraphics, view.fromX(), view.fromY(), color);
            return;
        }

        drawLine(guiGraphics, view.fromX(), view.fromY(), view.toX(), view.toY(), color);
        drawSegmentDirection(guiGraphics, view.segment().direction(), view.fromX(), view.fromY(), view.toX(),
            view.toY(), color);
    }

    private int routeSegmentColor(RouteSegmentView view) {
        if (viewState.isRouteSegmentSelected(view.segment().id())) {
            return TradeAtlasUi.TEXT_ACCENT;
        }
        if (viewState.isRouteSelected(view.route().id())) {
            return TradeAtlasUi.TARGET_COLOR;
        }
        return view.route().color();
    }

    private void drawRouteLoop(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.renderOutline(x - 8, y - 8, 16, 16, color);
    }

    private void drawSegmentDirection(GuiGraphics guiGraphics, TradeRouteDirection direction,
            int x1, int y1, int x2, int y2, int color) {
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.max(1.0D, Math.sqrt(dx * dx + dy * dy));
        int ax = (int) Math.round(dx / length * 3.0D);
        int ay = (int) Math.round(dy / length * 3.0D);
        guiGraphics.fill(midX - 1, midY - 1, midX + 2, midY + 2, TradeAtlasUi.MAP_BG);
        guiGraphics.fill(midX, midY, midX + ax + 1, midY + ay + 1, color);
        if (direction == TradeRouteDirection.BIDIRECTIONAL) {
            guiGraphics.fill(midX, midY, midX - ax + 1, midY - ay + 1, color);
        }
    }

    private void renderRouteStopBadges(GuiGraphics guiGraphics, MapMetrics metrics, int areaX, int areaY,
            int areaWidth, int areaHeight) {
        var font = Minecraft.getInstance().font;
        for (TradeRoute route : state.visibleRoutes()) {
            for (int i = 0; i < route.stops().size(); i++) {
                TradeRouteStop stop = route.stops().get(i);
                if (!shouldRenderStopBadge(route, stop)) {
                    continue;
                }
                StopAnchor anchor = stopAnchor(route, stop, metrics, areaX, areaY, areaWidth, areaHeight);
                int x = anchor.x();
                int y = anchor.y();
                boolean selectedStop = viewState.isRouteStopSelected(stop.id());
                boolean selectedRoute = viewState.isRouteSelected(route.id());
                int bg = selectedStop ? TradeAtlasUi.ROW_SELECTED_BG : selectedRoute ? 0xE0293540 : 0xE0161A1E;
                int outline = selectedStop ? TradeAtlasUi.TEXT_ACCENT : selectedRoute ? TradeAtlasUi.TARGET_COLOR : route.color();
                guiGraphics.fill(x - 5, y - 5, x + 6, y + 6, bg);
                guiGraphics.renderOutline(x - 5, y - 5, 11, 11, outline);
                String label = String.valueOf(Math.min(9, i + 1));
                guiGraphics.drawString(font, label, x - font.width(label) / 2, y - 4,
                    TradeAtlasUi.TEXT_PRIMARY, false);
            }
        }
    }

    private void renderPlayerMarker(GuiGraphics guiGraphics, MapMetrics metrics, int areaX, int areaY,
            int areaWidth, int areaHeight) {
        BlockPos playerPos = playerPosition();
        if (playerPos == null) {
            return;
        }

        int x = metrics.screenX(areaX, areaWidth, playerPos, mapOffsetX);
        int y = metrics.screenY(areaY, areaHeight, playerPos, mapOffsetY);
        guiGraphics.fill(x - 1, y - 4, x + 2, y + 5, TradeAtlasUi.PLAYER_COLOR);
        guiGraphics.fill(x - 4, y - 1, x + 5, y + 2, TradeAtlasUi.PLAYER_COLOR);
        guiGraphics.fill(x - 1, y - 1, x + 2, y + 2, TradeAtlasUi.PLAYER_CORE_COLOR);
    }

    private void renderLocatePanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        int panelX = toolbarX;
        int panelY = getY() + MAP_TOOLBAR_TOP + TradeAtlasUi.BUTTON_HEIGHT + 4;
        int panelWidth = toolbarWidth;
        int panelHeight = 92;
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF022262B);
        guiGraphics.renderOutline(panelX, panelY, panelWidth, panelHeight, TradeAtlasUi.PANEL_BORDER);
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_atlas.locate.choose_style"),
            panelX + 8,
            panelY + 8,
            TradeAtlasUi.TEXT_ACCENT,
            false);
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_atlas.locate.cost_pending"),
            panelX + 8,
            panelY + 20,
            canLocate() ? TradeAtlasUi.TEXT_WARN : TradeAtlasUi.TEXT_DIM,
            false);

        for (Button button : locateStyleButtons) {
            button.visible = true;
            button.active = canLocate();
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public void renderHoveredTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;
        if (isHoveringPlayerMarker(mouseX, mouseY)) {
            BlockPos playerPos = playerPosition();
            if (playerPos != null) {
                guiGraphics.renderTooltip(font,
                    List.of(
                        Component.translatable("gui.ruralroutes.trade_atlas.map.player"),
                        Component.literal(TradeAtlasUi.formatPosition(playerPos))
                    ),
                    Optional.empty(),
                    mouseX,
                    mouseY);
                return;
            }
        }
        findRouteStopBadgeAt(mouseX, mouseY).ifPresent(hit -> {
            TradeAtlasNode node = state.findNodeById(hit.stop().nodeId()).orElse(null);
            String nodeLabel = node == null ? "?" : TradeAtlasUi.buildShortNodeLabel(node);
            guiGraphics.renderTooltip(font,
                List.of(
                    Component.literal(hit.route().name()),
                    Component.literal(nodeLabel),
                    Component.literal(hit.stop().role().isBlank() ? "-" : hit.stop().role())
                ),
                Optional.empty(),
                mouseX,
                mouseY);
        });
        if (findRouteStopBadgeAt(mouseX, mouseY).isPresent()) {
            return;
        }
        findRouteSegmentAt(mouseX, mouseY).ifPresent(hit -> guiGraphics.renderTooltip(font,
            List.of(
                Component.literal(hit.route().name()),
                Component.translatable(hit.segment().direction().translationKey())
            ),
            Optional.empty(),
            mouseX,
            mouseY));
        if (findRouteSegmentAt(mouseX, mouseY).isPresent()) {
            return;
        }
        findMapNodeAt(mouseX, mouseY).ifPresent(node -> guiGraphics.renderTooltip(font,
            List.of(
                Component.translatable(node.style().translationKey()),
                Component.literal(TradeAtlasUi.formatPosition(node.position()))
            ),
            Optional.empty(),
            mouseX,
            mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        for (Button mapButton : mapButtons) {
            if (mapButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (viewState.isLocateSelectionOpen()) {
            for (Button locateButton : locateStyleButtons) {
                if (locateButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            if (isInsideLocatePanel(mouseX, mouseY)) {
                return true;
            }
        }

        MapHit hit = findMapHitAt(mouseX, mouseY);
        boolean doubleClick = isDoubleClick(hit, mouseX, mouseY);
        rememberClick(hit, mouseX, mouseY);

        if (hit.type() == MapHitType.ROUTE_STOP) {
            if (doubleClick) {
                onSelectRoute.accept(hit.route());
            } else {
                onSelectRouteStop.accept(hit.route(), hit.stop());
            }
            return true;
        }

        if (hit.type() == MapHitType.ROUTE_SEGMENT) {
            if (doubleClick) {
                onSelectRoute.accept(hit.route());
            } else {
                onSelectRouteSegment.accept(hit.route(), hit.segment());
            }
            return true;
        }

        if (hit.type() == MapHitType.NODE) {
            if (doubleClick) {
                singleVisibleRouteForNode(hit.node().id()).ifPresent(onSelectRoute);
            } else {
                onSelectNode.accept(hit.node());
            }
            return true;
        }

        if (isInsideMap(mouseX, mouseY)) {
            viewState.clearSelection();
            draggingMap = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingMap && button == 0) {
            mapOffsetX += dragX;
            mapOffsetY += dragY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingMap && button == 0) {
            draggingMap = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInsideMap(mouseX, mouseY)) {
            double factor = scrollY > 0.0D ? 1.15D : 0.85D;
            mapZoom = Mth.clamp(mapZoom * factor, 0.5D, 4.0D);
            return true;
        }
        return false;
    }

    public void centerOnSelected() {
        centerPositionForSelection().ifPresent(this::centerOnPosition);
    }

    public void centerOnPlayer() {
        BlockPos playerPos = playerPosition();
        if (playerPos != null) {
            centerOnPosition(playerPos);
        }
    }

    public void centerOnTarget() {
        state.currentTarget().ifPresent(node -> centerOnPosition(node.position()));
    }

    private Optional<BlockPos> centerPositionForSelection() {
        return switch (viewState.selectionType()) {
            case NODE -> viewState.selectedNode(state).map(TradeAtlasNode::position);
            case ROUTE_STOP -> viewState.selectedRouteStop(state)
                .flatMap(stop -> state.findNodeById(stop.nodeId()))
                .map(TradeAtlasNode::position);
            case ROUTE_SEGMENT -> viewState.selectedRoute(state)
                .flatMap(route -> viewState.selectedRouteSegment(state)
                    .flatMap(segment -> segmentMidpoint(route, segment)));
            case ROUTE -> viewState.selectedRoute(state).flatMap(this::routeCenter);
            case NONE -> Optional.empty();
        };
    }

    private Optional<BlockPos> segmentMidpoint(TradeRoute route, TradeRouteSegment segment) {
        TradeRouteStop fromStop = route.findStop(segment.fromStopId()).orElse(null);
        TradeRouteStop toStop = route.findStop(segment.toStopId()).orElse(null);
        if (fromStop == null || toStop == null) {
            return Optional.empty();
        }
        TradeAtlasNode fromNode = state.findNodeById(fromStop.nodeId()).orElse(null);
        TradeAtlasNode toNode = state.findNodeById(toStop.nodeId()).orElse(null);
        if (fromNode == null || toNode == null) {
            return Optional.empty();
        }
        BlockPos from = fromNode.position();
        BlockPos to = toNode.position();
        return Optional.of(new BlockPos((from.getX() + to.getX()) / 2, (from.getY() + to.getY()) / 2,
            (from.getZ() + to.getZ()) / 2));
    }

    private Optional<BlockPos> routeCenter(TradeRoute route) {
        int count = 0;
        long totalX = 0L;
        long totalY = 0L;
        long totalZ = 0L;
        for (TradeRouteStop stop : route.stops()) {
            TradeAtlasNode node = state.findNodeById(stop.nodeId()).orElse(null);
            if (node == null) {
                continue;
            }
            totalX += node.position().getX();
            totalY += node.position().getY();
            totalZ += node.position().getZ();
            count++;
        }
        if (count == 0) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos((int) (totalX / count), (int) (totalY / count), (int) (totalZ / count)));
    }

    private void centerOnPosition(BlockPos position) {
        MapMetrics metrics = calculateMapMetrics(areaWidth(), areaHeight());
        int positionX = metrics.screenX(areaX(), areaWidth(), position, mapOffsetX);
        int positionY = metrics.screenY(areaY(), areaHeight(), position, mapOffsetY);
        mapOffsetX += areaX() + areaWidth() / 2.0D - positionX;
        mapOffsetY += areaY() + areaHeight() / 2.0D - positionY;
    }

    private void updateMapButtonStates() {
        locateToggleButton.active = !state.locating();
        locateToggleButton.setMessage(Component.translatable(viewState.isLocateSelectionOpen()
            ? "gui.ruralroutes.trade_atlas.action.close_locate"
            : "gui.ruralroutes.trade_atlas.action.locate"));
        centerSelectedButton.active = centerPositionForSelection().isPresent();
        centerPlayerButton.active = playerPosition() != null;
        centerTargetButton.active = state.currentTargetNodeId().isPresent();
        zoomOutButton.active = mapZoom > 0.5D;
        zoomInButton.active = mapZoom < 4.0D;
    }

    private Optional<TradeAtlasNode> findMapNodeAt(double mouseX, double mouseY) {
        if (state.nodes().isEmpty() || !isInsideMap(mouseX, mouseY)) {
            return Optional.empty();
        }

        MapMetrics metrics = calculateMapMetrics(areaWidth(), areaHeight());
        for (TradeAtlasNode node : state.nodes()) {
            int nodeX = metrics.screenX(areaX(), areaWidth(), node, mapOffsetX);
            int nodeY = metrics.screenY(areaY(), areaHeight(), node, mapOffsetY);
            if (Math.abs(mouseX - nodeX) <= 7 && Math.abs(mouseY - nodeY) <= 7) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    private MapHit findMapHitAt(double mouseX, double mouseY) {
        Optional<RouteStopHit> routeStopHit = findRouteStopBadgeAt(mouseX, mouseY);
        if (routeStopHit.isPresent()) {
            return MapHit.routeStop(routeStopHit.get().route(), routeStopHit.get().stop());
        }

        Optional<RouteSegmentHit> routeSegmentHit = findRouteSegmentAt(mouseX, mouseY);
        if (routeSegmentHit.isPresent()) {
            return MapHit.routeSegment(routeSegmentHit.get().route(), routeSegmentHit.get().segment());
        }

        Optional<TradeAtlasNode> mapNode = findMapNodeAt(mouseX, mouseY);
        return mapNode.map(MapHit::node).orElse(MapHit.empty());
    }

    private boolean isDoubleClick(MapHit hit, double mouseX, double mouseY) {
        if (hit.type() == MapHitType.EMPTY) {
            return false;
        }
        long now = System.currentTimeMillis();
        double dx = mouseX - lastClickX;
        double dy = mouseY - lastClickY;
        return hit.key().equals(lastClickKey)
            && now - lastClickTime <= DOUBLE_CLICK_WINDOW_MS
            && dx * dx + dy * dy <= DOUBLE_CLICK_MAX_DISTANCE * DOUBLE_CLICK_MAX_DISTANCE;
    }

    private void rememberClick(MapHit hit, double mouseX, double mouseY) {
        lastClickTime = System.currentTimeMillis();
        lastClickX = mouseX;
        lastClickY = mouseY;
        lastClickKey = hit.key();
    }

    private Optional<RouteStopHit> findRouteStopBadgeAt(double mouseX, double mouseY) {
        if (!isInsideMap(mouseX, mouseY)) {
            return Optional.empty();
        }

        MapMetrics metrics = calculateMapMetrics(areaWidth(), areaHeight());
        for (TradeRoute route : state.visibleRoutes()) {
            for (TradeRouteStop stop : route.stops()) {
                if (!shouldRenderStopBadge(route, stop)) {
                    continue;
                }
                StopAnchor anchor = stopAnchor(route, stop, metrics, areaX(), areaY(), areaWidth(), areaHeight());
                if (Math.abs(mouseX - anchor.x()) <= 6 && Math.abs(mouseY - anchor.y()) <= 6) {
                    return Optional.of(new RouteStopHit(route, stop));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<RouteSegmentHit> findRouteSegmentAt(double mouseX, double mouseY) {
        if (!isInsideMap(mouseX, mouseY)) {
            return Optional.empty();
        }

        MapMetrics metrics = calculateMapMetrics(areaWidth(), areaHeight());
        List<RouteSegmentView> segmentViews = buildRouteSegmentViews(metrics, areaX(), areaY(), areaWidth(), areaHeight());
        RouteSegmentView bestView = null;
        double bestDistance = SEGMENT_HIT_TOLERANCE;
        for (RouteSegmentView view : segmentViews) {
            double distance = distanceToSegment(mouseX, mouseY, view.fromX(), view.fromY(), view.toX(), view.toY());
            if (distance <= bestDistance) {
                bestDistance = distance;
                bestView = view;
            }
        }
        return bestView == null ? Optional.empty() : Optional.of(new RouteSegmentHit(bestView.route(), bestView.segment()));
    }

    private static double distanceToSegment(double pointX, double pointY, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared < 1.0D) {
            double px = pointX - x1;
            double py = pointY - y1;
            return Math.sqrt(px * px + py * py);
        }

        double t = ((pointX - x1) * dx + (pointY - y1) * dy) / lengthSquared;
        t = Mth.clamp(t, 0.0D, 1.0D);
        double projectionX = x1 + t * dx;
        double projectionY = y1 + t * dy;
        double px = pointX - projectionX;
        double py = pointY - projectionY;
        return Math.sqrt(px * px + py * py);
    }

    private boolean isInsideMap(double mouseX, double mouseY) {
        return mouseX >= areaX() && mouseX < areaX() + areaWidth()
            && mouseY >= areaY() && mouseY < areaY() + areaHeight();
    }

    private boolean isInsideLocatePanel(double mouseX, double mouseY) {
        int panelX = toolbarX;
        int panelY = getY() + MAP_TOOLBAR_TOP + TradeAtlasUi.BUTTON_HEIGHT + 4;
        int panelWidth = toolbarWidth;
        int panelHeight = 92;
        return mouseX >= panelX && mouseX < panelX + panelWidth
            && mouseY >= panelY && mouseY < panelY + panelHeight;
    }

    private boolean canLocate() {
        return !state.locating() && !state.hasPendingClue();
    }

    private boolean isCurrentTarget(TradeAtlasNode node) {
        return state.currentTargetNodeId().isPresent() && state.currentTargetNodeId().get().equals(node.id());
    }

    private boolean shouldRenderStopBadge(TradeRoute route, TradeRouteStop stop) {
        return viewState.isRouteSelected(route.id())
            || viewState.isRouteStopSelected(stop.id())
            || countStopsReferencingNode(route, stop.nodeId()) > 1
            || countVisibleStopsReferencingNode(stop.nodeId()) > 1;
    }

    private StopAnchor stopAnchor(TradeRoute route, TradeRouteStop stop, MapMetrics metrics,
            int areaX, int areaY, int areaWidth, int areaHeight) {
        TradeAtlasNode node = state.findNodeById(stop.nodeId()).orElse(null);
        if (node == null) {
            return new StopAnchor(areaX + areaWidth / 2, areaY + areaHeight / 2);
        }

        int baseX = metrics.screenX(areaX, areaWidth, node, mapOffsetX);
        int baseY = metrics.screenY(areaY, areaHeight, node, mapOffsetY);
        int count = Math.max(countStopsReferencingNode(route, stop.nodeId()),
            countVisibleStopsReferencingNode(stop.nodeId()));
        boolean focusedStop = viewState.isRouteSelected(route.id()) || viewState.isRouteStopSelected(stop.id());
        if (count <= 1 && !focusedStop) {
            return new StopAnchor(baseX, baseY);
        }

        int index = countVisibleStopsReferencingNode(stop.nodeId()) > 1
            ? visibleOccurrenceIndex(route, stop)
            : occurrenceIndex(route, stop);
        double angle = (Math.PI * 2.0D * index / Math.max(1, count)) - Math.PI / 2.0D;
        int radius = count <= 1 ? 9 : 11;
        return new StopAnchor(
            baseX + (int) Math.round(Math.cos(angle) * radius),
            baseY + (int) Math.round(Math.sin(angle) * radius)
        );
    }

    private int countStopsReferencingNode(TradeRoute route, UUID nodeId) {
        int count = 0;
        for (TradeRouteStop stop : route.stops()) {
            if (stop.nodeId().equals(nodeId)) {
                count++;
            }
        }
        return count;
    }

    private int countVisibleStopsReferencingNode(UUID nodeId) {
        int count = 0;
        for (TradeRoute route : state.visibleRoutes()) {
            for (TradeRouteStop stop : route.stops()) {
                if (stop.nodeId().equals(nodeId)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int occurrenceIndex(TradeRoute route, TradeRouteStop targetStop) {
        int index = 0;
        for (TradeRouteStop stop : route.stops()) {
            if (stop.nodeId().equals(targetStop.nodeId())) {
                if (stop.id().equals(targetStop.id())) {
                    return index;
                }
                index++;
            }
        }
        return 0;
    }

    private int visibleOccurrenceIndex(TradeRoute targetRoute, TradeRouteStop targetStop) {
        int index = 0;
        for (TradeRoute route : state.visibleRoutes()) {
            for (TradeRouteStop stop : route.stops()) {
                if (!stop.nodeId().equals(targetStop.nodeId())) {
                    continue;
                }
                if (route.id().equals(targetRoute.id()) && stop.id().equals(targetStop.id())) {
                    return index;
                }
                index++;
            }
        }
        return 0;
    }

    private Optional<TradeRoute> singleVisibleRouteForNode(UUID nodeId) {
        TradeRoute matchedRoute = null;
        for (TradeRoute route : state.visibleRoutes()) {
            boolean routeContainsNode = route.stops().stream().anyMatch(stop -> stop.nodeId().equals(nodeId));
            if (!routeContainsNode) {
                continue;
            }
            if (matchedRoute != null) {
                return Optional.empty();
            }
            matchedRoute = route;
        }
        return Optional.ofNullable(matchedRoute);
    }

    private String overlapKey(UUID firstNodeId, UUID secondNodeId) {
        String first = firstNodeId.toString();
        String second = secondNodeId.toString();
        return first.compareTo(second) <= 0 ? first + ":" + second : second + ":" + first;
    }

    private MapMetrics calculateMapMetrics(int areaWidth, int areaHeight) {
        BlockPos playerPos = playerPosition();
        if (state.nodes().isEmpty() && playerPos == null) {
            return new MapMetrics(0.0D, 0.0D, 1.0D);
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (TradeAtlasNode node : state.nodes()) {
            BlockPos position = node.position();
            minX = Math.min(minX, position.getX());
            maxX = Math.max(maxX, position.getX());
            minZ = Math.min(minZ, position.getZ());
            maxZ = Math.max(maxZ, position.getZ());
        }
        if (playerPos != null) {
            minX = Math.min(minX, playerPos.getX());
            maxX = Math.max(maxX, playerPos.getX());
            minZ = Math.min(minZ, playerPos.getZ());
            maxZ = Math.max(maxZ, playerPos.getZ());
        }
        if (minX == Integer.MAX_VALUE) {
            minX = maxX = playerPos.getX();
            minZ = maxZ = playerPos.getZ();
        }

        double rangeX = Math.max(1.0D, maxX - minX);
        double rangeZ = Math.max(1.0D, maxZ - minZ);
        double baseScale = Math.min((areaWidth - 24.0D) / rangeX, (areaHeight - 24.0D) / rangeZ);
        double scale = Mth.clamp(baseScale, 0.02D, 4.0D) * mapZoom;
        return new MapMetrics((minX + maxX) / 2.0D, (minZ + maxZ) / 2.0D, scale);
    }

    private int areaX() {
        return getX();
    }

    private int areaY() {
        return getY();
    }

    private int areaWidth() {
        return getWidth();
    }

    private int areaHeight() {
        return getHeight();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }

    private boolean isHoveringPlayerMarker(double mouseX, double mouseY) {
        BlockPos playerPos = playerPosition();
        if (playerPos == null || !isInsideMap(mouseX, mouseY)) {
            return false;
        }

        MapMetrics metrics = calculateMapMetrics(areaWidth(), areaHeight());
        int x = metrics.screenX(areaX(), areaWidth(), playerPos, mapOffsetX);
        int y = metrics.screenY(areaY(), areaHeight(), playerPos, mapOffsetY);
        return Math.abs(mouseX - x) <= 6 && Math.abs(mouseY - y) <= 6;
    }

    private BlockPos playerPosition() {
        Player player = Minecraft.getInstance().player;
        return player == null ? null : player.blockPosition();
    }

    private static void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) {
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = x1 + Math.round(dx * i / (float) steps);
            int y = y1 + Math.round(dy * i / (float) steps);
            guiGraphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private record StopAnchor(int x, int y) {
    }

    private record RouteStopHit(TradeRoute route, TradeRouteStop stop) {
    }

    private record RouteSegmentHit(TradeRoute route, TradeRouteSegment segment) {
    }

    private enum MapHitType {
        EMPTY,
        NODE,
        ROUTE_STOP,
        ROUTE_SEGMENT
    }

    private record MapHit(
        MapHitType type,
        TradeAtlasNode node,
        TradeRoute route,
        TradeRouteStop stop,
        TradeRouteSegment segment
    ) {
        private static MapHit empty() {
            return new MapHit(MapHitType.EMPTY, null, null, null, null);
        }

        private static MapHit node(TradeAtlasNode node) {
            return new MapHit(MapHitType.NODE, node, null, null, null);
        }

        private static MapHit routeStop(TradeRoute route, TradeRouteStop stop) {
            return new MapHit(MapHitType.ROUTE_STOP, null, route, stop, null);
        }

        private static MapHit routeSegment(TradeRoute route, TradeRouteSegment segment) {
            return new MapHit(MapHitType.ROUTE_SEGMENT, null, route, null, segment);
        }

        private String key() {
            return switch (type) {
                case NODE -> "node:" + node.id();
                case ROUTE_STOP -> "stop:" + route.id() + ":" + stop.id();
                case ROUTE_SEGMENT -> "segment:" + route.id() + ":" + segment.id();
                case EMPTY -> "empty";
            };
        }
    }

    private record RouteSegmentView(
        TradeRoute route,
        TradeRouteSegment segment,
        int fromX,
        int fromY,
        int toX,
        int toY,
        String overlapKey
    ) {
    }

    private static final class MapMetrics {
        private final double centerX;
        private final double centerZ;
        private final double scale;

        private MapMetrics(double centerX, double centerZ, double scale) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.scale = scale;
        }

        private int screenX(int areaX, int areaWidth, TradeAtlasNode node, double offsetX) {
            return areaX + areaWidth / 2 + (int) Math.round((node.position().getX() - centerX) * scale + offsetX);
        }

        private int screenY(int areaY, int areaHeight, TradeAtlasNode node, double offsetY) {
            return areaY + areaHeight / 2 + (int) Math.round((node.position().getZ() - centerZ) * scale + offsetY);
        }

        private int screenX(int areaX, int areaWidth, BlockPos position, double offsetX) {
            return areaX + areaWidth / 2 + (int) Math.round((position.getX() - centerX) * scale + offsetX);
        }

        private int screenY(int areaY, int areaHeight, BlockPos position, double offsetY) {
            return areaY + areaHeight / 2 + (int) Math.round((position.getZ() - centerZ) * scale + offsetY);
        }
    }
}
