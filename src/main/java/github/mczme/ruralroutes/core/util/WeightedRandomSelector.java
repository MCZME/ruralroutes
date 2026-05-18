package github.mczme.ruralroutes.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * 加权随机选择工具。
 *
 * 提供不放回抽样，自动忽略非正权重候选。
 */
public final class WeightedRandomSelector {

    private WeightedRandomSelector() {}

    public static <T> List<T> selectWithoutReplacement(
            List<T> candidates,
            ToIntFunction<T> weightFunction,
            Random random,
            int count) {

        if (candidates.isEmpty() || count <= 0) {
            return List.of();
        }

        List<WeightedEntry<T>> pool = new ArrayList<>(candidates.size());
        for (T candidate : candidates) {
            int weight = Math.max(0, weightFunction.applyAsInt(candidate));
            if (weight > 0) {
                pool.add(new WeightedEntry<>(candidate, weight));
            }
        }

        if (pool.isEmpty()) {
            return List.of();
        }

        List<T> selected = new ArrayList<>(Math.min(count, pool.size()));
        int totalWeight = pool.stream().mapToInt(WeightedEntry::weight).sum();

        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            if (totalWeight <= 0) {
                break;
            }

            int roll = random.nextInt(totalWeight);
            int cumulative = 0;

            for (int j = 0; j < pool.size(); j++) {
                WeightedEntry<T> entry = pool.get(j);
                cumulative += entry.weight();

                if (roll < cumulative) {
                    selected.add(entry.value());
                    totalWeight -= entry.weight();
                    pool.remove(j);
                    break;
                }
            }
        }

        return List.copyOf(selected);
    }

    private record WeightedEntry<T>(T value, int weight) {}
}
