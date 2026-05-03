package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 铸币快捷操作组件
 */
public class CoinExchangeWidget extends AbstractWidget {

    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;

    private final List<Button> buttons = new ArrayList<>();

    public CoinExchangeWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void init() {
        buttons.clear();

        String[] labels = {"铜→铁", "铁→铜", "铁→金", "金→铁"};
        int buttonWidth = getWidth() - 10;
        int buttonX = getX() + 5;
        int currentY = getY() + 10;

        for (String label : labels) {
            Button button = Button.builder(
                Component.literal(label),
                btn -> {}
            ).bounds(buttonX, currentY, buttonWidth, BUTTON_HEIGHT).build();
            buttons.add(button);
            currentY += BUTTON_HEIGHT + BUTTON_SPACING;
        }
    }

    public List<Button> getButtons() {
        return buttons;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;

        // 绘制标题
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.coin_exchange"),
            getX() + 5, getY(), 0xAAAAAA);

        // 渲染按钮
        for (Button button : buttons) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 先检查鼠标是否在组件范围内
        if (!isMouseOver(mouseX, mouseY)) return false;

        for (Button btn : buttons) {
            if (btn.isMouseOver(mouseX, mouseY)) {
                return btn.mouseClicked(mouseX, mouseY, button);
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable("gui.ruralroutes.trade_station.coin_exchange"));
    }
}