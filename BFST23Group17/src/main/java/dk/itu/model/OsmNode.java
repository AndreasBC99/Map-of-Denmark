package dk.itu.model;

import dk.itu.utils.Theme;
import dk.itu.view.MapView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Optional;


public class OsmNode implements OsmElement {
    protected final long id;
    protected final float y, x;

    protected String memberRole;

    public OsmNode(long _id, float _y, float _x) {
        id = _id;
        y = _y;
        x = _x;
    }

    public long getId () {
        return id;
    }

    @Override
    public String getName(){return null;}
    @Override
    public int getLayer() {
        return 0;
    }

    @Override
    public void draw (MapView mapView, Optional < Color > fillColor, Optional < Color > borderColor){}

    @Override
    public float getMinX() {
        return x;
    }

    @Override
    public float getMaxX() {
        return x;
    }

    @Override
    public float getMinY() {
        return y;
    }

    @Override
    public float getMaxY() {
        return y;
    }

    @Override
    public OsmElementType getType() {
        return OsmElementType.NODE;
    }

    @Override
    public boolean shouldBeDrawn() {
        return false;
    }

    @Override
    public OsmNode setMemberType(String memberType) {
        memberRole = memberType.intern();
        return this;
    }

    @Override
    public boolean isRoad() {
        return false;
    }

    @Override
    public boolean getIndependent(){return true;}

    @Override
    public void setIndependent(boolean val){}

    @Override
    public Theme.ColorDescription getColorDescription() {
        return null;
    }
  
    public boolean isHC() {
        return false;

    }
}

