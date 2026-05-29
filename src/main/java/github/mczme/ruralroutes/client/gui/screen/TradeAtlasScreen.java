package github.mczme.ruralroutes.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import github.mczme.ruralroutes.client.gui.component.atlas.TradeAtlasDetailWidget;
import github.mczme.ruralroutes.client.gui.component.atlas.TradeAtlasFirstEntryWidget;
import github.mczme.ruralroutes.client.gui.component.atlas.TradeAtlasMapWidget;
import github.mczme.ruralroutes.client.gui.component.atlas.TradeAtlasNodeListWidget;
import github.mczme.ruralroutes.client.gui.component.atlas.TradeAtlasViewState;
import github.mczme.ruralroutes.client.gui.component.atlas.TradeRouteListWidget;
import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.core.atlas.TradeRouteSegment;
import github.mczme.ruralroutes.core.atlas.TradeRouteStop;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.network.packet.TradeAtlasActionPayload;
import github.mczme.ruralroutes.network.packet.TradeRouteActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 商路图册 GUI。
 */
public class TradeAtlasScreen extends Screen {

    private static final int MIN_GUI_WIDTH = 536;
    private static final int MAX_GUI_WIDTH = 720;
    private static final int MIN_GUI_HEIGHT = 256;
    private static final int MAX_GUI_HEIGHT = 322;
    private static final int HEADER_HEIGHT = 16;
    private static final int PADDING = 8;
    private static final int PANEL_GAP = 4;
    private static final int FLOATING_PANEL_TOP_OFFSET = 4;

    private static final int BACKDROP_TOP = 0xE015171A;
    private static final int BACKDROP_BOTTOM = 0xF0050608;
    private static final int BOARD_BG = 0xE0202428;
    private static final int PANEL_BORDER = 0xFF596470;
    private static final int PANEL_HEADER_BG = 0xCC384049;
    private static final int TEXT_PRIMARY = 0xFFF3F5F7;

    private final TradeAtlasState state;
    private final boolean firstEntrySelection;
    private final TradeAtlasViewState viewState;
    private int guiWidth;
    private int guiHeight;
    private int listWidth;
    private int mapWidth;
    private int detailWidth;
    private int leftPos;
    private int topPos;
    private TradeAtlasFirstEntryWidget firstEntryWidget;
    private TradeAtlasNodeListWidget nodeListWidget;
    private TradeRouteListWidget routeListWidget;
    private TradeAtlasMapWidget mapWidget;
    private TradeAtlasDetailWidget detailWidget;

    public TradeAtlasScreen(TradeAtlasState state) {
        super(Component.translatable("gui.ruralroutes.trade_atlas.title"));
        this.state = state;
        this.firstEntrySelection = !state.hasNodes() && !state.firstEntryUsed();
        this.viewState = new TradeAtlasViewState(state);
    }

