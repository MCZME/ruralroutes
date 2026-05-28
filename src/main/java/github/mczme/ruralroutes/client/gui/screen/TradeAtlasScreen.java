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
import github.mczme.ruralroutes.core.atlas.TradeRouteStop;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.network.packet.TradeAtlasActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 商路图册 GUI。
 */
public class TradeAtlasScreen extends Screen {

    private static final int GUI_WIDTH = 536;
    private static final int GUI_HEIGHT = 256;
    private static final int HEADER_HEIGHT = 22;
    private static final int PADDING = 8;
    private static final int PANEL_GAP = 8;
    private static final int LIST_WIDTH = 142;
    private static final int MAP_WIDTH = 224;
    private static final int DETAIL_WIDTH = 138;

    private static final int BACKDROP_TOP = 0xE015171A;
    private static final int BACKDROP_BOTTOM = 0xF0050608;
    private static final int BOARD_BG = 0xE0202428;
    private static final int PANEL_BORDER = 0xFF596470;
    private static final int PANEL_HEADER_BG = 0xCC384049;
    private static final int TEXT_PRIMARY = 0xFFF3F5F7;

    private final TradeAtlasState state;
    private final boolean firstEntrySelection;
    private final TradeAtlasViewState viewState;
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
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        if (firstEntrySelection) {
            firstEntryWidget = new TradeAtlasFirstEntryWidget(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, state,
                this::sendLocate);
            return;
        }

        nodeListWidget = new TradeAtlasNodeListWidget(
            listX(),
            contentY(),
            LIST_WIDTH,
            nodeListHeight(),
            state,
            viewState,
            this::selectNode
        );
        routeListWidget = new TradeRouteListWidget(
            listX(),
            routeListY(),
            LIST_WIDTH,
            routeListHeight(),
            state,
            viewState,
            this::selectRoute
        );
        mapWidget = new TradeAtlasMapWidget(
            mapX(),
            contentY(),
            MAP_WIDTH,
            contentHeight(),
            state,
            viewState,
            this::selectNode,
            this::selectRouteStop,
            this::sendLocate
        );
        detailWidget = new TradeAtlasDetailWidget(
            detailX(),
            contentY(),
            DETAIL_WIDTH,
            contentHeight(),
            state,
            viewState,
            this::setTarget,
            this::cancelPendingClue,
            this::clearTarget,
            () -> mapWidget.centerOnSelected(),
            viewState::toggleLocateSelection
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderFrame(guiGraphics);

        if (firstEntrySelection) {
            firstEntryWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            nodeListWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            routeListWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            mapWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            detailWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            mapWidget.renderHoveredTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderFrame(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, BACKDROP_TOP, BACKDROP_BOTTOM);
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, BOARD_BG);
        guiGraphics.renderOutline(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, PANEL_BORDER);
        guiGraphics.fill(leftPos + 1, topPos + 1, leftPos + GUI_WIDTH - 1, topPos + HEADER_HEIGHT, PANEL_HEADER_BG);
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 7, TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (firstEntrySelection) {
            return firstEntryWidget.mouseClicked(mouseX, mouseY, button);
        }

        if (detailWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (mapWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (routeListWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return nodeListWidget.mouseClicked(mouseX, mouseY, button);
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
        if (!firstEntrySelection && mapWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void selectNode(TradeAtlasNode node) {
        viewState.selectNode(node);
    }

    private void selectRoute(TradeRoute route) {
        viewState.selectRoute(route);
    }

    private void selectRouteStop(TradeRoute route, TradeRouteStop stop) {
        viewState.selectRouteStop(route, stop);
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
        return leftPos + PADDING;
    }

    private int mapX() {
        return listX() + LIST_WIDTH + PANEL_GAP;
    }

    private int detailX() {
        return mapX() + MAP_WIDTH + PANEL_GAP;
    }

    private int contentY() {
        return topPos + HEADER_HEIGHT + PADDING;
    }

    private int contentHeight() {
        return GUI_HEIGHT - HEADER_HEIGHT - PADDING * 2;
    }

    private int nodeListHeight() {
        return (contentHeight() - PANEL_GAP) / 2;
    }

    private int routeListY() {
        return contentY() + nodeListHeight() + PANEL_GAP;
    }

    private int routeListHeight() {
        return contentHeight() - nodeListHeight() - PANEL_GAP;
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
