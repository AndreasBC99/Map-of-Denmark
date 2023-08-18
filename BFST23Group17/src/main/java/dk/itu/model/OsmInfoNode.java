package dk.itu.model;

import dk.itu.utils.Theme;
import dk.itu.utils.ThemesFactory;
import dk.itu.view.MapView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.Map;
import java.util.Optional;

public class OsmInfoNode extends OsmNode {
    private Theme.ColorDescription colorDescription;
    private String housenumber, name;

    private float rotation;//For rotated icons eg. one-way

    public OsmInfoNode(long _id, float _y, float _x, Map<String, String> _tags) {
        super(_id, _y, _x);

        if (!_tags.isEmpty()){
            colorDescription = ThemesFactory.getTheme().getColorDescription(_tags);
            if (_tags.containsKey("name")) name = _tags.get("name").intern();
            if (_tags.containsKey("addr:housenumber")) housenumber = _tags.get("addr:housenumber").intern();
        };
    }

    @Override
    public long getId () {
        return id;
    }

    @Override
    public void draw (MapView mapView, Optional < Color > fillColor, Optional < Color > borderColor){
        GraphicsContext gc = mapView.gc;
        //Draw cartographic elements associated with the node -> icons or house numbers
        if(colorDescription.getIcon() != null){
            gc.setFont(MapView.getIconFont());
            gc.setFill(Color.web(String.valueOf(colorDescription.getIconColor())));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);

            //Determine if the canvas should be rotated to display arrow correctly
            if(rotation != 0){
                gc.save();
                gc.translate(0.56*getMinX(), -getMinY());
                gc.rotate(rotation);
                gc.translate(-0.56*getMinX(), getMinY());
                gc.fillText(colorDescription.getIcon(), 0.56*getMinX(), -getMinY());
                gc.restore();
            } else{
                gc.fillText(colorDescription.getIcon(), 0.56*getMinX(), -getMinY());
            }
        }

        if(name != null){
            gc.setFont(MapView.getTextFont());
            gc.fillText(name, 0.56*getMinX(), (-1)*(getMinY()-MapView.getDrawingSize()*12));
        }

        if(housenumber != null){
            gc.setFont(MapView.getTextFont());
            gc.setFill(Color.web("acaaa9"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.fillText(housenumber, 0.56*getMinX(), (-1)*getMinY());
        }
    }

    @Override
    public int getLayer() {
        return colorDescription.getLayer();
    }

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
        return OsmElementType.NODE_INFO;
    }

    @Override
    public boolean shouldBeDrawn() {
        if (colorDescription == null) {
            System.out.println(id);
        }
        return colorDescription.getVisible();
    }

    @Override
    public boolean isRoad() {
        return false;
    }

    public void setRotation(float _rotation){
        rotation = _rotation;
    }
}

