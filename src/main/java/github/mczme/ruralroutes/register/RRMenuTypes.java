package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.menu.ConfigToolMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
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
            net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(ConfigToolMenu::new));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}