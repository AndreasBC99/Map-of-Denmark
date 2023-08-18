package dk.itu.datastructure.phtree;


import java.io.Serializable;
import java.util.Arrays;
import dk.itu.model.OsmElement;
import dk.itu.model.OsmPathNode;
import dk.itu.model.OsmWay;
import dk.itu.utils.HaversineUtil;

import java.util.*;

/**
 * Ph-Tree for floats
 */
public class PhTree implements Serializable {
    private final int DIM = 4; // LowerLeft 2D - UpperRight 2D

    private PhNode root;

    private final int NN_PQ_MAX_SIZE = 3000;

    public void put(OsmElement el) {
        put(el.getMinX(), el.getMinY(), el.getMaxX(), el.getMaxY(), el);
    }

    public void put(float left, float bottom, float right, float top, OsmElement element) {
        if (root == null) {
            root = element; // First element in phTree
        } else {
            if (root.isHC()) {
                Node rootNode = (Node) root;
                if (rootNode.prefix == null) {
                    root = null;
                    put(left, bottom, right, top, element);
                } else {
                    // Root is a node - standard add element
                    rootNode.addNthVal(PhUtils.boundToHyperbox(left, bottom, right, top), element);
                }
            } else {
                OsmElement rootEl = (OsmElement) root;
                if (rootEl.getId() == element.getId()) return;
                // Root is an OsmElement -> make it a Node and add the 2 vals
                Node newRoot = new Node();
                newRoot.addVals(PhUtils.boundToHyperbox(rootEl.getMinX(), rootEl.getMinY(), rootEl.getMaxX(), rootEl.getMaxY()), rootEl, PhUtils.boundToHyperbox(left, bottom, right, top), element);
                root = newRoot;
            }
        }
    }

    public void delete(OsmElement element) {
        delete(element.getMinX(), element.getMinY(), element.getMaxX(), element.getMaxY(), element);
    }

    private void delete(float left, float bottom, float right, float top, OsmElement element) {
        if (!root.isHC()) {
            if (((OsmElement) root).getId() == element.getId()) {
                root = null;
            }
        } else {
            boolean[] bits = PhUtils.boundToHyperbox(left, bottom, right, top);
            delete((Node) root, new boolean[0], bits, element);
        }
    }

    private void delete(Node phNode, boolean[] prefix, boolean[] bits, OsmElement element) {
        int prefixLength = prefix.length;
        // Updates prefix with node perfix +1 for HC
        prefix = Arrays.copyOf(prefix, prefixLength+phNode.prefix.length+4);
        System.arraycopy(phNode.prefix, 0, prefix, prefixLength, phNode.prefix.length);
//        System.arraycopy(phNode.prefix, 0, prefix, prefixLength, phNode.prefix.length);

        for (int i = 0 ; i < prefix.length-4 ; i++) {
            if (
                prefix[i] != bits[i]
            ) {
                // Bit mismatch
                return;
            }
        }

        for (int i = 0 ; i < phNode.HC.length ; i++) {
            if (phNode.HC[i] != null) {
                boolean[] hcBits = PhUtils.intToBits(i);
                if (
                        hcBits[0] == bits[prefix.length-4] &&
                        hcBits[1] == bits[prefix.length-3] &&
                        hcBits[2] == bits[prefix.length-2] &&
                        hcBits[3] == bits[prefix.length-1]
                ) {
                    // Bits match
                    if (phNode.HC[i].isHC()) {
                        prefix[prefix.length-4] = hcBits[0];
                        prefix[prefix.length-3] = hcBits[1];
                        prefix[prefix.length-2] = hcBits[2];
                        prefix[prefix.length-1] = hcBits[3];
                        delete((Node) phNode.HC[i], Arrays.copyOf(prefix, prefix.length), bits, element);
                    } else {
                        if (((OsmElement) phNode.HC[i]).getId() == element.getId()) {
                            phNode.HC[i] = null;
                        }
                    }
                    return;
                }
            }
        }

    }

