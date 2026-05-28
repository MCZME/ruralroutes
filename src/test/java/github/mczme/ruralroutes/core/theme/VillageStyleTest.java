package github.mczme.ruralroutes.core.theme;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VillageStyleTest {

    @Test
    void stylesPointAtBaseVanillaVillageStructures() {
        assertEquals(ResourceLocation.parse("minecraft:village_plains"), VillageStyle.PLAINS.structureId());
        assertEquals(ResourceLocation.parse("minecraft:village_desert"), VillageStyle.DESERT.structureId());
        assertEquals(ResourceLocation.parse("minecraft:village_savanna"), VillageStyle.SAVANNA.structureId());
        assertEquals(ResourceLocation.parse("minecraft:village_taiga"), VillageStyle.TAIGA.structureId());
        assertEquals(ResourceLocation.parse("minecraft:village_snowy"), VillageStyle.SNOWY.structureId());
    }
}
