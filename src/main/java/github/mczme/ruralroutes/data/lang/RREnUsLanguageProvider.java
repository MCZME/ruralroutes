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
        add("ruralroutes.configuration.time_mode", "Time Mode");
        add("ruralroutes.configuration.time_mode.tooltip", "server_time: based on server runtime; game_time: based on in-game day/night cycle");
        add("ruralroutes.configuration.days", "Cycle Length (Days)");
        add("ruralroutes.configuration.days.tooltip", "Length of trade cycle in game days (1-30)");
        add("ruralroutes.configuration.refresh_time", "Refresh Time");
        add("ruralroutes.configuration.refresh_time.tooltip", "Time of day when trade cycle refreshes");

        // 市场系统配置
        add("ruralroutes.configuration.market", "Market");
        add("ruralroutes.configuration.market.button", "Market");
        add("ruralroutes.configuration.market.tooltip", "Market system settings");
        add("ruralroutes.configuration.enabled", "Market Enabled");
        add("ruralroutes.configuration.enabled.tooltip", "Enable market events that affect item prices and stock baselines each trade cycle");
        add("ruralroutes.configuration.rule_pick_count", "Rule Pick Count");
        add("ruralroutes.configuration.rule_pick_count.tooltip", "Number of market event rules selected per cycle (1-20)");
        add("ruralroutes.configuration.max_delta", "Max Price Delta");
        add("ruralroutes.configuration.max_delta.tooltip", "Maximum price adjustment percentage after market event stacking (0.1-1.0)");
        add("ruralroutes.configuration.max_stock_delta", "Max Stock Delta");
        add("ruralroutes.configuration.max_stock_delta.tooltip", "Maximum stock adjustment percentage after market event stacking (0.0-1.0)");

        // 方块翻译
        addBlock(RRBlocks.TRADE_STATION, "Trade Station");
        addBlock(RRBlocks.DISPLAY_CASE, "Display Case");
        addBlock(RRBlocks.RUMOR_BOARD, "Rumor Board");

        // 物品翻译
        addItem(RRItems.CONFIG_TOOL, "Config Tool");
        add("item.ruralroutes.config_tool.tooltip", "Developer tool for setting trade station themes and block styles");
        addItem(RRItems.NODE_DATA_VIEWER, "Node Data Viewer");
        add("item.ruralroutes.node_data_viewer.tooltip", "Developer tool for inspecting node data snapshots from core blocks");

        // 货币
        addItem(RRItems.COPPER_COIN, "Copper Coin");
        addItem(RRItems.IRON_COIN, "Iron Coin");
        addItem(RRItems.GOLD_COIN, "Gold Coin");
        add("item.ruralroutes.config_tool.no_theme", "No theme set. Available themes: %s");
        add("item.ruralroutes.config_tool.current_theme", "Current theme: %s");

        // GUI 翻译
        add("gui.ruralroutes.config_tool.title", "Theme & Style Configuration");
        add("gui.ruralroutes.config_tool.apply", "Apply");
        add("gui.ruralroutes.config_tool.cancel", "Cancel");
        add("gui.ruralroutes.config_tool.close", "Close");
        add("gui.ruralroutes.config_tool.selected", "Selected: %s");
        add("gui.ruralroutes.config_tool.selected_style", "Selected Style: %s");
        add("gui.ruralroutes.config_tool.current_theme", "Current: %s");
        add("gui.ruralroutes.config_tool.no_current_theme", "No theme set");
        add("gui.ruralroutes.config_tool.current_style", "Current Style: %s");
        add("gui.ruralroutes.config_tool.no_current_style", "Current Style: Not set");
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
        add("gui.ruralroutes.config_tool.section.available_styles", "Styles");
        add("gui.ruralroutes.config_tool.section.link_target", "Linking");
        add("gui.ruralroutes.config_tool.no_selection", "Choose a theme from the list before applying");
        add("gui.ruralroutes.config_tool.no_style_selection", "Choose a style from the list before applying");
        add("gui.ruralroutes.config_tool.clipboard_empty", "No node info is stored in the clipboard yet");
        add("gui.ruralroutes.config_tool.clipboard_source", "Source station: %s");
        add("gui.ruralroutes.config_tool.paste_ready", "The clipboard node can be linked to this block");
        add("gui.ruralroutes.config_tool.paste_missing", "Copy a node from a trade station before pasting here");
        add("gui.ruralroutes.config_tool.theme_biome", "Biome: %s");
        add("gui.ruralroutes.config_tool.theme_list_empty", "No themes loaded");
        add("gui.ruralroutes.config_tool.unknown_block", "Unknown Block");
        add("gui.ruralroutes.config_tool.style.plains", "Plains Style");
        add("gui.ruralroutes.config_tool.style.desert", "Desert Style");
        add("gui.ruralroutes.config_tool.style.savanna", "Savanna Style");
        add("gui.ruralroutes.config_tool.style.taiga", "Taiga Style");
        add("gui.ruralroutes.config_tool.style.snowy", "Snowy Style");

        // Node data viewer GUI
        add("gui.ruralroutes.node_data_viewer.title", "Node Data Viewer");
        add("gui.ruralroutes.node_data_viewer.target.trade_station", "Trade Station");
        add("gui.ruralroutes.node_data_viewer.target.display_case", "Display Case");
        add("gui.ruralroutes.node_data_viewer.target.rumor_board", "Rumor Board");
        add("gui.ruralroutes.node_data_viewer.section.summary", "Summary");
        add("gui.ruralroutes.node_data_viewer.section.sell_items", "Sell Items");
        add("gui.ruralroutes.node_data_viewer.section.buy_items", "Buy Items");
        add("gui.ruralroutes.node_data_viewer.section.specialties", "Specialties");
        add("gui.ruralroutes.node_data_viewer.section.stocks", "Stocks");
        add("gui.ruralroutes.node_data_viewer.summary.trade_node_id", "Trade Node ID");
        add("gui.ruralroutes.node_data_viewer.summary.theme", "Theme");
        add("gui.ruralroutes.node_data_viewer.summary.refresh_timestamp", "Refresh Timestamp");
        add("gui.ruralroutes.node_data_viewer.stock_pair", "%s/%s");
        add("gui.ruralroutes.node_data_viewer.empty", "No node data is available for this target");
        add("gui.ruralroutes.node_data_viewer.empty_list", "None");
        add("gui.ruralroutes.node_data_viewer.footer", "Scroll to browse, press Esc or Inventory to close");
        add("gui.ruralroutes.node_data_viewer.status.missing_station", "This block is not linked to a trade station yet");
        add("gui.ruralroutes.node_data_viewer.status.missing_node_data", "The target does not have commercial node data yet");
        add("gui.ruralroutes.node_data_viewer.missing_item", "Missing item: %s");

        // 贸易站 GUI
        add("gui.ruralroutes.trade_station.theme", "Theme: %s");
        add("gui.ruralroutes.trade_station.sell", "Selling");
        add("gui.ruralroutes.trade_station.buy", "Buying");
        add("gui.ruralroutes.trade_station.confirm", "Confirm Trade");
        add("gui.ruralroutes.trade_station.coin_exchange", "Currency Exchange");
        add("gui.ruralroutes.trade_station.coin_exchange.hint", "Click a currency to choose an exchange");
        add("gui.ruralroutes.trade_station.trade_area", "Trade Area");
        add("gui.ruralroutes.trade_station.want_area", "You Want");
        add("gui.ruralroutes.trade_station.pay_area", "You Pay");
        add("gui.ruralroutes.trade_station.delete", "Delete");
        add("gui.ruralroutes.trade_station.delete_active", "Deleting");
        add("gui.ruralroutes.trade_station.empty_hint", "Click items above to add");
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
        add("gui.ruralroutes.rumor_board.family.shortage", "Short");
        add("gui.ruralroutes.rumor_board.family.surplus", "Surplus");
        add("gui.ruralroutes.rumor_board.family.demand", "Demand");
        add("gui.ruralroutes.rumor_board.family.release", "Supply");
        add("gui.ruralroutes.rumor_board.family.gossip", "Quiet");
        add("gui.ruralroutes.rumor_board.scope.global", "All");
        add("gui.ruralroutes.rumor_board.scope.biome", "Biome");
        add("gui.ruralroutes.rumor_board.scope.theme", "Theme");
        add("gui.ruralroutes.rumor_board.page", "%s/%s");

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
        add("gui.ruralroutes.trade_station.error.cycle_changed", "Trade cycle refreshed. Please choose items again.");
        add("gui.ruralroutes.trade_station.coin_exchange.success", "Currency exchange complete!");
        add("gui.ruralroutes.trade_station.coin_exchange.fail.player_insufficient", "You do not have enough currency for this exchange");
        add("gui.ruralroutes.trade_station.coin_exchange.fail.village_insufficient", "The village does not have enough reserve for this exchange");
        add("gui.ruralroutes.trade_station.coin_exchange.popup.exchange", "Exchange");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.player_count", "You have: %d");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.village_count", "Village reserve: %d");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.max_trades", "Max exchanges: %d");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.open", "Click to view available exchanges");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.player_insufficient", "Not enough currency in your inventory");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.village_insufficient", "Village reserve is too low");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.single", "Click: exchange once");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.batch", "Shift+Click: exchange as much as possible");

        // Trade card UI
        add("gui.ruralroutes.trade_card.stock", "Stock %d");
        add("gui.ruralroutes.trade_card.can_buy", "Buy %d");
        add("gui.ruralroutes.trade_card.price_per", "/ea");
        add("gui.ruralroutes.trade_card.tooltip.stock", "Stock: %d");
        add("gui.ruralroutes.trade_card.tooltip.can_buy", "Can buy: %d");
        add("gui.ruralroutes.trade_card.tooltip.selected", "Selected: %d");
        add("gui.ruralroutes.trade_card.tooltip.price", "Price:");
        add("gui.ruralroutes.trade_card.tooltip.need", "Need:");
        add("gui.ruralroutes.trade_card.tooltip.shift_scroll", "Shift+Scroll: Adjust amount");
        add("gui.ruralroutes.trade_card.tooltip.shift_click", "Shift+Left Click: Add all");
        add("gui.ruralroutes.trade_card.empty_slot", "Empty slot");
        add("gui.ruralroutes.trade_station.tooltip.remove_one", "Left Click: Remove 1");
        add("gui.ruralroutes.trade_station.tooltip.clear_entry", "Shift+Left Click: Clear entry");
        add("gui.ruralroutes.trade_station.tooltip.clear_all", "Shift+Left Click Delete: Clear trade list");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "Rural Routes");

        // Advancements
        add("advancements.ruralroutes.root.title", "Enter a Village");
        add("advancements.ruralroutes.root.description", "Step into a village for the first time");
        add("advancements.ruralroutes.main.first_trade_station.title", "First Trade Station");
        add("advancements.ruralroutes.main.first_trade_station.description", "Find and open a trade station in a village");
        add("advancements.ruralroutes.main.first_trade.title", "First Business");
        add("advancements.ruralroutes.main.first_trade.description", "Complete your first trade at a trade station");
        add("advancements.ruralroutes.main.barter_trade.title", "Barter");
        add("advancements.ruralroutes.main.barter_trade.description", "Complete a fixed contract trade");
        add("advancements.ruralroutes.main.coin_exchange.title", "Coin Exchange");
        add("advancements.ruralroutes.main.coin_exchange.description", "Complete your first coin exchange");
        add("advancements.ruralroutes.side.open_rumor_board.title", "Open the Rumor Board");
        add("advancements.ruralroutes.side.open_rumor_board.description", "Read the rumor board in a village");
        add("advancements.ruralroutes.side.open_display_case.title", "Open the Display Case");
        add("advancements.ruralroutes.side.open_display_case.description", "Inspect the display case in a village");
        add("advancements.ruralroutes.side.buy_specialty.title", "Buy a Specialty");
        add("advancements.ruralroutes.side.buy_specialty.description", "Buy a specialty from a village");
        add("advancements.ruralroutes.side.collector.title", "Collector");
        add("advancements.ruralroutes.side.collector.description", "Purchase 3 different village specialties");
        add("advancements.ruralroutes.travel.enter_different_village_styles.title", "Far and Wide");
        add("advancements.ruralroutes.travel.enter_different_village_styles.description", "Find trade stations in villages of all five styles");
        add("advancements.ruralroutes.travel.enter_all_village_themes.title", "Know Every Road");
        add("advancements.ruralroutes.travel.enter_all_village_themes.description", "Find trade stations in villages of every theme");
        add("advancements.ruralroutes.currency.get_copper_coin.title", "Get a Copper Coin");
        add("advancements.ruralroutes.currency.get_copper_coin.description", "Obtain a copper coin for the first time");
        add("advancements.ruralroutes.currency.get_iron_coin.title", "Get an Iron Coin");
        add("advancements.ruralroutes.currency.get_iron_coin.description", "Obtain an iron coin for the first time");
        add("advancements.ruralroutes.currency.get_gold_coin.title", "Get a Gold Coin");
        add("advancements.ruralroutes.currency.get_gold_coin.description", "Obtain a gold coin for the first time");
        add("advancements.ruralroutes.currency.big_spender.title", "Big Spender");
        add("advancements.ruralroutes.currency.big_spender.description", "Close a deal worth 300 in a single trade");
        add("advancements.ruralroutes.challenge.trade_10_times.title", "Regular");
        add("advancements.ruralroutes.challenge.trade_10_times.description", "Complete 10 successful trades");
        add("advancements.ruralroutes.challenge.trade_100_times.title", "Patron");
        add("advancements.ruralroutes.challenge.trade_100_times.description", "Complete 100 successful trades");

        // ===== Rumor System =====

        // Rumor templates: shortage
        add("rumor.shortage.global.1", "Folks everywhere are scrambling for %s lately.");
        add("rumor.shortage.global.2", "%s has been tight all over lately.");
        add("rumor.shortage.biome.1", "Word is folks around %s have been short on %s.");
        add("rumor.shortage.biome.2", "People around %s have been snapping up %s.");
        add("rumor.shortage.theme.1", "%s has been short on %s lately.");
        add("rumor.shortage.theme.2", "Heard %s is looking for %s.");

        // Rumor templates: surplus
        add("rumor.surplus.global.1", "%s has been plentiful everywhere lately.");
        add("rumor.surplus.global.2", "There's plenty of %s on the roads right now.");
        add("rumor.surplus.biome.1", "%s has had plenty of %s moving lately.");
        add("rumor.surplus.biome.2", "Word is %s has %s piled up.");
        add("rumor.surplus.theme.1", "%s has had plenty of %s lately.");
        add("rumor.surplus.theme.2", "%s has been letting %s go.");

        // Rumor templates: demand
        add("rumor.demand.global.1", "%s has been selling fast everywhere lately.");
        add("rumor.demand.global.2", "%s is in demand across the routes right now.");
        add("rumor.demand.biome.1", "Heard folks around %s are chasing %s.");
        add("rumor.demand.biome.2", "%s has been hungry for %s lately.");
        add("rumor.demand.theme.1", "%s has been paying well for %s lately.");
        add("rumor.demand.theme.2", "Heard %s has its eye on %s.");

        // Rumor templates: release
        add("rumor.release.global.1", "Fresh batches of %s have been showing up everywhere.");
        add("rumor.release.global.2", "There's been a lot of %s arriving lately.");
        add("rumor.release.biome.1", "Around %s, fresh %s has been turning up lately.");
        add("rumor.release.biome.2", "Heard %s just let out more %s.");
        add("rumor.release.theme.1", "%s has just released a fresh batch of %s.");
        add("rumor.release.theme.2", "Heard %s has new %s on hand.");

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

        // Rumor target aliases
        add("rumor.target.food", "provisions");
        add("rumor.target.crop", "farm produce");
        add("rumor.target.wood", "timber");
        add("rumor.target.stone", "stone goods");
        add("rumor.target.mineral", "ore");
        add("rumor.target.dye", "dyes");
        add("rumor.target.decor", "decor goods");
        add("rumor.target.paper", "paper stock");
        add("rumor.target.terracotta", "terracotta wares");
        add("rumor.target.leather_fiber", "hides");
        add("rumor.target.ice_snow", "ice goods");
        add("rumor.target.nether_goods", "Nether goods");
        add("rumor.target.ocean_goods", "sea goods");
        add("rumor.target.end_goods", "End goods");
        add("rumor.target.precious", "valuables");

        // Theme translations
        add("ruralroutes.theme.plains_granary", "Plains Granary");
        add("ruralroutes.theme.plains_pasture", "Pasture Market");
        add("ruralroutes.theme.plains_workshop", "Plains Workshop");
        add("ruralroutes.theme.desert_quarry", "Desert Quarry");
        add("ruralroutes.theme.desert_oasis", "Oasis Farm");
        add("ruralroutes.theme.desert_dyeworks", "Desert Dyeworks");
        add("ruralroutes.theme.savanna_woodworks", "Acacia Lumberyard");
        add("ruralroutes.theme.savanna_terracotta", "Terracotta Kiln");
        add("ruralroutes.theme.savanna_herder", "Highland Ranch");
        add("ruralroutes.theme.taiga_lumber", "Spruce Lumber Camp");
        add("ruralroutes.theme.taiga_berries", "Berry Hamlet");
        add("ruralroutes.theme.taiga_fur", "Fur Waystation");
        add("ruralroutes.theme.snowy_iceworks", "Iceworks");
        add("ruralroutes.theme.snowy_waystation", "Snowy Waystation");
        add("ruralroutes.theme.snowy_hunter", "Polar Hunters");
    }
}
