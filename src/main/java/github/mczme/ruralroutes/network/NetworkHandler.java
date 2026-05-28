package github.mczme.ruralroutes.network;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.network.packet.CoinExchangeRequestPayload;
import github.mczme.ruralroutes.network.packet.CoinExchangeStatePayload;
import github.mczme.ruralroutes.network.packet.OpenNodeDataViewerPayload;
import github.mczme.ruralroutes.network.packet.OpenRumorBoardPayload;
import github.mczme.ruralroutes.network.packet.OpenTradeAtlasPayload;
import github.mczme.ruralroutes.network.packet.PendingTradeSyncPayload;
import github.mczme.ruralroutes.network.packet.TradeAtlasActionPayload;
import github.mczme.ruralroutes.network.packet.TradeFeedbackPayload;
import github.mczme.ruralroutes.network.packet.TradeRequestPayload;
import github.mczme.ruralroutes.network.packet.TradeSlotSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络包处理
 */
public class NetworkHandler {

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(RuralRoutes.MODID);

        // 配置工具应用主题（客户端→服务端）
        registrar.playToServer(ConfigToolApplyPayload.TYPE, ConfigToolApplyPayload.STREAM_CODEC,
            ConfigToolApplyPayload::handleServer);

        // 交易请求（客户端→服务端）
        registrar.playToServer(TradeRequestPayload.TYPE, TradeRequestPayload.STREAM_CODEC,
            TradeRequestPayload::handleServer);

        // 货币交换请求（客户端→服务端）
        registrar.playToServer(CoinExchangeRequestPayload.TYPE, CoinExchangeRequestPayload.STREAM_CODEC,
            CoinExchangeRequestPayload::handleServer);

        // 贸易槽位同步（服务端→客户端）
        registrar.playToClient(TradeSlotSyncPayload.TYPE, TradeSlotSyncPayload.STREAM_CODEC,
            TradeSlotSyncPayload::handleClient);

        // 暂存区交易同步（服务端→客户端）
        registrar.playToClient(PendingTradeSyncPayload.TYPE, PendingTradeSyncPayload.STREAM_CODEC,
            PendingTradeSyncPayload::handleClient);

        // 货币交换状态同步（服务端→客户端）
        registrar.playToClient(CoinExchangeStatePayload.TYPE, CoinExchangeStatePayload.STREAM_CODEC,
            CoinExchangeStatePayload::handleClient);

        // 交易反馈（服务端→客户端）
        registrar.playToClient(TradeFeedbackPayload.TYPE, TradeFeedbackPayload.STREAM_CODEC,
            TradeFeedbackPayload::handleClient);

        // 打开传闻板界面（服务端→客户端）
        registrar.playToClient(OpenRumorBoardPayload.TYPE, OpenRumorBoardPayload.STREAM_CODEC,
            OpenRumorBoardPayload::handleClient);

        // 打开节点数据查看器（服务端→客户端）
        registrar.playToClient(OpenNodeDataViewerPayload.TYPE, OpenNodeDataViewerPayload.STREAM_CODEC,
            OpenNodeDataViewerPayload::handleClient);

        // 打开商路图册（服务端→客户端）
        registrar.playToClient(OpenTradeAtlasPayload.TYPE, OpenTradeAtlasPayload.STREAM_CODEC,
            OpenTradeAtlasPayload::handleClient);

        // 商路图册操作（客户端→服务端）
        registrar.playToServer(TradeAtlasActionPayload.TYPE, TradeAtlasActionPayload.STREAM_CODEC,
            TradeAtlasActionPayload::handleServer);
    }
}
