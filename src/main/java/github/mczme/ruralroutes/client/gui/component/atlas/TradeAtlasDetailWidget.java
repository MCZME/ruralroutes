package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.AtlasNodeStatus;
import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class TradeAtlasDetailWidget extends AbstractWidget {

    private final TradeAtlasState state;
    private final TradeAtlasViewState viewState;
    private final Consumer<TradeAtlasNode> onSetTarget;
    private final Runnable onClearTarget;
    private final Runnable onCenterSelected;
    private final Runnable onToggleLocate;
    private final List<Button> buttons;
    private final Button setTargetButton;
    private final Button clearTargetButton;
    private final Button centerButton;
    private final Button locateToggleButton;

    public TradeAtlasDetailWidget(int x, int y, int width, int height,
            TradeAtlasState state,
            TradeAtlasViewState viewState,
            Consumer<TradeAtlasNode> onSetTarget,
            Runnable onClearTarget,
            Runnable onCenterSelected,
            Runnable onToggleLocate) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.section.detail"));
        this.state = state;
        this.viewState = viewState;
        this.onSetTarget = onSetTarget;
        this.onClearTarget = onClearTarget;
        this.onCenterSelected = onCenterSelected;
        this.onToggleLocate = onToggleLocate;

        int buttonX = getX() + 8;
        int buttonWidth = getWidth() - 16;
        int buttonY = getY() + getHeight() - 86;

        setTargetButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.set_target"),
            pressed -> viewState.selectedNode(state).ifPresent(onSetTarget)
        ).bounds(buttonX, buttonY, buttonWidth, 18).build();
        clearTargetButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.clear_target"),
            pressed -> onClearTarget.run()
        ).bounds(buttonX, buttonY + 22, buttonWidth, 18).build();
        centerButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.center_selected"),
            pressed -> onCenterSelected.run()
        ).bounds(buttonX, buttonY + 44, buttonWidth, 18).build();
        locateToggleButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_atlas.action.locate"),
            pressed -> onToggleLocate.run()
        ).bounds(buttonX, buttonY + 66, buttonWidth, 18).build();
        buttons = List.of(setTargetButton, clearTargetButton, centerButton, locateToggleButton);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        updateButtonStates();
        TradeAtlasUi.renderPanel(guiGraphics, font, getX(), getY(), getWidth(), getHeight(), getMessage());
        renderDetail(guiGraphics);
        for (Button button : buttons) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderDetail(GuiGraphics guiGraphics) {
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

    private void updateButtonStates() {
        Optional<TradeAtlasNode> selected = viewState.selectedNode(state);
        boolean hasSelected = selected.isPresent();
        boolean selectedIsTarget = hasSelected && isCurrentTarget(selected.get());

        setTargetButton.active = hasSelected && !selectedIsTarget;
        setTargetButton.setMessage(Component.translatable(selectedIsTarget
            ? "gui.ruralroutes.trade_atlas.action.current_target"
            : "gui.ruralroutes.trade_atlas.action.set_target"));
        clearTargetButton.active = state.currentTargetNodeId().isPresent();
        centerButton.active = hasSelected;
        locateToggleButton.active = !state.locating();
        locateToggleButton.setMessage(Component.translatable(viewState.isLocateSelectionOpen()
            ? "gui.ruralroutes.trade_atlas.action.close_locate"
            : "gui.ruralroutes.trade_atlas.action.locate"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Button actionButton : buttons) {
            if (actionButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentTarget(TradeAtlasNode node) {
        return state.currentTargetNodeId().isPresent() && state.currentTargetNodeId().get().equals(node.id());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
