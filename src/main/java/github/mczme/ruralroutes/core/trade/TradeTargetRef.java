package github.mczme.ruralroutes.core.trade;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.core.util.TagLookupCache;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 统一目标引用。
 *
 * 目前覆盖四类最小骨架目标：
 * - 裸物品 id
 * - 标签
 * - source key（@foo/bar）
 * - 带组件的精确物品
 */
public record TradeTargetRef(
    Optional<String> itemId,
    Optional<String> tagId,
    Optional<String> sourceKey,
    Optional<Map<String, String>> components
) {
    private static final Codec<TradeTargetRef> OBJECT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.optionalFieldOf("item").forGetter(TradeTargetRef::itemId),
            Codec.STRING.optionalFieldOf("tag").forGetter(TradeTargetRef::tagId),
            Codec.STRING.optionalFieldOf("source_key").forGetter(TradeTargetRef::sourceKey),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("components").forGetter(TradeTargetRef::components)
        ).apply(instance, TradeTargetRef::new)
    );

    public static final Codec<TradeTargetRef> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC)
        .xmap(
            either -> either.map(TradeTargetRef::fromString, ref -> ref),
            ref -> ref.canEncodeAsString()
                ? Either.left(ref.asString())
                : Either.right(ref)
        );

    public TradeTargetRef {
        itemId = itemId.map(String::strip).filter(s -> !s.isEmpty());
        tagId = tagId.map(String::strip).filter(s -> !s.isEmpty());
        sourceKey = sourceKey.map(String::strip).filter(s -> !s.isEmpty());
        components = components.map(Map::copyOf);

        int selectorCount = (itemId.isPresent() ? 1 : 0) + (tagId.isPresent() ? 1 : 0) + (sourceKey.isPresent() ? 1 : 0);
        if (selectorCount != 1) {
            throw new IllegalArgumentException("TradeTargetRef must define exactly one of item, tag, or source_key");
        }
        if (components.isPresent() && itemId.isEmpty()) {
            throw new IllegalArgumentException("TradeTargetRef components require an item target");
        }
    }

    public static TradeTargetRef item(String itemId) {
        return new TradeTargetRef(Optional.of(itemId), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static TradeTargetRef item(String itemId, Map<String, String> components) {
        return new TradeTargetRef(Optional.of(itemId), Optional.empty(), Optional.empty(), Optional.of(components));
    }

    public static TradeTargetRef tag(String tagId) {
        return new TradeTargetRef(Optional.empty(), Optional.of(tagId), Optional.empty(), Optional.empty());
    }

    public static TradeTargetRef sourceKey(String sourceKey) {
        return new TradeTargetRef(Optional.empty(), Optional.empty(), Optional.of(sourceKey), Optional.empty());
    }

    public static TradeTargetRef fromString(String raw) {
        String value = Objects.requireNonNull(raw, "raw").strip();
        if (value.startsWith("@")) {
            return sourceKey(value.substring(1));
        }
        if (value.startsWith("#")) {
            return tag(value.substring(1));
        }
        return item(value);
    }

    public static TradeTargetRef exactItem(String itemId, Map<String, String> components) {
        return item(itemId, components);
    }

    public boolean isItem() {
        return itemId.isPresent();
    }

    public boolean isTag() {
        return tagId.isPresent();
    }

    public boolean isSourceKey() {
        return sourceKey.isPresent();
    }

    public boolean hasComponents() {
        return components.isPresent() && !components.get().isEmpty();
    }

    public int matchSpecificity(ItemStack stack, Optional<String> sourceKey) {
        return matchSpecificity(stack, sourceKey, null);
    }

    public int matchSpecificity(ItemStack stack, Optional<String> sourceKey, TradeItemKey itemKey) {
        if ((stack == null || stack.isEmpty()) && itemKey == null) {
            return -1;
        }

        if (hasComponents()) {
            if (itemId.isEmpty()) {
                return -1;
            }
            if (itemKey != null && itemKey.matchesExactItem(itemId.orElseThrow(), components.orElse(Map.of()))) {
                return 3;
            }
            return -1;
        }

        if (sourceKey.isPresent() && isSourceKey()) {
            String expected = sourceKey.get();
            if (expected.equals(this.sourceKey.orElseThrow())) {
                return 2;
            }
            return -1;
        }

        if (isItem()) {
            if (itemKey != null && itemKey.itemId().toString().equals(itemId.orElseThrow())) {
                return 1;
            }
            return stack != null && !stack.isEmpty() && matchesItemId(stack) ? 1 : -1;
        }

        if (isTag()) {
            if (itemKey != null) {
                return TagLookupCache.matchesItem(itemKey.itemId(), "#" + tagId.orElseThrow()) ? 0 : -1;
            }
            return stack != null && !stack.isEmpty() && TagLookupCache.matchesItem(stack, "#" + tagId.orElseThrow()) ? 0 : -1;
        }

        return -1;
    }

    private boolean matchesItemId(ItemStack stack) {
        if (!isItem()) {
            return false;
        }
        String expected = itemId.orElseThrow();
        if (expected.startsWith("#")) {
            return TagLookupCache.matchesItem(stack, expected);
        }
        return TagLookupCache.matchesItem(stack, expected);
    }

    public String asString() {
        if (sourceKey.isPresent()) {
            return "@" + sourceKey.get();
        }
        if (tagId.isPresent()) {
            return "#" + tagId.get();
        }
        return itemId.orElseThrow();
    }

    public boolean canEncodeAsString() {
        return components.isEmpty();
    }

    /**
     * 生成适合做 map key / 去重 key 的稳定字符串。
     *
     * 对于精确组件目标，会使用 itemId + JSON 组件签名。
     */
    public String canonicalKey() {
        if (components.isEmpty()) {
            return asString();
        }

        JsonObject json = new JsonObject();
        components.get().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> json.addProperty(entry.getKey(), entry.getValue()));
        return itemId.orElseThrow() + "|" + json;
    }
}
