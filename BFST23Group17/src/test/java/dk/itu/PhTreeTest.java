package dk.itu;

import dk.itu.datastructure.phtree.PhTree;
import dk.itu.model.OsmPathNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PhTreeTest {

    private PhTree tree;

    @BeforeAll
    public void beforeAll() {
        tree = new PhTree();
        tree.put(new OsmPathNode(1, 2.0f, 2.0f));
        tree.put(new OsmPathNode(2, 2.0f, 2.0f)); // Shouldn't be added
        tree.put(new OsmPathNode(1, 1.0f, 1.0f)); // Shouldn't be added
        tree.put(new OsmPathNode(3, 2.0f, 2.0f));
        tree.put(new OsmPathNode(4, 3.0f, 3.0f));
        tree.put(new OsmPathNode(5, 5.0f, 5.0f));
    }


    @Test
    public void testPhDuplicates(){
        assertEquals(2, tree.windowQuery(1, 2, 1, 2).size());
    }

    @Test
    public void testWQ(){
        assertEquals(3, tree.windowQuery(0, 3, 0, 3).size());
    }
}
