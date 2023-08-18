package dk.itu.model;

import com.google.protobuf.MapEntry;
import dk.itu.datastructure.phtree.PhTree;
import dk.itu.utils.ReverseElementUtil;
import dk.itu.utils.Theme;
import dk.itu.utils.ThemesFactory;
import dk.itu.view.MapView;
import dk.itu.view.components.DebugDetailsComponent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

public class OsmRelation implements OsmElement {
    private final long id;
    private final OsmElement[] osmElements;
    private final List<OsmElement> nodeChain = new ArrayList<>();
    private final List<List<OsmElement>> outerNodeChains = new ArrayList<>(), innerNodeChains = new ArrayList<>();

    private final List<List<OsmElement>> chains = new ArrayList<>();

    private final List<List<OsmElement>> innerChains = new ArrayList<>();
    private float minX, maxX, minY, maxY;
    private Theme.ColorDescription colorDescription = null;

    private boolean isMultipolygon;

    public OsmRelation(long _id, List<OsmElement> _osmElements, Map<String, String> _tags) {
        id = _id;
        osmElements = _osmElements.toArray(new OsmElement[0]);
        colorDescription = ThemesFactory.getTheme().getColorDescription(_tags);

        if (!_osmElements.isEmpty()) {
            minX = _osmElements.get(0).getMinX();
            maxX = _osmElements.get(0).getMaxX();
            minY = _osmElements.get(0).getMinY();
            maxY = _osmElements.get(0).getMaxY();

            for (OsmElement el : osmElements) {
                if (el.getMinX() < minX) {
                    minX = el.getMinX();
                }
                if (el.getMaxX() > maxX) { // else if because if new min is found, new max isn't present
                    maxX = el.getMaxX();
                }
                if (el.getMinY() < minY) {
                    minY = el.getMinY();
                }
                if (el.getMaxY() > maxY) { // else if because if new min is found, new max isn't present
                    maxY = el.getMaxY();
                }
            }
        }

        if (!_tags.isEmpty()) {
            if (_tags.containsKey("type") && _tags.get("type").equals("multipolygon")) isMultipolygon = true;
            else isMultipolygon = false;
            _tags.clear();
        }
        try {
            fillNodes(); // Split containing nodes into separate structures
        } catch (Exception e) {
            System.out.println("RELATION: " + e.getMessage());
            System.out.println("ID: " + id);
        }
    }

    public Theme.ColorDescription getColorDescription() {
        return colorDescription;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return "";
    }

    //Helper method for the constructor -> determine chains of nodes that are connected in unique structures
    private void fillNodes(){
        if (osmElements == null) return;

        boolean[] marked = new boolean[osmElements.length];

        OsmWay way;

        for(OsmElement element : osmElements){
            if(element.getType().equals(OsmElementType.WAY)){
                way = (OsmWay) element;//Safe cast
                List<OsmElement> wayNodes = way.getOsmNodes();

                //Check if complete
                if(way.getIsComplete()){
                    //Test if the complete way is an role "inner"
                    if(way.getMemberType().equals("inner")){
                        innerChains.add(new ArrayList<>(List.of(way)));
                        continue;
                    }
                    chains.add(new ArrayList<OsmElement>(wayNodes));
                    continue;
                }

                //Way is not complete -> check for a connection to an existing chain
                boolean foundMatch = false;
                for(List<OsmElement> l : chains){
                    if(l.get(0).getId() == l.get(l.size() - 1).getId()) continue;//Skip if the chain is closed

                    //Check for match
                    if(way.first().getId() == l.get(0).getId()){
                        //current first == chain first -> reverse way and add to start of chain
                        Collections.reverse(wayNodes);
                        l.addAll(0, wayNodes);
                        foundMatch = true;
                        break;
                    } else if(way.first().getId() == l.get(l.size() - 1).getId()) {
                        //current first == chain last -> add way to chain
                        l.addAll(wayNodes);
                        foundMatch = true;
                        break;
                    } else if(way.last().getId() == l.get(0).getId()){
                        //current last == chain first -> add way to start of chain
                        l.addAll(0, wayNodes);
                        foundMatch = true;
                        break;
                    } else if(way.last().getId() == l.get(l.size() - 1).getId()){
                        //current last == chain last -> reverse way and add to end of chain
                        Collections.reverse(wayNodes);
                        l.addAll(wayNodes);
                        foundMatch = true;
                        break;
                    }
                }

                //No match for a current chain -> create a new one
                if(!foundMatch) chains.add(new ArrayList<OsmElement>(wayNodes));
            }
        }

        //Connect chains
        for(int i = 0; i < chains.size(); i++){
            if(chains.get(i).get(0).getId() == chains.get(i).get(chains.get(i).size() - 1).getId()) continue;//Skip way if complete

            //The way is not complete -> attempt to find partner
            for(int j = i +1; j < chains.size(); j++){
                //Test if j is a partner
                if(chains.get(i).get(0).getId() == chains.get(j).get(0).getId()){
                    //i first == j first
                    Collections.reverse(chains.get(i));
                    chains.get(j).addAll(0, chains.get(i));
                    chains.get(i).clear();
                    break;
                } else if(chains.get(i).get(0).getId() == chains.get(j).get(chains.get(j).size() - 1).getId()){
                    //i first == j last
                    chains.get(j).addAll(chains.get(i));
                    chains.get(i).clear();
                    break;
                } else if(chains.get(i).get(chains.get(i).size() - 1).getId() == chains.get(j).get(0).getId()){
                    //i last == j first
                    chains.get(j).addAll(0, chains.get(i));
                    chains.get(i).clear();
                    break;
                } else if(chains.get(i).get(chains.get(i).size() - 1).getId() == chains.get(j).get(chains.get(j).size() - 1).getId()){
                    //i last == j last
                    Collections.reverse(chains.get(i));
                    chains.get(j).addAll(chains.get(i));
                    chains.get(i).clear();
                    break;
                }
            }

        }
    }

