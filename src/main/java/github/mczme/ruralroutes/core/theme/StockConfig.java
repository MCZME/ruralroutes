package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.trade.TradeTargetRef;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 库存配置。
 * targets 支持共享范围或按 sell/buy 分向范围；specific 保留旧的精确覆盖映射。
 */
public record StockConfig(
    Optional<StockRange> defaultRange,
    Optional<Map<String, StockTarget>> targetEntries,
    Optional<Map<String, StockRange>> specific
) {
    public static final Codec<StockConfig> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            StockRange.CODEC.optionalFieldOf("default").forGetter(StockConfig::defaultRange),
            Codec.unboundedMap(Codec.STRING, StockTarget.CODEC).optionalFieldOf("targets").forGetter(StockConfig::targetEntries),
            Codec.unboundedMap(Codec.STRING, StockRange.CODEC).optionalFieldOf("specific").forGetter(StockConfig::specific)
        ).apply(instance, (defaultRange, targets, specific) -> new StockConfig(
            defaultRange,
            targets,
            specific.isPresent() ? specific : targets.map(StockConfig::projectSharedTargets)
        ))
    );

    public StockConfig {
        defaultRange = Objects.requireNonNull(defaultRange, "defaultRange");
        targetEntries = targetEntries.map(Map::copyOf);
        specific = specific.map(Map::copyOf);
    }

    public StockConfig(Optional<StockRange> defaultRange, Optional<Map<String, StockRange>> targets) {
        this(defaultRange, targets.map(StockConfig::toTargetEntries), targets);
    }

    public Optional<Map<String, StockRange>> targets() {
        return targetEntries.map(StockConfig::projectSharedTargets);
    }

    public Optional<Map<String, StockTarget>> targetEntries() {
        return targetEntries;
    }

    public Optional<Map<String, StockRange>> specific() {
        return specific;
    }

    /**
     * 按统一目标键解析库存范围。
     * 先走已有字符串 key，再保留旧的 tag/legacy 兼容分支。
     */
    public Optional<StockTarget> resolveTarget(TradeTargetRef targetRef) {
        if (targetRef == null) {
            return Optional.empty();
        }
        if (targetEntries.isEmpty()) {
            return Optional.empty();
        }

        Map<String, StockTarget> targets = targetEntries.get();
        String itemKey = targetRef.isItem() ? targetRef.itemId().orElseThrow() : null;
        String tagKey = targetRef.isTag() ? "#" + targetRef.tagId().orElseThrow() : null;
        String sourceKey = targetRef.isSourceKey() ? targetRef.sourceKey().orElseThrow() : null;

        StockTarget target = null;
        if (itemKey != null) {
            target = targets.get(itemKey);
        }
        if (target == null && sourceKey != null) {
            target = targets.get(sourceKey);
        }
        if (target == null && tagKey != null) {
            target = targets.get(tagKey);
        }
        if (target == null && targetRef.isTag()) {
            target = targets.get(targetRef.tagId().orElseThrow());
        }
        return Optional.ofNullable(target);
    }

    public Optional<StockTarget> resolveTarget(String rawRef, ResourceLocation itemId) {
        return resolveTarget(toTradeTargetRef(rawRef, itemId));
    }

    public Optional<StockRange> resolveSpecific(TradeTargetRef targetRef) {
        if (targetRef == null || specific.isEmpty()) {
            return Optional.empty();
        }

        Map<String, StockRange> map = specific.get();
        String canonical = targetRef.asString();
        if (map.containsKey(canonical)) {
            return Optional.of(map.get(canonical));
        }
        if (targetRef.isItem()) {
            String itemKey = targetRef.itemId().orElseThrow();
            if (map.containsKey(itemKey)) {
                return Optional.of(map.get(itemKey));
            }
        }
        if (targetRef.isSourceKey()) {
            String key = targetRef.sourceKey().orElseThrow();
            if (map.containsKey(key)) {
                return Optional.of(map.get(key));
            }
        }
        if (targetRef.isTag()) {
            String tagKey = targetRef.tagId().orElseThrow();
            if (map.containsKey(tagKey)) {
                return Optional.of(map.get(tagKey));
            }
            if (map.containsKey("#" + tagKey)) {
                return Optional.of(map.get("#" + tagKey));
            }
        }
        return Optional.empty();
    }

    public Optional<StockRange> resolveSpecific(String rawRef, ResourceLocation itemId) {
        return resolveSpecific(toTradeTargetRef(rawRef, itemId));
    }

    private static TradeTargetRef toTradeTargetRef(String rawRef, ResourceLocation itemId) {
        if (rawRef == null || rawRef.isBlank()) {
            return TradeTargetRef.item(itemId.toString());
        }
        if (rawRef.startsWith("#")) {
            return TradeTargetRef.tag(rawRef.substring(1));
        }
        if (rawRef.startsWith("@")) {
            return TradeTargetRef.sourceKey(rawRef.substring(1));
        }
        ResourceLocation parsed = ResourceLocation.tryParse(rawRef);
        if (parsed != null && BuiltInRegistries.ITEM.getOptional(parsed).isPresent()) {
            return TradeTargetRef.item(parsed.toString());
        }
        return TradeTargetRef.sourceKey(rawRef);
    }

    private static Map<String, StockRange> projectSharedTargets(Map<String, StockTarget> targets) {
        return targets.entrySet().stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toLegacyRange()
            ));
    }

    private static Map<String, StockTarget> toTargetEntries(Map<String, StockRange> targets) {
        return targets.entrySet().stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> StockTarget.shared(entry.getValue())
            ));
    }
}
