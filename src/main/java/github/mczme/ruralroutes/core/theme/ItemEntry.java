package github.mczme.ruralroutes.core.theme;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * 候选条目中的单个物品定义。
 * 保留字符串简写，必要时可携带组件信息。
 */
public record ItemEntry(
    Optional<String> id,
    Optional<Map<String, String>> components
) {
    public ItemEntry {
        id = id.map(String::strip).filter(str -> !str.isEmpty());
        components = components.map(Map::copyOf);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("ItemEntry requires an id");
        }
    }

    static final Codec<ItemEntry> OBJECT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.optionalFieldOf("id").forGetter(ItemEntry::id),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("components").forGetter(ItemEntry::components)
        ).apply(instance, ItemEntry::new)
    );

    public static final Codec<ItemEntry> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC)
        .xmap(
            either -> either.map(ItemEntry::fromString, entry -> entry),
            entry -> entry.canUseStringShorthand()
                ? Either.left(entry.ref())
                : Either.right(entry)
        );

    public static ItemEntry fromString(String id) {
        return new ItemEntry(Optional.of(id), Optional.empty());
    }

    public boolean canUseStringShorthand() {
        return components.isEmpty();
    }

    public boolean isTag() {
        return id.map(ref -> ref.startsWith("#")).orElse(false);
    }

    public String ref() {
        return id.orElseThrow();
    }
}
