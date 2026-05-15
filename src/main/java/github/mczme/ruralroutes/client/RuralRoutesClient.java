package github.mczme.ruralroutes.client;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.client.gui.screen.ConfigToolScreen;
import github.mczme.ruralroutes.client.gui.screen.TradeStationScreen;
import github.mczme.ruralroutes.client.renderer.blockentity.DisplayCaseBlockEntityRenderer;
import github.mczme.ruralroutes.register.RRBlockEntities;
import github.mczme.ruralroutes.register.RRMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 客户端初始化
 */
@EventBusSubscriber(modid = RuralRoutes.MODID, value = Dist.CLIENT)
public class RuralRoutesClient {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RRMenuTypes.CONFIG_TOOL.get(), ConfigToolScreen::new);
        event.register(RRMenuTypes.TRADE_STATION.get(), TradeStationScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RRBlockEntities.DISPLAY_CASE.get(), DisplayCaseBlockEntityRenderer::new);
    }
}
