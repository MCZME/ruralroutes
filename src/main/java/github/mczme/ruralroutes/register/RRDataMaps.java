package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.value.ItemValue;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * 价值表 DataMap 定义
 */
public final class RRDataMaps {

    @SuppressWarnings("null")
    public static final DataMapType<Item, ItemValue> ITEM_VALUE = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "value"),
            Registries.ITEM,
            ItemValue.CODEC
    ).build();

    @SuppressWarnings("null")
    public static void register(RegisterDataMapTypesEvent event) {
        event.register(ITEM_VALUE);
    }
}
