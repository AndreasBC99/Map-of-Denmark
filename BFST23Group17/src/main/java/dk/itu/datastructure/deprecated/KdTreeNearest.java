package dk.itu.datastructure.deprecated;

import dk.itu.model.OsmElement;
import dk.itu.model.OsmWay;

import java.util.List;
import java.util.TreeMap;
@Deprecated
public class KdTreeNearest<T extends OsmElement> extends KdTree<T> {
    public KdTreeNearest(List<T> allElements, int layer) {
        super(allElements, layer);
    }

    /**
     * Nearest neighbour search
     * @param coord is an array of length 2, being x and y
     * @return the nearest element
     */
    public NearestResult findNearest(double[] coord) {
        depth = 0;
        NearestResult nr = new NearestResult(coord);
        findNearest(coord, rootNode, nr);
        nr.calculateNearest();
        return nr;
    }

    public void findNearest(double[] coord, KdNode node, NearestResult nr) {
        if (node != null) { // Prevents NullPointerException
            if (node.isLeaf()) {
                for (T el : node.getLeafElements()) {
                    if (el.getType().equals(OsmElement.OsmElementType.WAY) && el.isRoad()) {
                        OsmWay way = (OsmWay) el;
                        double smallestParaDist = Double.MAX_VALUE;
                        int[] smallestNodes = new int[]{0, 1};
                        for (int i = 1 ; i<way.getOsmNodes().size() ; i++){
                            OsmElement n1 = way.getOsmNodes().get(i-1);
                            OsmElement n2 = way.getOsmNodes().get(i);

                            double paraDist = pDistance(coord[1], coord[0], n1.getMinX(), n1.getMinY(), n2.getMinX(), n2.getMinY());

                            if (paraDist < smallestParaDist) {
                                smallestParaDist = paraDist;
                                smallestNodes = new int[]{i-1, i};
                            }
                        }
                        nr.addCandidate(new NearestCand(way, smallestNodes, smallestParaDist));
                    }
                }
            } else {
                if (depth%2 == 0) {
                    if (coord[1] <= node.getMaxX()) {
                        depth++;
                        findNearest(coord, node.getLeftChild(), nr);
                        depth--;
                    }
                    if (coord[1] >= node.getMinX()) {
                        depth++;
                        findNearest(coord, node.getRightChild(), nr);
                        depth--;
                    }
                } else {
                    if (coord[0] <= node.getMaxY()) {
                        depth++;
                        findNearest(coord, node.getLeftChild(), nr);
                        depth--;
                    }
                    if (coord[0] >= node.getMinY()) {
                        depth++;
                        findNearest(coord, node.getRightChild(), nr);
                        depth--;
                    }
                }
            }
        }
    }

    private double pDistance(double x, double y, double x1, double y1, double x2, double y2) {
        double A = x - x1;
        double B = y - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = -1;
        if (len_sq != 0) //in case of 0 length line
            param = dot / len_sq;

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        }
        else if (param > 1) {
            xx = x2;
            yy = y2;
        }
        else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        var dx = x - xx;
        var dy = y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private class NearestCand {
        OsmWay way;
        int[] indexOfClosestNodes;
        double parallelDistance;

        public NearestCand(OsmWay _way, int[] _indexOfClosestNodes, double _parallelDistance) {
            way = _way;
            indexOfClosestNodes = _indexOfClosestNodes;
            parallelDistance = _parallelDistance;
        }
    }

    public class NearestResult {
        double[] startPoint;
        TreeMap<Double, NearestCand> candidates = new TreeMap<>();

        private boolean hasBeenCalculated = false;
        private OsmWay nearestWay;
        private OsmElement[] nearestNodes;
        private double[] intersectPoint;

        public NearestResult(double[] _startPoint) {
            startPoint = _startPoint;
        }

        public void addCandidate(NearestCand cand) {
            if (candidates.size()<5) {
                candidates.put(cand.parallelDistance, cand);
            } else {
                if (candidates.get(candidates.lastKey()).parallelDistance > cand.parallelDistance) {
                    candidates.remove(candidates.lastKey());
                    candidates.put(cand.parallelDistance, cand);
                }
            }
        }

        public void calculateNearest() {
            NearestCand cand = candidates.get(candidates.firstKey());
            nearestWay = cand.way;
            nearestNodes = new OsmElement[]{nearestWay.getOsmNodes().get(cand.indexOfClosestNodes[0]), nearestWay.getOsmNodes().get(cand.indexOfClosestNodes[1])};

            double A = startPoint[1] - nearestNodes[0].getMinX();
            double B = startPoint[0] - nearestNodes[0].getMinY();
            double C = nearestNodes[1].getMinX() - nearestNodes[0].getMinX();
            double D = nearestNodes[1].getMinY() - nearestNodes[0].getMinY();

            double dot = A * C + B * D;
            double len_sq = C * C + D * D;
            double param = -1;
            if (len_sq != 0) //in case of 0 length line
                param = dot / len_sq;

            double xx, yy;

            if (param < 0) {
                intersectPoint = new double[]{nearestNodes[0].getMinY(), nearestNodes[0].getMinX()};
            }
            else if (param > 1) {
                intersectPoint = new double[]{nearestNodes[1].getMinY(), nearestNodes[1].getMinX()};
            }
            else {
                xx = nearestNodes[0].getMinX() + param * C;
                yy = nearestNodes[0].getMinY() + param * D;
                intersectPoint = new double[]{yy, xx};
            }

            hasBeenCalculated = true;
        }

        public OsmWay getNearestWay() {
            if (!hasBeenCalculated) calculateNearest();
            return nearestWay;
        }

        public double[] getStartPoint() {
            return startPoint;
        }

        public OsmElement[] getNearestNodes() {
            return nearestNodes;
        }

        public double[] getIntersectPoint() {
            return intersectPoint;
        }
    }
}
