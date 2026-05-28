package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.AtlasNodeStatus;
import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class TradeAtlasMapWidget extends AbstractWidget {

    private static final int NODE_MARK_SIZE = 8;

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeAtlasNode> onSelect;
    private final Consumer<VillageStyle> onLocate;
    private final List<Button> locateStyleButtons = new ArrayList<>();

    private boolean draggingMap;
    private double mapZoom = 1.0D;
    private double mapOffsetX;
    private double mapOffsetY;

    public TradeAtlasMapWidget(int x, int y, int width, int height,
            TradeAtlasState state, TradeAtlasViewState viewState,
            Consumer<TradeAtlasNode> onSelect, Consumer<VillageStyle> onLocate) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.map"));
        this.state = state;
        this.viewState = viewState;
        this.onSelect = onSelect;
        this.onLocate = onLocate;
        buildLocateButtons();
    }

    private void buildLocateButtons() {
        int panelX = getX() + 12;
        int panelY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 42;
        int gap = 4;
        int buttonWidth = (getWidth() - 32 - gap) / 2;

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
            ).bounds(panelX + col * (buttonWidth + gap), panelY + row * 22, buttonWidth, 18).build();
            locateStyleButtons.add(button);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        TradeAtlasUi.renderPanel(guiGraphics, font, getX(), getY(), getWidth(), getHeight(), getMessage());
        renderMapArea(guiGraphics);
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
        guiGraphics.renderOutline(areaX, areaY, areaWidth, areaHeight, TradeAtlasUi.ROW_BORDER);
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
        renderPlayerMarker(guiGraphics, metrics, areaX, areaY, areaWidth, areaHeight);
        for (TradeAtlasNode node : state.nodes()) {
            int nodeX = metrics.screenX(areaX, areaWidth, node, mapOffsetX);
            int nodeY = metrics.screenY(areaY, areaHeight, node, mapOffsetY);
            renderMapNode(guiGraphics, node, nodeX, nodeY);
        }
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
        int outline = viewState.isSelected(node.id()) ? TradeAtlasUi.TEXT_ACCENT : TradeAtlasUi.ROW_BORDER;

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
        int panelX = getX() + 8;
        int panelY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 22;
        int panelWidth = getWidth() - 16;
        int panelHeight = 100;
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
            panelY + 22,
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

        Optional<TradeAtlasNode> mapNode = findMapNodeAt(mouseX, mouseY);
        if (mapNode.isPresent()) {
            onSelect.accept(mapNode.get());
            return true;
        }

        if (isInsideMap(mouseX, mouseY)) {
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
        viewState.selectedNode(state).ifPresent(node -> {
            MapMetrics metrics = calculateMapMetrics(areaWidth(), areaHeight());
            int nodeX = metrics.screenX(areaX(), areaWidth(), node, mapOffsetX);
            int nodeY = metrics.screenY(areaY(), areaHeight(), node, mapOffsetY);
            mapOffsetX += areaX() + areaWidth() / 2.0D - nodeX;
            mapOffsetY += areaY() + areaHeight() / 2.0D - nodeY;
        });
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

    private boolean isInsideMap(double mouseX, double mouseY) {
        return mouseX >= areaX() && mouseX < areaX() + areaWidth()
            && mouseY >= areaY() && mouseY < areaY() + areaHeight();
    }

    private boolean isInsideLocatePanel(double mouseX, double mouseY) {
        int panelX = getX() + 8;
        int panelY = getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 22;
        int panelWidth = getWidth() - 16;
        int panelHeight = 100;
        return mouseX >= panelX && mouseX < panelX + panelWidth
            && mouseY >= panelY && mouseY < panelY + panelHeight;
    }

    private boolean canLocate() {
        return !state.locating() && !state.hasPendingClue();
    }

    private boolean isCurrentTarget(TradeAtlasNode node) {
        return state.currentTargetNodeId().isPresent() && state.currentTargetNodeId().get().equals(node.id());
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
        return getX() + 6;
    }

    private int areaY() {
        return getY() + TradeAtlasUi.PANEL_HEADER_HEIGHT + 6;
    }

    private int areaWidth() {
        return getWidth() - 12;
    }

    private int areaHeight() {
        return getHeight() - TradeAtlasUi.PANEL_HEADER_HEIGHT - 12;
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
