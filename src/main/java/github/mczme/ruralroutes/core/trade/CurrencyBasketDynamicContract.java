package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.NodeStockEntry;
import github.mczme.ruralroutes.core.theme.CompositionStrategy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 动态货币篮契约
 * 调用 TradePricingService 计算价格，再生成具体货币物品报价
 */
public record CurrencyBasketDynamicContract(
    String contractId,
    TradeSide side,
    ItemStack targetItem,
    int targetCount,
    java.util.Optional<String> sourceKey,
    List<String> acceptedCurrencies,
    CompositionStrategy composition
) implements TradeContract {

    @Override
    public TradeContractType type() {
        return TradeContractType.CURRENCY_BASKET_DYNAMIC;
    }

    @Override
    public List<Component> getInputDescription() {
        if (side == TradeSide.SELL_TO_PLAYER) {
            return List.of(Component.translatable("gui.ruralroutes.trade.currency_payment"));
        } else {
            return List.of(Component.literal(targetCount + "x " + targetItem.getDisplayName().getString()));
        }
    }

    @Override
    public List<Component> getOutputDescription() {
        if (side == TradeSide.SELL_TO_PLAYER) {
            return List.of(Component.literal(targetCount + "x " + targetItem.getDisplayName().getString()));
        } else {
            return List.of(Component.translatable("gui.ruralroutes.trade.currency_reward"));
        }
    }

    @Override
    public boolean validateInput(ServerPlayer player, List<ItemStack> inputList) {
        if (side == TradeSide.SELL_TO_PLAYER) {
            return true;
        } else {
            int has = countItemInInventory(player, targetItem.getItem());
            return has >= targetCount;
        }
    }

    @Override
    public TradeResult execute(ServerLevel level, CommercialNodeData nodeData,
                                       ServerPlayer player, List<ItemStack> inputList) {
        int unitPrice = TradePricingService.calculateFinalPrice(level, nodeData, targetItem, side, sourceKey);
        int totalPrice = unitPrice * targetCount;

        TradePaymentPlan plan = generatePaymentPlan(totalPrice);

        if (!validatePlayerCanPay(player, plan)) {
            return TradeResult.fail(TradeResult.Reason.PLAYER_INSUFFICIENT);
        }

        if (!validateVillageCanPay(nodeData, plan)) {
            return TradeResult.fail(TradeResult.Reason.VILLAGE_INSUFFICIENT);
        }

        executeTransfer(player, plan);

        return TradeResult.success(plan.playerOutputs(), plan.playerInputs());
    }

    /**
     * 生成支付计划
     */
    public TradePaymentPlan generatePaymentPlan(int totalPrice) {
        List<ItemStack> currencyItems = CurrencyBasketComposer.compose(
            totalPrice, acceptedCurrencies, composition, side);

        ItemStack goodsStack = targetItem.copy();
        goodsStack.setCount(targetCount);

        if (side == TradeSide.SELL_TO_PLAYER) {
            return new TradePaymentPlan(
                currencyItems,
                List.of(goodsStack),
                List.of(goodsStack),
                currencyItems
            );
        } else {
            return new TradePaymentPlan(
                List.of(goodsStack),
                currencyItems,
                currencyItems,
                List.of(goodsStack)
            );
        }
    }

    private boolean validatePlayerCanPay(ServerPlayer player, TradePaymentPlan plan) {
        for (ItemStack required : plan.playerInputs()) {
            int has = countItemInInventory(player, required.getItem());
            if (has < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateVillageCanPay(CommercialNodeData nodeData, TradePaymentPlan plan) {
        for (ItemStack required : plan.villageInputs()) {
            NodeStockEntry entry = nodeData.getStock(required);
            if (entry == null || entry.current() < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    private void executeTransfer(ServerPlayer player, TradePaymentPlan plan) {
        for (ItemStack item : plan.playerInputs()) {
            removeItemFromPlayer(player, item);
        }

        for (ItemStack item : plan.playerOutputs()) {
            addItemToPlayer(player, item.copy());
        }
    }

    private int countItemInInventory(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeItemFromPlayer(ServerPlayer player, ItemStack toRemove) {
        int remaining = toRemove.getCount();
        for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.getItem() == toRemove.getItem()) {
                int toTake = Math.min(remaining, stack.getCount());
                stack.shrink(toTake);
                remaining -= toTake;
            }
        }
    }

    private void addItemToPlayer(ServerPlayer player, ItemStack toAdd) {
        boolean success = player.getInventory().add(toAdd);
        if (!success && !toAdd.isEmpty()) {
            player.spawnAtLocation(toAdd);
        }
    }
}
