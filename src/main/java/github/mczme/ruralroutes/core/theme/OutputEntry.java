package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 输出物品条目
 */
public record OutputEntry(
    String item,
    int count
) {
    public static final Codec<OutputEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.fieldOf("item").forGetter(OutputEntry::item),
            Codec.INT.fieldOf("count").forGetter(OutputEntry::count)
        ).apply(instance, OutputEntry::new)
    );
}
