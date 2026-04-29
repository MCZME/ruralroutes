package github.mczme.ruralroutes.data.lang;

import github.mczme.ruralroutes.RuralRoutes;
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

        // 物品翻译示例：
        // addItem(RRItems.EXAMPLE_ITEM, "Example Item");

        // 方块翻译示例：
        // addBlock(RRBlocks.EXAMPLE_BLOCK, "Example Block");

        // 通用翻译键：
        // add("itemGroup.ruralroutes", "Rural Routes");
    }
}