package dk.itu;

import dk.itu.datastructure.DijkstraSPNew;
import dk.itu.model.OsmPathNode;
import dk.itu.view.components.RouteTypeComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DijkstraTest {

    private OsmPathNode startNode, endNode;

    private DijkstraSPNew dijk;

    @BeforeEach
    public void setup(){
        startNode = new OsmPathNode(1, 55.6601784f, 12.5931273f);//Intersection of Rued Langgaards Vej & Amagerfælledvej
        endNode = new OsmPathNode(2, 55.6586704f, 12.5926689f);//Intersection of Kaj Munks Vej & Amagerfælledvej

        dijk = new DijkstraSPNew(startNode, endNode, RouteTypeComponent.options.FOOT);
    }

    //It is difficult to make a unit test for the exact distance match in the heuristic.
    //Therefore, we test that the heuristic is longer than 100m as is expected
    @Test
    public void testHeuristicDistance(){
        float expectedMinDistance = 0.1f;//Expected distance more than 100 m
        float actualDistance = (float) startNode.getHeuristicDist();
        assertTrue(actualDistance > expectedMinDistance);
    }

}
