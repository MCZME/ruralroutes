package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.advancement.trigger.OpenTradeStationTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组自定义进度触发器注册。
 */
public final class RRCriteriaTriggers {

    private static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
        DeferredRegister.create(Registries.TRIGGER_TYPE, RuralRoutes.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, OpenTradeStationTrigger> OPEN_TRADE_STATION =
        TRIGGER_TYPES.register("open_trade_station", OpenTradeStationTrigger::new);

    private RRCriteriaTriggers() {
    }

    public static void register(IEventBus eventBus) {
        TRIGGER_TYPES.register(eventBus);
    }
}
