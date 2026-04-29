package github.mczme.ruralroutes.core.value;

import github.mczme.ruralroutes.Config;
import github.mczme.ruralroutes.register.RRDataMaps;
import net.minecraft.world.item.ItemStack;

/**
 * 价值表查询管理器
 * 提供物品基础价值查询和静态价格计算接口
 */
public final class ValueTableManager {

    private ValueTableManager() {}

    /**
     * 查询物品基础价值
     * 若物品未定义价值规则，返回配置文件中的默认值
     */
    public static int queryBaseValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemValue value = stack.getItemHolder().getData(RRDataMaps.ITEM_VALUE);
        return value != null ? value.value() : Config.DEFAULT_VALUE.get();
    }

    /**
     * 计算静态价格
     * 静态价格 = 基础价值 × 主题修正系数
     * （市场波动因子第二阶段接入）
     */
    public static int calculateStaticPrice(ItemStack stack, float modifier) {
        return Math.round(queryBaseValue(stack) * modifier);
    }
}
