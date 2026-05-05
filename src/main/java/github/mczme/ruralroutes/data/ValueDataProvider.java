package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.core.value.ItemValue;
import github.mczme.ruralroutes.register.RRDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.DataMapProvider;

/**
 * 价值表数据生成器
 */
public class ValueDataProvider extends DataMapProvider {

    public ValueDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather() {
        var builder = this.builder(RRDataMaps.ITEM_VALUE);

        // ===== plains_granary 主题物品价值 =====
        // 村庄收购物品
        builder.add(ResourceLocation.parse("minecraft:wheat"), new ItemValue(2), false);
        builder.add(ResourceLocation.parse("minecraft:bread"), new ItemValue(6), false);

        // 村庄出售物品 - 基础材料
        builder.add(ResourceLocation.parse("minecraft:oak_planks"), new ItemValue(2), false);
        builder.add(ResourceLocation.parse("minecraft:oak_log"), new ItemValue(4), false);
        builder.add(ResourceLocation.parse("minecraft:spruce_log"), new ItemValue(4), false);
        builder.add(ResourceLocation.parse("minecraft:birch_log"), new ItemValue(4), false);
        builder.add(ResourceLocation.parse("minecraft:jungle_log"), new ItemValue(4), false);
        builder.add(ResourceLocation.parse("minecraft:acacia_log"), new ItemValue(4), false);
        builder.add(ResourceLocation.parse("minecraft:dark_oak_log"), new ItemValue(4), false);
        builder.add(ResourceLocation.parse("minecraft:mangrove_log"), new ItemValue(4), false);
        builder.add(ResourceLocation.parse("minecraft:cherry_log"), new ItemValue(4), false);

        // 村庄出售物品 - 武器工具
        builder.add(ResourceLocation.parse("minecraft:wooden_sword"), new ItemValue(6), false);
        builder.add(ResourceLocation.parse("minecraft:stone_sword"), new ItemValue(10), false);
        builder.add(ResourceLocation.parse("minecraft:iron_sword"), new ItemValue(50), false);
        builder.add(ResourceLocation.parse("minecraft:golden_sword"), new ItemValue(80), false);
        builder.add(ResourceLocation.parse("minecraft:diamond_sword"), new ItemValue(200), false);

        builder.add(ResourceLocation.parse("minecraft:wooden_axe"), new ItemValue(6), false);
        builder.add(ResourceLocation.parse("minecraft:stone_axe"), new ItemValue(10), false);
        builder.add(ResourceLocation.parse("minecraft:iron_axe"), new ItemValue(50), false);
        builder.add(ResourceLocation.parse("minecraft:golden_axe"), new ItemValue(80), false);
        builder.add(ResourceLocation.parse("minecraft:diamond_axe"), new ItemValue(200), false);

        builder.add(ResourceLocation.parse("minecraft:wooden_pickaxe"), new ItemValue(9), false);
        builder.add(ResourceLocation.parse("minecraft:stone_pickaxe"), new ItemValue(15), false);
        builder.add(ResourceLocation.parse("minecraft:iron_pickaxe"), new ItemValue(75), false);
        builder.add(ResourceLocation.parse("minecraft:golden_pickaxe"), new ItemValue(120), false);
        builder.add(ResourceLocation.parse("minecraft:diamond_pickaxe"), new ItemValue(300), false);

        // 特产物品
        builder.add(ResourceLocation.parse("minecraft:golden_carrot"), new ItemValue(100), false);
    }

    @Override
    public String getName() {
        return "RuralRoutes Value Table";
    }
}