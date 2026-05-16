package github.mczme.ruralroutes.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.InputConstants;
import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 展示柜 GUI 屏幕
 * 极简展柜特写：仅展示展品与铭牌。
 */
public class DisplayCaseScreen extends Screen {

    private static final ResourceLocation GUI_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "textures/gui/display_case.png");

    private static final int GUI_WIDTH = 214;
    private static final int GUI_HEIGHT = 168;
    private static final int ITEM_RENDER_SCALE = 3;
    private static final int ITEM_HOVER_SIZE = 48;
    private static final int ITEM_CENTER_Y = 80;
    private static final int NAME_PLATE_Y = 144;

    private static final int BACKDROP_TOP = 0xD915100C;
    private static final int BACKDROP_BOTTOM = 0xF0080604;

    private static final int TEXT_MUTED = 0xFFB49D84;
    private static final int TEXT_PLATE = 0xFFF1D6A5;

    private final ItemStack displayItem;
    private int leftPos;
    private int topPos;

    public DisplayCaseScreen(ItemStack displayItem) {
        super(Component.translatable("block.ruralroutes.display_case"));
        this.displayItem = displayItem.copy();
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderBackdrop(guiGraphics);
        renderBoard(guiGraphics);
        renderShowcase(guiGraphics);
        renderItemTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderBackdrop(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, BACKDROP_TOP, BACKDROP_BOTTOM);
    }

    private void renderBoard(GuiGraphics guiGraphics) {
        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 12, TEXT_MUTED);
    }

    private void renderShowcase(GuiGraphics guiGraphics) {
        if (displayItem.isEmpty()) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("block.ruralroutes.display_case.empty"),
                leftPos + GUI_WIDTH / 2,
                topPos + ITEM_CENTER_Y - font.lineHeight / 2,
                TEXT_MUTED);
            return;
        }

        int itemCenterX = leftPos + GUI_WIDTH / 2;
        int itemCenterY = topPos + ITEM_CENTER_Y;
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(itemCenterX, itemCenterY, 0);
        poseStack.scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, 1.0F);
        guiGraphics.renderItem(displayItem, -8, -8);
        poseStack.popPose();

        renderNamePlate(guiGraphics);
    }

    private void renderNamePlate(GuiGraphics guiGraphics) {
        int plateWidth = Math.min(144, Math.max(92, font.width(displayItem.getHoverName()) + 20));
        int plateX = leftPos + (GUI_WIDTH - plateWidth) / 2;
        guiGraphics.drawCenteredString(font, displayItem.getHoverName(), plateX + plateWidth / 2, topPos + NAME_PLATE_Y, TEXT_PLATE);
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!displayItem.isEmpty() && isHoveringItem(mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, displayItem, mouseX, mouseY);
        }
    }

    private boolean isHoveringItem(int mouseX, int mouseY) {
        int itemCenterX = leftPos + GUI_WIDTH / 2;
        int itemCenterY = topPos + ITEM_CENTER_Y;
        int itemLeft = itemCenterX - ITEM_HOVER_SIZE / 2;
        int itemTop = itemCenterY - ITEM_HOVER_SIZE / 2;
        return mouseX >= itemLeft && mouseX < itemLeft + ITEM_HOVER_SIZE
            && mouseY >= itemTop && mouseY < itemTop + ITEM_HOVER_SIZE;
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
    public boolean isPauseScreen() {
        return false;
    }
}
