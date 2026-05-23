package github.mczme.ruralroutes.data.content;

import github.mczme.ruralroutes.core.theme.InputEntry;
import github.mczme.ruralroutes.core.theme.OutputEntry;
import github.mczme.ruralroutes.data.builder.ThemeBuilder;
import github.mczme.ruralroutes.data.builder.TradeProfileBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * 内置玩法数据生成时复用的轻量辅助方法。
 */
public final class ContentRefs {
    private ContentRefs() {
    }

    public static ThemeBuilder theme(String name) {
        return ThemeBuilder.create(name).registrar((id, template) -> {});
    }

    public static TradeProfileBuilder profile(String name) {
        return TradeProfileBuilder.create(name).registrar((id, tradeProfile) -> {});
    }

    public static String tagRef(TagKey<Item> tag) {
        return "#" + tag.location();
    }

    public static String sourceRef(String key) {
        return "@" + key;
    }

    public static InputEntry in(String item, int count) {
        return new InputEntry(item, count);
    }

    public static OutputEntry out(String item, int count) {
        return new OutputEntry(item, count);
    }

    @SafeVarargs
    public static List<InputEntry> inputs(InputEntry... entries) {
        return List.of(entries);
    }

    @SafeVarargs
    public static List<OutputEntry> outputs(OutputEntry... entries) {
        return List.of(entries);
    }

    public static List<String> currencies(String... ids) {
        return List.of(ids);
    }

    public static List<String> refs(String... ids) {
        return List.of(ids);
    }
}
