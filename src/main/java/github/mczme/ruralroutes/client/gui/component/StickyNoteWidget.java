package github.mczme.ruralroutes.client.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.List;

/**
 * 便签组件
 * 支持旋转渲染、分层显示
 */
public class StickyNoteWidget extends AbstractWidget {

    private final Component text;
    private final int backgroundColor;
    private final float rotation;
    private final int layer;

    // 字体颜色
    private static final int TEXT_COLOR = 0x333333;

    // 图钉效果
    private static final int PIN_COLOR = 0xFFD32F2F;
    private static final int PIN_SIZE = 6;

    public StickyNoteWidget(int x, int y, int width, int height,
                            Component text, int backgroundColor, float rotation, int layer) {
        super(x, y, width, height, text);
        this.text = text;
        this.backgroundColor = backgroundColor;
        this.rotation = rotation;
        this.layer = layer;
    }

    /**
     * 设置位置
     */
    public void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        // 移动到便签中心
        float centerX = getX() + getWidth() / 2.0f;
        float centerY = getY() + getHeight() / 2.0f;
        poseStack.translate(centerX, centerY, 0);

        // 旋转（围绕 Z 轴）
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(rotation), 0, 0, 1)));

        // 移回原位
        poseStack.translate(-centerX, -centerY, 0);

        // 绘制阴影（仅顶层便签）
        if (layer == 1) {
            fill(poseStack, guiGraphics,
                    getX() + 3, getY() + 3,
                    getWidth(), getHeight(),
                    0x40000000);
        }

        // 绘制便签背景
        fill(poseStack, guiGraphics,
                getX(), getY(),
                getWidth(), getHeight(),
                backgroundColor);

        // 绘制便签边框（轻微深色）
        guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), 0x40333333);

        // 绘制图钉（在顶部中心）
        renderPin(guiGraphics);

        // 绘制文本（自动换行）
        renderText(guiGraphics);

        poseStack.popPose();
    }

    /**
     * 渲染图钉效果
     */
    private void renderPin(GuiGraphics guiGraphics) {
        int pinX = getX() + getWidth() / 2 - PIN_SIZE / 2;
        int pinY = getY() - PIN_SIZE / 2 + 2;

        // 图钉圆形
        guiGraphics.fill(pinX, pinY, pinX + PIN_SIZE, pinY + PIN_SIZE, PIN_COLOR);

        // 图钉高光
        guiGraphics.fill(pinX + 1, pinY + 1, pinX + 3, pinY + 3, 0xFFFFFFFF);
    }

    /**
     * 渲染文本（自动换行）
     */
    private void renderText(GuiGraphics guiGraphics) {
        var font = Minecraft.getInstance().font;

        // 计算可用文本宽度
        int maxWidth = getWidth() - 12;

        // 分割文本为多行
        List<FormattedCharSequence> lines = font.split(text, maxWidth);

        int lineHeight = font.lineHeight + 1;
        int totalHeight = lines.size() * lineHeight;
        int startY = getY() + 12 + (getHeight() - 12 - totalHeight) / 2;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int lineWidth = font.width(line);
            int x = getX() + (getWidth() - lineWidth) / 2;
            int y = startY + i * lineHeight;

            guiGraphics.drawString(font, line, x, y, TEXT_COLOR, false);
        }
    }

    private void fill(PoseStack poseStack, GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, text);
    }
}