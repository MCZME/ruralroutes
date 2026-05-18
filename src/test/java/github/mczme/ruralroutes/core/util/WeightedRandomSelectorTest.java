package github.mczme.ruralroutes.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedRandomSelectorTest {

    @Test
    void returnsEveryPositiveWeightCandidateAtMostOnce() {
        // 选择数量超过候选数时，也不应重复抽到同一个候选。
        List<Entry> selected = WeightedRandomSelector.selectWithoutReplacement(
                List.of(
                        new Entry("a", 10),
                        new Entry("b", 20),
                        new Entry("c", 30)
                ),
                Entry::weight,
                new Random(1234L),
                10
        );

        Set<String> selectedNames = selected.stream().map(Entry::name).collect(Collectors.toSet());

        assertEquals(selected.size(), selectedNames.size());
        assertTrue(selected.size() <= 3);
        assertTrue(selectedNames.stream().allMatch(name -> Set.of("a", "b", "c").contains(name)));
    }

    @Test
    void ignoresZeroAndNegativeWeights() {
        // 非正权重在业务上等价于“不可选”，这里防止后续改动把它们重新放回抽样池。
        List<Entry> selected = WeightedRandomSelector.selectWithoutReplacement(
                List.of(
                        new Entry("a", 0),
                        new Entry("b", -5),
                        new Entry("c", 10)
                ),
                Entry::weight,
                new Random(99L),
                3
        );

        assertIterableEquals(List.of(new Entry("c", 10)), selected);
    }

    @Test
    void returnsEmptyWhenNoSelectionIsPossible() {
        List<Entry> selected = WeightedRandomSelector.selectWithoutReplacement(
                List.of(new Entry("a", 0)),
                Entry::weight,
                new Random(1L),
                1
        );

        assertTrue(selected.isEmpty());
        assertTrue(WeightedRandomSelector.selectWithoutReplacement(List.of(), Entry::weight, new Random(1L), 1).isEmpty());
    }

    private record Entry(String name, int weight) {}
}
