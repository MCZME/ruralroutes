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
        add("ruralroutes.configuration.values", "价值设置");
        add("ruralroutes.configuration.values.button", "价值设置");
        add("ruralroutes.configuration.values.tooltip", "价值相关设置");
        add("ruralroutes.configuration.default", "默认价值");
        add("ruralroutes.configuration.default.tooltip", "未定义价值规则的物品使用的默认价值");

        // 方块翻译
        addBlock(RRBlocks.TRADE_STATION, "贸易站");
        addBlock(RRBlocks.DISPLAY_CASE, "展示柜");
        addBlock(RRBlocks.RUMOR_BOARD, "传闻板");

        // 物品翻译
        addItem(RRItems.CONFIG_TOOL, "配置工具");
        add("item.ruralroutes.config_tool.tooltip", "开发者工具，用于设置贸易站主题");

        // 货币
        addItem(RRItems.COPPER_COIN, "铜板");
        addItem(RRItems.IRON_COIN, "铁币");
        addItem(RRItems.GOLD_COIN, "金币");
        add("item.ruralroutes.config_tool.no_theme", "未设置主题。可用主题: %s");
        add("item.ruralroutes.config_tool.current_theme", "当前主题: %s");

        // GUI 翻译
        add("gui.ruralroutes.config_tool.title", "主题配置");
        add("gui.ruralroutes.config_tool.apply", "应用");
        add("gui.ruralroutes.config_tool.cancel", "取消");
        add("gui.ruralroutes.config_tool.close", "关闭");
        add("gui.ruralroutes.config_tool.selected", "已选择: %s");
        add("gui.ruralroutes.config_tool.current_theme", "当前: %s");
        add("gui.ruralroutes.config_tool.no_current_theme", "未设置主题");
        add("gui.ruralroutes.config_tool.node_id", "节点ID: %s");
        add("gui.ruralroutes.config_tool.no_node_id", "节点ID: 未激活");
        add("gui.ruralroutes.config_tool.type.trade_station", "类型: 贸易站");
        add("gui.ruralroutes.config_tool.type.display_case", "类型: 展示柜");
        add("gui.ruralroutes.config_tool.type.rumor_board", "类型: 传闻板");
        add("gui.ruralroutes.config_tool.type.unknown", "类型: 未知");

        // 贸易站 GUI
        add("gui.ruralroutes.trade_station.theme", "主题: %s");
        add("gui.ruralroutes.trade_station.sell", "出售");
        add("gui.ruralroutes.trade_station.buy", "收购");
        add("gui.ruralroutes.trade_station.you_give", "你付出:");
        add("gui.ruralroutes.trade_station.you_receive", "你获得:");
        add("gui.ruralroutes.trade_station.value_status", "价值状态:");
        add("gui.ruralroutes.trade_station.confirm", "确认交换");
        add("gui.ruralroutes.trade_station.coin_exchange", "铸币");
        add("gui.ruralroutes.trade_station.trade_area", "交易区");
        add("block.ruralroutes.trade_station.mismatch", "贸易站数据不匹配");

        // 展示柜交互
        add("block.ruralroutes.display_case.not_activated", "请先激活贸易站");
        add("block.ruralroutes.display_case.mismatch", "展示柜数据不匹配");
        add("block.ruralroutes.display_case.sell_items", "本村出售物品 (%d 种)");

        // 传闻板交互
        add("block.ruralroutes.rumor_board.not_activated", "请先激活贸易站");
        add("block.ruralroutes.rumor_board.mismatch", "传闻板数据不匹配");
        add("block.ruralroutes.rumor_board.no_news", "商路消息将在下一个周期到来");

        // 交易失败原因
        add("trade.fail.value_mismatch", "价值不匹配");
        add("trade.fail.player_insufficient", "库存不足");
        add("trade.fail.village_insufficient", "村庄缺货");
        add("trade.fail.player_no_space", "背包已满");
        add("trade.fail.invalid_request", "无效交易请求");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "乡野商路");
    }
}