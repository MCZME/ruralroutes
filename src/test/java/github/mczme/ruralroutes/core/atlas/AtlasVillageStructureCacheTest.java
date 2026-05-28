package github.mczme.ruralroutes.core.atlas;

import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasVillageStructureCacheTest {

    private static final ResourceLocation OVERWORLD = ResourceLocation.parse("minecraft:overworld");
    private static final ResourceLocation PLAINS_VILLAGE = ResourceLocation.parse("minecraft:village_plains");

    @Test
    void savedDataRoundTripsEntries() {
        AtlasVillageStructureCache cache = new AtlasVillageStructureCache();
        AtlasVillageStructureCache.Entry entry = cache.remember(
            OVERWORLD,
            VillageStyle.PLAINS,
            PLAINS_VILLAGE,
            new ChunkPos(10, -4),
            new BlockPos(160, 0, -64)
        );

        CompoundTag saved = cache.save(new CompoundTag(), null);
        AtlasVillageStructureCache loaded = AtlasVillageStructureCache.load(saved, null);

        assertEquals(1, loaded.size());
        assertEquals(entry, loaded.entries().getFirst());
    }

    @Test
    void rememberDeduplicatesByStructureKey() {
        AtlasVillageStructureCache cache = new AtlasVillageStructureCache();
        AtlasVillageStructureCache.Entry first = cache.remember(
            OVERWORLD,
            VillageStyle.PLAINS,
            PLAINS_VILLAGE,
            new ChunkPos(10, -4),
            new BlockPos(160, 0, -64)
        );
        AtlasVillageStructureCache.Entry duplicate = cache.remember(
            OVERWORLD,
            VillageStyle.PLAINS,
            PLAINS_VILLAGE,
            new ChunkPos(10, -4),
            new BlockPos(168, 0, -56)
        );

        assertEquals(first.id(), duplicate.id());
        assertEquals(1, cache.size());
        assertTrue(cache.contains(OVERWORLD, VillageStyle.PLAINS, PLAINS_VILLAGE, new ChunkPos(10, -4)));
    }

    @Test
    void findNearestCanApplyPlayerKnowledgeFilter() {
        AtlasVillageStructureCache cache = new AtlasVillageStructureCache();
        UUID blockedId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID usableId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        cache.remember(new AtlasVillageStructureCache.Entry(
            blockedId,
            OVERWORLD,
            VillageStyle.PLAINS,
            PLAINS_VILLAGE,
            1,
            0,
            new BlockPos(16, 0, 0)
        ));
        cache.remember(new AtlasVillageStructureCache.Entry(
            usableId,
            OVERWORLD,
            VillageStyle.PLAINS,
            PLAINS_VILLAGE,
            8,
            0,
            new BlockPos(128, 0, 0)
        ));

        AtlasVillageStructureCache.Entry nearest = cache.findNearest(
            OVERWORLD,
            VillageStyle.PLAINS,
            BlockPos.ZERO,
            entry -> !entry.id().equals(blockedId)
        ).orElseThrow();

        assertEquals(usableId, nearest.id());
    }
}
