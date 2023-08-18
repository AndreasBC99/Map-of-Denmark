package dk.itu.model;

import dk.itu.utils.HaversineUtil;
import dk.itu.utils.ArrayUtils;

public class OsmPathNode extends OsmNode{
    private OsmPathNode[] to = new OsmPathNode[1];
    private float[] weightsTo = new float[1];
    private OsmWay[] parentWaysTo = new OsmWay[1];

    private boolean[] vehicleAccessTo = new boolean[1];
    private float dist = Float.POSITIVE_INFINITY;//Distance from the startpoint to this point
    private float heuristicDist = Float.POSITIVE_INFINITY;//Remaining distance to the endpoint
    private OsmPathNode pf = null;

    private OsmWay parentWay;

    public OsmPathNode(long _id, float _y, float _x) {
        super(_id, _y, _x);
    }
  
    public void addConnectionTo(OsmPathNode osmNode, OsmWay _parentWay) {
        addFieldsConnectionTo(osmNode, _parentWay, true);
        if(!_parentWay.getIsOneWay()) osmNode.addFieldsConnectionTo(this, _parentWay, true);
        else osmNode.addFieldsConnectionTo(this, _parentWay, false);
        this.parentWay = _parentWay;
    }

    private void addFieldsConnectionTo(OsmPathNode osmNode, OsmWay _parentWay, boolean oneway) {
        to = ArrayUtils.insertToSmallOsmPathNodeArray(to, osmNode);
        parentWaysTo = ArrayUtils.insertToSmallOsmWayArray(parentWaysTo, _parentWay);
        weightsTo = ArrayUtils.insertToSmallFloatArray(weightsTo, HaversineUtil.pointsToKilometerFloat(this, osmNode), to.length);
        vehicleAccessTo = ArrayUtils.insertToSmallBooleanArray(vehicleAccessTo, oneway, weightsTo.length);
    }



    public static OsmPathNode convertToPathNode(OsmNode node) {
        return new OsmPathNode(node.id, node.y, node.x);
    }

    @Override
    public OsmElement.OsmElementType getType() {
        return OsmElement.OsmElementType.NODE_PATH;
    }

    public void reset() {
        dist = Float.POSITIVE_INFINITY;
        heuristicDist = Float.POSITIVE_INFINITY;
        //heuristicDist = HaversineUtil.pointsToKilometer(this, endNode);
        pf = null;
        for (OsmPathNode pn : to) {
            if (pn.pf != null) pn.reset();
        }
    }

    public void setDist(float _dist) {
        dist = _dist;
    }

    public void setHeuristicDist(float _dist){heuristicDist = _dist;}

    public void setPf(OsmPathNode pf) {
        this.pf = pf;
    }

    public double getDist() {
        return dist;
    }

    public double getHeuristicDist(){return heuristicDist;}

    public OsmPathNode getPf() {
        return pf;
    }

    public OsmPathNode[] getTo() {
        return to;
    }

    public float[] getWeightsTo() {
        return weightsTo;
    }

    public boolean[] getVehicleAccess(){return vehicleAccessTo;}
  
    public OsmWay getParentWay(){
          return parentWay == null ? parentWaysTo[0] : parentWay;
    }

    public String getParentWayName(){
        for (OsmWay way : parentWaysTo) {
            if (!way.getName().equals("unnamed")) return way.getName();
        }
        return "Unnamed road";
    }

    public OsmWay getParentWayTo(OsmPathNode pathnode){
        for(int i = 0; i < to.length; i++){
            if(to[i] == pathnode){
                return parentWaysTo[i];
            }
        }
        return null;
    }

    public OsmWay[] getParentWaysTo() {
        return parentWaysTo;
    }
}
