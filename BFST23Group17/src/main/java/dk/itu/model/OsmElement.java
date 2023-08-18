package dk.itu.model;


import dk.itu.utils.Theme;
import dk.itu.datastructure.phtree.PhNode;

import dk.itu.view.MapView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.Optional;

public interface OsmElement extends Serializable, PhNode {
    float getMinX();

    float getMaxX();

    float getMinY();

    float getMaxY();

    OsmElementType getType();

    int getLayer();

    long getId();

    String getName();
    void draw(MapView mapView, Optional<Color> fillColor, Optional<Color> borderColor);
    boolean shouldBeDrawn();
    OsmElement setMemberType(String memberType);
    boolean isRoad();

    enum OsmElementType {
        NODE,
        NODE_INFO,
        NODE_PATH,
        WAY,
        RELATION,
        RELATION_MULTIPOLYGON
    }

    public void setIndependent(boolean val);

    public boolean getIndependent();

    public Theme.ColorDescription getColorDescription();
}
