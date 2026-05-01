package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * 村庄收购清单组件 - 可横向滚动
 */
public class BuySectionWidget extends ScrollableSectionWidget {

    private static final int TITLE_COLOR = 0xFF5555;
    private static final int BG_COLOR = 0x40333333;

    public BuySectionWidget(int x, int y, int width, int height) {
        super(x, y, width, height, TITLE_COLOR, BG_COLOR,
              Component.translatable("gui.ruralroutes.trade_station.buy"));
    }

    @Override
    public BuySectionWidget setOnCardClick(Consumer<ItemCardWidget> onCardClick) {
        super.setOnCardClick(onCardClick);
        return this;
    }
}