    @Override
    protected void init() {
        super.init();
        int availableWidth = Math.max(320, this.width - 16);
        int availableHeight = Math.max(220, this.height - 16);
        int preferredWidth = Math.min(MAX_GUI_WIDTH, Math.max(MIN_GUI_WIDTH, this.width - 32));
        int preferredHeight = Math.min(MAX_GUI_HEIGHT, Math.max(MIN_GUI_HEIGHT, this.height - 32));

        guiWidth = Math.min(preferredWidth, availableWidth);
        guiHeight = Math.min(preferredHeight, availableHeight);

        if (guiWidth < MIN_GUI_WIDTH) {
            int columnsWidth = guiWidth - PADDING * 2 - PANEL_GAP * 2;
            listWidth = Math.max(112, columnsWidth * 29 / 100);
            detailWidth = Math.max(112, columnsWidth * 27 / 100);
        } else {
            listWidth = Math.min(156, Math.max(132, guiWidth / 5));
            detailWidth = Math.min(148, Math.max(126, guiWidth / 5));
        }
        mapWidth = guiWidth - PADDING * 2;

        leftPos = (this.width - guiWidth) / 2;
        topPos = (this.height - guiHeight) / 2;

        if (firstEntrySelection) {
            firstEntryWidget = new TradeAtlasFirstEntryWidget(leftPos, topPos, guiWidth, guiHeight, state,
                this::sendLocate);
            return;
        }

        nodeListWidget = new TradeAtlasNodeListWidget(
            listX(),
            floatingPanelY(),
            listWidth,
            nodeListHeight(),
            state,
            viewState,
            this::selectNode
        );
        routeListWidget = new TradeRouteListWidget(
            listX(),
            routeListY(),
            listWidth,
            routeListHeight(),
            state,
            viewState,
            this::selectRoute
        );
        mapWidget = new TradeAtlasMapWidget(
            contentX(),
            contentY(),
            contentWidth(),
            contentHeight(),
            state,
            viewState,
            this::selectNode,
            this::selectRoute,
            this::selectRouteStop,
            this::selectRouteSegment,
            this::sendLocate,
            mapToolbarX(),
            mapToolbarWidth()
        );
        detailWidget = new TradeAtlasDetailWidget(
            detailX(),
            floatingPanelY(),
            detailWidth,
            floatingPanelHeight(),
            state,
            viewState,
            this::setTarget,
            this::cancelPendingClue,
            this::clearTarget,
            () -> mapWidget.centerOnSelected()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderFrame(guiGraphics);

        if (firstEntrySelection) {
            firstEntryWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            mapWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            nodeListWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            routeListWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            detailWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            if (nodeListWidget.isMouseOver(mouseX, mouseY)) {
                nodeListWidget.renderHoveredTooltip(guiGraphics, mouseX, mouseY);
            } else if (routeListWidget.isMouseOver(mouseX, mouseY)) {
                routeListWidget.renderHoveredTooltip(guiGraphics, mouseX, mouseY);
            } else if (!isHoveringFloatingPanel(mouseX, mouseY)) {
                mapWidget.renderHoveredTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    private void renderFrame(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, BACKDROP_TOP, BACKDROP_BOTTOM);
        guiGraphics.fill(leftPos, topPos, leftPos + guiWidth, topPos + guiHeight, BOARD_BG);
        guiGraphics.renderOutline(leftPos, topPos, guiWidth, guiHeight, PANEL_BORDER);
        guiGraphics.fill(leftPos + 1, topPos + 1, leftPos + guiWidth - 1, topPos + HEADER_HEIGHT, PANEL_HEADER_BG);
        guiGraphics.drawCenteredString(font, this.title, leftPos + guiWidth / 2, topPos + 4, TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (firstEntrySelection) {
            return firstEntryWidget.mouseClicked(mouseX, mouseY, button);
        }

        if (detailWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (detailWidget.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        if (routeListWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (routeListWidget.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        if (nodeListWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (nodeListWidget.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        return mapWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!firstEntrySelection && detailWidget.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!firstEntrySelection && mapWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!firstEntrySelection && mapWidget.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!firstEntrySelection && nodeListWidget.isMouseOver(mouseX, mouseY)) {
            nodeListWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            return true;
        }
        if (!firstEntrySelection && routeListWidget.isMouseOver(mouseX, mouseY)) {
            routeListWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            return true;
        }
        if (!firstEntrySelection && detailWidget.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        if (!firstEntrySelection && mapWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void selectNode(TradeAtlasNode node) {
        viewState.routeDraftStartNodeId().ifPresent(startNodeId -> {
            if (!startNodeId.equals(node.id())) {
                PacketDistributor.sendToServer(TradeRouteActionPayload.createRoute(startNodeId, node.id()));
                viewState.clearRouteDraftStartNode();
            }
        });
        viewState.selectNode(node);
    }

    private void selectRoute(TradeRoute route) {
        viewState.selectRoute(route);
    }

    private void selectRouteStop(TradeRoute route, TradeRouteStop stop) {
        viewState.selectRouteStop(route, stop);
    }

    private void selectRouteSegment(TradeRoute route, TradeRouteSegment segment) {
        viewState.selectRouteSegment(route, segment);
    }

    private void setTarget(TradeAtlasNode node) {
        state.setCurrentTarget(node.id());
        PacketDistributor.sendToServer(TradeAtlasActionPayload.setTarget(node.id()));
    }

    private void clearTarget() {
        state.clearCurrentTarget();
        PacketDistributor.sendToServer(TradeAtlasActionPayload.clearTarget());
    }

    private void cancelPendingClue() {
        state.clearPendingClue();
        PacketDistributor.sendToServer(TradeAtlasActionPayload.cancelPendingClue());
    }

    private void sendLocate(VillageStyle style) {
        PacketDistributor.sendToServer(TradeAtlasActionPayload.requestLocate(style));
    }

    private int listX() {
        return contentX() + PADDING;
    }

    private int contentX() {
        return leftPos + PADDING;
    }

    private int detailX() {
        return leftPos + guiWidth - PADDING - detailWidth - PADDING;
    }

    private int contentY() {
        return topPos + HEADER_HEIGHT + PADDING;
    }

    private int contentWidth() {
        return guiWidth - PADDING * 2;
    }

    private int contentHeight() {
        return guiHeight - HEADER_HEIGHT - PADDING * 2;
    }

    private int nodeListHeight() {
        return Math.max(86, (floatingPanelHeight() - PANEL_GAP) * 56 / 100);
    }

    private int routeListY() {
        return floatingPanelY() + nodeListHeight() + PANEL_GAP;
    }

    private int routeListHeight() {
        return floatingPanelHeight() - nodeListHeight() - PANEL_GAP;
    }

    private int floatingPanelY() {
        return contentY() + FLOATING_PANEL_TOP_OFFSET;
    }

    private int floatingPanelHeight() {
        return contentHeight() - FLOATING_PANEL_TOP_OFFSET;
    }

    private int mapToolbarX() {
        return listX() + listWidth + PANEL_GAP;
    }

    private int mapToolbarWidth() {
        return Math.max(120, detailX() - PANEL_GAP - mapToolbarX());
    }

    private boolean isHoveringFloatingPanel(double mouseX, double mouseY) {
        return nodeListWidget.isMouseOver(mouseX, mouseY)
            || routeListWidget.isMouseOver(mouseX, mouseY)
            || detailWidget.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key inputKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (!firstEntrySelection && detailWidget.keyPressed(keyCode, scanCode, modifiers)) {
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
}
