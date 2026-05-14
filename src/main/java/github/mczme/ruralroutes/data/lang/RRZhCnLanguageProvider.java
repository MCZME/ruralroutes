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
        add("ruralroutes.configuration.days", "周期长度（日）");
        add("ruralroutes.configuration.days.tooltip", "贸易周期长度，单位为游戏日（1-30）");
        add("ruralroutes.configuration.refresh_time", "刷新时刻");
        add("ruralroutes.configuration.refresh_time.tooltip", "贸易周期刷新的时刻");

        // 方块翻译
        addBlock(RRBlocks.TRADE_STATION, "贸易站");
        addBlock(RRBlocks.DISPLAY_CASE, "展示柜");
        addBlock(RRBlocks.RUMOR_BOARD, "传闻板");

        // 物品翻译
        addItem(RRItems.CONFIG_TOOL, "配置工具");
        add("item.ruralroutes.config_tool.tooltip", "开发者工具，用于设置贸易站主题和方块外观");

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

        // 贸易站 GUI
        add("gui.ruralroutes.trade_station.theme", "主题: %s");
        add("gui.ruralroutes.trade_station.sell", "出售");
        add("gui.ruralroutes.trade_station.buy", "收购");
        add("gui.ruralroutes.trade_station.confirm", "确认交换");
        add("gui.ruralroutes.trade_station.coin_exchange", "铸币");
        add("gui.ruralroutes.trade_station.trade_area", "交易区");
        add("gui.ruralroutes.trade_station.want_area", "玩家想要的");
        add("gui.ruralroutes.trade_station.pay_area", "玩家支付的");
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

        // 交易卡片 UI
        add("gui.ruralroutes.trade_card.stock", "库存 %d");
        add("gui.ruralroutes.trade_card.can_buy", "可收 %d");
        add("gui.ruralroutes.trade_card.price_per", "/个");
        add("gui.ruralroutes.trade_card.tooltip.stock", "库存: %d");
        add("gui.ruralroutes.trade_card.tooltip.can_buy", "可收购: %d");
        add("gui.ruralroutes.trade_card.tooltip.price", "价格:");
        add("gui.ruralroutes.trade_card.tooltip.need", "需要:");
        add("gui.ruralroutes.trade_card.empty_slot", "空槽位");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "乡野商路");

        // ===== 情报系统 =====

        // 情报模板
        add("rumor.template.1", "%s在%s价格%s");
        add("rumor.template.2", "%s的%s价格%s");
        add("rumor.template.3", "听说%s的%s价格%s");
        add("rumor.template.4", "%s价格%s，%s受影响");
        add("rumor.template.5", "商路传闻：%s的%s价格%s");

        // 涨跌
        add("rumor.direction.up", "上涨");
        add("rumor.direction.down", "下跌");

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
    }
}
