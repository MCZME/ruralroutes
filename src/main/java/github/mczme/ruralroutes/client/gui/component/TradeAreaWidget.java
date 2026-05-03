package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * 交易区组件 - 左右布局，显示可点击的卡片和确认按钮
 */
public class TradeAreaWidget extends AbstractWidget {

    private static final int BG_COLOR = 0x40333333;
    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int GIVE_AREA_COLOR = 0x40FFAA00;
    private static final int RECEIVE_AREA_COLOR = 0x4000AAFF;
    private static final int PADDING = 4;
    private static final int CARD_SIZE = 18;

    private Button confirmButton;
    private ItemCardWidget giveCard;   // 付出卡片（可点击取消）
    private ItemCardWidget receiveCard; // 获得卡片（仅显示）
    private Consumer<ItemCardWidget> onGiveCardClick;  // 点击付出卡片的回调

    public TradeAreaWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void init(Button.OnPress confirmAction) {
        int buttonWidth = Math.min(50, getWidth() / 4);
        int buttonHeight = 18;
        int centerX = getX() + getWidth() / 2;
        int buttonX = centerX - buttonWidth / 2;
        int buttonY = getY() + getHeight() - PADDING - buttonHeight;

        confirmButton = Button.builder(
            Component.translatable("gui.ruralroutes.trade_station.confirm"),
            confirmAction
        ).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build();

        // 创建付出卡片（可点击）
        giveCard = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
        giveCard.setOnClick(card -> {
            if (onGiveCardClick != null) {
                onGiveCardClick.accept(card);
            }
        });

        // 创建获得卡片（仅显示，不可点击）
        receiveCard = new ItemCardWidget(0, 0, CARD_SIZE, CARD_SIZE);
    }

    /**
     * 设置点击付出卡片的回调（用于取消交易）
     */
    public void setOnGiveCardClick(Consumer<ItemCardWidget> onClick) {
        this.onGiveCardClick = onClick;
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public void setGiveItem(ItemStack stack) {
        if (giveCard != null) {
            giveCard.setItemStack(stack);
        }
    }

    public void setReceiveItem(ItemStack stack) {
        if (receiveCard != null) {
            receiveCard.setItemStack(stack);
        }
    }

    /**
     * 清空交易区内容
     */
    public void clearContent() {
        if (giveCard != null) {
            giveCard.setItemStack(ItemStack.EMPTY);
        }
        if (receiveCard != null) {
            receiveCard.setItemStack(ItemStack.EMPTY);
        }
    }

    public ItemStack getGiveItem() {
        return giveCard != null ? giveCard.getItemStack() : ItemStack.EMPTY;
    }

    public ItemStack getReceiveItem() {
        return receiveCard != null ? receiveCard.getItemStack() : ItemStack.EMPTY;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景
        fill(guiGraphics, getX(), getY(), getWidth(), getHeight(), BG_COLOR);

        var font = Minecraft.getInstance().font;
        int sideWidth = getWidth() * 2 / 5;
        int titleHeight = 12;
        int areaHeight = getHeight() - PADDING * 3 - titleHeight - 20; // 为按钮留空间
        int contentY = getY() + PADDING;

        // "你付出" 区域（左侧）
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.you_give"),
            getX() + PADDING, contentY, LABEL_COLOR);
        int areaY = contentY + titleHeight;
        fill(guiGraphics, getX() + PADDING, areaY, sideWidth - PADDING * 2, areaHeight, GIVE_AREA_COLOR);

        // 计算卡片位置并渲染付出卡片
        int giveItemX = getX() + PADDING + (sideWidth - PADDING * 2 - CARD_SIZE) / 2;
        int giveItemY = areaY + (areaHeight - CARD_SIZE) / 2;
        if (giveCard != null) {
            giveCard.setX(giveItemX);
            giveCard.setY(giveItemY);
            giveCard.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 中间箭头
        int centerX = getX() + getWidth() / 2;
        guiGraphics.drawCenteredString(font, "→", centerX, areaY + areaHeight / 2 - 10, LABEL_COLOR);

        // "你获得" 区域（右侧）
        int receiveX = getX() + getWidth() - sideWidth;
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.you_receive"),
            receiveX, contentY, LABEL_COLOR);
        fill(guiGraphics, receiveX, areaY, sideWidth - PADDING * 2, areaHeight, RECEIVE_AREA_COLOR);

        // 计算卡片位置并渲染获得卡片
        int receiveItemX = receiveX + (sideWidth - PADDING * 2 - CARD_SIZE) / 2;
        int receiveItemY = areaY + (areaHeight - CARD_SIZE) / 2;
        if (receiveCard != null) {
            receiveCard.setX(receiveItemX);
            receiveCard.setY(receiveItemY);
            receiveCard.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 渲染确认按钮
        if (confirmButton != null) {
            confirmButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查付出卡片点击（取消交易）
        if (giveCard != null && !giveCard.getItemStack().isEmpty()) {
            if (giveCard.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        // 检查确认按钮点击
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
