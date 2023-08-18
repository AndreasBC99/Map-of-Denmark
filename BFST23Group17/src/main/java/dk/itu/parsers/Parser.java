package dk.itu.parsers;

import dk.itu.datastructure.phtree.PhTree;
import dk.itu.model.OsmInfoNode;

import java.util.HashMap;
import java.util.List;

public interface Parser {
    List<PhTree> getAllNewPhTrees();
    PhTree getNewPhTreeNN();

    double getMinX();
    double getMinY();
    double getMaxX();
    double getMaxY();

    void addIconToPhTree(OsmInfoNode node);
    HashMap<String, OsmInfoNode> getAddressMap();
}
