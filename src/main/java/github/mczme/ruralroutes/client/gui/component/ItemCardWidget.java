package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * 可点击的物品卡片组件
 */
public class ItemCardWidget extends AbstractWidget {

    private static final int DEFAULT_COLOR = 0x40FFFFFF;
    private static final int HOVER_COLOR = 0x80FFFFFF;

    private ItemStack itemStack;
    private Consumer<ItemCardWidget> onClick;

    public ItemCardWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.itemStack = ItemStack.EMPTY;
    }

    public ItemCardWidget setItemStack(ItemStack stack) {
        this.itemStack = stack;
        return this;
    }

    public ItemCardWidget setOnClick(Consumer<ItemCardWidget> onClick) {
        this.onClick = onClick;
        return this;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 确定背景颜色
        int bgColor = isHovered() ? HOVER_COLOR : DEFAULT_COLOR;

        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), bgColor);

        // 绘制物品图标
        if (!itemStack.isEmpty()) {
            guiGraphics.renderItem(itemStack, getX() + 1, getY() + 1);
            if (itemStack.getCount() > 1) {
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, getX() + 1, getY() + 1);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button) && isHovered()) {
            if (onClick != null) {
                onClick.accept(this);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE,
            itemStack.isEmpty() ? Component.literal("空槽位") : itemStack.getDisplayName());
    }

    private void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }
}