    public OsmPathNode nn(float x, float y, boolean car, boolean bike, boolean walk) {
        if (root == null) return null;
        if (!root.isHC()) return (OsmPathNode) root;

        PriorityQueue<OsmPathNode> pq = new PriorityQueue<>((o1, o2) -> (int) ((HaversineUtil.pointsToKilometerFloat(o1, x, y) - HaversineUtil.pointsToKilometerFloat(o2, x, y)) * 1000000));

        try {
            nn(pq, root, new boolean[0], x, y);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (pq.isEmpty()) return null;

        OsmPathNode nearest = pq.poll();

        float dx = Math.abs(x-nearest.getMinX()), dy = Math.abs(y-nearest.getMinY());
        float radius = (float) (Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2))*1.5);

        List<OsmElement> elsWithin = windowQuery(x-radius, x+radius, y-radius, y+radius);

        pq.clear();

        OsmPathNode nearer = null;

        for (OsmElement element : elsWithin) {
            if (element instanceof OsmPathNode) {
                boolean oneOfRequired = false;
                for (OsmWay parentWay : ((OsmPathNode) element).getParentWaysTo()) {
                    oneOfRequired = ((car && parentWay.getDriving()) || (bike && parentWay.getCycling()) || (walk && parentWay.getWalking()));
                    if (oneOfRequired) break;
                }
                if (oneOfRequired) pq.add((OsmPathNode) element);
            }
        }

        if (!pq.isEmpty()) nearer = pq.poll();

