package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.market.MarketContext;
import github.mczme.ruralroutes.core.market.MarketPriceAdjustment;
import github.mczme.ruralroutes.core.market.MarketState;
import github.mczme.ruralroutes.core.market.MarketStateResolver;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.theme.ThemePriceModifierResolver;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.core.value.ValueTableManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * 统一贸易定价服务
 * 价格计算权威入口，组合基础价值、主题修正、市场因子。
 * 确保 GUI 显示、暂存区、交易请求、服务端执行校验使用同一价格来源。
 */
public final class TradePricingService {

    private TradePricingService() {}

    /**
     * 计算贸易价格
     *
     * @param level 服务端世界实例
     * @param nodeData 商业节点数据
     * @param stack 物品栈
     * @param side 交易方向
     * @return 完整价格计算结果
     */
    public static TradePrice calculate(
            ServerLevel level,
            CommercialNodeData nodeData,
            ItemStack stack,
            TradeSide side) {

        if (stack.isEmpty()) {
            return new TradePrice(0, 1.0f, MarketPriceAdjustment.NONE, 0);
        }

        // 1. 获取基础价值
        int baseValue = ValueTableManager.queryBaseValue(stack);
        if (baseValue <= 0) {
            return new TradePrice(0, 1.0f, MarketPriceAdjustment.NONE, 0);
        }

        // 2. 获取主题修正
        float themeModifier = resolveThemeModifier(nodeData, stack, side);

        // 3. 获取市场调整
        MarketPriceAdjustment marketAdj = resolveMarketAdjustment(level, nodeData, stack);

        // 4. 计算最终价格
        // 公式: round(baseValue * themeModifier * marketFactor)
        // 价格下限: max(1, calculatedPrice)
        float rawPrice = baseValue * themeModifier * marketAdj.factor();
        int finalPrice = Math.max(1, Math.round(rawPrice));

        return new TradePrice(baseValue, themeModifier, marketAdj, finalPrice);
    }

    /**
     * 仅计算最终价格（简化接口）
     *
     * @param level 服务端世界实例
     * @param nodeData 商业节点数据
     * @param stack 物品栈
     * @param side 交易方向
     * @return 最终价格
     */
    public static int calculateFinalPrice(
            ServerLevel level,
            CommercialNodeData nodeData,
            ItemStack stack,
            TradeSide side) {
        return calculate(level, nodeData, stack, side).finalPrice();
    }

    // ===== 内部辅助方法 =====

    /**
     * 解析主题修正系数
     */
    private static float resolveThemeModifier(
            CommercialNodeData nodeData,
            ItemStack stack,
            TradeSide side) {

        ResourceLocation themeName = nodeData.themeName();
        if (themeName == null) {
            return 1.0f;
        }

        ThemeTemplate template = ThemeManager.INSTANCE.getTheme(themeName);
        if (template == null) {
            return 1.0f;
        }

        ThemeTemplate.PriceModifier modifier =
            ThemePriceModifierResolver.resolve(template, stack);

        return switch (side) {
            case SELL_TO_PLAYER -> modifier.sell();
            case BUY_FROM_PLAYER -> modifier.buy();
        };
    }

    /**
     * 解析市场价格调整
     */
    private static MarketPriceAdjustment resolveMarketAdjustment(
            ServerLevel level,
            CommercialNodeData nodeData,
            ItemStack stack) {

        // 获取当前周期市场状态
        CycleManager cycleManager = CycleManager.get(level);
        MarketState marketState = cycleManager.getOrInitMarketState();

        if (marketState == null || marketState.isEmpty()) {
            return MarketPriceAdjustment.NONE;
        }

        // 构建市场上下文
        MarketContext context = buildMarketContext(nodeData);

        // 解析价格调整
        return MarketStateResolver.resolvePriceAdjustment(marketState, context, stack);
    }

    /**
     * 构建市场上下文
     */
    private static MarketContext buildMarketContext(CommercialNodeData nodeData) {
        ResourceLocation themeName = nodeData.themeName();
        ResourceLocation biomeId = null;

        // 从主题获取 biome
        if (themeName != null) {
            ThemeTemplate template = ThemeManager.INSTANCE.getTheme(themeName);
            if (template != null) {
                biomeId = template.biome();
            }
        }

        if (biomeId != null && themeName != null) {
            return MarketContext.of(themeName, biomeId);
        } else {
            return MarketContext.empty();
        }
    }
}