    //Determine if a way should be filled or not depending on tags and complete-ness
    private boolean shouldFill(){
        //return (getColorDescription().getShouldFill() && nodeChain.isEmpty());
        return (getColorDescription().getShouldFill());
    }

    //Method for drawing chains of nodes
    private void drawChain(List<OsmElement> nodeChain, MapView mapView, boolean isOuter){
        GraphicsContext gc = mapView.gc;
        if (nodeChain.isEmpty()) {
            return;
        }

        int nodesToSkip = 1;

        if (getColorDescription().getNodeSkip() != null) {
            for (Map.Entry<Float, Integer> entry : getColorDescription().getNodeSkip().entrySet()) {
                if (entry.getKey() <= mapView.getScreenWidthX() && entry.getValue()>nodesToSkip) {
                    nodesToSkip = entry.getValue();
                }
            }
        }

        if (nodeChain.size() < nodesToSkip*3) {
            return;
        }
      
        gc.beginPath();
        gc.moveTo(0.56*nodeChain.get(0).getMinX(), -nodeChain.get(0).getMinY());

        for (int i = 1 ; i < nodeChain.size() ; i+=nodesToSkip) gc.lineTo(0.56*nodeChain.get(i).getMinX(), -nodeChain.get(i).getMinY());

        if(shouldFill()){
            gc.setFill(Color.web(getColorDescription().getFillColor()));
            gc.fill();
        }

        gc.closePath();

        gc.setStroke(Color.web(getColorDescription().getBorderColor()));
        gc.stroke();
    }

    public void draw(MapView mapView, Optional<Color> _fillColor, Optional<Color> _borderColor){
        if(!isMultipolygon) return;//Only draw a relation that is a multipolygon

        for(List<OsmElement> nodeChain : chains) drawChain(nodeChain, mapView, true);

        for(List<OsmElement> list : innerChains){
            for(OsmElement e : list){
                if(((OsmWay) e).hasTags()){
                    e.draw(mapView, Optional.empty(), Optional.empty());
                } else {
                    e.draw(mapView, Optional.of(Color.web(getColorDescription().getInnerFillColor())), Optional.of(Color.web(getColorDescription().getBorderColor())));
                }
            }
        }
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
        return colorDescription.getVisible() ? OsmElementType.RELATION_MULTIPOLYGON : OsmElementType.RELATION;
    }

    @Override
    public OsmRelation setMemberType(String memberType) {
//        boolean isOuter;
//        if (memberType.equals("inner")) isOuter = true;
        return this;
    }

    @Override
    public boolean isRoad() {
        return false;
    }

    @Override
    public boolean shouldBeDrawn() {
        return colorDescription.getVisible();
    }

    @Override

    public boolean getIndependent(){return true;}

    @Override
    public void setIndependent(boolean val){}

    public boolean isHC() {
        return false;
    }
  
    public List<List<OsmElement>> getChains() {
        return chains;
    }

}
