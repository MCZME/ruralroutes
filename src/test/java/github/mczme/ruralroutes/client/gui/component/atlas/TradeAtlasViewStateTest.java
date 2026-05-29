package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import github.mczme.ruralroutes.core.atlas.TradeRoute;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeAtlasViewStateTest {

    @Test
    void opensWithoutSelectingTargetOrFirstNode() {
        TestAtlas atlas = atlasWithRoute();
        atlas.state().setCurrentTarget(atlas.firstNode().id());

        TradeAtlasViewState viewState = new TradeAtlasViewState(atlas.state());

        assertEquals(TradeAtlasViewState.SelectionType.NONE, viewState.selectionType());
        assertTrue(viewState.selectedNode(atlas.state()).isEmpty());
        assertTrue(viewState.selectedRoute(atlas.state()).isEmpty());
    }

    @Test
    void explicitSelectionMovesBetweenNodeRouteStopAndSegment() {
        TestAtlas atlas = atlasWithRoute();
        TradeAtlasViewState viewState = new TradeAtlasViewState(atlas.state());

        viewState.selectNode(atlas.firstNode());
        assertEquals(TradeAtlasViewState.SelectionType.NODE, viewState.selectionType());
        assertEquals(atlas.firstNode().id(), viewState.selectedNode(atlas.state()).orElseThrow().id());
        assertTrue(viewState.selectedRoute(atlas.state()).isEmpty());

        viewState.selectRoute(atlas.route());
        assertEquals(TradeAtlasViewState.SelectionType.ROUTE, viewState.selectionType());
        assertEquals(atlas.route().id(), viewState.selectedRoute(atlas.state()).orElseThrow().id());
        assertTrue(viewState.selectedNode(atlas.state()).isEmpty());
        assertTrue(viewState.selectedRouteStop(atlas.state()).isEmpty());
        assertTrue(viewState.selectedRouteSegment(atlas.state()).isEmpty());

        viewState.selectRouteStop(atlas.route(), atlas.route().stops().get(0));
        assertEquals(TradeAtlasViewState.SelectionType.ROUTE_STOP, viewState.selectionType());
        assertEquals(atlas.route().stops().get(0).id(), viewState.selectedRouteStop(atlas.state()).orElseThrow().id());
        assertEquals(atlas.route().id(), viewState.selectedRoute(atlas.state()).orElseThrow().id());

        viewState.selectRouteSegment(atlas.route(), atlas.route().segments().get(0));
        assertEquals(TradeAtlasViewState.SelectionType.ROUTE_SEGMENT, viewState.selectionType());
        assertEquals(atlas.route().segments().get(0).id(),
            viewState.selectedRouteSegment(atlas.state()).orElseThrow().id());
        assertEquals(atlas.route().id(), viewState.selectedRoute(atlas.state()).orElseThrow().id());

        viewState.clearSelection();
        assertEquals(TradeAtlasViewState.SelectionType.NONE, viewState.selectionType());
        assertTrue(viewState.selectedNode(atlas.state()).isEmpty());
        assertTrue(viewState.selectedRoute(atlas.state()).isEmpty());
    }

    @Test
    void missingSelectedObjectDoesNotFallbackToAnotherRoute() {
        TestAtlas atlas = atlasWithRoute();
        TradeRoute secondRoute = TradeRoute.create("Second", 0xFFFFCC44, atlas.secondNode().id(), atlas.firstNode().id());
        atlas.state().addRoute(secondRoute, true);
        TradeAtlasViewState viewState = new TradeAtlasViewState(atlas.state());

        viewState.selectRoute(atlas.route());
        atlas.state().removeRoute(atlas.route().id());

        assertEquals(TradeAtlasViewState.SelectionType.ROUTE, viewState.selectionType());
        assertTrue(viewState.selectedRoute(atlas.state()).isEmpty());
    }

    private static TestAtlas atlasWithRoute() {
        TradeAtlasState state = TradeAtlasState.empty();
        ResourceLocation dimensionId = ResourceLocation.parse("minecraft:overworld");
        TradeAtlasNode firstNode = TradeAtlasNode.recorded(
            UUID.fromString("00000000-0000-0000-0000-000000000101"),
            dimensionId,
            new BlockPos(0, 64, 0),
            VillageStyle.PLAINS,
            null
        );
        TradeAtlasNode secondNode = TradeAtlasNode.recorded(
            UUID.fromString("00000000-0000-0000-0000-000000000102"),
            dimensionId,
            new BlockPos(96, 64, 48),
            VillageStyle.DESERT,
            null
        );
        state.upsertNodeAtLocation(firstNode, true);
        state.upsertNodeAtLocation(secondNode, true);
        TradeRoute route = TradeRoute.create("Route", 0xFF9BD2F7, firstNode.id(), secondNode.id());
        state.addRoute(route, true);
        return new TestAtlas(state, firstNode, secondNode, route);
    }

    private record TestAtlas(
        TradeAtlasState state,
        TradeAtlasNode firstNode,
        TradeAtlasNode secondNode,
        TradeRoute route
    ) {
    }
}
