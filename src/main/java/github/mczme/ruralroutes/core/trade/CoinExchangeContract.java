package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.core.value.ValueTableManager;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 铸币兑换契约
 * 处理固定面额兑换，兑换比例通过 ValueTableManager 动态计算
 */
public record CoinExchangeContract(
    ExchangeType exchangeType
) implements TradeContract {

    public enum ExchangeType {
        /** 铜板 → 铁币 */
        COPPER_TO_IRON(RRItems.COPPER_COIN.get(), RRItems.IRON_COIN.get()),
        /** 铁币 → 铜板 */
        IRON_TO_COPPER(RRItems.IRON_COIN.get(), RRItems.COPPER_COIN.get()),
        /** 铁币 → 金币 */
        IRON_TO_GOLD(RRItems.IRON_COIN.get(), RRItems.GOLD_COIN.get()),
        /** 金币 → 铁币 */
        GOLD_TO_IRON(RRItems.GOLD_COIN.get(), RRItems.IRON_COIN.get());

        final net.minecraft.world.item.Item inputItem;
        final net.minecraft.world.item.Item outputItem;

        ExchangeType(net.minecraft.world.item.Item inputItem, net.minecraft.world.item.Item outputItem) {
            this.inputItem = inputItem;
            this.outputItem = outputItem;
        }

        /**
         * 获取输入物品数量（基于价值计算）
         */
        public int getInputCount() {
            int inputValue = ValueTableManager.queryBaseValue(new ItemStack(inputItem));
            int outputValue = ValueTableManager.queryBaseValue(new ItemStack(outputItem));
            if (inputValue <= 0 || outputValue <= 0) return 0;
            // 输入数量 = 输出价值 / 输入价值（向上取整，确保等值或超值兑换）
            return (outputValue + inputValue - 1) / inputValue;
        }

        /**
         * 获取输出物品数量（固定为1）
         */
        public int getOutputCount() {
            return 1;
        }
    }

    @Override
    public TradeContractType type() {
        return TradeContractType.COIN_EXCHANGE;
    }

    @Override
    public List<Component> getInputDescription() {
        return List.of(Component.literal(exchangeType.getInputCount() + "x " +
            new ItemStack(exchangeType.inputItem).getDisplayName().getString()));
    }

    @Override
    public List<Component> getOutputDescription() {
        return List.of(Component.literal(exchangeType.getOutputCount() + "x " +
            new ItemStack(exchangeType.outputItem).getDisplayName().getString()));
    }

    @Override
    public boolean validateInput(ServerPlayer player, List<ItemStack> inputList) {
        int has = countItemInInventory(player, exchangeType.inputItem);
        return has >= exchangeType.getInputCount();
    }

    @Override
    public TradeResult execute(ServerLevel level, CommercialNodeData nodeData,
                                       ServerPlayer player, List<ItemStack> inputList) {
        int inputCount = exchangeType.getInputCount();
        int outputCount = exchangeType.getOutputCount();

        if (inputCount <= 0) {
            return TradeResult.fail(TradeResult.Reason.INVALID_INPUT);
        }

        ResourceLocation outputId = BuiltInRegistries.ITEM.getKey(exchangeType.outputItem);
        StockEntry outputStock = nodeData.getStock(outputId);

        if (outputStock == null || outputStock.current() < outputCount) {
            return TradeResult.fail(TradeResult.Reason.VILLAGE_INSUFFICIENT);
        }

        removeItemFromPlayer(player, new ItemStack(exchangeType.inputItem, inputCount));

        ItemStack outputStack = new ItemStack(exchangeType.outputItem, outputCount);
        addItemToPlayer(player, outputStack.copy());

        return TradeResult.success(
            List.of(outputStack),
            List.of(new ItemStack(exchangeType.inputItem, inputCount))
        );
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
