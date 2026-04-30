package github.mczme.ruralroutes.network;

import github.mczme.ruralroutes.RuralRoutes;
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

        // 注册配置工具应用主题的网络包（客户端到服务端）
        registrar.playToServer(ConfigToolApplyPayload.TYPE, ConfigToolApplyPayload.STREAM_CODEC,
            ConfigToolApplyPayload::handleServer);
    }
}