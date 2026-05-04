package github.mczme.ruralroutes.data.lang;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.register.RRBlocks;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 英文语言提供器
 */
public class RREnUsLanguageProvider extends LanguageProvider {

    public RREnUsLanguageProvider(PackOutput output) {
        super(output, RuralRoutes.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // 配置翻译
        add("ruralroutes.configuration.values", "Values");
        add("ruralroutes.configuration.values.button", "Values");
        add("ruralroutes.configuration.values.tooltip", "Value-related settings");
        add("ruralroutes.configuration.default", "Default Value");
        add("ruralroutes.configuration.default.tooltip", "Default value for items without defined value rules");

        // 方块翻译
        addBlock(RRBlocks.TRADE_STATION, "Trade Station");
        addBlock(RRBlocks.DISPLAY_CASE, "Display Case");
        addBlock(RRBlocks.RUMOR_BOARD, "Rumor Board");

        // 物品翻译
        addItem(RRItems.CONFIG_TOOL, "Config Tool");
        add("item.ruralroutes.config_tool.tooltip", "Developer tool for setting trade station theme");

        // 货币
        addItem(RRItems.COPPER_COIN, "Copper Coin");
        addItem(RRItems.IRON_COIN, "Iron Coin");
        addItem(RRItems.GOLD_COIN, "Gold Coin");
        add("item.ruralroutes.config_tool.no_theme", "No theme set. Available themes: %s");
        add("item.ruralroutes.config_tool.current_theme", "Current theme: %s");

        // GUI 翻译
        add("gui.ruralroutes.config_tool.title", "Theme Configuration");
        add("gui.ruralroutes.config_tool.apply", "Apply");
        add("gui.ruralroutes.config_tool.cancel", "Cancel");
        add("gui.ruralroutes.config_tool.close", "Close");
        add("gui.ruralroutes.config_tool.selected", "Selected: %s");
        add("gui.ruralroutes.config_tool.current_theme", "Current: %s");
        add("gui.ruralroutes.config_tool.no_current_theme", "No theme set");
        add("gui.ruralroutes.config_tool.node_id", "Node ID: %s");
        add("gui.ruralroutes.config_tool.no_node_id", "Node ID: Not activated");
        add("gui.ruralroutes.config_tool.type.trade_station", "Type: Trade Station");
        add("gui.ruralroutes.config_tool.type.display_case", "Type: Display Case");
        add("gui.ruralroutes.config_tool.type.rumor_board", "Type: Rumor Board");
        add("gui.ruralroutes.config_tool.type.unknown", "Type: Unknown");

        // 贸易站 GUI
        add("gui.ruralroutes.trade_station.theme", "Theme: %s");
        add("gui.ruralroutes.trade_station.sell", "Selling");
        add("gui.ruralroutes.trade_station.buy", "Buying");
        add("gui.ruralroutes.trade_station.confirm", "Confirm Trade");
        add("gui.ruralroutes.trade_station.coin_exchange", "Coin Exchange");
        add("gui.ruralroutes.trade_station.trade_area", "Trade Area");
        add("gui.ruralroutes.trade_station.want_area", "You Want");
        add("gui.ruralroutes.trade_station.pay_area", "You Pay");
        add("block.ruralroutes.trade_station.mismatch", "Trade station data mismatch");

        // 展示柜交互
        add("block.ruralroutes.display_case.not_activated", "Please activate the trade station first");
        add("block.ruralroutes.display_case.mismatch", "Display case data mismatch");
        add("block.ruralroutes.display_case.sell_items", "Village Selling Items (%d kinds)");

        // 传闻板交互
        add("block.ruralroutes.rumor_board.not_activated", "Please activate the trade station first");
        add("block.ruralroutes.rumor_board.mismatch", "Rumor board data mismatch");
        add("block.ruralroutes.rumor_board.no_news", "Trade route news will arrive next cycle");

        // 交易结果
        add("trade.success", "Trade successful");
        add("trade.fail.value_mismatch", "Value mismatch");
        add("trade.fail.player_insufficient", "Player does not meet trade conditions");
        add("trade.fail.village_insufficient", "Village stock does not meet trade conditions");
        add("trade.fail.invalid_request", "Invalid trade request");

        // 交易站交易结果
        add("gui.ruralroutes.trade_station.success", "Trade successful!");
        add("gui.ruralroutes.trade_station.error.no_data", "Unable to get village data");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "Rural Routes");
    }
}