package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeRouteStateTest {

    @Test
    void routeCanReferenceSameNodeMoreThanOnce() {
        TradeAtlasState state = stateWithNodes();
        UUID firstNodeId = state.nodes().get(0).id();
        UUID secondNodeId = state.nodes().get(1).id();
        TradeRoute route = TradeRoute.create("Loop", 0xFF00AAFF, firstNodeId, secondNodeId)
            .withAddedStop(firstNodeId, null);

        assertTrue(state.addRoute(route, false));

        TradeRoute saved = state.routes().get(0);
        assertEquals(3, saved.stops().size());
        assertEquals(2, saved.stops().stream().filter(stop -> stop.nodeId().equals(firstNodeId)).count());
        assertEquals(2, saved.segments().size());
    }

    @Test
    void routeVisibilityIsExplicitAndSupportsMultipleRoutes() {
        TradeAtlasState state = stateWithNodes();
        UUID firstNodeId = state.nodes().get(0).id();
        UUID secondNodeId = state.nodes().get(1).id();
        TradeRoute first = TradeRoute.create("First", 0xFF00AAFF, firstNodeId, secondNodeId);
        TradeRoute second = TradeRoute.create("Second", 0xFFFFAA00, secondNodeId, firstNodeId);

        assertTrue(state.addRoute(first, false));
        assertTrue(state.addRoute(second, false));

        assertFalse(state.isRouteVisible(first.id()));
        assertFalse(state.isRouteVisible(second.id()));
        assertTrue(state.visibleRoutes().isEmpty());
        state.setRouteVisible(first.id(), true);
        state.setRouteVisible(second.id(), true);

        assertEquals(List.of(first.id(), second.id()), state.visibleRouteIds());
        assertEquals(2, state.visibleRoutes().size());
    }

    @Test
    void deletingStopCannotBreakMinimumRouteShape() {
        TradeAtlasState state = stateWithNodes();
        UUID firstNodeId = state.nodes().get(0).id();
        UUID secondNodeId = state.nodes().get(1).id();
        TradeRoute route = TradeRoute.create("Minimum", 0xFF00AAFF, firstNodeId, secondNodeId);

        TradeRoute unchanged = route.withoutStop(route.stops().get(0).id());

        assertEquals(2, unchanged.stops().size());
        assertEquals(1, unchanged.segments().size());
    }

    @Test
    void codecRoundTripKeepsRoutesAndVisibleRouteSelection() {
        TradeAtlasState state = stateWithNodes();
        UUID firstNodeId = state.nodes().get(0).id();
        UUID secondNodeId = state.nodes().get(1).id();
        TradeRoute route = TradeRoute.create("Codec", 0xFF00AAFF, firstNodeId, secondNodeId)
            .withStatus(TradeRouteStatus.REGULAR);
        assertTrue(state.addRoute(route, true));

        var encoded = TradeAtlasState.CODEC.encodeStart(JsonOps.INSTANCE, state).result().orElseThrow();
        TradeAtlasState decoded = TradeAtlasState.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();

        assertEquals(1, decoded.routes().size());
        assertEquals("Codec", decoded.routes().get(0).name());
        assertEquals(TradeRouteStatus.REGULAR, decoded.routes().get(0).status());
        assertEquals(route.id(), decoded.visibleRouteIds().get(0));
    }

    @Test
    void routeRejectsUnknownNodeReference() {
        TradeAtlasState state = stateWithNodes();
        UUID knownNodeId = state.nodes().get(0).id();
        TradeRoute route = TradeRoute.create("Bad", 0xFF00AAFF, knownNodeId,
            UUID.fromString("00000000-0000-0000-0000-000000000099"));

        assertFalse(state.addRoute(route, false));
        assertTrue(state.routes().isEmpty());
    }

    private static TradeAtlasState stateWithNodes() {
        TradeAtlasState state = TradeAtlasState.empty();
        ResourceLocation dimensionId = ResourceLocation.parse("minecraft:overworld");
        state.upsertNodeAtLocation(TradeAtlasNode.recorded(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            dimensionId,
            new BlockPos(0, 64, 0),
            VillageStyle.PLAINS,
            null
        ), true);
        state.upsertNodeAtLocation(TradeAtlasNode.recorded(
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            dimensionId,
            new BlockPos(128, 64, 96),
            VillageStyle.DESERT,
            null
        ), true);
        return state;
    }
}
