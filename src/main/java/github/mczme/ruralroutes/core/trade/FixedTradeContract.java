package github.mczme.ruralroutes.core.trade;

import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.StockEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定交换契约
 * 预定义输入输出，不受市场影响
 */
public record FixedTradeContract(
    String contractId,
    List<ItemStack> inputs,
    List<ItemStack> outputs
) implements TradeContract {

    @Override
    public TradeContractType type() {
        return TradeContractType.FIXED;
    }

    @Override
    public List<Component> getInputDescription() {
        return inputs.stream()
            .map(stack -> (Component) Component.literal(stack.getCount() + "x " + stack.getDisplayName().getString()))
            .toList();
    }

    @Override
    public List<Component> getOutputDescription() {
        return outputs.stream()
            .map(stack -> (Component) Component.literal(stack.getCount() + "x " + stack.getDisplayName().getString()))
            .toList();
    }

    @Override
    public boolean validateInput(ServerPlayer player, List<ItemStack> inputList) {
        for (ItemStack required : this.inputs) {
            int has = countItemInInventory(player, required.getItem());
            if (has < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TradeResult execute(ServerLevel level, CommercialNodeData nodeData,
                                       ServerPlayer player, List<ItemStack> inputList) {
        for (ItemStack output : this.outputs) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(output.getItem());
            StockEntry entry = nodeData.getStock(itemId);
            if (entry == null || entry.current() < output.getCount()) {
                return TradeResult.fail(TradeResult.Reason.VILLAGE_INSUFFICIENT);
            }
        }

        for (ItemStack input : this.inputs) {
            removeItemFromPlayer(player, input);
        }

        List<ItemStack> actualOutputs = new ArrayList<>();
        for (ItemStack output : this.outputs) {
            ItemStack copy = output.copy();
            addItemToPlayer(player, copy);
            actualOutputs.add(copy);
        }

        return TradeResult.success(actualOutputs, new ArrayList<>(this.inputs));
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
