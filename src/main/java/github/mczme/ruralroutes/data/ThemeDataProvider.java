package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.data.builder.ThemeBuilder;
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
        // ==================== 平原群系 ====================

        theme("plains_granary")
            .biome("minecraft:plains")
            .sell("minecraft:bread")
            .buy("minecraft:wheat")
            .specialty("minecraft:golden_carrot")
            .register();

        theme("plains_pasture")
            .biome("minecraft:plains")
            .sell("minecraft:leather")
            .buy("minecraft:iron_sword")
            .specialty("minecraft:saddle")
            .register();

        theme("plains_workshop")
            .biome("minecraft:plains")
            .sell("minecraft:iron_pickaxe")
            .buy("minecraft:iron_ingot")
            .specialty("minecraft:anvil")
            .register();

        // ==================== 沙漠群系 ====================

        theme("desert_quarry")
            .biome("minecraft:desert")
            .sell("minecraft:sandstone")
            .buy("minecraft:bread")
            .specialty("minecraft:chiseled_sandstone")
            .register();

        theme("desert_oasis")
            .biome("minecraft:desert")
            .sell("minecraft:wheat")
            .buy("minecraft:iron_ingot")
            .specialty("minecraft:golden_carrot")
            .register();

        theme("desert_dyeworks")
            .biome("minecraft:desert")
            .sell("minecraft:cyan_dye")
            .buy("minecraft:clay_ball")
            .specialty("minecraft:cyan_dye")
            .register();

        // ==================== 热带草原群系 ====================

        theme("savanna_woodworks")
            .biome("minecraft:savanna")
            .sell("minecraft:acacia_log")
            .buy("minecraft:iron_ingot")
            .specialty("minecraft:saddle")
            .register();

        theme("savanna_terracotta")
            .biome("minecraft:savanna")
            .sell("minecraft:terracotta")
            .buy("minecraft:bread")
            .specialty("minecraft:yellow_glazed_terracotta")
            .register();

        theme("savanna_herder")
            .biome("minecraft:savanna")
            .sell("minecraft:leather")
            .buy("minecraft:iron_sword")
            .specialty("minecraft:rabbit_hide")
            .register();

        // ==================== 针叶林群系 ====================

        theme("taiga_lumber")
            .biome("minecraft:taiga")
            .sell("minecraft:spruce_log")
            .buy("minecraft:iron_ingot")
            .specialty("minecraft:spruce_boat")
            .register();

        theme("taiga_berries")
            .biome("minecraft:taiga")
            .sell("minecraft:sweet_berries")
            .buy("minecraft:iron_ingot")
            .specialty("minecraft:glow_berries")
            .register();

        theme("taiga_fur")
            .biome("minecraft:taiga")
            .sell("minecraft:leather")
            .buy("minecraft:iron_sword")
            .specialty("minecraft:campfire")
            .register();

        // ==================== 积雪平原群系 ====================

        theme("snowy_iceworks")
            .biome("minecraft:snowy_plains")
            .sell("minecraft:ice")
            .buy("minecraft:bread")
            .specialty("minecraft:blue_ice")
            .register();

        theme("snowy_waystation")
            .biome("minecraft:snowy_plains")
            .sell("minecraft:bread")
            .buy("minecraft:iron_ingot")
            .specialty("minecraft:campfire")
            .register();

        theme("snowy_hunter")
            .biome("minecraft:snowy_plains")
            .sell("minecraft:beef")
            .buy("minecraft:iron_sword")
            .specialty("minecraft:bone_block")
            .register();
    }

    /**
     * 创建主题构建器
     */
    private ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name).registrar(this::unconditional);
    }
}