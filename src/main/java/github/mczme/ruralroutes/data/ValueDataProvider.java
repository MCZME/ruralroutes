package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.core.value.ItemValue;
import github.mczme.ruralroutes.register.RRDataMaps;
import github.mczme.ruralroutes.register.RRItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;

/**
 * 价值表数据生成器。
 * 采用“系统池基线 + 单品覆写”的结构，先给各类资源一档基础价值，
 * 再对当前主题、特产和关键阶段物资做精确修正。
 */
public class ValueDataProvider extends DataMapProvider {

    public ValueDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather() {
        var builder = this.builder(RRDataMaps.ITEM_VALUE);

        // ===== 货币 =====
        value(builder, "ruralroutes:copper_coin", 1);
        value(builder, "ruralroutes:iron_coin", 10);
        value(builder, "ruralroutes:gold_coin", 100);

        // ===== 系统池基线 =====
        // 注意：后声明的标签会覆盖先声明的重叠项，因此将 precious 放在最后，
        // 而 wood 放在 nether_goods 之后，避免下界木系整体吃到下界货基线。
        value(builder, RRItemTags.POOL_STONE, 1);
        value(builder, RRItemTags.POOL_CROP, 2);
        value(builder, RRItemTags.POOL_FOOD, 4);
        value(builder, RRItemTags.POOL_DYE_DECOR, 4);
        value(builder, RRItemTags.POOL_LEATHER_FIBER, 4);
        value(builder, RRItemTags.POOL_ICE_SNOW, 3);
        value(builder, RRItemTags.POOL_MINERAL, 8);
        value(builder, RRItemTags.POOL_OCEAN_GOODS, 8);
        value(builder, RRItemTags.POOL_NETHER_GOODS, 10);
        value(builder, RRItemTags.POOL_WOOD, 2);
        value(builder, RRItemTags.POOL_END_GOODS, 12);
        value(builder, RRItemTags.POOL_PRECIOUS, 60);

        // ===== 基础材料与一阶加工 =====
        value(builder, "minecraft:stick", 1);
        value(builder, "minecraft:oak_planks", 2);
        value(builder, "minecraft:oak_log", 3);
        value(builder, "minecraft:spruce_planks", 2);
        value(builder, "minecraft:spruce_log", 3);
        value(builder, "minecraft:acacia_planks", 2);
        value(builder, "minecraft:acacia_log", 3);
        value(builder, "minecraft:charcoal", 4);
        value(builder, "minecraft:coal", 4);

        // ===== 农作物与食品 =====
        value(builder, "minecraft:wheat", 2);
        value(builder, "minecraft:carrot", 2);
        value(builder, "minecraft:potato", 2);
        value(builder, "minecraft:beetroot", 2);
        value(builder, "minecraft:pumpkin", 2);
        value(builder, "minecraft:melon", 2);
        value(builder, "minecraft:sugar_cane", 2);
        value(builder, "minecraft:cactus", 2);
        value(builder, "minecraft:apple", 4);
        value(builder, "minecraft:bread", 5);
        value(builder, "minecraft:baked_potato", 5);
        value(builder, "minecraft:dried_kelp", 4);
        value(builder, "minecraft:pumpkin_pie", 7);
        value(builder, "minecraft:brown_mushroom", 2);
        value(builder, "minecraft:red_mushroom", 2);
        value(builder, "minecraft:mushroom_stew", 8);
        value(builder, "minecraft:beef", 4);
        value(builder, "minecraft:cooked_beef", 6);
        value(builder, "minecraft:mutton", 4);
        value(builder, "minecraft:cooked_mutton", 6);
        value(builder, "minecraft:rabbit", 4);
        value(builder, "minecraft:cooked_rabbit", 6);
        value(builder, "minecraft:rabbit_stew", 10);
        value(builder, "minecraft:salmon", 5);
        value(builder, "minecraft:sweet_berries", 4);
        value(builder, "minecraft:glow_berries", 8);
        value(builder, "minecraft:glistering_melon_slice", 24);
        value(builder, "minecraft:golden_carrot", 40);
        value(builder, "minecraft:golden_apple", 80);

        // ===== 皮革纤维与狩猎副产物 =====
        value(builder, "minecraft:leather", 5);
        value(builder, "minecraft:white_wool", 4);
        value(builder, "minecraft:brown_wool", 4);
        value(builder, "minecraft:gray_wool", 4);
        value(builder, "minecraft:feather", 3);
        value(builder, "minecraft:rabbit_hide", 6);
        value(builder, "minecraft:bone", 2);
        value(builder, "minecraft:bone_meal", 3);
        value(builder, "minecraft:bone_block", 6);

        // ===== 石材、装饰与窑制/装饰成品 =====
        value(builder, "minecraft:sand", 1);
        value(builder, "minecraft:red_sand", 1);
        value(builder, "minecraft:stone", 1);
        value(builder, "minecraft:gravel", 1);
        value(builder, "minecraft:sandstone", 2);
        value(builder, "minecraft:red_sandstone", 2);
        value(builder, "minecraft:cut_sandstone", 3);
        value(builder, "minecraft:smooth_sandstone", 3);
        value(builder, "minecraft:chiseled_sandstone", 5);
        value(builder, "minecraft:clay_ball", 2);
        value(builder, "minecraft:terracotta", 4);
        value(builder, "minecraft:orange_terracotta", 4);
        value(builder, "minecraft:yellow_terracotta", 4);
        value(builder, "minecraft:glass", 4);
        value(builder, "minecraft:green_dye", 4);
        value(builder, "minecraft:orange_dye", 4);
        value(builder, "minecraft:yellow_dye", 4);
        value(builder, "minecraft:cyan_dye", 6);
        value(builder, "minecraft:brick", 6);
        value(builder, "minecraft:orange_stained_glass", 5);
        value(builder, "minecraft:yellow_stained_glass", 5);
        value(builder, "minecraft:white_concrete_powder", 4);
        value(builder, "minecraft:orange_concrete_powder", 4);
        value(builder, "minecraft:yellow_concrete_powder", 4);
        value(builder, "minecraft:yellow_glazed_terracotta", 16);

        // ===== 冰雪 =====
        value(builder, "minecraft:snowball", 1);
        value(builder, "minecraft:snow_block", 2);
        value(builder, "minecraft:ice", 3);
        value(builder, "minecraft:packed_ice", 6);
        value(builder, "minecraft:blue_ice", 24);

        // ===== 主世界矿物阶段 =====
        value(builder, "minecraft:redstone", 6);
        value(builder, "minecraft:lapis_lazuli", 6);
        value(builder, "minecraft:raw_copper", 6);
        value(builder, "minecraft:copper_ingot", 8);
        value(builder, "minecraft:raw_iron", 8);
        value(builder, "minecraft:iron_ingot", 12);
        value(builder, "minecraft:raw_gold", 12);
        value(builder, "minecraft:gold_ingot", 16);
        value(builder, "minecraft:iron_nugget", 2);
        value(builder, "minecraft:gold_nugget", 2);
        value(builder, "minecraft:emerald", 24);
        value(builder, "minecraft:diamond", 40);

        // ===== 常见工具与器具 =====
        value(builder, "minecraft:wooden_hoe", 3);
        value(builder, "minecraft:wooden_axe", 4);
        value(builder, "minecraft:wooden_sword", 4);
        value(builder, "minecraft:wooden_pickaxe", 4);
        value(builder, "minecraft:wooden_shovel", 3);
        value(builder, "minecraft:stone_hoe", 6);
        value(builder, "minecraft:stone_axe", 8);
        value(builder, "minecraft:stone_sword", 8);
        value(builder, "minecraft:stone_pickaxe", 8);
        value(builder, "minecraft:stone_shovel", 6);
        value(builder, "minecraft:iron_hoe", 14);
        value(builder, "minecraft:iron_axe", 18);
        value(builder, "minecraft:iron_sword", 18);
        value(builder, "minecraft:iron_pickaxe", 20);
        value(builder, "minecraft:iron_shovel", 14);
        value(builder, "minecraft:shears", 14);
        value(builder, "minecraft:torch", 2);
        value(builder, "minecraft:bucket", 12);
        value(builder, "minecraft:water_bucket", 16);
        value(builder, "minecraft:glass_bottle", 2);
        value(builder, "minecraft:campfire", 8);
        value(builder, "minecraft:lantern", 10);
        value(builder, "minecraft:barrel", 6);
        value(builder, "minecraft:chest", 6);
        value(builder, "minecraft:acacia_fence", 4);
        value(builder, "minecraft:spruce_boat", 10);
        value(builder, "minecraft:composter", 6);
        value(builder, "minecraft:bow", 14);
        value(builder, "minecraft:crossbow", 24);
        value(builder, "minecraft:arrow", 1);

        // ===== 装备与路线高价值工具 =====
        value(builder, "minecraft:leather_helmet", 8);
        value(builder, "minecraft:leather_chestplate", 16);
        value(builder, "minecraft:leather_leggings", 14);
        value(builder, "minecraft:leather_boots", 8);
        value(builder, "minecraft:chainmail_helmet", 20);
        value(builder, "minecraft:chainmail_chestplate", 32);
        value(builder, "minecraft:chainmail_leggings", 28);
        value(builder, "minecraft:chainmail_boots", 20);
        value(builder, "minecraft:iron_helmet", 32);
        value(builder, "minecraft:iron_chestplate", 52);
        value(builder, "minecraft:iron_leggings", 44);
        value(builder, "minecraft:iron_boots", 32);
        value(builder, "minecraft:golden_pickaxe", 36);
        value(builder, "minecraft:golden_axe", 36);
        value(builder, "minecraft:golden_sword", 36);
        value(builder, "minecraft:diamond_pickaxe", 120);
        value(builder, "minecraft:diamond_axe", 110);
        value(builder, "minecraft:diamond_sword", 100);
        value(builder, "minecraft:diamond_shovel", 80);
        value(builder, "minecraft:diamond_hoe", 80);

        // ===== 特殊流通物与高价值制成品 =====
        value(builder, "minecraft:lead", 24);
        value(builder, "minecraft:name_tag", 40);
        value(builder, "minecraft:saddle", 48);
        value(builder, "minecraft:anvil", 96);
        value(builder, "minecraft:ender_pearl", 80);
        value(builder, "minecraft:blaze_rod", 100);
    }

    @Override
    public String getName() {
        return "RuralRoutes Value Table";
    }

    private static void value(DataMapProvider.Builder<ItemValue, Item> builder, String itemId, int value) {
        builder.add(ResourceLocation.parse(itemId), new ItemValue(value), false);
    }

    private static void value(DataMapProvider.Builder<ItemValue, Item> builder, TagKey<Item> tag, int value) {
        builder.add(tag, new ItemValue(value), false);
    }
}