        return nearer == null ? nearest : nearer;
    }

    private void nn(PriorityQueue<OsmPathNode> pq, PhNode phNode, boolean[] prefix, float x, float y) throws IllegalArgumentException {
        if (phNode.isHC()) {
            // Ph Node

            Node hcNode = (Node) phNode;

            // Updates Prefix
            if (hcNode.prefix != null) {
                int pl = prefix.length;
                prefix = Arrays.copyOf(prefix, pl+hcNode.prefix.length+4);
                System.arraycopy(hcNode.prefix, 0, prefix, pl, hcNode.prefix.length);
            }

            boolean foundRegion = false;
            List<PhNode> nodesToCheck = new ArrayList<>();

            for (int i = 0 ; i < 4 ; i++) {
                if (hcNode.HC[i*5] != null) {
                    boolean[] hc = PhUtils.intToBits(i*5);
                    prefix[prefix.length-4] = hc[0];
                    prefix[prefix.length-3] = hc[1];
                    prefix[prefix.length-2] = hc[2];
                    prefix[prefix.length-1] = hc[3];

                    boolean[] hcminx = new boolean[prefix.length/4], hcminy = new boolean[prefix.length/4], hcmaxx = new boolean[prefix.length/4], hcmaxy = new boolean[prefix.length/4];

                    for (int m = 0 ; m < prefix.length/4 ; m++) {
                        hcminx[m] = prefix[m*4];
                        hcminy[m] = prefix[(m*4)+1];
                        hcmaxx[m] = prefix[(m*4)];
                        hcmaxy[m] = prefix[(m*4)+1];
                    }

                    // HC BOUNDS
                    hcminx = PhUtils.smallestPossibleFloat(hcminx);
                    hcminy = PhUtils.smallestPossibleFloat(hcminy);
                    hcmaxx = PhUtils.biggestPossibleFloat(hcmaxx);
                    hcmaxy = PhUtils.biggestPossibleFloat(hcmaxy);

                    float hcminxf = PhUtils.bitsToFloat(hcminx);
                    float hcminyf = PhUtils.bitsToFloat(hcminy);
                    float hcmaxxf = PhUtils.bitsToFloat(hcmaxx);
                    float hcmaxyf = PhUtils.bitsToFloat(hcmaxy);

                    if (foundRegion) continue;

                    if (
                            hcminxf <= x &&
                            x <= hcmaxxf &&
                            hcminyf <= y &&
                            y <= hcmaxyf
                    ) {
                        // Point is in HC
                        foundRegion = true;
                        nn(pq, hcNode.HC[i*5], Arrays.copyOf(prefix, prefix.length), x, y);
                    } else {
                        // No HC with point anymore, find all Elements
                        nodesToCheck.add(hcNode.HC[i*5]);
                    }
                }
            }

            if (!foundRegion) {
                for (PhNode nodeToCheck : nodesToCheck) {
                    nn(pq, nodeToCheck, Arrays.copyOf(prefix, prefix.length), x, y);
                }
            }

        } else {
            // Leaf Node / Osm Element
            if (pq.size() > NN_PQ_MAX_SIZE) throw new IllegalArgumentException("NN clicked point too far from landmass");
            pq.add((OsmPathNode) phNode);
        }

    }


    /**
     * UL-------------UR
     * |       |       |
     * |               |
     * |->           <-|
     * |               |
     * |       |       |
     * LL------------ LR
     *
     * Returns all elements in given window
     * @param minx left most x coordinate
     * @param maxx right most x coordinate
     * @param miny down most y coordinate
     * @param maxy upper most y coordinate
     */
    public List<OsmElement> windowQuery(float minx, float maxx, float miny, float maxy) {
        /**
         * Look at elements where:
         * - minx <= pre+HC[0]+0...
         * - miny <= pre+HC[1]+0...
         * - maxx >= pre+HC[2]+0...
         * - maxy >= pre+HC[3]+0...
         *
         * Query will store previous bits from parent nodes, and add prefix to it (lb).
         * It will then fill lb with 0, such that lb.length() == 0.
         * It will then look at HC addresses, where the index of the critbit is replace with the HC address,
         * and that satisfy the conditions. The look in the node if present.
         */
        List<OsmElement> rs = new ArrayList<>();

        if (root != null) windowQuery(rs, root, new boolean[0], PhUtils.floatToBits(minx), PhUtils.floatToBits(miny), PhUtils.floatToBits(maxx), PhUtils.floatToBits(maxy));

        return rs;
    }

    private void windowQuery(List<OsmElement> returnList, PhNode node, boolean[] prefix, boolean[] minx, boolean[] miny, boolean[] maxx, boolean[] maxy) {
        if (node.isHC()) {

            Node hcNode = (Node) node;

            // Is not leaf
            if (hcNode.prefix != null) {
                int prefixLength = prefix.length;
                prefix = Arrays.copyOf(prefix, prefixLength+hcNode.prefix.length+4);
                System.arraycopy(hcNode.prefix, 0, prefix, prefixLength, hcNode.prefix.length);
            }

            for (int i = 0 ; i < hcNode.HC.length ; i++) {
                if (hcNode.HC[i] != null) {
                    boolean[] hc = PhUtils.intToBits(i);
                    prefix[prefix.length-4] = hc[0];
                    prefix[prefix.length-3] = hc[1];
                    prefix[prefix.length-2] = hc[2];
                    prefix[prefix.length-1] = hc[3];

                    if (isWithin(prefix, minx, miny, maxx, maxy)) {
                        windowQuery(returnList, hcNode.HC[i]);
                    } else {
                        if (intersects(prefix, minx, miny, maxx, maxy)) windowQuery(returnList, hcNode.HC[i], Arrays.copyOf(prefix, prefix.length), minx, miny, maxx, maxy);
                    }
                }
            }
        } else {
            OsmElement element = (OsmElement) node;
            // Is leaf that intersects
            if (
                    PhUtils.bitsToFloat(minx) <= element.getMaxX() &&
                            PhUtils.bitsToFloat(miny) <= element.getMaxY() &&
                            PhUtils.bitsToFloat(maxx) >= element.getMinX() &&
                            PhUtils.bitsToFloat(maxy) >= element.getMinY()
            ) {
                returnList.add(element);
            }
        }
    }

    private void windowQuery(List<OsmElement> returnList, PhNode phNode) {
        if (phNode.isHC()) {
            Node node = (Node) phNode;
            for (int i = 0; i < 16; i++) {
                if (node.HC[i] != null) {
                    windowQuery(returnList, node.HC[i]);
                }
            }
        } else {
            returnList.add((OsmElement) phNode);
        }
    }

    private static boolean intersects(boolean[] prefix, boolean[] minx, boolean[] miny, boolean[] maxx, boolean[] maxy) {

        boolean[] hcminx = new boolean[prefix.length/4], hcminy = new boolean[prefix.length/4], hcmaxx = new boolean[prefix.length/4], hcmaxy = new boolean[prefix.length/4];

        for (int i = 0 ; i < prefix.length/4 ; i++) {
            hcminx[i] = prefix[i*4];
            hcminy[i] = prefix[(i*4)+1];
            hcmaxx[i] = prefix[(i*4)+2];
            hcmaxy[i] = prefix[(i*4)+3];
        }

        hcminx = PhUtils.smallestPossibleFloat(hcminx);
        hcminy = PhUtils.smallestPossibleFloat(hcminy);
        hcmaxx = PhUtils.biggestPossibleFloat(hcmaxx);
        hcmaxy = PhUtils.biggestPossibleFloat(hcmaxy);

        boolean isLeftBound = PhUtils.compareFloats(minx, hcmaxx) <= 0, isBottomBound = PhUtils.compareFloats(miny, hcmaxy) <= 0, isRightBound = PhUtils.compareFloats(maxx, hcminx) >= 0, isTopBound = PhUtils.compareFloats(maxy, hcminy) >= 0;

        return isLeftBound & isBottomBound && isRightBound && isTopBound;
    }

    private static boolean isWithin(boolean[] prefix, boolean[] minx, boolean[] miny, boolean[] maxx, boolean[] maxy) {

        boolean[] hcminx = new boolean[prefix.length/4], hcminy = new boolean[prefix.length/4], hcmaxx = new boolean[prefix.length/4], hcmaxy = new boolean[prefix.length/4];

        for (int i = 0 ; i < prefix.length/4 ; i++) {
            hcminx[i] = prefix[i*4];
            hcminy[i] = prefix[(i*4)+1];
            hcmaxx[i] = prefix[(i*4)+2];
            hcmaxy[i] = prefix[(i*4)+3];
        }

        hcminx = PhUtils.smallestPossibleFloat(hcminx);
        hcminy = PhUtils.smallestPossibleFloat(hcminy);
        hcmaxx = PhUtils.biggestPossibleFloat(hcmaxx);
        hcmaxy = PhUtils.biggestPossibleFloat(hcmaxy);

        boolean isLeftBound = PhUtils.compareFloats(hcmaxx, maxx) <= 0, isBottomBound = PhUtils.compareFloats(hcmaxy, maxy) <= 0, isRightBound = PhUtils.compareFloats(minx, hcminx) <= 0, isTopBound = PhUtils.compareFloats(miny, hcminy) <= 0;

        return isLeftBound & isBottomBound && isRightBound && isTopBound;
    }

    private class Node implements PhNode {
        boolean[] prefix;
        PhNode[] HC; // [HC Index]

        @Override
        public boolean isHC() {
            return true;
        }

        public void addVals(boolean[] val1, OsmElement el1, boolean[] val2, OsmElement el2) {
            for (int i = 0 ; i<(val1.length)/4 ; i++) {
                if (
                    val1[4*i] == val2[4*i] &&
                    val1[4*i + 1] == val2[4*i + 1] &&
                    val1[4*i + 2] == val2[4*i + 2] &&
                    val1[4*i + 3] == val2[4*i + 3]
                ) continue; // Continue if bits are the same

                // i -> index of critbit - goes in HC
                // i+1 -> 32 - post

                if (HC == null) {
                    // Create new postfix array
                    HC = new PhNode[(int) Math.pow(2, DIM)];
                }

                // Find index out of binary val
                int valHcIndex = PhUtils.bitsToInt(val1[4*i], val1[4*i+1], val1[4*i+2], val1[4*i+3]);
                int orgHcIndex = PhUtils.bitsToInt(val2[4*i], val2[4*i+1], val2[4*i+2], val2[4*i+3]);

                HC[valHcIndex] = el1;

                HC[orgHcIndex] = el2;

                prefix = Arrays.copyOfRange(val1, 0, i*4); // Update prefix
                return;
            }
        }

        private void addNthVal(boolean[] val, OsmElement element) {
            for (int i = 0 ; i<(val.length-1)/4 ; i++) {
                if (prefix == null) {
                    System.out.println();
                }
                if (prefix.length/4 > i) {
                    // Checking prefix
                    if (PhUtils.compareUnequalBits(prefix, val, i)) {
                        // Diff bits at index i in prefix

                        // Get copy of prefix before critbit (excluded)
                        boolean[] prePre = Arrays.copyOfRange(prefix, 0, i*4);
                        // Get copy of prefix after critbit (excluded)
                        boolean[] prePost = Arrays.copyOfRange(prefix, (i+1)*4, prefix.length);

                        // HC index of prefix critbit
                        int orgHcIndex = PhUtils.bitsToInt(prefix[i*4], prefix[i*4+1], prefix[i*4+2], prefix[i*4+3]);
                        // HC index of new value
                        int nnHcIndex = PhUtils.bitsToInt(val[i*4], val[i*4+1], val[i*4+2], val[i*4+3]);

                        prefix = prePre; // Set new Prefix (shortens it) on original node

                        // Creates new subnode which is the original node - prePre bits (+ clones HC)
                        Node newOrgNode = new Node();
                        newOrgNode.prefix = prePost;
//                        newOrgNode.data = data;
                        newOrgNode.HC = HC.clone();

                        // Resets HC on current Node
                        HC = new PhNode[(int) Math.pow(2, DIM)];

                        // Sets original Node to be in HC of current node
                        HC[orgHcIndex] = newOrgNode;

                        // Create new node off of val - prePre bits
                        HC[nnHcIndex] = element;

                        return;
                    }
                } else if (prefix.length/4 == i) {
                    // On HC
                    int valHcIndex = PhUtils.bitsToInt(val[i*4], val[i*4+1], val[i*4+2], val[i*4+3]);
                    if (HC[valHcIndex] == null) {
                        HC[valHcIndex] = element;
                    } else {
                        // HC is already occupied - Add to that HC Node
                        if (HC[valHcIndex].isHC()) {
                            Node newValNode = (Node) HC[valHcIndex];
                            newValNode.addNthVal(Arrays.copyOfRange(val, (i+1)*4, val.length), element);
                            HC[valHcIndex] = newValNode;
                        } else {
                            OsmElement el1 = (OsmElement) HC[valHcIndex];
                            if (el1.getId() == element.getId()) return;
                            if (el1.getMinX() == element.getMinX() && el1.getMinY() == element.getMinY() && el1.getMaxX() == element.getMaxX() && el1.getMaxY() == element.getMaxY()) return;
                            boolean[] val1 = PhUtils.boundToHyperbox(el1.getMinX(), el1.getMinY(), el1.getMaxX(), el1.getMaxY());
                            Node newNode = new Node();
                            newNode.addVals(Arrays.copyOfRange(val1, (128-val.length)+(i+1)*4, val1.length), el1, Arrays.copyOfRange(val, (i+1)*4, val.length), element);
                            HC[valHcIndex] = newNode;
                        }
                    }
                }
            }
        }
    }
}