package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 创造模式标签页注册类
 */
public final class RRCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RuralRoutes.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RURAL_ROUTES =
        CREATIVE_MODE_TABS.register("rural_routes", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ruralroutes"))
            .icon(() -> new ItemStack(RRItems.TRADE_STATION.get()))
            .displayItems((parameters, output) -> {
                // 添加所有方块物品
                output.accept(RRItems.TRADE_STATION.get());
                output.accept(RRItems.DISPLAY_CASE.get());
                output.accept(RRItems.RUMOR_BOARD.get());
                // 配置工具（开发者工具）
                output.accept(RRItems.CONFIG_TOOL.get());
            })
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}