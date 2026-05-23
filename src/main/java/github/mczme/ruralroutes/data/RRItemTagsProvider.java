package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.register.RRItemTags;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

/**
 * 物品池标签数据生成器。
 * 定义各物品池包含的物品。
 */
public class RRItemTagsProvider extends ItemTagsProvider {

        public RRItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                        CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
                        @Nullable ExistingFileHelper existingFileHelper) {
                super(output, lookupProvider, blockTags, RuralRoutes.MODID, existingFileHelper);
        }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 货币标签
        tag(RRItemTags.CURRENCY)
                .add(RRItems.COPPER_COIN.get())
                .add(RRItems.IRON_COIN.get())
                .add(RRItems.GOLD_COIN.get());

        tag(RRItemTags.CURRENCY_BASE)
                .add(RRItems.COPPER_COIN.get());

        // ===== 第一批系统池 =====

        tag(RRItemTags.POOL_STONE)
                .addTag(Tags.Items.STONES)
                .addTag(Tags.Items.COBBLESTONES_NORMAL)
                .addTag(Tags.Items.COBBLESTONES_DEEPSLATE)
                .addTag(Tags.Items.SANDS)
                .addTag(Tags.Items.GRAVELS)
                .addTag(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS)
                .addTag(Tags.Items.SANDSTONE_RED_BLOCKS);

        tag(RRItemTags.POOL_WOOD)
                .addTag(ItemTags.LOGS)
                .addTag(Tags.Items.STRIPPED_LOGS)
                .addTag(Tags.Items.STRIPPED_WOODS)
                .addTag(ItemTags.PLANKS)
                .addTag(Tags.Items.RODS_WOODEN)
                .add(Items.CHARCOAL);

        tag(RRItemTags.POOL_CROP)
                .addTag(Tags.Items.CROPS_WHEAT)
                .addTag(Tags.Items.CROPS_CARROT)
                .addTag(Tags.Items.CROPS_POTATO)
                .addTag(Tags.Items.CROPS_BEETROOT)
                .addTag(Tags.Items.CROPS_PUMPKIN)
                .addTag(Tags.Items.CROPS_MELON)
                .addTag(Tags.Items.CROPS_SUGAR_CANE)
                .addTag(Tags.Items.CROPS_CACTUS);

        tag(RRItemTags.POOL_FOOD)
                .addTag(Tags.Items.FOODS_BREAD)
                .addTag(Tags.Items.FOODS_RAW_MEAT)
                .addTag(Tags.Items.FOODS_COOKED_MEAT)
                .addTag(Tags.Items.FOODS_BERRY)
                .add(
                        Items.COD,
                        Items.COOKED_COD,
                        Items.SALMON,
                        Items.COOKED_SALMON,
                        Items.BAKED_POTATO,
                        Items.PUMPKIN_PIE,
                        Items.APPLE,
                        Items.RABBIT_STEW,
                        Items.GLISTERING_MELON_SLICE,
                        Items.GOLDEN_CARROT,
                        Items.GOLDEN_APPLE);

        tag(RRItemTags.POOL_MINERAL)
                .add(Items.COAL)
                .addTag(Tags.Items.RAW_MATERIALS_IRON)
                .addTag(Tags.Items.RAW_MATERIALS_COPPER)
                .addTag(Tags.Items.RAW_MATERIALS_GOLD)
                .addTag(Tags.Items.INGOTS_IRON)
                .addTag(Tags.Items.INGOTS_COPPER)
                .addTag(Tags.Items.INGOTS_GOLD)
                .addTag(Tags.Items.DUSTS_REDSTONE)
                .addTag(Tags.Items.GEMS_LAPIS)
                .addTag(Tags.Items.GEMS_DIAMOND)
                .addTag(Tags.Items.GEMS_EMERALD);

        // 染料与装饰成品池：只收成品，不收骨粉、黏土球、沙子等原料
        tag(RRItemTags.POOL_DYE_DECOR)
                .addTag(ItemTags.TERRACOTTA)
                .addTag(Tags.Items.DYES)
                .addTag(Tags.Items.GLAZED_TERRACOTTAS)
                .addTag(Tags.Items.GLASS_BLOCKS)
                .addTag(Tags.Items.GLASS_BLOCKS_TINTED);

        tag(RRItemTags.POOL_LEATHER_FIBER)
                .addTag(ItemTags.WOOL)
                .addTag(Tags.Items.LEATHERS)
                .addTag(Tags.Items.STRINGS)
                .addTag(Tags.Items.FEATHERS)
                .add(Items.RABBIT_HIDE);

        tag(RRItemTags.POOL_ICE_SNOW)
                .add(
                        Items.SNOWBALL,
                        Items.SNOW_BLOCK,
                        Items.ICE,
                        Items.PACKED_ICE,
                        Items.BLUE_ICE);

        tag(RRItemTags.POOL_NETHER_GOODS)
                .addTag(Tags.Items.NETHERRACKS)
                .addTag(Tags.Items.BRICKS_NETHER)
                .addTag(Tags.Items.GEMS_QUARTZ)
                .addTag(Tags.Items.DUSTS_GLOWSTONE)
                .addTag(Tags.Items.CROPS_NETHER_WART)
                .addTag(Tags.Items.RODS_BLAZE)
                .addTag(Tags.Items.INGOTS_NETHERITE)
                .addTag(ItemTags.CRIMSON_STEMS)
                .addTag(ItemTags.WARPED_STEMS)
                .add(
                        Items.SOUL_SAND,
                        Items.SOUL_SOIL,
                        Items.BLACKSTONE,
                        Items.CRIMSON_PLANKS,
                        Items.WARPED_PLANKS,
                        Items.MAGMA_CREAM,
                        Items.GHAST_TEAR);

        tag(RRItemTags.POOL_OCEAN_GOODS)
                .add(
                        Items.PRISMARINE_SHARD,
                        Items.PRISMARINE_CRYSTALS,
                        Items.INK_SAC,
                        Items.GLOW_INK_SAC,
                        Items.NAUTILUS_SHELL,
                        Items.HEART_OF_THE_SEA,
                        Items.KELP);

        tag(RRItemTags.POOL_END_GOODS)
                .add(
                        Items.ENDER_PEARL,
                        Items.CHORUS_FRUIT,
                        Items.POPPED_CHORUS_FRUIT,
                        Items.SHULKER_SHELL,
                        Items.END_STONE,
                        Items.DRAGON_BREATH,
                        Items.ELYTRA);

        tag(RRItemTags.POOL_PRECIOUS)
                .addTag(Tags.Items.INGOTS_NETHERITE)
                .addTag(Tags.Items.NETHER_STARS)
                .add(
                        Items.DRAGON_BREATH,
                        Items.ENCHANTED_GOLDEN_APPLE,
                        Items.ELYTRA);

        // 特产工具标签（用于随机特产抽取，不视为常规系统池）
        tag(RRItemTags.POOL_SPECIALTY)
                .add(
                        Items.GOLDEN_CARROT,
                        Items.CHISELED_SANDSTONE,
                        Items.CYAN_DYE,
                        Items.YELLOW_GLAZED_TERRACOTTA,
                        Items.RABBIT_HIDE,
                        Items.SPRUCE_BOAT,
                        Items.GLOW_BERRIES,
                        Items.CAMPFIRE,
                        Items.GLISTERING_MELON_SLICE,
                        Items.RABBIT_STEW,
                        Items.GOLDEN_APPLE,
                        Items.LEAD,
                        Items.NAME_TAG,
                        Items.DIAMOND_PICKAXE,
                        Items.DIAMOND_AXE,
                        Items.DIAMOND_SWORD,
                        Items.BLUE_ICE,
                        Items.ENDER_PEARL,
                        Items.BLAZE_ROD);

        // ===== 群系候选标签 =====

        tag(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD)
                .add(
                        Items.OAK_LOG,
                        Items.OAK_PLANKS,
                        Items.STICK);

        tag(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP)
                .add(
                        Items.WHEAT,
                        Items.CARROT,
                        Items.POTATO,
                        Items.BEETROOT,
                        Items.PUMPKIN,
                        Items.MELON,
                        Items.SUGAR_CANE);

        tag(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD)
                .add(
                        Items.BREAD,
                        Items.BAKED_POTATO,
                        Items.PUMPKIN_PIE,
                        Items.APPLE,
                        Items.BEEF,
                        Items.COOKED_BEEF,
                        Items.MUTTON,
                        Items.COOKED_MUTTON);

        tag(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER)
                .add(
                        Items.LEATHER,
                        Items.WHITE_WOOL,
                        Items.FEATHER);

        tag(RRItemTags.CANDIDATE_BIOME_DESERT_STONE)
                .add(
                        Items.SAND,
                        Items.RED_SAND,
                        Items.SANDSTONE,
                        Items.CUT_SANDSTONE,
                        Items.SMOOTH_SANDSTONE,
                        Items.RED_SANDSTONE,
                        Items.CUT_RED_SANDSTONE);

        tag(RRItemTags.CANDIDATE_BIOME_DESERT_CROP)
                .add(
                        Items.WHEAT,
                        Items.MELON,
                        Items.SUGAR_CANE,
                        Items.CACTUS);

        tag(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR)
                .add(
                        Items.TERRACOTTA,
                        Items.ORANGE_TERRACOTTA,
                        Items.YELLOW_TERRACOTTA,
                        Items.GLASS,
                        Items.ORANGE_STAINED_GLASS,
                        Items.YELLOW_STAINED_GLASS,
                        Items.GREEN_DYE,
                        Items.CYAN_DYE);

        tag(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD)
                .add(
                        Items.ACACIA_LOG,
                        Items.ACACIA_PLANKS,
                        Items.STICK,
                        Items.CHARCOAL);

        tag(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR)
                .add(
                        Items.TERRACOTTA,
                        Items.ORANGE_TERRACOTTA,
                        Items.YELLOW_TERRACOTTA,
                        Items.GLASS,
                        Items.ORANGE_STAINED_GLASS,
                        Items.YELLOW_GLAZED_TERRACOTTA,
                        Items.CYAN_DYE);

        tag(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER)
                .add(
                        Items.LEATHER,
                        Items.BROWN_WOOL,
                        Items.WHITE_WOOL,
                        Items.FEATHER);

        tag(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD)
                .add(
                        Items.BEEF,
                        Items.COOKED_BEEF,
                        Items.MUTTON,
                        Items.COOKED_MUTTON);

        tag(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD)
                .add(
                        Items.SPRUCE_LOG,
                        Items.SPRUCE_PLANKS,
                        Items.STICK,
                        Items.CHARCOAL);

        tag(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD)
                .add(
                        Items.SWEET_BERRIES,
                        Items.CARROT,
                        Items.POTATO,
                        Items.BEETROOT,
                        Items.RABBIT,
                        Items.COOKED_RABBIT,
                        Items.SALMON,
                        Items.COOKED_SALMON);

        tag(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER)
                .add(
                        Items.LEATHER,
                        Items.RABBIT_HIDE,
                        Items.GRAY_WOOL,
                        Items.WHITE_WOOL);

        tag(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW)
                .add(
                        Items.SNOWBALL,
                        Items.SNOW_BLOCK,
                        Items.ICE,
                        Items.PACKED_ICE,
                        Items.BLUE_ICE);

        tag(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD)
                .add(
                        Items.BEEF,
                        Items.COOKED_BEEF,
                        Items.RABBIT,
                        Items.COOKED_RABBIT,
                        Items.SALMON,
                        Items.COOKED_SALMON);

        tag(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER)
                .add(
                        Items.LEATHER,
                        Items.RABBIT_HIDE,
                        Items.WHITE_WOOL,
                        Items.FEATHER);

        // ===== 主题候选标签 =====

        tag(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS)
                .add(
                        Items.IRON_PICKAXE,
                        Items.IRON_AXE,
                        Items.IRON_SWORD,
                        Items.IRON_SHOVEL,
                        Items.IRON_HOE,
                        Items.STONE_PICKAXE,
                        Items.STONE_AXE,
                        Items.STONE_SWORD,
                        Items.STONE_SHOVEL,
                        Items.STONE_HOE,
                        Items.BOW,
                        Items.CROSSBOW,
                        Items.ARROW,
                        Items.CHAINMAIL_HELMET,
                        Items.CHAINMAIL_CHESTPLATE,
                        Items.CHAINMAIL_LEGGINGS,
                        Items.CHAINMAIL_BOOTS,
                        Items.IRON_HELMET,
                        Items.IRON_CHESTPLATE,
                        Items.IRON_LEGGINGS,
                        Items.IRON_BOOTS);

        tag(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK)
                .add(
                        Items.STONE,
                        Items.GRAVEL,
                        Items.SANDSTONE,
                        Items.RED_SANDSTONE,
                        Items.CUT_SANDSTONE,
                        Items.SMOOTH_SANDSTONE);

        tag(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS)
                .add(
                        Items.ORANGE_DYE,
                        Items.YELLOW_DYE,
                        Items.GREEN_DYE,
                        Items.CYAN_DYE,
                        Items.GLASS,
                        Items.ORANGE_STAINED_GLASS,
                        Items.TERRACOTTA);

        tag(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS)
                .add(
                        Items.TERRACOTTA,
                        Items.ORANGE_TERRACOTTA,
                        Items.YELLOW_TERRACOTTA,
                        Items.GLASS,
                        Items.BRICK,
                        Items.YELLOW_GLAZED_TERRACOTTA);

        tag(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS)
                .add(
                        Items.LEATHER,
                        Items.RABBIT_HIDE,
                        Items.WHITE_WOOL,
                        Items.GRAY_WOOL,
                        Items.BOW,
                        Items.ARROW);

        tag(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS)
                .add(
                        Items.ICE,
                        Items.PACKED_ICE,
                        Items.SNOW_BLOCK,
                        Items.BLUE_ICE);

        tag(RRItemTags.CANDIDATE_THEME_SNOWY_WAYSTATION_SUPPLIES)
                .add(
                        Items.BREAD,
                        Items.BAKED_POTATO,
                        Items.PUMPKIN_PIE,
                        Items.OAK_LOG,
                        Items.SPRUCE_LOG,
                        Items.CHARCOAL,
                        Items.CAMPFIRE);

        tag(RRItemTags.CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS)
                .add(
                        Items.BEEF,
                        Items.RABBIT,
                        Items.COOKED_RABBIT,
                        Items.LEATHER,
                        Items.RABBIT_HIDE,
                        Items.FEATHER,
                        Items.BONE,
                        Items.BONE_BLOCK,
                        Items.BOW,
                        Items.ARROW);
    }
}
