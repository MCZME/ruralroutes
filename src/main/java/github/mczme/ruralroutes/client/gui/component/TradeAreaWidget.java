package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

/**
 * 交易区组件 - 左右布局，中间箭头和按钮
 */
public class TradeAreaWidget extends AbstractWidget {

    private static final int BG_COLOR = 0x40333333;
    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int GIVE_AREA_COLOR = 0x40FFAA00;
    private static final int RECEIVE_AREA_COLOR = 0x4000AAFF;
    private static final int PADDING = 4;

    private Button confirmButton;

    public TradeAreaWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void init(Button.OnPress confirmAction) {
        int buttonWidth = Math.min(30, getWidth() / 4);
        int buttonHeight = 18;
        int centerX = getX() + getWidth() / 2;
        int buttonX = centerX - buttonWidth / 2;
        int buttonY = getY() + getHeight() / 2 + 4;

        confirmButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_station.confirm"),
            confirmAction
        ).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build();
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), BG_COLOR);

        var font = Minecraft.getInstance().font;
        int sideWidth = getWidth() * 2 / 5;  // 左右各占 40%
        int titleHeight = 12;
        // 确保区域底部不超过交易区底部
        int areaHeight = getHeight() - PADDING * 3 - titleHeight;
        int contentY = getY() + PADDING;

        // "你付出" 区域（左侧 1/3）
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.you_give"),
            getX() + PADDING, contentY, LABEL_COLOR);
        int areaY = contentY + titleHeight;
        fill(guiGraphics, getX() + PADDING, areaY, sideWidth - PADDING * 2, areaHeight, GIVE_AREA_COLOR);

        // 中间箭头
        int centerX = getX() + getWidth() / 2;
        guiGraphics.drawCenteredString(font, "→", centerX, areaY + areaHeight / 2 - 10, LABEL_COLOR);

        // "你获得" 区域（右侧）
        int receiveX = getX() + getWidth() - sideWidth;
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.you_receive"),
            receiveX, contentY, LABEL_COLOR);
        fill(guiGraphics, receiveX, areaY, sideWidth - PADDING * 2, areaHeight, RECEIVE_AREA_COLOR);

        // 渲染确认按钮
        if (confirmButton != null) {
            confirmButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (confirmButton != null && confirmButton.isMouseOver(mouseX, mouseY)) {
            return confirmButton.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable("gui.ruralroutes.trade_station.trade_area"));
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}