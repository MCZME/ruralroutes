package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * 贸易站 GUI 屏幕 - 显示基本信息
 */
public class TradeStationScreen extends Screen implements MenuAccess<TradeStationMenu> {

    private final TradeStationMenu menu;

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
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, this.width / 2, 15, 0xFFFFFF);

        // 显示主题名称
        String themeDisplay = menu.getThemeDisplayName();
        guiGraphics.drawCenteredString(font,
            Component.translatable("gui.ruralroutes.trade_station.theme", themeDisplay),
            this.width / 2, 30, 0xAAAAAA);

        // 显示出售物品（村庄卖给玩家）
        int startY = 50;
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.sell"),
            20, startY, 0x55FF55);

        List<TradeStationMenu.StockInfo> sellStocks = menu.getSellStocks();
        for (int i = 0; i < Math.min(10, sellStocks.size()); i++) {
            TradeStationMenu.StockInfo info = sellStocks.get(i);
            String itemText = info.itemId().getPath() + ": " + info.getDisplayText();
            guiGraphics.drawString(font, itemText, 25, startY + 15 + i * 12, 0xFFFFFF);
        }

        // 显示收购物品（玩家卖给村庄）
        int buyStartY = startY + 15 + Math.min(10, sellStocks.size()) * 12 + 20;
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_station.buy"),
            20, buyStartY, 0xFF5555);

        List<TradeStationMenu.StockInfo> buyStocks = menu.getBuyStocks();
        for (int i = 0; i < Math.min(10, buyStocks.size()); i++) {
            TradeStationMenu.StockInfo info = buyStocks.get(i);
            int currentSpace = info.max() - info.current();
            String itemText = info.itemId().getPath() + ": " + currentSpace + "/" + info.max();
            guiGraphics.drawString(font, itemText, 25, buyStartY + 15 + i * 12, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}