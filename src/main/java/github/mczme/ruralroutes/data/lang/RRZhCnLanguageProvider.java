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
        add("item.ruralroutes.config_tool.no_theme", "未设置主题。可用主题: %s");
        add("item.ruralroutes.config_tool.current_theme", "当前主题: %s");

        // GUI 翻译
        add("gui.ruralroutes.config_tool.title", "主题配置");
        add("gui.ruralroutes.config_tool.apply", "应用");
        add("gui.ruralroutes.config_tool.cancel", "取消");
        add("gui.ruralroutes.config_tool.selected", "已选择: %s");
        add("gui.ruralroutes.config_tool.current_theme", "当前: %s");
        add("gui.ruralroutes.config_tool.no_current_theme", "未设置主题");

        // 贸易站 GUI
        add("gui.ruralroutes.trade_station.theme", "主题: %s");
        add("gui.ruralroutes.trade_station.sell", "出售");
        add("gui.ruralroutes.trade_station.buy", "收购");
        add("block.ruralroutes.trade_station.mismatch", "贸易站数据不匹配");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "乡野商路");
    }
}