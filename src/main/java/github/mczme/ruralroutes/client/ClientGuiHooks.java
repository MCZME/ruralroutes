package github.mczme.ruralroutes.client;

import github.mczme.ruralroutes.client.gui.screen.DisplayCaseScreen;
import github.mczme.ruralroutes.client.gui.screen.NodeDataViewerScreen;
import github.mczme.ruralroutes.client.gui.screen.RumorBoardScreen;
import github.mczme.ruralroutes.client.gui.screen.TradeAtlasScreen;
import github.mczme.ruralroutes.network.packet.OpenNodeDataViewerPayload;
import github.mczme.ruralroutes.network.packet.OpenRumorBoardPayload;
import github.mczme.ruralroutes.network.packet.OpenTradeAtlasPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * 客户端界面桥接。
 *
 * 将客户端 Screen 打开逻辑隔离在客户端包内，
 * 避免公共类在服务端环境直接引用 net.minecraft.client.*。
 */
public final class ClientGuiHooks {

    private ClientGuiHooks() {}

    public static void openDisplayCaseScreen(ItemStack displayItem) {
        Minecraft.getInstance().setScreen(new DisplayCaseScreen(displayItem));
    }

    public static void openNodeDataViewerScreen(OpenNodeDataViewerPayload payload) {
        Minecraft.getInstance().setScreen(
                new NodeDataViewerScreen(payload.targetBlockType(), payload.viewStatus(), payload.nodeData())
        );
    }

    public static void openRumorBoardScreen(OpenRumorBoardPayload payload) {
        Minecraft.getInstance().setScreen(
                new RumorBoardScreen(payload.blockPos(), payload.marketState())
        );
    }

    public static void openTradeAtlasScreen(OpenTradeAtlasPayload payload) {
        Minecraft.getInstance().setScreen(new TradeAtlasScreen(payload.state()));
    }
}
