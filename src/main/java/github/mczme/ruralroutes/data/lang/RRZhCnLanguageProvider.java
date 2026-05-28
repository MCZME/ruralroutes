package github.mczme.ruralroutes.data.lang;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.register.RRBlocks;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 简体中文语言提供器
 */
public class RRZhCnLanguageProvider extends LanguageProvider {

    public RRZhCnLanguageProvider(PackOutput output) {
        super(output, RuralRoutes.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        // 配置翻译
        add("ruralroutes.configuration.title", "乡野商路配置");
        add("ruralroutes.configuration.section.ruralroutes.common.toml", "乡野商路");
        add("ruralroutes.configuration.section.ruralroutes.common.toml.title", "乡野商路配置");
        add("ruralroutes.configuration.values", "价值设置");
        add("ruralroutes.configuration.values.button", "价值设置");
        add("ruralroutes.configuration.values.tooltip", "价值相关设置");
        add("ruralroutes.configuration.default", "默认价值");
        add("ruralroutes.configuration.default.tooltip", "未定义价值规则的物品使用的默认价值");

        // 贸易周期配置
        add("ruralroutes.configuration.cycle", "贸易周期");
        add("ruralroutes.configuration.cycle.button", "贸易周期");
        add("ruralroutes.configuration.cycle.tooltip", "贸易周期刷新设置");
        add("ruralroutes.configuration.time_mode", "时间模式");
        add("ruralroutes.configuration.time_mode.tooltip", "server_time: 基于服务器运行时间; game_time: 基于游戏内昼夜循环");
        add("ruralroutes.configuration.days", "周期长度（日）");
        add("ruralroutes.configuration.days.tooltip", "贸易周期长度，单位为游戏日（1-30）");
        add("ruralroutes.configuration.refresh_time", "刷新时刻");
        add("ruralroutes.configuration.refresh_time.tooltip", "贸易周期刷新的时刻");

        // 市场系统配置
        add("ruralroutes.configuration.market", "市场");
        add("ruralroutes.configuration.market.button", "市场");
        add("ruralroutes.configuration.market.tooltip", "市场系统设置");
        add("ruralroutes.configuration.enabled", "启用市场");
        add("ruralroutes.configuration.enabled.tooltip", "启用后每个贸易周期会生成市场事件影响物品价格与库存基线");
        add("ruralroutes.configuration.rule_pick_count", "规则选择数量");
        add("ruralroutes.configuration.rule_pick_count.tooltip", "每个贸易周期随机选择的市场事件规则数量（1-20）");
        add("ruralroutes.configuration.max_delta", "最大价格调整幅度");
        add("ruralroutes.configuration.max_delta.tooltip", "市场事件叠加后的最大价格调整百分比（0.1-1.0）");
        add("ruralroutes.configuration.max_stock_delta", "最大库存调整幅度");
        add("ruralroutes.configuration.max_stock_delta.tooltip", "市场事件叠加后的最大库存调整百分比（0.0-1.0）");

        // 方块翻译
        addBlock(RRBlocks.TRADE_STATION, "贸易站");
        addBlock(RRBlocks.DISPLAY_CASE, "展示柜");
        addBlock(RRBlocks.RUMOR_BOARD, "传闻板");

        // 物品翻译
        addItem(RRItems.CONFIG_TOOL, "配置工具");
        add("item.ruralroutes.config_tool.tooltip", "开发者工具，用于设置贸易站主题和方块外观");
        addItem(RRItems.NODE_DATA_VIEWER, "节点数据查看器");
        add("item.ruralroutes.node_data_viewer.tooltip", "开发者工具，用于查看核心方块对应的节点数据快照");
        addItem(RRItems.TRADE_ATLAS, "商路图册");
        add("item.ruralroutes.trade_atlas.tooltip", "打开图册并管理已知的村庄节点");

        // 货币
        addItem(RRItems.COPPER_COIN, "铜板");
        addItem(RRItems.IRON_COIN, "铁币");
        addItem(RRItems.GOLD_COIN, "金币");
        add("item.ruralroutes.config_tool.no_theme", "未设置主题。可用主题: %s");
        add("item.ruralroutes.config_tool.current_theme", "当前主题: %s");

        // GUI 翻译
        add("gui.ruralroutes.config_tool.title", "主题与外观配置");
        add("gui.ruralroutes.config_tool.apply", "应用");
        add("gui.ruralroutes.config_tool.cancel", "取消");
        add("gui.ruralroutes.config_tool.close", "关闭");
        add("gui.ruralroutes.config_tool.selected", "已选择: %s");
        add("gui.ruralroutes.config_tool.selected_style", "已选择外观: %s");
        add("gui.ruralroutes.config_tool.current_theme", "当前: %s");
        add("gui.ruralroutes.config_tool.no_current_theme", "未设置主题");
        add("gui.ruralroutes.config_tool.current_style", "当前外观: %s");
        add("gui.ruralroutes.config_tool.no_current_style", "当前外观: 未设置");
        add("gui.ruralroutes.config_tool.node_id", "节点ID: %s");
        add("gui.ruralroutes.config_tool.no_node_id", "节点ID: 未激活");
        add("gui.ruralroutes.config_tool.type.trade_station", "类型: 贸易站");
        add("gui.ruralroutes.config_tool.type.display_case", "类型: 展示柜");
        add("gui.ruralroutes.config_tool.type.rumor_board", "类型: 传闻板");
        add("gui.ruralroutes.config_tool.type.unknown", "类型: 未知");
        add("gui.ruralroutes.config_tool.paste_node_info", "粘贴节点信息: %s");
        add("gui.ruralroutes.config_tool.copied_node_info", "已复制节点: %s");
        add("gui.ruralroutes.config_tool.copy_node_info", "复制节点信息: %s");
        add("gui.ruralroutes.config_tool.copy_action", "复制节点");
        add("gui.ruralroutes.config_tool.paste_action", "粘贴节点");
        add("gui.ruralroutes.config_tool.section.block_info", "目标信息");
        add("gui.ruralroutes.config_tool.section.current_config", "当前配置");
        add("gui.ruralroutes.config_tool.section.clipboard", "剪贴板");
        add("gui.ruralroutes.config_tool.section.available_themes", "可用主题");
        add("gui.ruralroutes.config_tool.section.available_styles", "可用外观");
        add("gui.ruralroutes.config_tool.section.link_target", "关联说明");
        add("gui.ruralroutes.config_tool.no_selection", "点击右侧主题列表选择后再应用");
        add("gui.ruralroutes.config_tool.no_style_selection", "点击右侧外观列表选择后再应用");
        add("gui.ruralroutes.config_tool.clipboard_empty", "剪贴板中还没有节点信息");
        add("gui.ruralroutes.config_tool.clipboard_source", "来源贸易站: %s");
        add("gui.ruralroutes.config_tool.paste_ready", "可将剪贴板中的节点绑定到当前方块");
        add("gui.ruralroutes.config_tool.paste_missing", "先从贸易站复制节点后才能粘贴");
        add("gui.ruralroutes.config_tool.theme_biome", "群系: %s");
        add("gui.ruralroutes.config_tool.theme_list_empty", "没有加载到主题");
        add("gui.ruralroutes.config_tool.unknown_block", "未知方块");
        add("gui.ruralroutes.config_tool.style.plains", "平原风格");
        add("gui.ruralroutes.config_tool.style.desert", "沙漠风格");
        add("gui.ruralroutes.config_tool.style.savanna", "热带草原风格");
        add("gui.ruralroutes.config_tool.style.taiga", "针叶林风格");
        add("gui.ruralroutes.config_tool.style.snowy", "雪地风格");

        // 节点数据查看器 GUI
        add("gui.ruralroutes.node_data_viewer.title", "节点数据查看器");
        add("gui.ruralroutes.node_data_viewer.target.trade_station", "贸易站");
        add("gui.ruralroutes.node_data_viewer.target.display_case", "展示柜");
        add("gui.ruralroutes.node_data_viewer.target.rumor_board", "传闻板");
        add("gui.ruralroutes.node_data_viewer.section.summary", "概要");
        add("gui.ruralroutes.node_data_viewer.section.sell_items", "出售物品");
        add("gui.ruralroutes.node_data_viewer.section.buy_items", "收购物品");
        add("gui.ruralroutes.node_data_viewer.section.specialties", "特产");
        add("gui.ruralroutes.node_data_viewer.section.stocks", "库存");
        add("gui.ruralroutes.node_data_viewer.summary.trade_node_id", "节点 ID");
        add("gui.ruralroutes.node_data_viewer.summary.theme", "主题");
        add("gui.ruralroutes.node_data_viewer.summary.refresh_timestamp", "刷新时间戳");
        add("gui.ruralroutes.node_data_viewer.stock_pair", "%s/%s");
        add("gui.ruralroutes.node_data_viewer.empty", "当前没有可显示的节点数据");
        add("gui.ruralroutes.node_data_viewer.empty_list", "无");
        add("gui.ruralroutes.node_data_viewer.footer", "滚轮滚动，Esc 或背包键关闭");
        add("gui.ruralroutes.node_data_viewer.status.missing_station", "当前方块还没有关联贸易站");
        add("gui.ruralroutes.node_data_viewer.status.missing_node_data", "目标还没有商业节点数据");
        add("gui.ruralroutes.node_data_viewer.missing_item", "缺失物品: %s");

        // 商路图册 GUI
        add("gui.ruralroutes.trade_atlas.title", "商路图册");
        add("gui.ruralroutes.trade_atlas.first_entry_prompt", "请选择第一个村庄风格");
        add("gui.ruralroutes.trade_atlas.first_entry_hint", "第一次线索定位是免费的。");
        add("gui.ruralroutes.trade_atlas.first_entry.plains", "平原：稳定起步");
        add("gui.ruralroutes.trade_atlas.first_entry.desert", "沙漠：稀疏路线");
        add("gui.ruralroutes.trade_atlas.first_entry.savanna", "热带草原：开阔行程");
        add("gui.ruralroutes.trade_atlas.first_entry.taiga", "针叶林：木材路径");
        add("gui.ruralroutes.trade_atlas.first_entry.snowy", "雪地：远方驿路");
        add("gui.ruralroutes.trade_atlas.section.locate", "定位");
        add("gui.ruralroutes.trade_atlas.section.nodes", "节点");
        add("gui.ruralroutes.trade_atlas.section.map", "节点图");
        add("gui.ruralroutes.trade_atlas.section.detail", "详情");
        add("gui.ruralroutes.trade_atlas.empty", "当前还没有村庄节点");
        add("gui.ruralroutes.trade_atlas.action.locate", "定位");
        add("gui.ruralroutes.trade_atlas.action.close_locate", "收起定位");
        add("gui.ruralroutes.trade_atlas.action.set_target", "设为目标");
        add("gui.ruralroutes.trade_atlas.action.current_target", "当前目标");
        add("gui.ruralroutes.trade_atlas.action.clear_target", "取消目标");
        add("gui.ruralroutes.trade_atlas.action.center_selected", "居中");
        add("gui.ruralroutes.trade_atlas.detail.empty", "请选择一个节点查看");
        add("gui.ruralroutes.trade_atlas.detail.status", "状态");
        add("gui.ruralroutes.trade_atlas.detail.style", "风格");
        add("gui.ruralroutes.trade_atlas.detail.dimension", "维度");
        add("gui.ruralroutes.trade_atlas.detail.position", "坐标");
        add("gui.ruralroutes.trade_atlas.detail.target", "目标");
        add("gui.ruralroutes.trade_atlas.detail.yes", "是");
        add("gui.ruralroutes.trade_atlas.detail.no", "否");
        add("gui.ruralroutes.trade_atlas.detail.theme", "主题");
        add("gui.ruralroutes.trade_atlas.detail.invalid_hint", "需要现场重新确认");
        add("gui.ruralroutes.trade_atlas.map.zoom", "缩放 %s%%");
        add("gui.ruralroutes.trade_atlas.locate.choose_style", "选择风格");
        add("gui.ruralroutes.trade_atlas.locate.cost_pending", "成本：待接入");
        add("gui.ruralroutes.trade_atlas.locate.no_style", "请先选择村庄风格。");
        add("gui.ruralroutes.trade_atlas.locate.busy", "正在进行一次定位，请稍候。");
        add("gui.ruralroutes.trade_atlas.locate.pending_clue", "请先确认或放弃当前未确认线索。");
        add("gui.ruralroutes.trade_atlas.locate.failed", "没有找到对应的村庄结构。");
        add("gui.ruralroutes.trade_atlas.locate.duplicate", "这个村庄已经记录过了。");
        add("gui.ruralroutes.trade_atlas.locate.success", "已找到 %s 线索。");
        add("block.ruralroutes.trade_station.invalid_village", "这个贸易站附近没有有效村庄。");
        add("gui.ruralroutes.trade_atlas.status.clue", "线索");
        add("gui.ruralroutes.trade_atlas.status.recorded", "已记录");
        add("gui.ruralroutes.trade_atlas.status.invalid", "失效");

        // 贸易站 GUI
        add("gui.ruralroutes.trade_station.theme", "主题: %s");
        add("gui.ruralroutes.trade_station.sell", "出售");
        add("gui.ruralroutes.trade_station.buy", "收购");
        add("gui.ruralroutes.trade_station.confirm", "确认交换");
        add("gui.ruralroutes.trade_station.coin_exchange", "货币交换");
        add("gui.ruralroutes.trade_station.coin_exchange.hint", "点击货币选择交换");
        add("gui.ruralroutes.trade_station.trade_area", "交易区");
        add("gui.ruralroutes.trade_station.want_area", "玩家想要的");
        add("gui.ruralroutes.trade_station.pay_area", "玩家支付的");
        add("gui.ruralroutes.trade_station.delete", "删除");
        add("gui.ruralroutes.trade_station.delete_active", "删除中");
        add("gui.ruralroutes.trade_station.empty_hint", "点击上方卡片添加到交换清单");
        add("block.ruralroutes.trade_station.mismatch", "贸易站数据不匹配");

        // 展示柜交互
        add("block.ruralroutes.display_case.not_activated", "请先激活贸易站");
        add("block.ruralroutes.display_case.mismatch", "展示柜数据不匹配");
        add("block.ruralroutes.display_case.displaying", "展示: %s");
        add("block.ruralroutes.display_case.empty", "无展示物品");

        // 传闻板交互
        add("block.ruralroutes.rumor_board.not_activated", "请先激活贸易站");
        add("block.ruralroutes.rumor_board.mismatch", "传闻板数据不匹配");

        // 传闻板 GUI
        add("gui.ruralroutes.rumor_board.refresh_in", "约 %s 后刷新");
        add("gui.ruralroutes.rumor_board.time_days", "%d 天");
        add("gui.ruralroutes.rumor_board.time_hours", "%d 小时");
        add("gui.ruralroutes.rumor_board.time_soon", "即将刷新");
        add("gui.ruralroutes.rumor_board.family.shortage", "缺货");
        add("gui.ruralroutes.rumor_board.family.surplus", "余货");
        add("gui.ruralroutes.rumor_board.family.demand", "畅销");
        add("gui.ruralroutes.rumor_board.family.release", "放货");
        add("gui.ruralroutes.rumor_board.family.gossip", "闲谈");
        add("gui.ruralroutes.rumor_board.scope.global", "各地");
        add("gui.ruralroutes.rumor_board.scope.biome", "群系");
        add("gui.ruralroutes.rumor_board.scope.theme", "主题");
        add("gui.ruralroutes.rumor_board.page", "%s/%s");

        // 交易结果
        add("trade.success", "交易成功");
        add("trade.fail.player_insufficient", "玩家不满足交易条件");
        add("trade.fail.village_insufficient", "村庄库存不满足交易条件");
        add("trade.fail.invalid_request", "无效交易请求");

        // 契约交易
        add("gui.ruralroutes.trade.currency_payment", "货币支付");
        add("gui.ruralroutes.trade.currency_reward", "货币奖励");

        // 交易站交易结果
        add("gui.ruralroutes.trade_station.success", "交易成功！");
        add("gui.ruralroutes.trade_station.error.no_data", "无法获取村庄数据");
        add("gui.ruralroutes.trade_station.error.cycle_changed", "交易周期已刷新，请重新选择商品");
        add("gui.ruralroutes.trade_station.coin_exchange.success", "货币交换成功！");
        add("gui.ruralroutes.trade_station.coin_exchange.fail.player_insufficient", "你的货币不足，无法完成这次交换");
        add("gui.ruralroutes.trade_station.coin_exchange.fail.village_insufficient", "村庄储备不足，暂时无法完成这次交换");
        add("gui.ruralroutes.trade_station.coin_exchange.popup.exchange", "兑换");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.player_count", "你持有: %d");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.village_count", "村庄可供: %d");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.max_trades", "最多可换: %d 次");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.open", "点击查看可用交换");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.player_insufficient", "你的货币不足");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.village_insufficient", "村庄储备不足");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.single", "单击: 兑换 1 次");
        add("gui.ruralroutes.trade_station.coin_exchange.tooltip.batch", "Shift+单击: 尽可能全部兑换");

        // 交易卡片 UI
        add("gui.ruralroutes.trade_card.stock", "库存 %d");
        add("gui.ruralroutes.trade_card.can_buy", "可收 %d");
        add("gui.ruralroutes.trade_card.price_per", "/个");
        add("gui.ruralroutes.trade_card.tooltip.stock", "库存: %d");
        add("gui.ruralroutes.trade_card.tooltip.can_buy", "可收购: %d");
        add("gui.ruralroutes.trade_card.tooltip.selected", "已加入: %d");
        add("gui.ruralroutes.trade_card.tooltip.price", "价格:");
        add("gui.ruralroutes.trade_card.tooltip.need", "需要:");
        add("gui.ruralroutes.trade_card.tooltip.shift_scroll", "Shift+滚轮: 增减数量");
        add("gui.ruralroutes.trade_card.tooltip.shift_click", "Shift+左键: 全部添加");
        add("gui.ruralroutes.trade_card.empty_slot", "空槽位");
        add("gui.ruralroutes.trade_station.tooltip.remove_one", "左键: 减少 1");
        add("gui.ruralroutes.trade_station.tooltip.clear_entry", "Shift+左键: 清空该项");
        add("gui.ruralroutes.trade_station.tooltip.clear_all", "Shift+左键删除按钮: 清空交易区");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "乡野商路");

        // 进度
        add("advancements.ruralroutes.root.title", "进入村庄");
        add("advancements.ruralroutes.root.description", "第一次走进一个村庄");
        add("advancements.ruralroutes.main.first_trade_station.title", "初见贸易站");
        add("advancements.ruralroutes.main.first_trade_station.description", "在村庄中找到并打开贸易站");
        add("advancements.ruralroutes.main.first_trade.title", "第一笔生意");
        add("advancements.ruralroutes.main.first_trade.description", "在贸易站完成第一笔交换");
        add("advancements.ruralroutes.main.barter_trade.title", "以物易物");
        add("advancements.ruralroutes.main.barter_trade.description", "完成一次固定合约交易");
        add("advancements.ruralroutes.main.coin_exchange.title", "货币兑换");
        add("advancements.ruralroutes.main.coin_exchange.description", "第一次完成货币兑换");
        add("advancements.ruralroutes.side.open_rumor_board.title", "打开传闻板");
        add("advancements.ruralroutes.side.open_rumor_board.description", "查看村庄中的传闻板");
        add("advancements.ruralroutes.side.open_display_case.title", "打开展示柜");
        add("advancements.ruralroutes.side.open_display_case.description", "查看村庄中的展示柜");
        add("advancements.ruralroutes.travel.enter_different_village_styles.title", "走南闯北");
        add("advancements.ruralroutes.travel.enter_different_village_styles.description", "在五种不同风格的村庄中找到贸易站");
        add("advancements.ruralroutes.travel.enter_all_village_themes.title", "识遍乡野");
        add("advancements.ruralroutes.travel.enter_all_village_themes.description", "在所有主题的村庄中找到贸易站");
        add("advancements.ruralroutes.currency.get_copper_coin.title", "获得铜币");
        add("advancements.ruralroutes.currency.get_copper_coin.description", "第一次得到铜板");
        add("advancements.ruralroutes.currency.get_iron_coin.title", "获得铁币");
        add("advancements.ruralroutes.currency.get_iron_coin.description", "第一次得到铁币");
        add("advancements.ruralroutes.currency.get_gold_coin.title", "获得金币");
        add("advancements.ruralroutes.currency.get_gold_coin.description", "第一次得到金币");
        add("advancements.ruralroutes.currency.big_spender.title", "一掷千金");
        add("advancements.ruralroutes.currency.big_spender.description", "完成一笔交易额达到 300 的大单");
        add("advancements.ruralroutes.challenge.trade_10_times.title", "熟客");
        add("advancements.ruralroutes.challenge.trade_10_times.description", "成功完成 10 次交易");
        add("advancements.ruralroutes.challenge.trade_100_times.title", "老主顾");
        add("advancements.ruralroutes.challenge.trade_100_times.description", "成功完成 100 次交易");

        // ===== 情报系统 =====

        // 传闻模板：shortage
        add("rumor.shortage.global.1", "最近各地都在抢%s。");
        add("rumor.shortage.global.2", "这阵子%s紧俏得很。");
        add("rumor.shortage.biome.1", "听说%s一带缺%s。");
        add("rumor.shortage.biome.2", "最近%s那边在抢%s。");
        add("rumor.shortage.theme.1", "%s最近缺%s。");
        add("rumor.shortage.theme.2", "听说%s正缺%s。");

        // 传闻模板：surplus
        add("rumor.surplus.global.1", "最近各地%s出得多。");
        add("rumor.surplus.global.2", "这阵子%s行情松了点。");
        add("rumor.surplus.biome.1", "%s一带%s出得多。");
        add("rumor.surplus.biome.2", "听说%s那边%s堆了不少。");
        add("rumor.surplus.theme.1", "%s最近%s出得多。");
        add("rumor.surplus.theme.2", "%s这阵子在放%s。");

        // 传闻模板：demand
        add("rumor.demand.global.1", "最近%s在各地都畅销。");
        add("rumor.demand.global.2", "这阵子%s卖得挺快。");
        add("rumor.demand.biome.1", "听说%s一带在抢%s。");
        add("rumor.demand.biome.2", "最近%s那边收%s收得勤。");
        add("rumor.demand.theme.1", "%s最近在高价收%s。");
        add("rumor.demand.theme.2", "听说%s看上%s了。");

        // 传闻模板：release
        add("rumor.release.global.1", "最近各地放出了一批%s。");
        add("rumor.release.global.2", "这阵子%s到货挺多。");
        add("rumor.release.biome.1", "%s一带最近到了一批%s。");
        add("rumor.release.biome.2", "听说%s那边刚放出了些%s。");
        add("rumor.release.theme.1", "%s最近放出了一批%s。");
        add("rumor.release.theme.2", "听说%s新放出了些%s。");

        // 作用域
        add("rumor.scope.global", "世界各地");
        add("rumor.scope.biome", "%s一带");
        add("rumor.scope.theme", "%s");

        // 闲谈
        add("rumor.gossip.1", "最近风平平静，没有特别的消息。");
        add("rumor.gossip.2", "商路上没什么新鲜事。");
        add("rumor.gossip.3", "各村庄物价平稳。");

        // 标签翻译（按需添加）
        add("ruralroutes.tag.minecraft.planks", "木板类物品");
        add("ruralroutes.tag.minecraft.crops", "农作物类");

        // 传闻目标别名
        add("rumor.target.food", "食物");
        add("rumor.target.crop", "农作物");
        add("rumor.target.wood", "木材");
        add("rumor.target.tool", "工具");
        add("rumor.target.stone", "石料");
        add("rumor.target.mineral", "矿物");
        add("rumor.target.dye", "染料");
        add("rumor.target.decor", "染色商品");
        add("rumor.target.paper", "纸张");
        add("rumor.target.terracotta", "陶瓦");
        add("rumor.target.leather_fiber", "皮制品");
        add("rumor.target.ice_snow", "冰雪商品");
        add("rumor.target.nether_goods", "下界商品");
        add("rumor.target.ocean_goods", "海洋商品");
        add("rumor.target.end_goods", "末地商品");
        add("rumor.target.precious", "稀罕货");

        // 主题翻译
        add("ruralroutes.theme.plains_granary", "平原粮仓");
        add("ruralroutes.theme.plains_pasture", "牧野集市");
        add("ruralroutes.theme.plains_workshop", "平原匠坊");
        add("ruralroutes.theme.desert_quarry", "砂岩采石场");
        add("ruralroutes.theme.desert_oasis", "绿洲田园");
        add("ruralroutes.theme.desert_dyeworks", "染料工坊");
        add("ruralroutes.theme.savanna_woodworks", "金合欢木场");
        add("ruralroutes.theme.savanna_terracotta", "陶瓦窑");
        add("ruralroutes.theme.savanna_herder", "高原牧场");
        add("ruralroutes.theme.taiga_lumber", "云杉林场");
        add("ruralroutes.theme.taiga_berries", "浆果之乡");
        add("ruralroutes.theme.taiga_fur", "毛皮驿站");
        add("ruralroutes.theme.snowy_iceworks", "冰矿坊");
        add("ruralroutes.theme.snowy_waystation", "雪原旅站");
        add("ruralroutes.theme.snowy_hunter", "极地猎户");
    }
}
