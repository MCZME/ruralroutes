package github.mczme.ruralroutes.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.register.RRCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class OpenTradeStationTrigger extends SimpleCriterionTrigger<OpenTradeStationTrigger.TriggerInstance> {

    public enum TradeStationEvent {
        OPEN("open"),
        FIRST_TRADE("first_trade"),
        FIXED_TRADE("fixed_trade"),
        COIN_EXCHANGE("coin_exchange"),
        OPEN_RUMOR_BOARD("open_rumor_board"),
        OPEN_DISPLAY_CASE("open_display_case"),
        BUY_SPECIALTY("buy_specialty"),
        COLLECTOR("collector"),
        DISCOVER_VILLAGE_STYLE("discover_village_style"),
        DISCOVER_VILLAGE_THEME("discover_village_theme"),
        TRADE_10_TIMES("trade_10_times"),
        TRADE_100_TIMES("trade_100_times"),
        BIG_SPENDER("big_spender");

        public static final Codec<TradeStationEvent> CODEC = Codec.STRING.xmap(TradeStationEvent::byName, TradeStationEvent::serializedName);

        private final String serializedName;

        TradeStationEvent(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        private static TradeStationEvent byName(String name) {
            for (TradeStationEvent value : values()) {
                if (value.serializedName.equals(name)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown trade station event: " + name);
        }
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, TradeStationEvent event) {
        this.trigger(player, instance -> instance.matches(event, null, null));
    }

    public void trigger(ServerPlayer player, TradeStationEvent event, String villageStyle, String villageTheme) {
        this.trigger(player, instance -> instance.matches(event, villageStyle, villageTheme));
    }

    public static Criterion<TriggerInstance> openTradeStation() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.OPEN), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> firstTrade() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.FIRST_TRADE), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> fixedTrade() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.FIXED_TRADE), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> coinExchange() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.COIN_EXCHANGE), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> openRumorBoard() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.OPEN_RUMOR_BOARD), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> openDisplayCase() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.OPEN_DISPLAY_CASE), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> buySpecialty() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.BUY_SPECIALTY), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> collector() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.COLLECTOR), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> discoverVillageStyle(String villageStyle) {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(TradeStationEvent.DISCOVER_VILLAGE_STYLE),
                Optional.of(villageStyle),
                Optional.empty()
            ));
    }

    public static Criterion<TriggerInstance> discoverVillageTheme(String villageTheme) {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(TradeStationEvent.DISCOVER_VILLAGE_THEME),
                Optional.empty(),
                Optional.of(villageTheme)
            ));
    }

    public static Criterion<TriggerInstance> trade10Times() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.TRADE_10_TIMES), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> trade100Times() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.TRADE_100_TIMES), Optional.empty(), Optional.empty()));
    }

    public static Criterion<TriggerInstance> bigSpender() {
        return RRCriteriaTriggers.OPEN_TRADE_STATION.get()
            .createCriterion(new TriggerInstance(Optional.empty(), Optional.of(TradeStationEvent.BIG_SPENDER), Optional.empty(), Optional.empty()));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<TradeStationEvent> event,
        Optional<String> villageStyle,
        Optional<String> villageTheme
    ) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            TradeStationEvent.CODEC.optionalFieldOf("event").forGetter(TriggerInstance::event),
            Codec.STRING.optionalFieldOf("village_style").forGetter(TriggerInstance::villageStyle),
            Codec.STRING.optionalFieldOf("village_theme").forGetter(TriggerInstance::villageTheme)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(TradeStationEvent actualEvent, String actualVillageStyle, String actualVillageTheme) {
            if (event.isPresent() && event.get() != actualEvent) {
                return false;
            }
            if (villageStyle.isPresent() && !villageStyle.get().equals(actualVillageStyle)) {
                return false;
            }
            return villageTheme.isEmpty() || villageTheme.get().equals(actualVillageTheme);
        }
    }
}
