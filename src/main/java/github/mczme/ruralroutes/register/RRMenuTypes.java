package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.menu.ConfigToolMenu;
import github.mczme.ruralroutes.menu.RumorBoardMenu;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 菜单类型注册类
 */
public final class RRMenuTypes {

    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(Registries.MENU, RuralRoutes.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ConfigToolMenu>> CONFIG_TOOL =
        MENU_TYPES.register("config_tool", () ->
            IMenuTypeExtension.create(ConfigToolMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<TradeStationMenu>> TRADE_STATION =
        MENU_TYPES.register("trade_station", () ->
            IMenuTypeExtension.create(TradeStationMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RumorBoardMenu>> RUMOR_BOARD =
        MENU_TYPES.register("rumor_board", () ->
            IMenuTypeExtension.create(RumorBoardMenu::new));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}