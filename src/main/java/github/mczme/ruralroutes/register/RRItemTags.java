package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 物品池标签定义。
 * 定义本模组专用的物品分组标签。
 * 对于已有合适标签的情况，直接引用原版或其他模组的标签即可。
 */
public class RRItemTags {

    // 货币标签
    /** 所有货币物品（铜板、铁币、金币） */
    public static final TagKey<Item> CURRENCY = tag("currency");
    /** 基础货币物品（仅铜板） */
    public static final TagKey<Item> CURRENCY_BASE = tag("currency_base");

    // 自定义物品池标签（当没有合适的现有标签时定义）
    // 命名规范：pool/<池名>，但这只是内部规范，非强制


    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, path));
    }
}
