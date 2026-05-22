package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.theme.CompositionStrategy;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import github.mczme.ruralroutes.core.value.ValueTableManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 货币篮生成器
 * 根据价格和策略生成具体货币物品列表
 *
 * 货币价值通过 ValueTableManager 动态获取
 * 货币判断通过 TagLookupCache 统一处理
 */
public final class CurrencyBasketComposer {

    /** 货币标签引用 */
    private static final String CURRENCY_TAG = "#ruralroutes:currency";
    /** 基础货币标签引用 */
    private static final String CURRENCY_BASE_TAG = "#ruralroutes:currency_base";

    private CurrencyBasketComposer() {}

    /**
     * 将价格转换为货币物品列表
     *
     * @param price 价格（基础货币单位）
     * @param acceptedCurrencies 允许使用的货币物品 ID 列表
     * @param strategy 组合策略
     * @param side 交易方向（影响取整）
     * @return 货币物品列表
     */
    public static List<ItemStack> compose(int price, List<String> acceptedCurrencies,
                                          CompositionStrategy strategy, TradeSide side) {
        if (price <= 0) {
            return List.of();
        }

        List<CurrencyDenomination> denominations = parseDenominations(acceptedCurrencies);
        if (denominations.isEmpty()) {
            return List.of();
        }

        int adjustedPrice = adjustPrice(price, denominations, side);

        return switch (strategy) {
            case SMALLEST_ONLY -> composeSmallestOnly(adjustedPrice, denominations);
            case LARGEST_FIRST -> composeLargestFirst(adjustedPrice, denominations);
            case SINGLE -> composeSingle(adjustedPrice, denominations);
        };
    }

    /**
     * 计算货币篮的总价值
     */
    public static int calculateTotalValue(List<ItemStack> currencyItems) {
        int total = 0;
        for (ItemStack stack : currencyItems) {
            total += calculateCurrencyValue(stack);
        }
        return total;
    }

    /**
     * 计算单个货币物品的价值
     * 使用 ValueTableManager 动态获取
     */
    public static int calculateCurrencyValue(ItemStack stack) {
        if (stack.isEmpty() || !isCurrency(stack)) {
            return 0;
        }
        return ValueTableManager.queryBaseValue(stack) * stack.getCount();
    }

    /**
     * 判断物品是否为货币
     * 使用 TagLookupCache 统一处理
     */
    public static boolean isCurrency(ItemStack stack) {
        return TagLookupCache.matchesItem(stack, CURRENCY_TAG);
    }

    /**
     * 判断物品是否为基础货币（最小面额）
     * 使用 TagLookupCache 统一处理
     */
    public static boolean isBaseCurrency(ItemStack stack) {
        return TagLookupCache.matchesItem(stack, CURRENCY_BASE_TAG);
    }

    /**
     * 解析货币面额列表
     * 使用 ValueTableManager 获取每个货币的价值
     */
    private static List<CurrencyDenomination> parseDenominations(List<String> currencyIds) {
        List<CurrencyDenomination> list = new ArrayList<>();

        for (String id : currencyIds) {
            // 处理标签引用和精确引用
            if (TagLookupCache.isTagRef(id)) {
                // 展开标签为具体物品
                for (Item item : TagLookupCache.getItems(id)) {
                    addCurrencyDenomination(list, item);
                }
            } else {
                // 精确物品引用
                try {
                    ResourceLocation loc = ResourceLocation.parse(id);
                    Item item = BuiltInRegistries.ITEM.get(loc);
                    if (item != net.minecraft.world.item.Items.AIR) {
                        addCurrencyDenomination(list, item);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 按面额降序排序
        list.sort(Comparator.comparingInt(CurrencyDenomination::value).reversed());
        return list;
    }

    /**
     * 添加货币面额到列表（去重）
     */
    private static void addCurrencyDenomination(List<CurrencyDenomination> list, Item item) {
        ItemStack stack = new ItemStack(item);
        if (!isCurrency(stack)) {
            return;
        }
        int value = ValueTableManager.queryBaseValue(stack);
        if (value <= 0) {
            return;
        }
        // 去重：已存在相同价值的货币则跳过
        for (CurrencyDenomination d : list) {
            if (d.item() == item) return;
        }
        list.add(new CurrencyDenomination(item, value));
    }

    /**
     * 价格取整（当不包含基础货币时）
     */
    private static int adjustPrice(int price, List<CurrencyDenomination> denominations, TradeSide side) {
        // 检查是否包含基础货币
        boolean hasBaseCurrency = denominations.stream()
            .anyMatch(d -> isBaseCurrency(new ItemStack(d.item())));

        if (hasBaseCurrency) {
            return price;
        }

        // 找到最小面额
        int minDenomination = denominations.stream()
            .mapToInt(CurrencyDenomination::value)
            .min()
            .orElse(1);

        // 取整规则：
        // sell_to_player: 向上取整（避免玩家少付）
        // buy_from_player: 向下取整（避免村庄多付）
        if (side == TradeSide.SELL_TO_PLAYER) {
            return ((price + minDenomination - 1) / minDenomination) * minDenomination;
        } else {
            return (price / minDenomination) * minDenomination;
        }
    }

    /**
     * 最小面额策略
     */
    private static List<ItemStack> composeSmallestOnly(int price, List<CurrencyDenomination> denominations) {
        CurrencyDenomination smallest = denominations.stream()
            .min(Comparator.comparingInt(CurrencyDenomination::value))
            .orElse(null);

        if (smallest == null) {
            return List.of();
        }

        int count = price / smallest.value();
        if (count <= 0) {
            return List.of();
        }

        return List.of(new ItemStack(smallest.item(), count));
    }

    /**
     * 优先高面额策略
     */
    private static List<ItemStack> composeLargestFirst(int price, List<CurrencyDenomination> denominations) {
        List<ItemStack> result = new ArrayList<>();
        int remaining = price;

        for (CurrencyDenomination denom : denominations) {
            if (remaining >= denom.value()) {
                int count = remaining / denom.value();
                result.add(new ItemStack(denom.item(), count));
                remaining -= count * denom.value();
            }
        }

        return result;
    }

    /**
     * 单一货币策略
     */
    private static List<ItemStack> composeSingle(int price, List<CurrencyDenomination> denominations) {
        if (denominations.size() != 1) {
            return composeLargestFirst(price, denominations);
        }

        CurrencyDenomination denom = denominations.get(0);
        int count = price / denom.value();
        if (count <= 0) {
            return List.of();
        }

        return List.of(new ItemStack(denom.item(), count));
    }

    /** 货币面额定义 */
    private record CurrencyDenomination(Item item, int value) {}
}
