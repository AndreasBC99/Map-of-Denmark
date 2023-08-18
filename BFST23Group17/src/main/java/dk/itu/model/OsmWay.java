package dk.itu.model;

import dk.itu.parsers.Parser;
import dk.itu.utils.Theme;
import dk.itu.utils.ThemesFactory;
import dk.itu.view.MapView;
import dk.itu.view.components.DebugDetailsComponent;
import dk.itu.view.components.RouteTypeComponent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.ObjectStreamClass;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OsmWay implements OsmElement {
    private boolean allowWalking, allowCycling, allowDriving;
    private long id = -1;
    private String name;
    private OsmElement[] osmNodes;
    private float minX, minY, maxX, maxY;
    private Theme.ColorDescription colorDescription;
    private boolean isRoad, isOuter;
    private short maxSpeed = 50;//Standard speed
    private boolean isRoundabout = false;
    private boolean independent = true;
    private boolean isOneWay;
    private boolean hasTags;
  
    public OsmWay(long _id, List<OsmElement> _osmNodes, Map<String, String> _tags, Parser parser) {
        id = _id;
        osmNodes = _osmNodes.toArray(new OsmElement[0]);

        if (!_osmNodes.isEmpty()) {
            minX = _osmNodes.get(0).getMinX();
            maxX = _osmNodes.get(0).getMaxX();
            minY = _osmNodes.get(0).getMinY();
            maxY = _osmNodes.get(0).getMaxY();
        }

        if(_tags != null && !_tags.isEmpty()){
            hasTags = true;
            if (_tags.containsKey("highway")) isRoad = true;
            if (_tags.containsKey("name")) name = _tags.get("name").intern();
            if (_tags.containsKey("maxspeed")) maxSpeed = Short.parseShort(_tags.get("maxspeed"));
            if (_tags.containsKey("oneway")) isOneWay = _tags.get("oneway").equals("yes") ? true : false;
            if (_tags.containsKey("junction")){
                isRoundabout = _tags.get("junction").equals("roundabout");
                if(isRoundabout) isOneWay = true;
            }
        } else {
            hasTags = false;
        }

        for(int i = 0; i < _osmNodes.size(); i++) {
            //If the way is oneway -> create oneway icons and add to parser
            if (isOneWay) {
                if (i > 0) {
                    float xicon, yicon;
                    float x1 = osmNodes[i - 1].getMinX();
                    float x2 = osmNodes[i].getMinX();
                    float y1 = osmNodes[i - 1].getMinY();
                    float y2 = osmNodes[i].getMinY();

                    float deltaX = x2 - x1;
                    float deltaY = y2 - y1;
                    float angleDegrees = (float) Math.toDegrees(Math.atan2(deltaX, deltaY));
                    if (angleDegrees < 0) angleDegrees += 360;

                    if (x1 >= x2) xicon = x1 - (x1 - x2) / 2;
                    else xicon = x2 - (x2 - x1) / 2;

                    if (y1 >= y2) yicon = y1 - (y1 - y2) / 2;
                    else yicon = y2 - (y2 - y1) / 2;

                    //Create a OsmInfoNode to represent the oneway-icon
                    Map<String, String> tagList = new HashMap<>();
                    tagList.put("icon", "oneway");
                    OsmInfoNode iconNode = new OsmInfoNode(id + i, yicon, xicon, tagList);
                    iconNode.setRotation(angleDegrees);
                    parser.addIconToPhTree(iconNode);
                }
            }

            //Determine bounds for the object
            if (_osmNodes.get(i).getMinX() < minX) {
                minX = _osmNodes.get(i).getMinX();
            } else if (_osmNodes.get(i).getMaxX() > maxX) { // else if because if new min is found, new max isn't present
                maxX = _osmNodes.get(i).getMaxX();
            }
            if (_osmNodes.get(i).getMinY() < minY) {
                minY = _osmNodes.get(i).getMinY();
            } else if (_osmNodes.get(i).getMaxY() > maxY) { // else if because if new min is found, new max isn't present
                maxY = _osmNodes.get(i).getMaxY();
            }
        }

        allowWalking = true;
        allowCycling = true;
        allowDriving = true;

        colorDescription = ThemesFactory.getTheme().getColorDescription(_tags);

        //Determine if the way has an associated icon -> if so, add to parser
        if(colorDescription.getIcon() != null){
            float iconX = getMinX() + ((getMaxX() - getMinX()) / 2);
            float iconY = getMinY() + ((getMaxY() - getMinY()) / 2);
            OsmInfoNode iconNode = new OsmInfoNode(id + 1, iconY, iconX, _tags);
            parser.addIconToPhTree(iconNode);
        }
    }

    public OsmElement first(){
        return osmNodes[0];
    }

    public OsmElement last(){
        return osmNodes[osmNodes.length - 1];
    }

    @Override
    public long getId() {
        return id;
    }

    public void setRoundAbout(boolean b){isRoundabout = b;}

    public boolean getRoundAbout(){return isRoundabout;}

    public void setIndependent(boolean val){ independent = val;}

    public boolean getIndependent(){return independent;}

    public String getName(){
        return name == null ? "unnamed" : name;
    }

    @Override
    public Theme.ColorDescription getColorDescription() {
        return colorDescription;
    }

    public List<OsmElement> getOsmNodes() {
        return Arrays.asList(osmNodes);
    }

    // Determine if the way is closed -> the first and the last node is equal
    public boolean getIsComplete() {
        return osmNodes[0].equals(osmNodes[osmNodes.length-1]);
    }

    //Determine if a way should be filled or not depending on tags and complete-ness
    private boolean shouldFill(){
        return (getColorDescription().getShouldFill() && getIsComplete());
    }

    public void draw(MapView mapView, Optional<Color> _fillColor, Optional<Color> _borderColor) {
        GraphicsContext gc = mapView.gc;

        gc.setLineDashes(null); // Reset dashing when a new was is about to be drawn
        gc.beginPath();
        gc.setLineWidth(getColorDescription().getWidth() * MapView.getDrawingSize());
        if(getColorDescription().getDashed()) gc.setLineDashes(2.25*gc.getLineWidth()); // Set the dash as 2.25 times the size of line-width

        int nodesToSkip = 1;

        if (getIsComplete()) {
            if (getColorDescription().getNodeSkip() != null) {
                for (Map.Entry<Float, Integer> entry : getColorDescription().getNodeSkip().entrySet()) {
                    if (entry.getKey() <= mapView.getScreenWidthX() && entry.getValue() > nodesToSkip) {
                        nodesToSkip = entry.getValue();
                    }
                }
            }

            if (osmNodes.length < nodesToSkip*3) {
                return;
            }
        }

        gc.moveTo(0.56* osmNodes[0].getMinX(), -osmNodes[0].getMinY());

        for (int i = 1; i < osmNodes.length; i+=nodesToSkip) {
            gc.lineTo(0.56* osmNodes[i].getMinX(), -osmNodes[i].getMinY());
        }

        if (getIsComplete()) gc.closePath();

        // Check if the way should be filled or not
        if(shouldFill()){
            gc.setFill((_fillColor.orElse(Color.web(getColorDescription().getFillColor()))));
            gc.fill();
        }

        gc.setStroke(_borderColor.orElse(Color.web(getColorDescription().getBorderColor())));
        gc.stroke();

        //Draw cartographic elements associated with the element -> icon, name, address number etc.
        if(MapView.getZoomLevel() <= 10) return;//Stop the function if the zoomLevel is not high enough
        double iconX = getMinX() + ((getMaxX() - getMinX()) / 2);
        double iconY = getMinY() + ((getMaxY() - getMinY()) / 2);
        if(getColorDescription().getIcon() != null){
            gc.setFont(MapView.getIconFont());
            gc.setFill(Color.web(getColorDescription().getIconColor()));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.fillText(getColorDescription().getIcon(), 0.56*iconX, (-1)*iconY);
        } else{
            gc.setFill(Color.web("#34495e"));
        }

    }

    public String getMemberType() {
        return isOuter ? "outer" : "inner";
    }

    public boolean getWalking(){return allowWalking;}

    public boolean getCycling(){return allowCycling;}

    public boolean getDriving(){return allowDriving;}

    public boolean getTransportation(RouteTypeComponent.options value){
        if(value == RouteTypeComponent.options.FOOT) return getWalking();
        if(value == RouteTypeComponent.options.BIKE) return getCycling();
        if(value == RouteTypeComponent.options.VEHICLE) return getDriving();
        return false;
    }

    public void setWalking(boolean value){
        allowWalking = value;
    }

    public void setCycling(boolean value){
        allowCycling = value;
    }

    public void setDriving(boolean value){
        allowDriving = value;
    }

    public void replaceWithPathNodes(List<OsmPathNode> newNodes) {
        osmNodes = newNodes.toArray(OsmPathNode[]::new);
    }

    @Override
    public OsmWay setMemberType(String memberType) {
        isOuter = memberType.equals("outer");
        return this;
    }

    @Override
    public int getLayer() {
        return getColorDescription().getLayer();
    }

    @Override
    public float getMinX() {
        return minX;
    }
    @Override
    public float getMaxX() {
        return maxX;
    }
    @Override
    public float getMinY() {
        return minY;
    }
    @Override
    public float getMaxY() {
        return maxY;
    }
    @Override
    public OsmElementType getType() {
        return OsmElementType.WAY;
    }
    @Override
    public boolean shouldBeDrawn() {
        return colorDescription.getVisible();
    }
    @Override
    public boolean isRoad() {
        return isRoad;
    }

    public short getMaxSpeed() {
        return maxSpeed;
    }

    public boolean getIsOneWay(){return isOneWay;}

    @Override
    public boolean isHC() {
        return false;
    }

    public boolean hasTags() {
        return hasTags;
    }

}
