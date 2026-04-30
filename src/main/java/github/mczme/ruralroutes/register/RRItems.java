package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.item.ConfigToolItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/**
 * 物品注册类
 */
public final class RRItems {

    private static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(RuralRoutes.MODID);

    public static final DeferredItem<BlockItem> TRADE_STATION =
        ITEMS.registerSimpleBlockItem("trade_station", RRBlocks.TRADE_STATION);

    public static final DeferredItem<BlockItem> DISPLAY_CASE =
        ITEMS.registerSimpleBlockItem("display_case", RRBlocks.DISPLAY_CASE);

    public static final DeferredItem<BlockItem> RUMOR_BOARD =
        ITEMS.registerSimpleBlockItem("rumor_board", RRBlocks.RUMOR_BOARD);

    public static final DeferredItem<ConfigToolItem> CONFIG_TOOL =
        ITEMS.register("config_tool",
            id -> new ConfigToolItem(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}