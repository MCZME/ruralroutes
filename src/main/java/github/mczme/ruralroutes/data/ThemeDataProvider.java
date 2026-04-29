package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

/**
 * 主题模板数据生成器
 * 生成试点主题 JSON 文件到 data/<namespace>/ruralroutes/themes/
 */
public class ThemeDataProvider extends JsonCodecProvider<ThemeTemplate> {

    public ThemeDataProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Target.DATA_PACK, "ruralroutes/themes", PackType.SERVER_DATA,
              ThemeTemplate.CODEC, lookupProvider, "ruralroutes", existingFileHelper);
    }

    @Override
    protected void gather() {
        // 平原粮仓主题
        theme("plains_granary")
            .biome("minecraft:plains")
            .sell("tag:#ruralroutes:pool/crop", "item:minecraft:bread")
            .buy("tag:#ruralroutes:pool/mineral")
            .specialty("minecraft:golden_carrot")
            .stock(8, 16)
            .stockSpecific("#ruralroutes:pool/crop", 32, 64)
            .stockSpecific("minecraft:golden_carrot", 1, 3)
            .priceMod("#ruralroutes:pool/crop", 0.8f, 1.2f)
            .priceMod("#ruralroutes:pool/mineral", 1.2f, 0.8f)
            .register();

        // 沙漠采石场主题
        theme("desert_quarry")
            .biome("minecraft:desert")
            .sell("tag:#ruralroutes:pool/sand", "item:minecraft:sandstone")
            .buy("tag:#ruralroutes:pool/crop")
            .specialty("minecraft:red_sand")
            .stock(8, 16)
            .stockSpecific("#ruralroutes:pool/sand", 48, 96)
            .stockSpecific("minecraft:red_sand", 4, 8)
            .priceMod("#ruralroutes:pool/sand", 0.7f, 1.3f)
            .priceMod("#ruralroutes:pool/crop", 1.3f, 0.7f)
            .register();

        // 森林伐木场主题
        theme("forest_lumbermill")
            .biome("minecraft:forest")
            .sell("tag:#ruralroutes:pool/wood", "item:minecraft:charcoal")
            .buy("tag:#ruralroutes:pool/mineral")
            .specialty("minecraft:bee_nest")
            .stock(8, 16)
            .stockSpecific("#ruralroutes:pool/wood", 32, 64)
            .stockSpecific("minecraft:bee_nest", 1, 2)
            .priceMod("#ruralroutes:pool/wood", 0.8f, 1.2f)
            .priceMod("#ruralroutes:pool/mineral", 1.2f, 0.8f)
            .register();
    }

    /**
     * 创建主题构建器
     */
    private ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name).registrar(this::unconditional);
    }
}