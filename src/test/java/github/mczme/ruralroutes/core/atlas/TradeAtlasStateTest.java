package github.mczme.ruralroutes.core.atlas;

import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TradeAtlasStateTest {

    @Test
    void upsertReplacesExistingNodeByIdWhenRecordedPositionDiffersFromCluePosition() {
        ResourceLocation dimensionId = ResourceLocation.parse("minecraft:overworld");
        BlockPos cluePos = new BlockPos(0, 0, 0);
        BlockPos stationPos = new BlockPos(48, 64, 32);
        TradeAtlasState state = TradeAtlasState.empty();
        TradeAtlasNode clue = TradeAtlasNode.clue(dimensionId, cluePos, VillageStyle.PLAINS);

        assertTrue(state.addClueNode(clue));

        TradeAtlasNode recorded = TradeAtlasNode.recorded(
            clue.id(),
            dimensionId,
            stationPos,
            VillageStyle.PLAINS,
            ResourceLocation.parse("ruralroutes:plains_granary")
        );
        state.upsertNodeAtLocation(recorded, true);

        assertEquals(1, state.nodes().size());
        assertFalse(state.hasPendingClue());
        assertTrue(state.findNodeAt(dimensionId, cluePos).isEmpty());
        assertEquals(AtlasNodeStatus.RECORDED, state.findNodeAt(dimensionId, stationPos).orElseThrow().status());
    }

    @Test
    void findClueByStyleOnlyReturnsMatchingClue() {
        ResourceLocation dimensionId = ResourceLocation.parse("minecraft:overworld");
        TradeAtlasState state = TradeAtlasState.empty();
        UUID recordedId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        state.upsertNodeAtLocation(TradeAtlasNode.recorded(
            recordedId,
            dimensionId,
            new BlockPos(0, 64, 0),
            VillageStyle.PLAINS,
            null
        ), false);
        TradeAtlasNode clue = TradeAtlasNode.clue(dimensionId, new BlockPos(128, 0, 128), VillageStyle.DESERT);
        state.addClueNode(clue);

        assertTrue(state.findClueByStyle(dimensionId, VillageStyle.PLAINS).isEmpty());
        assertEquals(clue.id(), state.findClueByStyle(dimensionId, VillageStyle.DESERT).orElseThrow().id());
    }

    @Test
    void cancellingPendingClueKeepsClueNodeButAllowsFutureLocate() {
        ResourceLocation dimensionId = ResourceLocation.parse("minecraft:overworld");
        TradeAtlasState state = TradeAtlasState.empty();
        TradeAtlasNode clue = TradeAtlasNode.clue(dimensionId, new BlockPos(128, 0, 128), VillageStyle.DESERT);

        assertTrue(state.addClueNode(clue));
        assertTrue(state.hasPendingClue());

        state.clearPendingClue();

        assertFalse(state.hasPendingClue());
        assertEquals(clue.id(), state.findNodeAt(dimensionId, clue.position()).orElseThrow().id());
        assertEquals(AtlasNodeStatus.CLUE, state.findNodeAt(dimensionId, clue.position()).orElseThrow().status());
    }

    @Test
    void addingNewClueAfterCancellingRepointsPendingClue() {
        ResourceLocation dimensionId = ResourceLocation.parse("minecraft:overworld");
        TradeAtlasState state = TradeAtlasState.empty();
        TradeAtlasNode firstClue = TradeAtlasNode.clue(dimensionId, new BlockPos(128, 0, 128), VillageStyle.DESERT);
        TradeAtlasNode secondClue = TradeAtlasNode.clue(dimensionId, new BlockPos(256, 0, 256), VillageStyle.PLAINS);

        assertTrue(state.addClueNode(firstClue));
        state.clearPendingClue();
        assertTrue(state.addClueNode(secondClue));

        assertTrue(state.hasPendingClue());
        assertEquals(secondClue.id(), state.pendingClue().orElseThrow().id());
        assertNotEquals(firstClue.id(), state.pendingClue().orElseThrow().id());
        assertEquals(2, state.nodes().size());
    }
}
