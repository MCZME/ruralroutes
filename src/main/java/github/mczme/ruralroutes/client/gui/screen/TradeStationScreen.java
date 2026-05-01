package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.client.gui.component.BuySectionWidget;
import github.mczme.ruralroutes.client.gui.component.CoinExchangeWidget;
import github.mczme.ruralroutes.client.gui.component.SellSectionWidget;
import github.mczme.ruralroutes.client.gui.component.TradeAreaWidget;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 贸易站 GUI 屏幕
 */
public class TradeStationScreen extends Screen implements MenuAccess<TradeStationMenu> {

    // GUI 尺寸
    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;
    private static final int MARGIN = 10;
    private static final int SECTION_SPACING = 8;

    private final TradeStationMenu menu;

    // 组件
    private SellSectionWidget sellSection;
    private BuySectionWidget buySection;
    private TradeAreaWidget tradeArea;
    private CoinExchangeWidget coinExchange;

    // GUI 左上角位置（居中）
    private int leftPos;
    private int topPos;

    public TradeStationScreen(TradeStationMenu menu, Inventory ignored, Component title) {
        super(title);
        this.menu = menu;
    }

    @Override
    public TradeStationMenu getMenu() {
        return menu;
    }

    @Override
    protected void init() {
        super.init();

        // 计算 GUI 左上角位置（居中）
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        int currentY = topPos;
        int mainWidth = GUI_WIDTH - MARGIN * 2;

        // 村庄出售清单 - 网格布局（增加内边距）
        int gridHeight = 12 + 2 * (18 + 2) + 8; // 标题 + 2行卡片 + padding
        sellSection = new SellSectionWidget(
            leftPos + MARGIN,
            currentY,
            mainWidth,
            gridHeight
        );
        // 测试数据
        for (int i = 0; i < 30; i++) {
            sellSection.addItem(new ItemStack(Items.WHEAT, i + 1));
        }
        currentY += gridHeight + SECTION_SPACING;

        // 村庄收购清单 - 网格布局（增加内边距）
        buySection = new BuySectionWidget(
            leftPos + MARGIN,
            currentY,
            mainWidth,
            gridHeight
        );
        // 测试数据
        for (int i = 0; i < 25; i++) {
            buySection.addItem(new ItemStack(Items.EMERALD, i + 1));
        }
        currentY += gridHeight + SECTION_SPACING;

        // 交易区（左侧约 70% 宽度）
        int tradeWidth = mainWidth * 7 / 10 - SECTION_SPACING / 2;
        tradeArea = new TradeAreaWidget(
            leftPos + MARGIN,
            currentY,
            tradeWidth,
            110
        );
        tradeArea.init(btn -> {
            // 占位：确认按钮点击逻辑
        });

        // 铸币快捷操作（右侧约 30% 宽度）
        int coinWidth = mainWidth * 3 / 10 - SECTION_SPACING / 2;
        int coinX = leftPos + MARGIN + tradeWidth + SECTION_SPACING;
        coinExchange = new CoinExchangeWidget(
            coinX,
            currentY,
            coinWidth,
            110
        );
        coinExchange.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制半透明背景
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制 GUI 背景
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xCC222222);

        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, leftPos + GUI_WIDTH / 2, topPos + 5, 0xFFFFFF);

        // 渲染各组件
        sellSection.render(guiGraphics, mouseX, mouseY, partialTick);
        buySection.render(guiGraphics, mouseX, mouseY, partialTick);
        tradeArea.render(guiGraphics, mouseX, mouseY, partialTick);
        coinExchange.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (sellSection.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (buySection.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (tradeArea.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (coinExchange.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        sellSection.mouseReleased(mouseX, mouseY, button);
        buySection.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (sellSection.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (buySection.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (sellSection.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        if (buySection.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}