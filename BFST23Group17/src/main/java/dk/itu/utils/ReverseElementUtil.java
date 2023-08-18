package dk.itu.utils;

import dk.itu.model.OsmElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReverseElementUtil {
    private static final Set<Long> reverseRelation = new HashSet<>(List.of(5175856L, 8312746L));
    private static final Set<Long> reverseWay = new HashSet<>(List.of(439423532L, 324241833L));
    public static boolean checkRelation (OsmElement rel) {
        return reverseRelation.contains(rel.getId());
    }
    public static boolean checkWay(OsmElement rel) {
        return reverseWay.contains(rel.getId());
    }
}
