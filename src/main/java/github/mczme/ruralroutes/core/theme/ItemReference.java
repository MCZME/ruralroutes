package github.mczme.ruralroutes.core.theme;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 物品引用，支持标签、精确物品，以及带组件的精确物品。
 * 可使用纯字符串、单引用对象，或混合候选组对象。
 */
public record ItemReference(
    Optional<String> id,
    Optional<List<ItemEntry>> items,
    Optional<Integer> pick,
    Optional<String> key,
    Optional<Map<String, String>> components
) {
    public ItemReference {
        id = id.map(String::strip).filter(str -> !str.isEmpty());
        items = items.map(list -> List.copyOf(list.stream().map(Objects::requireNonNull).toList()));
        key = key.map(String::strip).filter(str -> !str.isEmpty());
        components = components.map(Map::copyOf);

        boolean hasId = id.isPresent();
        boolean hasItems = items.isPresent();
        if (hasId == hasItems) {
            throw new IllegalArgumentException("ItemReference must define exactly one of id or items");
        }
        if (items.isPresent() && items.get().isEmpty()) {
            throw new IllegalArgumentException("ItemReference items cannot be empty");
        }
        if (components.isPresent() && id.isEmpty()) {
            throw new IllegalArgumentException("ItemReference components require a single item id");
        }
    }

    private static final Codec<ItemEntry> ITEM_ENTRY_CODEC = ItemEntry.CODEC;

    private static final Codec<ItemReference> OBJECT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.optionalFieldOf("id").forGetter(ItemReference::id),
            ITEM_ENTRY_CODEC.listOf().optionalFieldOf("items").forGetter(ItemReference::items),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("pick").forGetter(ItemReference::pick),
            Codec.STRING.optionalFieldOf("key").forGetter(ItemReference::key),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("components").forGetter(ItemReference::components)
        ).apply(instance, ItemReference::new)
    );

    public static final Codec<ItemReference> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC)
        .xmap(
            either -> either.map(ItemReference::single, ref -> ref),
            ref -> ref.canUseStringShorthand()
                ? Either.left(ref.id().orElseThrow())
                : Either.right(ref)
        );

    public static ItemReference single(String id) {
        return new ItemReference(Optional.of(id), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static ItemReference single(String id, Integer pick) {
        return new ItemReference(Optional.of(id), Optional.empty(), Optional.ofNullable(pick), Optional.empty(), Optional.empty());
    }

    public static ItemReference single(String id, Map<String, String> components) {
        return new ItemReference(Optional.of(id), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(components));
    }

    public static ItemReference single(String id, Integer pick, Map<String, String> components) {
        return new ItemReference(Optional.of(id), Optional.empty(), Optional.ofNullable(pick), Optional.empty(), Optional.of(components));
    }

    public static ItemReference group(List<String> refs, Integer pick, String key) {
        return new ItemReference(
            Optional.empty(),
            Optional.of(refs.stream().map(ItemEntry::fromString).toList()),
            Optional.ofNullable(pick),
            Optional.ofNullable(key),
            Optional.empty()
        );
    }

    public static ItemReference groupEntries(List<ItemEntry> refs, Integer pick, String key) {
        return new ItemReference(Optional.empty(), Optional.of(refs), Optional.ofNullable(pick), Optional.ofNullable(key), Optional.empty());
    }

    public boolean isSingle() {
        return id.isPresent();
    }

    public boolean isGroup() {
        return items.isPresent();
    }

    public List<String> refs() {
        return items.map(list -> list.stream().map(ItemEntry::ref).toList())
            .orElseGet(() -> List.of(id.orElseThrow()));
    }

    public List<ItemEntry> itemEntries() {
        return items.orElseGet(() -> List.of(new ItemEntry(Optional.of(id.orElseThrow()), components)));
    }

    public String sourceKey() {
        if (key.isPresent()) {
            return key.get();
        }
        if (id.isPresent()) {
            return id.get();
        }
        return "group:" + String.join("|", refs());
    }

    public String debugLabel() {
        return isSingle() ? id.orElse("<unknown>") : refs().toString();
    }

    public boolean canUseStringShorthand() {
        return id.isPresent() && items.isEmpty() && pick.isEmpty() && key.isEmpty() && components.isEmpty();
    }

    public boolean isTag() {
        return id.map(ref -> ref.startsWith("#")).orElse(false);
    }

    public String itemId() {
        String ref = id.orElseThrow(() -> new IllegalStateException("Grouped ItemReference has no single item id"));
        return isTag() ? ref.substring(1) : ref;
    }

    public boolean hasPickLimit() {
        return pick.isPresent();
    }

    public boolean isExactItem() {
        return id.isPresent() && !isTag();
    }
}
