package github.mczme.ruralroutes.client.gui.component;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * 村庄出售清单组件 - 可横向滚动
 */
public class SellSectionWidget extends ScrollableSectionWidget {

    private static final int TITLE_COLOR = 0x55FF55;
    private static final int BG_COLOR = 0x40333333;

    public SellSectionWidget(int x, int y, int width, int height) {
        super(x, y, width, height, TITLE_COLOR, BG_COLOR,
              Component.translatable("gui.ruralroutes.trade_station.sell"));
    }

    @Override
    public SellSectionWidget setOnCardClick(Consumer<ItemCardWidget> onCardClick) {
        super.setOnCardClick(onCardClick);
        return this;
    }
}
