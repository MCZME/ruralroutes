package github.mczme.ruralroutes.core.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 输入物品条目
 */
public record InputEntry(
    String item,
    int count
) {
    public static final Codec<InputEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.STRING.fieldOf("item").forGetter(InputEntry::item),
            Codec.INT.fieldOf("count").forGetter(InputEntry::count)
        ).apply(instance, InputEntry::new)
    );
}
