package github.mczme.ruralroutes.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import github.mczme.ruralroutes.Config;
import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.client.gui.component.StickyNoteWidget;
import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.market.MarketScopeType;
import github.mczme.ruralroutes.core.market.MarketState;
import github.mczme.ruralroutes.core.rumor.RumorEntry;
import github.mczme.ruralroutes.core.rumor.RumorGenerator;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayout;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayoutGenerator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/**
 * 传闻板 GUI 屏幕。
 * 使用 board / notes / icons 三张贴图，但保持原先的杂乱布局。
 */
public class RumorBoardScreen extends Screen {

    private static final ResourceLocation BOARD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "textures/gui/board.png");
    private static final ResourceLocation NOTES_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "textures/gui/notes.png");

    private static final int BOARD_SOURCE_WIDTH = 61;
    private static final int BOARD_SOURCE_HEIGHT = 38;
    private static final int MAX_SCALE = 5;
    private static final int MIN_SCALE = 4;
    private static final int NOTE_RENDER_WIDTH = StickyNoteLayoutGenerator.NOTE_WIDTH;
    private static final int NOTE_RENDER_HEIGHT = StickyNoteLayoutGenerator.NOTE_HEIGHT;
    private static final int LEGEND_ICON_SIZE = 14;
    private static final int LEGEND_GAP = 1;
    private static final int LEGEND_RIGHT_PADDING = 3;
    private static final int TEXT_PRIMARY = 0xFFE6C892;
    private static final int TEXT_MUTED = 0xFFD8BC88;
    private static final int TEXT_DIM = 0xFF9B7A51;
    private static final double DRAG_THRESHOLD_SQ = 16.0D;
    private static final double IDLE_POSITION_LERP = 0.28D;
    private static final double DRAG_POSITION_LERP = 0.62D;
    private static final float ROTATION_LERP = 0.35F;
    private static final float SCALE_LERP = 0.35F;
    private static final float HOVER_SCALE = 1.01F;
    private static final float DRAG_SCALE = 1.03F;
    private static final float MAX_DRAG_ROTATION = 2.0F;
    private static final int DRAG_LAYER = 2;

    private final BlockPos blockPos;
    private final MarketState marketState;
    private final Random random = new Random();
    private final List<NoteRuntimeState> noteStates = new ArrayList<>();
    private final List<Component> displayTexts = new ArrayList<>();
    private final List<RumorEntry> rumorEntries = new ArrayList<>();

    private List<StickyNoteLayout> layouts = new ArrayList<>();
    private String timeRemainingDesc = "";
    private int leftPos;
    private int topPos;
    private int renderScale;
    private boolean dataCollected = false;
    private NoteRuntimeState pressedState;
    private double pressMouseX;
    private double pressMouseY;
    private double dragOffsetX;
    private double dragOffsetY;

    public RumorBoardScreen(BlockPos blockPos, MarketState marketState) {
        super(Component.translatable("block.ruralroutes.rumor_board"));
        this.blockPos = blockPos;
        this.marketState = marketState;
    }

    @Override
    protected void init() {
        super.init();
        renderScale = determineRenderScale();
        leftPos = alignToScale((this.width - getGuiWidth()) / 2);
        topPos = alignToScale((this.height - getGuiHeight()) / 2);
        noteStates.clear();
        displayTexts.clear();
        rumorEntries.clear();
        layouts = new ArrayList<>();
        timeRemainingDesc = "";
        dataCollected = false;
        pressedState = null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderBoard(guiGraphics);

        if (!dataCollected && collectClientData()) {
            dataCollected = true;
            buildNoteWidgets();
        }

        updateNoteAnimations(mouseX, mouseY);
        renderNotesByLayer(guiGraphics, mouseX, mouseY, partialTick);
        renderTitle(guiGraphics);
        renderFooter(guiGraphics);
        renderHoveredRumorTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderBoard(GuiGraphics guiGraphics) {
        renderScaledSprite(
                guiGraphics,
                BOARD_TEXTURE,
                leftPos,
                topPos,
                renderScale,
                0,
                0,
                BOARD_SOURCE_WIDTH,
                BOARD_SOURCE_HEIGHT,
                BOARD_SOURCE_WIDTH,
                BOARD_SOURCE_HEIGHT
        );
    }

    private void renderNotesByLayer(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (int layer = 0; layer <= 1; layer++) {
            for (NoteRuntimeState state : noteStates) {
                if (state.activeLayer == layer) {
                    state.widget.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            }
        }
        for (NoteRuntimeState state : noteStates) {
            if (state.activeLayer >= DRAG_LAYER) {
                state.widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        guiGraphics.drawCenteredString(font, this.title, leftPos + getGuiWidth() / 2, topPos + renderScale + 1, TEXT_PRIMARY);
    }

    private void renderFooter(GuiGraphics guiGraphics) {
        int footerY = topPos + getGuiHeight() - 16;
        Component refreshText = timeRemainingDesc.isEmpty()
                ? Component.empty()
                : Component.translatable("gui.ruralroutes.rumor_board.refresh_in", timeRemainingDesc);
        int refreshX = leftPos + getGuiWidth() - 6 - font.width(refreshText);
        int legendLimit = refreshX - 8;

        int legendX = leftPos + 6;
        for (LegendEntry entry : buildLegendEntries()) {
            int nextX = renderLegendEntry(guiGraphics, legendX, footerY, entry, legendLimit);
            if (nextX < 0) {
                break;
            }
            legendX = nextX;
        }

        if (!timeRemainingDesc.isEmpty()) {
            guiGraphics.drawString(font, refreshText, refreshX, footerY + 3, TEXT_DIM, false);
        }
    }

    private int renderLegendEntry(GuiGraphics guiGraphics, int x, int y, LegendEntry entry, int limitX) {
        int width = LEGEND_ICON_SIZE + LEGEND_GAP + font.width(entry.label()) + LEGEND_RIGHT_PADDING;
        if (x + width > limitX) {
            return -1;
        }

        renderScaledSprite(
                guiGraphics,
                NOTES_TEXTURE,
                x,
                y,
                1,
                entry.noteSourceU(),
                entry.noteSourceV(),
                StickyNoteWidget.NOTE_SOURCE_WIDTH,
                StickyNoteWidget.NOTE_SOURCE_HEIGHT,
                StickyNoteWidget.NOTES_TEXTURE_WIDTH,
                StickyNoteWidget.NOTES_TEXTURE_HEIGHT
        );
        guiGraphics.drawString(font, entry.label(), x + LEGEND_ICON_SIZE + LEGEND_GAP, y + 3, TEXT_MUTED, false);
        return x + width;
    }

    private void buildNoteWidgets() {
        noteStates.clear();

        for (int i = 0; i < rumorEntries.size() && i < displayTexts.size(); i++) {
            RumorEntry rumor = rumorEntries.get(i);
            StickyNoteLayout layout = (i < layouts.size()) ? layouts.get(i) : null;
            NoteSkin skin = resolveNoteSkin(rumor);
            int x = resolveNoteX(i, layout);
            int y = resolveNoteY(i, layout);
            int layer = layout != null ? layout.layer() : 0;
            StickyNoteWidget widget = new StickyNoteWidget(
                    x,
                    y,
                    NOTE_RENDER_WIDTH,
                    NOTE_RENDER_HEIGHT,
                    displayTexts.get(i),
                    getFamilyLabel(rumor),
                    getScopeLabel(rumor),
                    skin.u(),
                    skin.v(),
                    getPinScale(),
                    layout != null ? layout.rotation() : 0.0f,
                    layer,
                    skin.labelInsetLeft(),
                    skin.labelInsetRight(),
                    skin.labelTop(),
                    skin.bodyInsetLeft(),
                    skin.bodyInsetRight(),
                    skin.bodyTop(),
                    skin.bodyBottom()
            );
            noteStates.add(new NoteRuntimeState(i, widget, x, y, layer));
        }
    }

    private boolean collectClientData() {
        if (minecraft == null || minecraft.level == null) {
            return false;
        }

        Level level = minecraft.level;
        long effectiveTime = CycleManager.getEffectiveTime(level);
        long cycleIndex = marketState.cycleIndex();
        BlockEntity be = level.getBlockEntity(blockPos);

        if (be instanceof RumorBoardBlockEntity rumorBoard && rumorBoard.hasRumorCacheFor(cycleIndex)) {
            List<RumorEntry> cachedRumors = rumorBoard.getCachedRumors();
            rumorEntries.clear();
            displayTexts.clear();
            rumorEntries.addAll(cachedRumors);
            random.setSeed(StickyNoteLayoutGenerator.generateSeed(cycleIndex, blockPos));
            for (RumorEntry rumor : cachedRumors) {
                displayTexts.add(rumor.getDisplayText(random));
            }
            layouts = rumorBoard.getLayouts();
            timeRemainingDesc = calculateTimeRemaining(effectiveTime);
            return true;
        }

        random.setSeed(StickyNoteLayoutGenerator.generateSeed(cycleIndex, blockPos));
        List<RumorEntry> generatedRumors = RumorGenerator.generateFromMarketState(marketState, random);
        rumorEntries.clear();
        displayTexts.clear();
        rumorEntries.addAll(generatedRumors);
        for (RumorEntry rumor : generatedRumors) {
            displayTexts.add(rumor.getDisplayText(random));
        }

        if (be instanceof RumorBoardBlockEntity rumorBoard) {
            layouts = rumorBoard.getOrGenerateLayouts(generatedRumors.size(), cycleIndex, random);
            rumorBoard.setRumorCache(cycleIndex, generatedRumors);
        } else {
            layouts = StickyNoteLayoutGenerator.generate(generatedRumors.size(), random);
        }

        timeRemainingDesc = calculateTimeRemaining(effectiveTime);
        return true;
    }

    private String calculateTimeRemaining(long effectiveTime) {
        long cycleLength = Config.getCycleLengthInTicks();
        long ticksRemaining = cycleLength - (effectiveTime % cycleLength);
        long daysRemaining = ticksRemaining / 24000;
        long hoursRemaining = (ticksRemaining % 24000) / 1000;

        if (daysRemaining > 0) {
            return Component.translatable("gui.ruralroutes.rumor_board.time_days", daysRemaining).getString();
        }
        if (hoursRemaining > 0) {
            return Component.translatable("gui.ruralroutes.rumor_board.time_hours", hoursRemaining).getString();
        }
        return Component.translatable("gui.ruralroutes.rumor_board.time_soon").getString();
    }

    private void renderHoveredRumorTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (pressedState != null && pressedState.dragging) {
            return;
        }

        NoteRuntimeState hoveredState = findTopmostStateAt(mouseX, mouseY);
        if (hoveredState == null) {
            return;
        }

        int hoveredRumorIndex = hoveredState.rumorIndex;
        RumorEntry rumor = rumorEntries.get(hoveredRumorIndex);
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(displayTexts.get(hoveredRumorIndex));

        if (!isGossip(rumor)) {
            MutableComponent meta = getFamilyLabel(rumor).copy().withColor(TEXT_PRIMARY);
            Component scopeLabel = getScopeLabel(rumor);
            if (scopeLabel != null) {
                meta.append(Component.literal(" · ").withColor(TEXT_DIM));
                meta.append(scopeLabel.copy().withColor(TEXT_MUTED));
            }
            tooltip.add(meta);

            if (rumor.scopeType() != MarketScopeType.GLOBAL && !rumor.scopeTargetName().isEmpty()) {
                tooltip.add(Component.translatable(rumor.scopeTargetName()).withColor(TEXT_MUTED));
            }
        }

        guiGraphics.renderTooltip(
                font,
                tooltip.stream().map(Component::getVisualOrderText).toList(),
                mouseX,
                mouseY
        );
    }

    private void updateNoteAnimations(int mouseX, int mouseY) {
        NoteRuntimeState hoveredState = (pressedState != null && pressedState.dragging)
                ? pressedState
                : findTopmostStateAt(mouseX, mouseY);

        for (NoteRuntimeState state : noteStates) {
            if (!state.dragging) {
                state.targetScale = state == hoveredState ? HOVER_SCALE : 1.0F;
                state.targetExtraRotation = 0.0F;
            }
            state.tick();
        }
    }

    private NoteRuntimeState findTopmostStateAt(double mouseX, double mouseY) {
        for (int layer = DRAG_LAYER; layer >= 0; layer--) {
            for (int i = noteStates.size() - 1; i >= 0; i--) {
                NoteRuntimeState state = noteStates.get(i);
                if (state.activeLayer == layer && state.contains(mouseX, mouseY)) {
                    return state;
                }
            }
        }
        return null;
    }

    private List<LegendEntry> buildLegendEntries() {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<LegendEntry> entries = new ArrayList<>();
        for (RumorEntry rumor : rumorEntries) {
            String key = isGossip(rumor) ? "gossip" : rumor.family().name();
            if (!seen.add(key)) {
                continue;
            }
            NoteSkin skin = resolveNoteSkin(rumor);
            entries.add(new LegendEntry(getFamilyLabel(rumor), skin.u(), skin.v()));
        }
        return entries;
    }

    private int resolveNoteX(int index, StickyNoteLayout layout) {
        if (shouldCenterQuietNote(index)) {
            return getContentLeft() + (getContentWidth() - NOTE_RENDER_WIDTH) / 2;
        }
        if (layout == null) {
            return getContentLeft() + Math.max(0, (getContentWidth() - NOTE_RENDER_WIDTH) / 2);
        }
        return layout.getAbsoluteX(getContentLeft(), getContentWidth(), NOTE_RENDER_WIDTH);
    }

    private int resolveNoteY(int index, StickyNoteLayout layout) {
        if (shouldCenterQuietNote(index)) {
            return getContentTop() + (getContentHeight() - NOTE_RENDER_HEIGHT) / 2;
        }
        if (layout == null) {
            return getContentTop() + Math.max(0, (getContentHeight() - NOTE_RENDER_HEIGHT) / 2);
        }
        return layout.getAbsoluteY(getContentTop(), getContentHeight(), NOTE_RENDER_HEIGHT);
    }

    private boolean shouldCenterQuietNote(int index) {
        return index == 0 && rumorEntries.size() == 1 && isGossip(rumorEntries.getFirst());
    }

    private NoteSkin resolveNoteSkin(RumorEntry rumor) {
        if (isGossip(rumor)) {
            return new NoteSkin(0, 0, 6, 6, 5, 6, 6, 18, 6);
        }
        return switch (rumor.family()) {
            case DEMAND -> new NoteSkin(17, 0, 8, 8, 5, 12, 10, 18, 7);
            case SHORTAGE -> new NoteSkin(34, 0, 15, 8, 5, 18, 10, 18, 7);
            case SURPLUS -> new NoteSkin(0, 15, 7, 7, 5, 7, 7, 18, 6);
            case RELEASE -> new NoteSkin(17, 15, 8, 10, 5, 12, 11, 18, 8);
        };
    }

    private Component getFamilyLabel(RumorEntry rumor) {
        if (isGossip(rumor)) {
            return Component.translatable("gui.ruralroutes.rumor_board.family.gossip");
        }
        return Component.translatable("gui.ruralroutes.rumor_board.family." + rumor.family().key());
    }

    private Component getScopeLabel(RumorEntry rumor) {
        if (isGossip(rumor)) {
            return null;
        }
        return switch (rumor.scopeType()) {
            case GLOBAL -> Component.translatable("gui.ruralroutes.rumor_board.scope.global");
            case BIOME -> Component.translatable("gui.ruralroutes.rumor_board.scope.biome");
            case THEME -> Component.translatable("gui.ruralroutes.rumor_board.scope.theme");
        };
    }

    private boolean isGossip(RumorEntry rumor) {
        return rumor.targetNameKey().startsWith("rumor.gossip.");
    }

    private int getGuiWidth() {
        return BOARD_SOURCE_WIDTH * renderScale;
    }

    private int getGuiHeight() {
        return BOARD_SOURCE_HEIGHT * renderScale;
    }

    private int getPinScale() {
        return Math.max(2, renderScale / 2);
    }

    private int getContentLeft() {
        return leftPos + renderScale * 4;
    }

    private int getContentTop() {
        return topPos + renderScale * 4;
    }

    private int getContentWidth() {
        return getGuiWidth() - renderScale * 8;
    }

    private int getContentHeight() {
        return getGuiHeight() - renderScale * 8;
    }

    private double clampNoteX(double x) {
        return clamp(x, getContentLeft(), getContentLeft() + getContentWidth() - NOTE_RENDER_WIDTH);
    }

    private double clampNoteY(double y) {
        return clamp(y, getContentTop(), getContentTop() + getContentHeight() - NOTE_RENDER_HEIGHT);
    }

    private int determineRenderScale() {
        int scale = MAX_SCALE;
        while (scale > MIN_SCALE) {
            if (BOARD_SOURCE_WIDTH * scale <= this.width - 20 && BOARD_SOURCE_HEIGHT * scale <= this.height - 40) {
                break;
            }
            scale--;
        }
        return scale;
    }

    private int alignToScale(int value) {
        int mod = Math.floorMod(value, renderScale);
        return value - mod;
    }

    private static void renderScaledSprite(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            int x,
            int y,
            int scale,
            int sourceU,
            int sourceV,
            int sourceWidth,
            int sourceHeight,
            int textureWidth,
            int textureHeight
    ) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0F);
        guiGraphics.blit(texture, 0, 0, sourceU, sourceV, sourceWidth, sourceHeight, textureWidth, textureHeight);
        poseStack.popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key inputKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.minecraft != null && this.minecraft.options.keyInventory.isActiveAndMatches(inputKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            NoteRuntimeState state = findTopmostStateAt(mouseX, mouseY);
            if (state != null) {
                pressedState = state;
                pressMouseX = mouseX;
                pressMouseY = mouseY;
                dragOffsetX = mouseX - state.currentX;
                dragOffsetY = mouseY - state.currentY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && pressedState != null) {
            if (!pressedState.dragging) {
                double dx = mouseX - pressMouseX;
                double dy = mouseY - pressMouseY;
                if (dx * dx + dy * dy >= DRAG_THRESHOLD_SQ) {
                    startDragging(pressedState);
                }
            }

            if (pressedState.dragging) {
                pressedState.targetX = clampNoteX(mouseX - dragOffsetX);
                pressedState.targetY = clampNoteY(mouseY - dragOffsetY);
                pressedState.targetScale = DRAG_SCALE;
                float tilt = (float) ((pressedState.targetX - pressedState.originalX) / 14.0D);
                pressedState.targetExtraRotation = clamp(tilt, -MAX_DRAG_ROTATION, MAX_DRAG_ROTATION);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && pressedState != null) {
            if (pressedState.dragging) {
                pressedState.dragging = false;
                pressedState.targetScale = 1.0F;
                pressedState.targetExtraRotation = 0.0F;
            }
            pressedState = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void startDragging(NoteRuntimeState state) {
        state.dragging = true;
        state.activeLayer = DRAG_LAYER;
        state.targetScale = DRAG_SCALE;
        noteStates.remove(state);
        noteStates.add(state);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private record NoteSkin(
            int u,
            int v,
            int labelInsetLeft,
            int labelInsetRight,
            int labelTop,
            int bodyInsetLeft,
            int bodyInsetRight,
            int bodyTop,
            int bodyBottom
    ) {
    }

    private record LegendEntry(Component label, int noteSourceU, int noteSourceV) {
    }

    private final class NoteRuntimeState {
        private final int rumorIndex;
        private final StickyNoteWidget widget;
        private final int originalX;
        private final int originalY;
        private double currentX;
        private double currentY;
        private double targetX;
        private double targetY;
        private float extraRotation = 0.0F;
        private float targetExtraRotation = 0.0F;
        private float scale = 1.0F;
        private float targetScale = 1.0F;
        private int activeLayer;
        private boolean dragging = false;

        private NoteRuntimeState(int rumorIndex, StickyNoteWidget widget, int x, int y, int baseLayer) {
            this.rumorIndex = rumorIndex;
            this.widget = widget;
            this.originalX = x;
            this.originalY = y;
            this.currentX = x;
            this.currentY = y;
            this.targetX = x;
            this.targetY = y;
            this.activeLayer = baseLayer;
            this.widget.setPosition(x, y);
            this.widget.setInteractionState(0.0F, 1.0F, false);
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= currentX && mouseX < currentX + widget.getWidth()
                    && mouseY >= currentY && mouseY < currentY + widget.getHeight();
        }

        private void tick() {
            double positionLerp = dragging ? DRAG_POSITION_LERP : IDLE_POSITION_LERP;
            currentX += (targetX - currentX) * positionLerp;
            currentY += (targetY - currentY) * positionLerp;
            if (Math.abs(targetX - currentX) < 0.2D) {
                currentX = targetX;
            }
            if (Math.abs(targetY - currentY) < 0.2D) {
                currentY = targetY;
            }

            extraRotation += (targetExtraRotation - extraRotation) * ROTATION_LERP;
            scale += (targetScale - scale) * SCALE_LERP;

            widget.setPosition((int) Math.round(currentX), (int) Math.round(currentY));
            widget.setInteractionState(extraRotation, scale, dragging);
        }
    }
}
