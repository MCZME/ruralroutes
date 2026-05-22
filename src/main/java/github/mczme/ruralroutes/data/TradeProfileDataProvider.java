package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.theme.CompositionStrategy;
import github.mczme.ruralroutes.core.theme.InputEntry;
import github.mczme.ruralroutes.core.theme.OutputEntry;
import github.mczme.ruralroutes.core.theme.TradeProfile;
import github.mczme.ruralroutes.core.trade.TradeSide;
import github.mczme.ruralroutes.data.builder.TradeProfileBuilder;
import github.mczme.ruralroutes.register.RRItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 交易 profile 数据生成器。
 * 直接生成到 trade_profiles 目录。
 */
public class TradeProfileDataProvider extends JsonCodecProvider<TradeProfile> {

    public TradeProfileDataProvider(
        PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider,
        ExistingFileHelper existingFileHelper
    ) {
        super(
            output,
            Target.DATA_PACK,
            "ruralroutes/trade_profiles",
            PackType.SERVER_DATA,
            TradeProfile.CODEC,
            lookupProvider,
            "ruralroutes",
            existingFileHelper
        );
    }

    @Override
    protected void gather() {
        defineTradeProfiles(builder -> builder.registrar(this::unconditional).register());
    }

    static void defineTradeProfiles(java.util.function.Consumer<TradeProfileBuilder> consumer) {
        consumer.accept(profile("plains_granary")
            .withCurrency()
            .sell("minecraft:oak_planks")
            .sellPick("plains_granary/staples", 3,
                "minecraft:bread",
                tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_CROP))
            .buy("minecraft:wheat", "minecraft:iron_ingot", "minecraft:coal")
            .buyPick("plains_granary/procurement", 2,
                "minecraft:raw_iron",
                "minecraft:charcoal",
                "minecraft:stone_hoe")
            .addFixedTrade(inputs(
                in("minecraft:carrot", 8),
                in("minecraft:bread", 2),
                in("minecraft:wheat", 6)),
                outputs(out("minecraft:golden_carrot", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:golden_carrot"));

        consumer.accept(profile("plains_pasture")
            .withCurrency()
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_LEATHER_FIBER), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_FOOD), 2)
            .buy("minecraft:stone_axe", "minecraft:iron_sword")
            .buyPick("plains_pasture/tools", 2,
                "minecraft:iron_axe",
                "minecraft:shears",
                "minecraft:stone_sword")
            .addFixedTrade(inputs(
                in("minecraft:leather", 4),
                in("minecraft:white_wool", 2),
                in("minecraft:cooked_beef", 3)),
                outputs(out("minecraft:saddle", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:saddle"));

        consumer.accept(profile("plains_workshop")
            .withCurrency()
            .sell("minecraft:iron_pickaxe")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_PLAINS_WOOD), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal", "minecraft:diamond")
            .buyPick("plains_workshop/materials", 2,
                "minecraft:raw_iron",
                "minecraft:charcoal",
                "minecraft:oak_planks")
            .addFixedTrade(inputs(
                in("minecraft:iron_ingot", 6),
                in("minecraft:charcoal", 3),
                in("minecraft:oak_planks", 6)),
                outputs(out("minecraft:anvil", 1)))
            .addCurrencyBasketTrade(
                TradeSide.SELL_TO_PLAYER,
                refs("minecraft:iron_pickaxe", tagRef(RRItemTags.CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:anvil"));

        consumer.accept(profile("desert_quarry")
            .withCurrency()
            .sell("minecraft:sandstone")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_STONE), 3)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_QUARRY_STONEWORK), 2)
            .buy("minecraft:bread", "minecraft:oak_log")
            .buyPick("desert_quarry/supplies", 2,
                "minecraft:charcoal",
                "minecraft:stick",
                "minecraft:cooked_beef")
            .addFixedTrade(inputs(
                in("minecraft:red_sandstone", 2),
                in("minecraft:stick", 1)),
                outputs(out("minecraft:chiseled_sandstone", 1)))
            .specialty("minecraft:chiseled_sandstone"));

        consumer.accept(profile("desert_oasis")
            .withCurrency()
            .sell("minecraft:wheat")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_CROP), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .buyPick("desert_oasis/inputs", 2,
                "minecraft:raw_iron",
                "minecraft:bone_meal",
                "minecraft:bucket")
            .addFixedTrade(inputs(
                in("minecraft:melon", 4),
                in("minecraft:wheat", 6),
                in("minecraft:carrot", 8)),
                outputs(out("minecraft:golden_carrot", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), "minecraft:bucket"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:golden_carrot"));

        consumer.accept(profile("desert_dyeworks")
            .withCurrency()
            .sell("minecraft:terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_DESERT_DYE_DECOR), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS), 2)
            .buy("minecraft:clay_ball", "minecraft:bone_meal")
            .buyPick("desert_dyeworks/inputs", 2,
                "minecraft:cactus",
                "minecraft:sand",
                "minecraft:charcoal")
            .addFixedTrade(inputs(
                in("minecraft:cactus", 1),
                in("minecraft:bone_meal", 1)),
                outputs(out("minecraft:cyan_dye", 1)))
            .specialty("minecraft:cyan_dye"));

        consumer.accept(profile("savanna_woodworks")
            .withCurrency()
            .sell("minecraft:acacia_log", "minecraft:charcoal")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_WOOD), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .buyPick("savanna_woodworks/tools", 2,
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:raw_iron")
            .addFixedTrade(inputs(
                in("minecraft:leather", 4),
                in("minecraft:acacia_log", 4),
                in("minecraft:charcoal", 4)),
                outputs(out("minecraft:saddle", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), "minecraft:iron_axe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:saddle"));

        consumer.accept(profile("savanna_terracotta")
            .withCurrency()
            .sell("minecraft:terracotta")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_DYE_DECOR), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS), 2)
            .buy("minecraft:bread", "minecraft:cooked_beef")
            .buyPick("savanna_terracotta/supplies", 2,
                "minecraft:charcoal",
                "minecraft:clay_ball",
                "minecraft:acacia_log")
            .addFixedTrade(inputs(
                in("minecraft:terracotta", 1),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:yellow_glazed_terracotta", 1)))
            .specialty("minecraft:yellow_glazed_terracotta"));

        consumer.accept(profile("savanna_herder")
            .withCurrency()
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SAVANNA_FOOD), 2)
            .buy("minecraft:stone_axe", "minecraft:iron_sword")
            .buyPick("savanna_herder/tools", 2,
                "minecraft:iron_axe",
                "minecraft:shears",
                "minecraft:stone_sword")
            .addFixedTrade(inputs(
                in("minecraft:feather", 1),
                in("minecraft:cooked_beef", 1)),
                outputs(out("minecraft:rabbit_hide", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:rabbit_hide"));

        consumer.accept(profile("taiga_lumber")
            .withCurrency()
            .sell("minecraft:spruce_log", "minecraft:charcoal")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_WOOD), 3)
            .buy("minecraft:iron_ingot", "minecraft:coal")
            .buyPick("taiga_lumber/tools", 2,
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:raw_iron")
            .addFixedTrade(inputs(
                in("minecraft:spruce_log", 2),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:spruce_boat", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL), "minecraft:iron_axe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:spruce_boat"));

        consumer.accept(profile("taiga_berries")
            .withCurrency()
            .sellPick("taiga_berries/produce", 3,
                tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_FOOD),
                "minecraft:glow_berries")
            .buy("minecraft:iron_ingot", "minecraft:cyan_dye")
            .buyPick("taiga_berries/farming", 2,
                "minecraft:bone_meal",
                "minecraft:stone_hoe",
                "minecraft:iron_hoe")
            .addFixedTrade(inputs(
                in("minecraft:sweet_berries", 1),
                in("minecraft:bone_meal", 1)),
                outputs(out("minecraft:glow_berries", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_ingot", "minecraft:iron_hoe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:glow_berries"));

        consumer.accept(profile("taiga_fur")
            .withCurrency()
            .sell("minecraft:leather")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_TAIGA_LEATHER_FIBER), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS), 2)
            .buy("minecraft:iron_sword", "minecraft:iron_axe")
            .buyPick("taiga_fur/tools", 2,
                "minecraft:stone_axe",
                "minecraft:shears",
                "minecraft:charcoal")
            .addFixedTrade(inputs(
                in("minecraft:leather", 1),
                in("minecraft:charcoal", 1)),
                outputs(out("minecraft:campfire", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:shears"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:campfire"));

        consumer.accept(profile("snowy_iceworks")
            .withCurrency()
            .sell("minecraft:ice")
            .sellPick(tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_ICE_SNOW), 2)
            .sellPick(tagRef(RRItemTags.CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS), 2)
            .buy("minecraft:bread", "minecraft:spruce_log")
            .buyPick("snowy_iceworks/supplies", 2,
                "minecraft:charcoal",
                "minecraft:cooked_beef",
                "minecraft:oak_log")
            .addFixedTrade(inputs(
                in("minecraft:packed_ice", 2),
                in("minecraft:cooked_beef", 1)),
                outputs(out("minecraft:blue_ice", 1)))
            .specialty("minecraft:blue_ice"));

        consumer.accept(profile("snowy_waystation")
            .withCurrency()
            .sellPick("snowy_waystation/supplies", 4,
                tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_FOOD),
                "minecraft:bread",
                "minecraft:oak_log",
                "minecraft:spruce_log",
                "minecraft:charcoal")
            .buy("minecraft:iron_ingot", "minecraft:gold_ingot")
            .buyPick("snowy_waystation/valuables", 2,
                "minecraft:raw_iron",
                "minecraft:coal",
                "minecraft:charcoal")
            .addFixedTrade(inputs(
                in("minecraft:bread", 1),
                in("minecraft:spruce_log", 1)),
                outputs(out("minecraft:campfire", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs(tagRef(RRItemTags.POOL_MINERAL)),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:campfire"));

        consumer.accept(profile("snowy_hunter")
            .withCurrency()
            .sellPick("snowy_hunter/game_goods", 3,
                tagRef(RRItemTags.CANDIDATE_BIOME_SNOWY_LEATHER_FIBER),
                "minecraft:beef",
                "minecraft:rabbit")
            .buy("minecraft:iron_sword", "minecraft:spruce_log")
            .buyPick("snowy_hunter/tools", 2,
                "minecraft:stone_axe",
                "minecraft:iron_axe",
                "minecraft:charcoal")
            .addFixedTrade(inputs(in("minecraft:bone", 3)),
                outputs(out("minecraft:bone_block", 1)))
            .addCurrencyBasketTrade(
                TradeSide.BUY_FROM_PLAYER,
                refs("minecraft:iron_sword", "minecraft:iron_axe"),
                currencies("ruralroutes:iron_coin", "ruralroutes:copper_coin"),
                CompositionStrategy.LARGEST_FIRST)
            .specialty("minecraft:bone_block"));
    }

    private static TradeProfileBuilder profile(String name) {
        return TradeProfileBuilder.create(name).registrar((id, tradeProfile) -> {});
    }

    private static String tagRef(TagKey<Item> tag) {
        return "#" + tag.location();
    }

    private static InputEntry in(String item, int count) {
        return new InputEntry(item, count);
    }

    private static OutputEntry out(String item, int count) {
        return new OutputEntry(item, count);
    }

    @SafeVarargs
    private static List<InputEntry> inputs(InputEntry... entries) {
        return List.of(entries);
    }

    @SafeVarargs
    private static List<OutputEntry> outputs(OutputEntry... entries) {
        return List.of(entries);
    }

    private static List<String> currencies(String... ids) {
        return List.of(ids);
    }

    private static List<String> refs(String... ids) {
        return List.of(ids);
    }
}
