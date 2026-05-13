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
        add("ruralroutes.configuration.title", "Rural Routes Configuration");
        add("ruralroutes.configuration.section.ruralroutes.common.toml", "Rural Routes");
        add("ruralroutes.configuration.section.ruralroutes.common.toml.title", "Rural Routes Configuration");
        add("ruralroutes.configuration.values", "Values");
        add("ruralroutes.configuration.values.button", "Values");
        add("ruralroutes.configuration.values.tooltip", "Value-related settings");
        add("ruralroutes.configuration.default", "Default Value");
        add("ruralroutes.configuration.default.tooltip", "Default value for items without defined value rules");

        // 贸易周期配置
        add("ruralroutes.configuration.cycle", "Trade Cycle");
        add("ruralroutes.configuration.cycle.button", "Trade Cycle");
        add("ruralroutes.configuration.cycle.tooltip", "Trade cycle refresh settings");
        add("ruralroutes.configuration.days", "Cycle Length (Days)");
        add("ruralroutes.configuration.days.tooltip", "Length of trade cycle in game days (1-30)");
        add("ruralroutes.configuration.refresh_time", "Refresh Time");
        add("ruralroutes.configuration.refresh_time.tooltip", "Time of day when trade cycle refreshes");

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
        add("gui.ruralroutes.config_tool.paste_node_info", "Paste Node Info: %s");
        add("gui.ruralroutes.config_tool.copied_node_info", "Clipboard: %s");
        add("gui.ruralroutes.config_tool.copy_node_info", "Copy Node Info: %s");
        add("gui.ruralroutes.config_tool.copy_action", "Copy Node");
        add("gui.ruralroutes.config_tool.paste_action", "Paste Node");
        add("gui.ruralroutes.config_tool.section.block_info", "Target Info");
        add("gui.ruralroutes.config_tool.section.current_config", "Current Setup");
        add("gui.ruralroutes.config_tool.section.clipboard", "Clipboard");
        add("gui.ruralroutes.config_tool.section.available_themes", "Themes");
        add("gui.ruralroutes.config_tool.section.link_target", "Linking");
        add("gui.ruralroutes.config_tool.no_selection", "Choose a theme from the list before applying");
        add("gui.ruralroutes.config_tool.clipboard_empty", "No node info is stored in the clipboard yet");
        add("gui.ruralroutes.config_tool.clipboard_source", "Source station: %s");
        add("gui.ruralroutes.config_tool.paste_ready", "The clipboard node can be linked to this block");
        add("gui.ruralroutes.config_tool.paste_missing", "Copy a node from a trade station before pasting here");
        add("gui.ruralroutes.config_tool.theme_biome", "Biome: %s");
        add("gui.ruralroutes.config_tool.theme_list_empty", "No themes loaded");
        add("gui.ruralroutes.config_tool.unknown_block", "Unknown Block");

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
        add("block.ruralroutes.display_case.displaying", "Displaying: %s");
        add("block.ruralroutes.display_case.empty", "No display item");

        // 传闻板交互
        add("block.ruralroutes.rumor_board.not_activated", "Please activate the trade station first");
        add("block.ruralroutes.rumor_board.mismatch", "Rumor board data mismatch");

        // 传闻板 GUI
        add("gui.ruralroutes.rumor_board.refresh_in", "Refreshes in about %s");
        add("gui.ruralroutes.rumor_board.time_days", "%d days");
        add("gui.ruralroutes.rumor_board.time_hours", "%d hours");
        add("gui.ruralroutes.rumor_board.time_soon", "soon");

        // 交易结果
        add("trade.success", "Trade successful");
        add("trade.fail.player_insufficient", "Player does not meet trade conditions");
        add("trade.fail.village_insufficient", "Village stock does not meet trade conditions");
        add("trade.fail.invalid_request", "Invalid trade request");

        // Contract trade
        add("gui.ruralroutes.trade.currency_payment", "Currency Payment");
        add("gui.ruralroutes.trade.currency_reward", "Currency Reward");

        // 交易站交易结果
        add("gui.ruralroutes.trade_station.success", "Trade successful!");
        add("gui.ruralroutes.trade_station.error.no_data", "Unable to get village data");

        // Trade card UI
        add("gui.ruralroutes.trade_card.stock", "Stock %d");
        add("gui.ruralroutes.trade_card.can_buy", "Buy %d");
        add("gui.ruralroutes.trade_card.price_per", "/ea");
        add("gui.ruralroutes.trade_card.tooltip.stock", "Stock: %d");
        add("gui.ruralroutes.trade_card.tooltip.can_buy", "Can buy: %d");
        add("gui.ruralroutes.trade_card.tooltip.price", "Price:");
        add("gui.ruralroutes.trade_card.tooltip.need", "Need:");
        add("gui.ruralroutes.trade_card.empty_slot", "Empty slot");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "Rural Routes");

        // ===== Rumor System =====

        // Rumor templates
        add("rumor.template.1", "%s prices %s in %s");
        add("rumor.template.2", "%s prices %s around %s");
        add("rumor.template.3", "Heard that %s prices %s in %s");
        add("rumor.template.4", "%s prices %s, %s affected");
        add("rumor.template.5", "Trade route rumor: %s prices %s in %s");

        // Direction
        add("rumor.direction.up", "risen");
        add("rumor.direction.down", "fallen");

        // Scope
        add("rumor.scope.global", "everywhere");
        add("rumor.scope.biome", "%s area");
        add("rumor.scope.theme", "%s");

        // Gossip
        add("rumor.gossip.1", "All's quiet lately, no special news.");
        add("rumor.gossip.2", "Nothing new on the trade routes.");
        add("rumor.gossip.3", "Prices are stable across villages.");

        // Tag translations
        add("ruralroutes.tag.minecraft.planks", "Plank items");
        add("ruralroutes.tag.minecraft.crops", "Crop items");
    }
}
