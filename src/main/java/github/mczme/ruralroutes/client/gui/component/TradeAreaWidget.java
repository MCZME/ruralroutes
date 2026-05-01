package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

/**
 * 交易区组件
 */
public class TradeAreaWidget extends AbstractWidget {

    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int GIVE_AREA_COLOR = 0x40FFAA00;
    private static final int RECEIVE_AREA_COLOR = 0x4000AAFF;
    private static final int STATUS_COLOR = 0x40AAAAAA;

    private Button confirmButton;

    public TradeAreaWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void init(Button.OnPress confirmAction) {
        int buttonWidth = 80;
        int buttonHeight = 20;
        int buttonX = getX() + (getWidth() - buttonWidth) / 2;
        int buttonY = getY() + getHeight() - buttonHeight - 5;

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
        var font = Minecraft.getInstance().font;
        int currentY = getY();

        // "你付出" 区域
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.you_give"),
            getX(), currentY, LABEL_COLOR);
        currentY += 12;
        fill(guiGraphics, getX(), currentY, getWidth(), 30, GIVE_AREA_COLOR);
        currentY += 35;

        // 箭头分隔
        guiGraphics.drawCenteredString(font, "↓", getX() + getWidth() / 2, currentY, LABEL_COLOR);
        currentY += 15;

        // "你获得" 区域
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.you_receive"),
            getX(), currentY, LABEL_COLOR);
        currentY += 12;
        fill(guiGraphics, getX(), currentY, getWidth(), 30, RECEIVE_AREA_COLOR);
        currentY += 35;

        // 价值状态区域
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.value_status"),
            getX(), currentY, LABEL_COLOR);
        currentY += 12;
        fill(guiGraphics, getX(), currentY, getWidth(), 16, STATUS_COLOR);

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