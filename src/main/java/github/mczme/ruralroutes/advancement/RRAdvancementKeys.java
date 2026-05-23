package github.mczme.ruralroutes.advancement;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.resources.ResourceLocation;

/**
 * 统一管理模组进度 ID。
 */
public final class RRAdvancementKeys {

    public static final ResourceLocation ROOT = id("root");
    public static final ResourceLocation FIRST_TRADE_STATION = id("main/first_trade_station");
    public static final ResourceLocation FIRST_TRADE = id("main/first_trade");
    public static final ResourceLocation BARTER_TRADE = id("main/barter_trade");
    public static final ResourceLocation COIN_EXCHANGE = id("main/coin_exchange");

    public static final ResourceLocation OPEN_RUMOR_BOARD = id("side/open_rumor_board");
    public static final ResourceLocation OPEN_DISPLAY_CASE = id("side/open_display_case");
    public static final ResourceLocation ENTER_DIFFERENT_VILLAGE_STYLES = id("travel/enter_different_village_styles");
    public static final ResourceLocation ENTER_ALL_VILLAGE_THEMES = id("travel/enter_all_village_themes");

    public static final ResourceLocation GET_COPPER_COIN = id("currency/get_copper_coin");
    public static final ResourceLocation GET_IRON_COIN = id("currency/get_iron_coin");
    public static final ResourceLocation GET_GOLD_COIN = id("currency/get_gold_coin");
    public static final ResourceLocation BIG_SPENDER = id("currency/big_spender");

    public static final ResourceLocation TRADE_10_TIMES = id("challenge/trade_10_times");
    public static final ResourceLocation TRADE_100_TIMES = id("challenge/trade_100_times");

    private RRAdvancementKeys() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, path);
    }
}
