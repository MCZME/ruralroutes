package github.mczme.ruralroutes.core.market;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 市场事件规则目录
 *
 * 资源重载监听器，从数据包加载市场事件生成规则。
 * 规则从 data/<namespace>/ruralroutes/market_event_rules/*.json 加载。
 */
public final class MarketEventRuleCatalog extends SimpleJsonResourceReloadListener {

    private static final String PATH = "ruralroutes/market_event_rules";
    private static final Gson GSON = new Gson();

    public static final MarketEventRuleCatalog INSTANCE = new MarketEventRuleCatalog();

    private Map<ResourceLocation, MarketEventRule> rulesById = Map.of();
    private List<MarketEventRule> allRules = List.of();

    private MarketEventRuleCatalog() {
        super(GSON, PATH);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, MarketEventRule> idMap = new HashMap<>();
        List<MarketEventRule> ruleList = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                MarketEventRule rule = MarketEventRule.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .getOrThrow(msg -> new RuntimeException(
                                "Failed to parse market rule " + entry.getKey() + ": " + msg));

                idMap.put(rule.id(), rule);
                ruleList.add(rule);

                RuralRoutes.LOGGER.debug("Loaded market rule: {} (weight={})", rule.id(), rule.effectiveWeight());
            } catch (RuntimeException e) {
                RuralRoutes.LOGGER.error("Failed to parse market rule: {}", entry.getKey(), e);
            }
        }

        this.rulesById = Map.copyOf(idMap);
        this.allRules = List.copyOf(ruleList);

        RuralRoutes.LOGGER.info("Loaded {} market rules", allRules.size());
    }

    /**
     * 根据 ID 获取规则
     */
    public MarketEventRule getRule(ResourceLocation id) {
        return rulesById.get(id);
    }

    /**
     * 获取所有规则
     */
    public List<MarketEventRule> getAllRules() {
        return allRules;
    }

    /**
     * 检查是否已加载规则
     */
    public boolean hasRules() {
        return !allRules.isEmpty();
    }

    /**
     * 随机选择 N 条规则（不放回加权随机）
     *
     * @param random 随机数生成器
     * @param count 要选择的规则数量
     * @return 选中的规则列表
     */
    public List<MarketEventRule> selectRandomRules(Random random, int count) {
        if (allRules.isEmpty() || count <= 0) {
            return List.of();
        }

        List<MarketEventRule> candidates = new ArrayList<>(allRules);
        List<MarketEventRule> selected = new ArrayList<>();
        int totalWeight = candidates.stream().mapToInt(MarketEventRule::effectiveWeight).sum();

        for (int i = 0; i < Math.min(count, allRules.size()); i++) {
            if (totalWeight <= 0) break;

            int roll = random.nextInt(totalWeight);
            int cumulative = 0;

            for (int j = 0; j < candidates.size(); j++) {
                MarketEventRule rule = candidates.get(j);
                cumulative += rule.effectiveWeight();

                if (roll < cumulative) {
                    selected.add(rule);
                    totalWeight -= rule.effectiveWeight();
                    candidates.remove(j);
                    break;
                }
            }
        }

        return selected;
    }
}