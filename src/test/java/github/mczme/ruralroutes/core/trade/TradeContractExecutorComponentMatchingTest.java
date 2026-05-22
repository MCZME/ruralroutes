package github.mczme.ruralroutes.core.trade;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeContractExecutorComponentMatchingTest {

    @Test
    void consolidateStacksPreservesDistinctComponentVariants() {
        ItemStack namedA1 = namedBook("A", 1);
        ItemStack namedA2 = namedBook("A", 2);
        ItemStack namedB = namedBook("B", 3);
        ItemStack plain = new ItemStack(Items.BOOK, 4);

        TradePaymentPlan plan = new TradePaymentPlan(
            List.of(namedA1, namedA2, namedB, plain),
            List.of(),
            List.of(),
            List.of()
        );

        TradePaymentPlan consolidated = TradeContractExecutor.INSTANCE.consolidatePlan(plan);

        assertEquals(3, consolidated.playerInputs().size());
        assertEquals(3, exactVariantCount(consolidated.playerInputs(), namedBook("A", 1)));
        assertEquals(3, exactVariantCount(consolidated.playerInputs(), namedBook("B", 1)));
        assertEquals(4, plainVariantCount(consolidated.playerInputs(), Items.BOOK));
    }

    @Test
    void playerInventoryMatchingRequiresExactComponentsWhenPresent() {
        ItemStack namedA = namedBook("A", 2);
        ItemStack namedB = namedBook("B", 5);
        ItemStack plain = new ItemStack(Items.BOOK, 7);
        List<ItemStack> inventory = new ArrayList<>(List.of(namedA.copy(), namedB.copy(), plain.copy()));

        assertEquals(2, TradeContractExecutor.countMatchingItems(inventory, namedBook("A", 1)));
        assertEquals(14, TradeContractExecutor.countMatchingItems(inventory, new ItemStack(Items.BOOK, 1)));

        TradeContractExecutor.removeMatchingItems(inventory, namedBook("A", 2));

        assertEquals(0, exactVariantCount(inventory, namedBook("A", 1)));
        assertEquals(5, exactVariantCount(inventory, namedBook("B", 1)));
        assertEquals(7, plainVariantCount(inventory, Items.BOOK));
    }

    @Test
    void shortfallCalculationDoesNotDoubleCountPlainAndComponentVariants() {
        List<ItemStack> inventory = List.of(
            namedBook("A", 2),
            new ItemStack(Items.BOOK, 3)
        );
        List<ItemStack> required = List.of(
            namedBook("A", 2),
            new ItemStack(Items.BOOK, 4)
        );

        List<ItemStack> shortfall = TradeContractExecutor.calculatePlayerShortfall(inventory, required);

        assertEquals(1, shortfall.size());
        assertEquals(Items.BOOK, shortfall.get(0).getItem());
        assertEquals(1, shortfall.get(0).getCount());
        assertTrue(TradeItemKey.from(shortfall.get(0)).componentSignature().isEmpty());
    }

    private static int exactVariantCount(List<ItemStack> stacks, ItemStack probe) {
        TradeItemKey key = TradeItemKey.from(probe);
        return stacks.stream()
            .filter(stack -> TradeItemKey.from(stack).equals(key))
            .mapToInt(ItemStack::getCount)
            .sum();
    }

    private static int plainVariantCount(List<ItemStack> stacks, net.minecraft.world.item.Item item) {
        return stacks.stream()
            .filter(stack -> stack.getItem() == item)
            .filter(stack -> TradeItemKey.from(stack).componentSignature().isEmpty())
            .mapToInt(ItemStack::getCount)
            .sum();
    }

    private static ItemStack namedBook(String name, int count) {
        ItemStack stack = new ItemStack(Items.BOOK, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }
}
