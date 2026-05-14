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

    /** 石材类资源 */
    public static final TagKey<Item> POOL_STONE = tag("pool/stone");
    /** 木材类资源 */
    public static final TagKey<Item> POOL_WOOD = tag("pool/wood");
    /** 农作物类资源 */
    public static final TagKey<Item> POOL_CROP = tag("pool/crop");
    /** 食物类资源 */
    public static final TagKey<Item> POOL_FOOD = tag("pool/food");
    /** 矿物类资源 */
    public static final TagKey<Item> POOL_MINERAL = tag("pool/mineral");
    /** 染料与装饰类资源 */
    public static final TagKey<Item> POOL_DYE_DECOR = tag("pool/dye_decor");
    /** 皮革与纤维类资源 */
    public static final TagKey<Item> POOL_LEATHER_FIBER = tag("pool/leather_fiber");
    /** 冰雪类资源 */
    public static final TagKey<Item> POOL_ICE_SNOW = tag("pool/ice_snow");
    /** 下界来源资源 */
    public static final TagKey<Item> POOL_NETHER_GOODS = tag("pool/nether_goods");
    /** 海洋来源资源 */
    public static final TagKey<Item> POOL_OCEAN_GOODS = tag("pool/ocean_goods");
    /** 末地来源资源 */
    public static final TagKey<Item> POOL_END_GOODS = tag("pool/end_goods");
    /** 高阶珍材 */
    public static final TagKey<Item> POOL_PRECIOUS = tag("pool/precious");

    /** 全局特产池，用于随机特产抽取；属于工具标签，不参与常规系统池设计 */
    public static final TagKey<Item> POOL_SPECIALTY = tag("pool/specialty");

    // 群系候选标签：表达群系公共底色
    public static final TagKey<Item> CANDIDATE_BIOME_PLAINS_WOOD = tag("candidate/biome/plains/wood");
    public static final TagKey<Item> CANDIDATE_BIOME_PLAINS_CROP = tag("candidate/biome/plains/crop");
    public static final TagKey<Item> CANDIDATE_BIOME_PLAINS_FOOD = tag("candidate/biome/plains/food");
    public static final TagKey<Item> CANDIDATE_BIOME_PLAINS_LEATHER_FIBER = tag("candidate/biome/plains/leather_fiber");

    public static final TagKey<Item> CANDIDATE_BIOME_DESERT_STONE = tag("candidate/biome/desert/stone");
    public static final TagKey<Item> CANDIDATE_BIOME_DESERT_CROP = tag("candidate/biome/desert/crop");
    public static final TagKey<Item> CANDIDATE_BIOME_DESERT_DYE_DECOR = tag("candidate/biome/desert/dye_decor");

    public static final TagKey<Item> CANDIDATE_BIOME_SAVANNA_WOOD = tag("candidate/biome/savanna/wood");
    public static final TagKey<Item> CANDIDATE_BIOME_SAVANNA_DYE_DECOR = tag("candidate/biome/savanna/dye_decor");
    public static final TagKey<Item> CANDIDATE_BIOME_SAVANNA_LEATHER_FIBER = tag("candidate/biome/savanna/leather_fiber");
    public static final TagKey<Item> CANDIDATE_BIOME_SAVANNA_FOOD = tag("candidate/biome/savanna/food");

    public static final TagKey<Item> CANDIDATE_BIOME_TAIGA_WOOD = tag("candidate/biome/taiga/wood");
    public static final TagKey<Item> CANDIDATE_BIOME_TAIGA_FOOD = tag("candidate/biome/taiga/food");
    public static final TagKey<Item> CANDIDATE_BIOME_TAIGA_LEATHER_FIBER = tag("candidate/biome/taiga/leather_fiber");

    public static final TagKey<Item> CANDIDATE_BIOME_SNOWY_ICE_SNOW = tag("candidate/biome/snowy/ice_snow");
    public static final TagKey<Item> CANDIDATE_BIOME_SNOWY_FOOD = tag("candidate/biome/snowy/food");
    public static final TagKey<Item> CANDIDATE_BIOME_SNOWY_LEATHER_FIBER = tag("candidate/biome/snowy/leather_fiber");

    // 主题候选标签：作为群系候选的主题化补充，仅在确有语义价值时定义
    public static final TagKey<Item> CANDIDATE_THEME_PLAINS_WORKSHOP_TOOL_GOODS =
            tag("candidate/theme/plains_workshop/tool_goods");
    public static final TagKey<Item> CANDIDATE_THEME_DESERT_QUARRY_STONEWORK =
            tag("candidate/theme/desert_quarry/stonework");
    public static final TagKey<Item> CANDIDATE_THEME_DESERT_DYEWORKS_FINISHED_GOODS =
            tag("candidate/theme/desert_dyeworks/finished_goods");
    public static final TagKey<Item> CANDIDATE_THEME_SAVANNA_TERRACOTTA_KILN_GOODS =
            tag("candidate/theme/savanna_terracotta/kiln_goods");
    public static final TagKey<Item> CANDIDATE_THEME_TAIGA_FUR_FUR_GOODS =
            tag("candidate/theme/taiga_fur/fur_goods");
    public static final TagKey<Item> CANDIDATE_THEME_SNOWY_ICEWORKS_ICE_GOODS =
            tag("candidate/theme/snowy_iceworks/ice_goods");
    public static final TagKey<Item> CANDIDATE_THEME_SNOWY_WAYSTATION_SUPPLIES =
            tag("candidate/theme/snowy_waystation/supplies");
    public static final TagKey<Item> CANDIDATE_THEME_SNOWY_HUNTER_HUNTER_GOODS =
            tag("candidate/theme/snowy_hunter/hunter_goods");

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, path));
    }
}
