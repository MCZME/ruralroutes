package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.advancement.RRAdvancementKeys;
import github.mczme.ruralroutes.advancement.trigger.OpenTradeStationTrigger;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import github.mczme.ruralroutes.register.RRBlocks;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RRAdvancementProvider extends AdvancementProvider {

    public RRAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, existingFileHelper, List.of(new RRAdvancementSubProvider()));
    }

    private static class RRAdvancementSubProvider implements AdvancementProvider.AdvancementGenerator {
        private static final List<ResourceLocation> VILLAGE_THEME_IDS = ThemeDataProvider.collectBuiltinThemeIds();

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
                             ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = buildRoot(registries, saver);
            AdvancementHolder firstTradeStation = buildFirstTradeStation(root, saver);
            AdvancementHolder firstTrade = buildFirstTrade(firstTradeStation, saver);
            buildBarterTrade(firstTrade, saver);
            buildCoinExchange(firstTrade, saver);
            buildOpenRumorBoard(firstTradeStation, saver);
            AdvancementHolder openDisplayCase = buildOpenDisplayCase(firstTradeStation, saver);
            AdvancementHolder buySpecialty = buildBuySpecialty(openDisplayCase, saver);
            buildCollector(buySpecialty, saver);
            AdvancementHolder differentVillageStyles = buildEnterDifferentVillageStyles(root, saver);
            buildEnterAllVillageThemes(differentVillageStyles, saver);

            AdvancementHolder copperCoin = buildGetCopperCoin(root, saver);
            AdvancementHolder ironCoin = buildGetIronCoin(copperCoin, saver);
            AdvancementHolder goldCoin = buildGetGoldCoin(ironCoin, saver);
            buildBigSpender(goldCoin, saver);

            AdvancementHolder trade10 = buildTrade10Times(firstTrade, saver);
            buildTrade100Times(trade10, saver);
        }

        private static AdvancementHolder buildRoot(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
            HolderSet<Structure> villages = registries.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getOrThrow(StructureTags.VILLAGE);

            return Advancement.Builder.advancement()
                .display(
                    Items.BELL,
                    Component.translatable(titleKey(RRAdvancementKeys.ROOT)),
                    Component.translatable(descKey(RRAdvancementKeys.ROOT)),
                    ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("enter_village", PlayerTrigger.TriggerInstance.located(
                    LocationPredicate.Builder.location().setStructures(villages)
                ))
                .save(saver, RRAdvancementKeys.ROOT.toString());
        }

        private static AdvancementHolder buildFirstTradeStation(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    RRBlocks.TRADE_STATION.asItem(),
                    Component.translatable(titleKey(RRAdvancementKeys.FIRST_TRADE_STATION)),
                    Component.translatable(descKey(RRAdvancementKeys.FIRST_TRADE_STATION)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("open_trade_station", OpenTradeStationTrigger.openTradeStation())
                .save(saver, RRAdvancementKeys.FIRST_TRADE_STATION.toString());
        }

        private static AdvancementHolder buildFirstTrade(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.EMERALD,
                    Component.translatable(titleKey(RRAdvancementKeys.FIRST_TRADE)),
                    Component.translatable(descKey(RRAdvancementKeys.FIRST_TRADE)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("first_trade", OpenTradeStationTrigger.firstTrade())
                .save(saver, RRAdvancementKeys.FIRST_TRADE.toString());
        }

        private static AdvancementHolder buildBarterTrade(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.BUNDLE,
                    Component.translatable(titleKey(RRAdvancementKeys.BARTER_TRADE)),
                    Component.translatable(descKey(RRAdvancementKeys.BARTER_TRADE)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("fixed_trade", OpenTradeStationTrigger.fixedTrade())
                .save(saver, RRAdvancementKeys.BARTER_TRADE.toString());
        }

        private static AdvancementHolder buildCoinExchange(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.GOLD_NUGGET,
                    Component.translatable(titleKey(RRAdvancementKeys.COIN_EXCHANGE)),
                    Component.translatable(descKey(RRAdvancementKeys.COIN_EXCHANGE)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("coin_exchange", OpenTradeStationTrigger.coinExchange())
                .save(saver, RRAdvancementKeys.COIN_EXCHANGE.toString());
        }

        private static AdvancementHolder buildOpenRumorBoard(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    RRBlocks.RUMOR_BOARD.asItem(),
                    Component.translatable(titleKey(RRAdvancementKeys.OPEN_RUMOR_BOARD)),
                    Component.translatable(descKey(RRAdvancementKeys.OPEN_RUMOR_BOARD)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("open_rumor_board", OpenTradeStationTrigger.openRumorBoard())
                .save(saver, RRAdvancementKeys.OPEN_RUMOR_BOARD.toString());
        }

        private static AdvancementHolder buildOpenDisplayCase(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    RRBlocks.DISPLAY_CASE.asItem(),
                    Component.translatable(titleKey(RRAdvancementKeys.OPEN_DISPLAY_CASE)),
                    Component.translatable(descKey(RRAdvancementKeys.OPEN_DISPLAY_CASE)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("open_display_case", OpenTradeStationTrigger.openDisplayCase())
                .save(saver, RRAdvancementKeys.OPEN_DISPLAY_CASE.toString());
        }

        private static AdvancementHolder buildBuySpecialty(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.ITEM_FRAME,
                    Component.translatable(titleKey(RRAdvancementKeys.BUY_SPECIALTY)),
                    Component.translatable(descKey(RRAdvancementKeys.BUY_SPECIALTY)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("buy_specialty", OpenTradeStationTrigger.buySpecialty())
                .save(saver, RRAdvancementKeys.BUY_SPECIALTY.toString());
        }

        private static AdvancementHolder buildCollector(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.CHEST,
                    Component.translatable(titleKey(RRAdvancementKeys.COLLECTOR)),
                    Component.translatable(descKey(RRAdvancementKeys.COLLECTOR)),
                    null,
                    AdvancementType.GOAL,
                    true,
                    true,
                    false
                )
                .addCriterion("collector", OpenTradeStationTrigger.collector())
                .save(saver, RRAdvancementKeys.COLLECTOR.toString());
        }

        private static AdvancementHolder buildEnterDifferentVillageStyles(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            Advancement.Builder builder = Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.COMPASS,
                    Component.translatable(titleKey(RRAdvancementKeys.ENTER_DIFFERENT_VILLAGE_STYLES)),
                    Component.translatable(descKey(RRAdvancementKeys.ENTER_DIFFERENT_VILLAGE_STYLES)),
                    null,
                    AdvancementType.GOAL,
                    true,
                    true,
                    false
                );

            for (VillageStyle style : VillageStyle.values()) {
                builder.addCriterion(
                    "style_" + style.getSerializedName(),
                    OpenTradeStationTrigger.discoverVillageStyle(style.getSerializedName())
                );
            }

            return builder.save(saver, RRAdvancementKeys.ENTER_DIFFERENT_VILLAGE_STYLES.toString());
        }

        private static AdvancementHolder buildEnterAllVillageThemes(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            Advancement.Builder builder = Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.FILLED_MAP,
                    Component.translatable(titleKey(RRAdvancementKeys.ENTER_ALL_VILLAGE_THEMES)),
                    Component.translatable(descKey(RRAdvancementKeys.ENTER_ALL_VILLAGE_THEMES)),
                    null,
                    AdvancementType.CHALLENGE,
                    true,
                    true,
                    false
                );

            for (ResourceLocation themeId : VILLAGE_THEME_IDS) {
                builder.addCriterion(
                    "theme_" + themeId.getPath(),
                    OpenTradeStationTrigger.discoverVillageTheme(themeId.toString())
                );
            }

            return builder.save(saver, RRAdvancementKeys.ENTER_ALL_VILLAGE_THEMES.toString());
        }

        private static AdvancementHolder buildGetCopperCoin(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    RRItems.COPPER_COIN.get(),
                    Component.translatable(titleKey(RRAdvancementKeys.GET_COPPER_COIN)),
                    Component.translatable(descKey(RRAdvancementKeys.GET_COPPER_COIN)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("get_copper_coin", InventoryChangeTrigger.TriggerInstance.hasItems(RRItems.COPPER_COIN.get()))
                .save(saver, RRAdvancementKeys.GET_COPPER_COIN.toString());
        }

        private static AdvancementHolder buildGetIronCoin(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    RRItems.IRON_COIN.get(),
                    Component.translatable(titleKey(RRAdvancementKeys.GET_IRON_COIN)),
                    Component.translatable(descKey(RRAdvancementKeys.GET_IRON_COIN)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("get_iron_coin", InventoryChangeTrigger.TriggerInstance.hasItems(RRItems.IRON_COIN.get()))
                .save(saver, RRAdvancementKeys.GET_IRON_COIN.toString());
        }

        private static AdvancementHolder buildGetGoldCoin(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    RRItems.GOLD_COIN.get(),
                    Component.translatable(titleKey(RRAdvancementKeys.GET_GOLD_COIN)),
                    Component.translatable(descKey(RRAdvancementKeys.GET_GOLD_COIN)),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
                )
                .addCriterion("get_gold_coin", InventoryChangeTrigger.TriggerInstance.hasItems(RRItems.GOLD_COIN.get()))
                .save(saver, RRAdvancementKeys.GET_GOLD_COIN.toString());
        }

        private static AdvancementHolder buildBigSpender(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.GOLD_BLOCK,
                    Component.translatable(titleKey(RRAdvancementKeys.BIG_SPENDER)),
                    Component.translatable(descKey(RRAdvancementKeys.BIG_SPENDER)),
                    null,
                    AdvancementType.GOAL,
                    true,
                    true,
                    false
                )
                .addCriterion("big_spender", OpenTradeStationTrigger.bigSpender())
                .save(saver, RRAdvancementKeys.BIG_SPENDER.toString());
        }

        private static AdvancementHolder buildTrade10Times(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.WRITTEN_BOOK,
                    Component.translatable(titleKey(RRAdvancementKeys.TRADE_10_TIMES)),
                    Component.translatable(descKey(RRAdvancementKeys.TRADE_10_TIMES)),
                    null,
                    AdvancementType.CHALLENGE,
                    true,
                    true,
                    false
                )
                .addCriterion("trade_10_times", OpenTradeStationTrigger.trade10Times())
                .save(saver, RRAdvancementKeys.TRADE_10_TIMES.toString());
        }

        private static AdvancementHolder buildTrade100Times(AdvancementHolder parent, Consumer<AdvancementHolder> saver) {
            return Advancement.Builder.advancement()
                .parent(parent)
                .display(
                    Items.ENCHANTED_BOOK,
                    Component.translatable(titleKey(RRAdvancementKeys.TRADE_100_TIMES)),
                    Component.translatable(descKey(RRAdvancementKeys.TRADE_100_TIMES)),
                    null,
                    AdvancementType.CHALLENGE,
                    true,
                    true,
                    false
                )
                .addCriterion("trade_100_times", OpenTradeStationTrigger.trade100Times())
                .save(saver, RRAdvancementKeys.TRADE_100_TIMES.toString());
        }

        private static String titleKey(ResourceLocation id) {
            return "advancements." + RuralRoutes.MODID + "." + id.getPath().replace('/', '.') + ".title";
        }

        private static String descKey(ResourceLocation id) {
            return "advancements." + RuralRoutes.MODID + "." + id.getPath().replace('/', '.') + ".description";
        }
    }
}
