package github.mczme.ruralroutes.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.trade.TradeSide;
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
        // 平原粮仓主题 - 货币篮 + 固定交换契约测试
        theme("plains_granary")
            .biome("minecraft:plains")
            .sell("minecraft:bread", "#minecraft:planks", "#minecraft:logs", "#minecraft:swords")
            .buy("minecraft:wheat", "minecraft:bread")
            .specialty("minecraft:golden_carrot")
            .stock(8, 16)
            .stockSpecific("minecraft:golden_carrot", 1, 3)
            .priceModifier("minecraft:bread", 1.2f, 0.8f)
            .withCurrency()
            .addCurrencyBasketTrade(
                TradeSide.SELL_TO_PLAYER,
                List.of("minecraft:diamond_sword"),
                List.of("ruralroutes:copper_coin", "ruralroutes:iron_coin"),
                ThemeTemplate.CompositionStrategy.LARGEST_FIRST
            )
            .addFixedTrade(
                List.of(
                    new ThemeTemplate.InputEntry("minecraft:wheat", 8)
                ),
                List.of(
                    new ThemeTemplate.OutputEntry("minecraft:bread", 4)
                )
            )
            .register();
    }

    /**
     * 创建主题构建器
     */
    private ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name).registrar(this::unconditional);
    }
}