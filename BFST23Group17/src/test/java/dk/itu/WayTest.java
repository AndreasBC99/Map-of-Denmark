package dk.itu;

import dk.itu.model.OsmElement;
import dk.itu.model.OsmNode;
import dk.itu.model.OsmWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WayTest {

    private OsmWay way;

    @BeforeEach
    public void setup(){
        OsmElement element1 = new OsmNode(10, 0.0f,0.0f);
        OsmElement element2 = new OsmNode(11, 0.1f,0.1f);
        OsmElement element3 = new OsmNode(12, 0.2f,0.2f);
        OsmElement element4 = new OsmNode(13, 0.3f,0.3f);
        OsmElement element5 = new OsmNode(14, 0.4f,0.4f);
        List<OsmElement> nodeList = new ArrayList<>(List.of(element1, element2, element3, element4, element5, element1));
        Map<String, String> tags = new HashMap<>();
        tags.put("highway", "motorway");

        way = new OsmWay(1, nodeList, tags, null);
    }

    @Test
    public void testIfComplete(){
        boolean actualValue = way.getIsComplete();
        boolean expectedValue = true;
        assertEquals(expectedValue, actualValue);
    }
}
