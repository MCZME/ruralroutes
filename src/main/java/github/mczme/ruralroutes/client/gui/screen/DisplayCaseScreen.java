package github.mczme.ruralroutes.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 展示柜 GUI 屏幕
 * 纯展示物品信息，不需要 Menu
 */
public class DisplayCaseScreen extends Screen {

    // GUI 尺寸
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 120;

    // 物品显示位置
    private static final int ITEM_X = 80;
    private static final int ITEM_Y = 30;

    // 背景颜色
    private static final int BACKGROUND_COLOR = 0xCC4A3728;
    private static final int BORDER_COLOR = 0xFF8B7355;

    private final ItemStack displayItem;

    public DisplayCaseScreen(ItemStack displayItem) {
        super(Component.translatable("block.ruralroutes.display_case"));
        this.displayItem = displayItem;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // 计算居中位置
        int leftPos = (this.width - GUI_WIDTH) / 2;
        int topPos = (this.height - GUI_HEIGHT) / 2;

        // 渲染背景
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, BACKGROUND_COLOR);
        guiGraphics.renderOutline(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, BORDER_COLOR);

        // 绘制物品槽背景
        int slotX = leftPos + ITEM_X - 2;
        int slotY = topPos + ITEM_Y - 2;
        guiGraphics.fill(slotX, slotY, slotX + 20, slotY + 20, 0x55000000);
        guiGraphics.renderOutline(slotX, slotY, 20, 20, 0xFF6B5344);

        // 渲染标题
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 5, 0xFFFFFF);

        // 渲染展示物品
        if (!displayItem.isEmpty()) {
            // 渲染物品图标
            guiGraphics.renderItem(displayItem, leftPos + ITEM_X, topPos + ITEM_Y);

            // 渲染物品名称
            Component itemName = displayItem.getDisplayName();
            guiGraphics.drawCenteredString(font, itemName, leftPos + GUI_WIDTH / 2, topPos + ITEM_Y + 20, 0xFFFFFF);

            // 如果鼠标悬停在物品上，渲染 tooltip
            if (isHovering(leftPos + ITEM_X, topPos + ITEM_Y, 16, 16, mouseX, mouseY)) {
                guiGraphics.renderTooltip(font, displayItem, mouseX, mouseY);
            }
        } else {
            // 无展示物品时显示提示
            Component emptyText = Component.translatable("block.ruralroutes.display_case.empty");
            guiGraphics.drawCenteredString(font, emptyText, leftPos + GUI_WIDTH / 2, topPos + ITEM_Y + 8, 0xAAAAAA);
        }
    }

    /**
     * 检查鼠标是否悬停在指定区域
     */
    private boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}