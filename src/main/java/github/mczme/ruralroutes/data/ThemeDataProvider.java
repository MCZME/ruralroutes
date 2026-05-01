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
            .sell("item:minecraft:bread")
            .buy("item:minecraft:wheat")
            .specialty("minecraft:golden_carrot")
            .stock(8, 16)
            .stockSpecific("minecraft:golden_carrot", 1, 3)
            .withCurrency()
            .register();
    }

    /**
     * 创建主题构建器
     */
    private ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name).registrar(this::unconditional);
    }
}