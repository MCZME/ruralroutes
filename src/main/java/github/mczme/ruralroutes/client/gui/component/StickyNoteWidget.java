package github.mczme.ruralroutes.client.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * 基于精灵图渲染的便签组件。
 */
public class StickyNoteWidget extends AbstractWidget {

    public static final int NOTE_SOURCE_WIDTH = 14;
    public static final int NOTE_SOURCE_HEIGHT = 14;
    public static final int NOTES_TEXTURE_WIDTH = 50;
    public static final int NOTES_TEXTURE_HEIGHT = 29;
    public static final int ICONS_TEXTURE_WIDTH = 24;
    public static final int ICONS_TEXTURE_HEIGHT = 16;

    private static final ResourceLocation NOTES_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "textures/gui/notes.png");
    private static final ResourceLocation ICONS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "textures/gui/icons.png");

    private static final int PIN_SOURCE_U = 0;
    private static final int PIN_HOVER_SOURCE_U = 6;
    private static final int PIN_SOURCE_V = 0;
    private static final int PIN_SOURCE_SIZE = 5;
    private static final int HOVER_LIFT = 2;
    private static final int DRAG_LIFT = 3;
    private static final int TEXT_COLOR = 0xFF24180E;
    private static final int LABEL_COLOR = 0xFF321F11;
    private static final int SCOPE_COLOR = 0xFF3B2817;

    private final Component text;
    private final Component familyLabel;
    private final Component scopeLabel;
    private final int noteSourceU;
    private final int noteSourceV;
    private final int pinScale;
    private final float rotation;
    private final int layer;
    private final int labelInsetLeft;
    private final int labelInsetRight;
    private final int labelTop;
    private final int bodyInsetLeft;
    private final int bodyInsetRight;
    private final int bodyTop;
    private final int bodyBottom;
    private float extraRotation = 0.0f;
    private float scaleMultiplier = 1.0f;
    private boolean dragging = false;

    public StickyNoteWidget(
            int x,
            int y,
            int width,
            int height,
            Component text,
            Component familyLabel,
            Component scopeLabel,
            int noteSourceU,
            int noteSourceV,
            int pinScale,
            float rotation,
            int layer,
            int labelInsetLeft,
            int labelInsetRight,
            int labelTop,
            int bodyInsetLeft,
            int bodyInsetRight,
            int bodyTop,
            int bodyBottom
    ) {
        super(x, y, width, height, text);
        this.text = text;
        this.familyLabel = familyLabel;
        this.scopeLabel = scopeLabel;
        this.noteSourceU = noteSourceU;
        this.noteSourceV = noteSourceV;
        this.pinScale = pinScale;
        this.rotation = rotation;
        this.layer = layer;
        this.labelInsetLeft = labelInsetLeft;
        this.labelInsetRight = labelInsetRight;
        this.labelTop = labelTop;
        this.bodyInsetLeft = bodyInsetLeft;
        this.bodyInsetRight = bodyInsetRight;
        this.bodyTop = bodyTop;
        this.bodyBottom = bodyBottom;
    }

    public void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHoveredOrFocused();
        int renderX = getX();
        int lift = dragging ? DRAG_LIFT : (hovered ? HOVER_LIFT : 0);
        int renderY = getY() - lift;
        float scaleX = getWidth() / (float) NOTE_SOURCE_WIDTH;
        float scaleY = getHeight() / (float) NOTE_SOURCE_HEIGHT;
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        float centerX = renderX + getWidth() / 2.0f;
        float centerY = renderY + getHeight() / 2.0f;
        poseStack.translate(centerX, centerY, 0);
        poseStack.mulPose(new org.joml.Quaternionf().rotationZ((float) Math.toRadians(rotation + extraRotation)));
        poseStack.scale(scaleMultiplier, scaleMultiplier, 1.0f);
        poseStack.translate(-centerX, -centerY, 0);

        renderScaledSprite(
                guiGraphics,
                NOTES_TEXTURE,
                renderX,
                renderY,
                scaleX,
                scaleY,
                noteSourceU,
                noteSourceV,
                NOTE_SOURCE_WIDTH,
                NOTE_SOURCE_HEIGHT,
                NOTES_TEXTURE_WIDTH,
                NOTES_TEXTURE_HEIGHT
        );

        renderPin(guiGraphics, renderX, renderY, hovered);
        renderLabels(guiGraphics, renderX, renderY);
        renderText(guiGraphics, renderX, renderY);
        poseStack.popPose();
    }

    private void renderPin(GuiGraphics guiGraphics, int x, int y, boolean hovered) {
        int pinX = x + (getWidth() - PIN_SOURCE_SIZE * pinScale) / 2;
        int pinY = y - pinScale;
        renderScaledSprite(
                guiGraphics,
                ICONS_TEXTURE,
                pinX,
                pinY,
                pinScale,
                pinScale,
                hovered ? PIN_HOVER_SOURCE_U : PIN_SOURCE_U,
                PIN_SOURCE_V,
                PIN_SOURCE_SIZE,
                PIN_SOURCE_SIZE,
                ICONS_TEXTURE_WIDTH,
                ICONS_TEXTURE_HEIGHT
        );
    }

    private void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        var font = Minecraft.getInstance().font;
        int labelY = y + labelTop;
        guiGraphics.drawString(font, familyLabel, x + labelInsetLeft, labelY, LABEL_COLOR, false);

        if (scopeLabel == null || scopeLabel.getString().isEmpty()) {
            return;
        }

        int scopeX = x + getWidth() - labelInsetRight - font.width(scopeLabel);
        guiGraphics.drawString(font, scopeLabel, scopeX, labelY, SCOPE_COLOR, false);
    }

    private void renderText(GuiGraphics guiGraphics, int x, int y) {
        var font = Minecraft.getInstance().font;
        int maxWidth = getWidth() - bodyInsetLeft - bodyInsetRight;
        List<FormattedCharSequence> lines = font.split(text, maxWidth);
        int lineHeight = font.lineHeight;
        int bodyStartY = y + bodyTop;
        int availableHeight = getHeight() - bodyTop - bodyBottom;
        int maxLines = Math.max(1, availableHeight / lineHeight);
        int visibleLines = Math.min(maxLines, lines.size());

        for (int i = 0; i < visibleLines; i++) {
            guiGraphics.drawString(font, lines.get(i), x + bodyInsetLeft, bodyStartY + i * lineHeight, TEXT_COLOR, false);
        }
    }

    private static void renderScaledSprite(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            int x,
            int y,
            float scaleX,
            float scaleY,
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
        poseStack.scale(scaleX, scaleY, 1.0F);
        guiGraphics.blit(texture, 0, 0, sourceU, sourceV, sourceWidth, sourceHeight, textureWidth, textureHeight);
        poseStack.popPose();
    }

    public void setInteractionState(float extraRotation, float scaleMultiplier, boolean dragging) {
        this.extraRotation = extraRotation;
        this.scaleMultiplier = scaleMultiplier;
        this.dragging = dragging;
    }

    public int layer() {
        return layer;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, text);
    }
}
