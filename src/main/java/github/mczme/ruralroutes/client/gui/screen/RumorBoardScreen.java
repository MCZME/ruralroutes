package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.client.gui.component.StickyNoteWidget;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayout;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayoutGenerator;
import github.mczme.ruralroutes.menu.RumorBoardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * 传闻板 GUI 屏幕
 * 便签风格，杂乱分布，奇偶分层
 */
public class RumorBoardScreen extends AbstractContainerScreen<RumorBoardMenu> {

    // GUI 尺寸常量
    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 200;

    // 便签样式常量
    private static final int NOTE_WIDTH = StickyNoteLayoutGenerator.NOTE_WIDTH;
    private static final int NOTE_HEIGHT = StickyNoteLayoutGenerator.NOTE_HEIGHT;

    // 便签颜色（多种柔和颜色）
    private static final int[] NOTE_COLORS = {
            0xFFFFF9C4,  // 浅黄
            0xFFFFE0B2,  // 浅橙
            0xFFFFCDD2,  // 浅红
            0xFFF8BBD9,  // 浅粉
            0xFFE1BEE7,  // 浅紫
            0xFFC5CAE9,  // 浅蓝
            0xFFB3E5FC,  // 天蓝
            0xFFB2DFDB,  // 浅绿
    };

    // 背景颜色
    private static final int BACKGROUND_COLOR = 0xCC4A3728;  // 深棕木纹
    private static final int BORDER_COLOR = 0xFF8B7355;      // 边框颜色

    private final List<StickyNoteWidget> noteWidgets = new ArrayList<>();
    private boolean dataCollected = false;

    public RumorBoardScreen(RumorBoardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        // 清除旧组件
        noteWidgets.clear();
        dataCollected = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // 首次渲染时收集数据
        if (!dataCollected) {
            menu.collectClientData();
            dataCollected = true;
            buildNoteWidgets();
        }

        // 按层级渲染便签（先底层，后顶层）
        renderNotesByLayer(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染标题和周期时间
        renderHeader(guiGraphics);
    }

    /**
     * 构建便签组件
     */
    private void buildNoteWidgets() {
        noteWidgets.clear();

        List<Component> displayTexts = menu.getDisplayTexts();
        List<StickyNoteLayout> layouts = menu.getLayouts();

        for (int i = 0; i < displayTexts.size(); i++) {
            StickyNoteLayout layout = (i < layouts.size()) ? layouts.get(i) : null;
            Component displayText = displayTexts.get(i);

            // 选择便签颜色（循环使用）
            int colorIndex = i % NOTE_COLORS.length;
            int noteColor = NOTE_COLORS[colorIndex];

            StickyNoteWidget widget = new StickyNoteWidget(
                    0, 0,  // 位置在渲染时计算
                    NOTE_WIDTH, NOTE_HEIGHT,
                    displayText,
                    noteColor,
                    layout != null ? layout.rotation() : 0,
                    layout != null ? layout.layer() : 0
            );

            noteWidgets.add(widget);
        }
    }

    /**
     * 按层级渲染便签
     */
    private void renderNotesByLayer(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<StickyNoteLayout> layouts = menu.getLayouts();

        // 先渲染底层（layer 0）
        for (int i = 0; i < noteWidgets.size(); i++) {
            StickyNoteWidget widget = noteWidgets.get(i);
            StickyNoteLayout layout = (i < layouts.size()) ? layouts.get(i) : null;

            if (layout != null && layout.layer() == 0) {
                int x = layout.getAbsoluteX(leftPos, GUI_WIDTH, NOTE_WIDTH);
                int y = layout.getAbsoluteY(topPos, GUI_HEIGHT, NOTE_HEIGHT);
                widget.setPosition(x, y);
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        // 再渲染顶层（layer 1）
        for (int i = 0; i < noteWidgets.size(); i++) {
            StickyNoteWidget widget = noteWidgets.get(i);
            StickyNoteLayout layout = (i < layouts.size()) ? layouts.get(i) : null;

            if (layout != null && layout.layer() == 1) {
                int x = layout.getAbsoluteX(leftPos, GUI_WIDTH, NOTE_WIDTH);
                int y = layout.getAbsoluteY(topPos, GUI_HEIGHT, NOTE_HEIGHT);
                widget.setPosition(x, y);
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    /**
     * 渲染标题和周期时间
     */
    private void renderHeader(GuiGraphics guiGraphics) {
        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 5, 0xFFFFFF);

        // 绘制周期剩余时间
        String timeDesc = menu.getTimeRemainingDesc();
        if (!timeDesc.isEmpty()) {
            guiGraphics.drawCenteredString(font,
                    Component.translatable("gui.ruralroutes.rumor_board.refresh_in", timeDesc),
                    leftPos + GUI_WIDTH / 2, topPos + GUI_HEIGHT - 12,
                    0xAAAAAA);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 绘制木板背景
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, BACKGROUND_COLOR);

        // 绘制边框
        guiGraphics.renderOutline(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, BORDER_COLOR);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}