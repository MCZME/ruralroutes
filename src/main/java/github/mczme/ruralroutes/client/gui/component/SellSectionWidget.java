package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

/**
 * 村庄出售清单组件 - 网格卡片布局
 */
public class SellSectionWidget extends AbstractWidget {

    private static final int TITLE_COLOR = 0x55FF55;
    private static final int CARD_COLOR = 0x4055FF55;
    private static final int CARD_SIZE = 18;
    private static final int CARD_SPACING = 2;
    private static final int COLS = 9;
    private static final int ROWS = 2;

    public SellSectionWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_station.sell"));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制标题
        guiGraphics.drawString(Minecraft.getInstance().font, getMessage(), getX(), getY(), TITLE_COLOR);

        // 绘制网格卡片
        int gridX = getX();
        int gridY = getY() + 12;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cardX = gridX + col * (CARD_SIZE + CARD_SPACING);
                int cardY = gridY + row * (CARD_SIZE + CARD_SPACING);
                fill(guiGraphics, cardX, cardY, CARD_SIZE, CARD_SIZE, CARD_COLOR);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}
