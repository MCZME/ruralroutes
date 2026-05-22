package github.mczme.ruralroutes.core.theme;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeTemplateItemReferenceTest {

    @Test
    void singleReferenceKeepsStringShorthandBehavior() {
        // 单引用是主题 JSON 最常见的写法，保证它不会被后续扩展破坏。
        ItemReference reference = ItemReference.single("#ruralroutes:pool/crop");

        assertTrue(reference.isSingle());
        assertTrue(reference.canUseStringShorthand());
        assertTrue(reference.isTag());
        assertEquals("ruralroutes:pool/crop", reference.itemId());
        assertEquals("#ruralroutes:pool/crop", reference.sourceKey());
    }

    @Test
    void groupedReferenceUsesStableDerivedSourceKey() {
        // group 来源键会参与库存 specific 匹配，因此必须是稳定且可预测的。
        ItemReference reference = ItemReference.group(
                List.of("#ruralroutes:pool/crop", "minecraft:bread"),
                1,
                null
        );

        assertTrue(reference.isGroup());
        assertFalse(reference.canUseStringShorthand());
        assertEquals(List.of("#ruralroutes:pool/crop", "minecraft:bread"), reference.refs());
        assertEquals("group:#ruralroutes:pool/crop|minecraft:bread", reference.sourceKey());
        assertTrue(reference.hasPickLimit());
    }

    @Test
    void componentItemKeepsObjectShorthandOnlyWhenNeeded() {
        ItemReference reference = ItemReference.single(
            "minecraft:book",
            Map.of("minecraft:custom_name", "{\"text\":\"Catalog\"}")
        );

        assertTrue(reference.isSingle());
        assertFalse(reference.canUseStringShorthand());
        assertEquals("minecraft:book", reference.itemId());
        assertTrue(reference.components().isPresent());
        assertEquals(
            Map.of("minecraft:custom_name", "{\"text\":\"Catalog\"}"),
            reference.itemEntries().get(0).components().orElseThrow()
        );
    }

    @Test
    void constructorRejectsMissingOrConflictingDefinitions() {
        // 这里把几种非法输入一次性卡死，避免数据包在运行期才暴露结构错误。
        assertThrows(IllegalArgumentException.class, () -> new ItemReference(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        ));

        assertThrows(IllegalArgumentException.class, () -> new ItemReference(
                Optional.of("minecraft:bread"),
                Optional.of(List.of(ItemEntry.fromString("minecraft:apple"))),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        ));

        assertThrows(IllegalArgumentException.class, () -> new ItemReference(
                Optional.empty(),
                Optional.of(List.of()),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        ));
    }
}
