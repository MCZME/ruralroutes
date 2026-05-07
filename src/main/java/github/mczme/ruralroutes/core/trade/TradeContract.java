package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.node.CommercialNodeData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 以物易物交易契约 - 底层抽象
 * 位于 core/trade/ 包下
 */
public interface TradeContract {

    /** 契约类型 */
    TradeContractType type();

    /** 获取输入物品描述（GUI 显示） */
    List<Component> getInputDescription();

    /** 获取输出物品描述（GUI 显示） */
    List<Component> getOutputDescription();

    /** 验证玩家输入是否满足契约要求 */
    boolean validateInput(ServerPlayer player, List<ItemStack> inputs);

    /** 执行契约 */
    TradeResult execute(ServerLevel level, CommercialNodeData nodeData,
                                ServerPlayer player, List<ItemStack> inputs);
}
