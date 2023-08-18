package dk.itu.model;

import dk.itu.datastructure.phtree.PhTree;
import dk.itu.parsers.Parser;
import dk.itu.utils.ThemesFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import java.util.*;


public class MapModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 123456789L;
    private final HashMap<String, OsmInfoNode> addressMap;

    private final List<PhTree> allNewPhTrees;
    private final PhTree newPhTreeNN;
    private final HashMap<String, HashMap<Float, Float>> poiMap = new HashMap<>();
    private final List<String> poiNameList = new ArrayList<>();
    private final double minX, maxX, minY, maxY;
    private final int minLayer, maxLayer;

    public MapModel(Parser _parser) {
        minLayer = ThemesFactory.getTheme().getMinLayer();
        maxLayer = ThemesFactory.getTheme().getMaxLayer();
        addressMap = _parser.getAddressMap();

        allNewPhTrees = _parser.getAllNewPhTrees();
        newPhTreeNN = _parser.getNewPhTreeNN();
   
        minX = _parser.getMinX();
        maxX = _parser.getMaxX();
        minY = _parser.getMinY();
        maxY = _parser.getMaxY();

        _parser = null;
        System.gc();
    }

    public HashMap<String, OsmInfoNode> getAddressMap() {return addressMap;}

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public List<PhTree> getAllNewPhTrees() {
        return allNewPhTrees;
    }

    public PhTree getNewPhTreeNN() {
        return newPhTreeNN;
    }

    public int getMinLayer() {
        return minLayer;
    }

    public int getMaxLayer() {
        return maxLayer;
    }

  
    public void setPoIMap(String name, float x, float y) {
        HashMap<Float, Float> coordMap = new HashMap<>();
        coordMap.put(x, y);
        poiMap.put(name, coordMap);
    }
  
    public void setPoiNameList(String name) {poiNameList.add(name);}
  
    public HashMap<String, HashMap<Float, Float>> getPoiMap() {return poiMap;}

}


