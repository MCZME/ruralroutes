package github.mczme.ruralroutes.data.lang;

import github.mczme.ruralroutes.RuralRoutes;
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

        // 物品翻译示例：
        // addItem(RRItems.EXAMPLE_ITEM, "示例物品");

        // 方块翻译示例：
        // addBlock(RRBlocks.EXAMPLE_BLOCK, "示例方块");

        // 通用翻译键：
        // add("itemGroup.ruralroutes", "乡野商路");
    }
}