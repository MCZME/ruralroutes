package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.node.CommercialNodeData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 交易引擎接口
 * 底层只做价值匹配的物品交易，不区分购买、出售、铸币
 */
public interface TradeEngine {

    /**
     * 获取单例实例
     */
    static TradeEngine getInstance() {
        return TradeEngineImpl.INSTANCE;
    }

    /**
     * 执行交易
     * @param nodeData 商业节点数据
     * @param player 玩家
     * @param request 交易请求
     * @return 交易结果
     */
    TradeResult executeTrade(CommercialNodeData nodeData, Player player, TradeRequest request);

    /**
     * 预检查（不执行，仅验证）
     */
    boolean canExecuteTrade(CommercialNodeData nodeData, Player player, TradeRequest request);

    /**
     * 计算差额（UI 动态提示用）
     */
    List<ItemStack> calculateTradeShortfall(CommercialNodeData nodeData, Player player, TradeRequest request);
}