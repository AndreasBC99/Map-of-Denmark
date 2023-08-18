package dk.itu.utils;

import dk.itu.model.OsmPathNode;
import dk.itu.view.MapView;
import javafx.geometry.Point2D;

public class NnUtils {
    public static OsmPathNode nn(MapView mapView, Point2D point, boolean car, boolean bike, boolean walk) {
        return mapView.getMapModel().getNewPhTreeNN().nn((float) (point.getX()), (float) point.getY(), car, bike, walk);
    }
}
