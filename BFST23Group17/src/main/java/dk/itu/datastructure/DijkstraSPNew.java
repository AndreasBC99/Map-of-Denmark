package dk.itu.datastructure;

import dk.itu.model.OsmPathNode;
import dk.itu.model.OsmWay;
import dk.itu.utils.HaversineUtil;
import dk.itu.view.components.RouteTypeComponent;
import edu.princeton.cs.algs4.Stack;

import java.util.PriorityQueue;

public class DijkstraSPNew {
    RouteTypeComponent.options type;
    OsmPathNode endNode;
    PriorityQueue<OsmPathNode> pq = new PriorityQueue<>((o1, o2) -> {
        double point2val = o2.getDist()+o2.getHeuristicDist();
        double point1val = o1.getDist()+o1.getHeuristicDist();
        return (int) (point1val*1000 - point2val*1000);
    });

    public DijkstraSPNew(OsmPathNode startNode, OsmPathNode _endNode, RouteTypeComponent.options _type) {
        endNode = _endNode;
        startNode.setDist(0.0f);
        startNode.setHeuristicDist((float) HaversineUtil.pointsToKilometer(startNode, endNode));
        type = _type;

        pq.add(startNode);
        while (!pq.isEmpty()) {
            OsmPathNode curNode = pq.poll();
            if(curNode == endNode) break;
            OsmPathNode[] ntl = curNode.getTo();
            float[] wtl = curNode.getWeightsTo();
            boolean[] val = curNode.getVehicleAccess();
            for (int i = 0 ; i < curNode.getTo().length ; i++) {
                try {
                    relax(curNode, ntl[i], wtl[i], val[i], i);
                } catch (Exception e){

                }
            }
        }
    }

    private void relax(OsmPathNode curNode, OsmPathNode toNode, double weight, boolean vehicleAcccess, int index) {
        if(!curNode.getParentWayTo(toNode).getTransportation(type)) return;
        if(type == RouteTypeComponent.options.VEHICLE && !vehicleAcccess) return;//If not traversable by vehicle -> stop function

        double timeToTraverse;
        double speed = 0;
        if(type == RouteTypeComponent.options.FOOT) speed = 5.5;
        else if (type == RouteTypeComponent.options.BIKE) speed = 18;
        else if (type == RouteTypeComponent.options.VEHICLE) speed = curNode.getParentWayTo(toNode).getMaxSpeed();

        if(type == RouteTypeComponent.options.VEHICLE) toNode.setHeuristicDist((float) HaversineUtil.pointsToKilometer(toNode, endNode) / 130 * 3600);
        else toNode.setHeuristicDist((float) (HaversineUtil.pointsToKilometer(toNode, endNode) / speed * 3600));

        double dist = toNode.getDist();

        timeToTraverse = (weight / speed) * 3600;

        if (dist > curNode.getDist() + timeToTraverse) {
            // New shortest vertex
            toNode.setDist((float) (curNode.getDist()+timeToTraverse));
            toNode.setPf(curNode);
            if (!pq.contains(toNode)) pq.add(toNode);
        }
    }

    public boolean hasPathTo(OsmPathNode node) {
        return node.getDist() < Double.POSITIVE_INFINITY;
    }

    public Iterable<OsmPathNode> pathTo(OsmPathNode endNode){
        if(!hasPathTo(endNode)) return null;
        Stack<OsmPathNode> path = new Stack<>();

        OsmPathNode curNode = endNode;
        while (curNode.getDist() >= 0.0) {
            path.push(curNode);

            if(curNode.getDist() == 0.0) break;//Break to avoid exception as startNode has no Pf

            curNode = curNode.getPf();
        }

        return path;
    }
}
