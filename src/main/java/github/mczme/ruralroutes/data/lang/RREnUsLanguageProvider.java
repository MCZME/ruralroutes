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
        add("item.ruralroutes.config_tool.no_theme", "No theme set. Available themes: %s");
        add("item.ruralroutes.config_tool.current_theme", "Current theme: %s");

        // GUI 翻译
        add("gui.ruralroutes.config_tool.title", "Theme Configuration");
        add("gui.ruralroutes.config_tool.apply", "Apply");
        add("gui.ruralroutes.config_tool.cancel", "Cancel");
        add("gui.ruralroutes.config_tool.selected", "Selected: %s");
        add("gui.ruralroutes.config_tool.current_theme", "Current: %s");
        add("gui.ruralroutes.config_tool.no_current_theme", "No theme set");

        // 创造模式标签页
        add("itemGroup.ruralroutes", "Rural Routes");
    }
}