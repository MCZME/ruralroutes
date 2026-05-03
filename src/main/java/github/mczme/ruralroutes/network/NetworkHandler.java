package github.mczme.ruralroutes.network;

import github.mczme.ruralroutes.RuralRoutes;
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

        // 贸易槽位同步（服务端→客户端）
        registrar.playToClient(TradeSlotSyncPayload.TYPE, TradeSlotSyncPayload.STREAM_CODEC,
            TradeSlotSyncPayload::handleClient);
    }
}