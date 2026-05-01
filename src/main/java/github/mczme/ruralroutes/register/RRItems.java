package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.item.ConfigToolItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 物品注册类
 */
public final class RRItems {

    private static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(RuralRoutes.MODID);

    /** 所有注册物品的列表，用于创造模式标签页 */
    private static final List<Supplier<? extends Item>> ALL_ITEMS = new ArrayList<>();

    /**
     * 注册物品并添加到创造模式标签页列表
     */
    private static <T extends Item> DeferredItem<T> register(String name, Function<ResourceLocation, T> factory) {
        DeferredItem<T> item = ITEMS.register(name, factory);
        ALL_ITEMS.add(item);
        return item;
    }

    /**
     * 注册方块物品并添加到创造模式标签页列表
     */
    private static DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<?> block) {
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(name, block);
        ALL_ITEMS.add(item);
        return item;
    }

    /**
     * 获取所有注册物品的列表
     */
    public static List<Supplier<? extends Item>> getAllItems() {
        return ALL_ITEMS;
    }

    // 方块物品
    public static final DeferredItem<BlockItem> TRADE_STATION =
        registerBlockItem("trade_station", RRBlocks.TRADE_STATION);

    public static final DeferredItem<BlockItem> DISPLAY_CASE =
        registerBlockItem("display_case", RRBlocks.DISPLAY_CASE);

    public static final DeferredItem<BlockItem> RUMOR_BOARD =
        registerBlockItem("rumor_board", RRBlocks.RUMOR_BOARD);

    // 工具
    public static final DeferredItem<ConfigToolItem> CONFIG_TOOL =
        register("config_tool", id -> new ConfigToolItem(new Item.Properties()));

    // 货币物品
    public static final DeferredItem<Item> COPPER_COIN =
        register("copper_coin", id -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_COIN =
        register("iron_coin", id -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GOLD_COIN =
        register("gold_coin", id -> new Item(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}