package dk.itu.datastructure.deprecated;

import dk.itu.model.OsmElement;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * This KdTree is very suboptimal.
 * From what I could see, each Node should contain an element, however it isn't, as that is an easier solution for now.
 * In the future, we would like to look at a KdTree with insertion. The challenge there is, however balancing.
 * Do say something if you get any good ideas:)
 */
@Deprecated
public class KdTree<T extends OsmElement> implements Serializable {
    protected int depth = 0; // Variable depth counter
    protected KdNode rootNode;
    private final int layer;

    public KdTree(List<T> allElements, int _layer) {
        layer = _layer;
        if (allElements.size() > 0) {
            addChild(null, allElements, null);
        }
    }

    // Adds children to nodes - starting from root node
    private void addChild(KdNode kdNode, List<T> elements, Boolean isLeft) {
        int leafNodeElements = 25;
        boolean isSortX = (depth % 2) == 0; // Check whether to compare on X or Y
        // Amount of elements in leaf
        boolean isLeafNode = elements.size() <= leafNodeElements; // Check whether is on leaf node

        // Sort on minX on even depths, minY otherwise
        if (isSortX) {
            elements = elements.stream().parallel().sorted(Comparator.comparing(T::getMinX)).toList();
        } else {
            elements = elements.stream().parallel().sorted(Comparator.comparing(T::getMinY)).toList();
        }

        List<T> left = null, right = null; // Left and right branches elements

        KdNode newChildNode;

        // Only need to split List if we know that we want to create more children that aren't leaves
        if (!isLeafNode) {
            int mid = elements.size() / 2;

            left = elements.subList(0, mid);
            right = elements.subList(mid+1, elements.size());

            newChildNode = new KdNode(elements.get(mid));
        } else {
            newChildNode = new KdNode(null); // New kdNode to be added to param node
        }

        // Inits root node, if not done - else sett new child node to param node left and right children
        if (rootNode != null) {
            if (isLeft) {
                kdNode.setLeftChild(newChildNode);
            } else {
                kdNode.setRightChild(newChildNode);
            }
        } else {
            rootNode = newChildNode;
        }

        if (isLeafNode) {
            newChildNode.setAsLeafNode(elements.toArray((T[]) new OsmElement[0])); // Sets leaf elements to leaf node
        } else {
            // Goes down 1 depth, and repeats recursively, until a leaf node is obtained
            depth++;
            addChild(newChildNode, left, true);
            addChild(newChildNode, right, false);
            depth--;
        }
    }


    // Range search
    public List<T> search(KdTree.SearchBox<T> searchBox) {
        depth = 0;
        List<T> returnList = new ArrayList<>();
        search(searchBox, rootNode, returnList);
        return returnList;
    }

    private void search(KdTree.SearchBox<T> searchBox, KdNode kdNode, List<T> rl) {
        if (kdNode != null) { // Prevents NullPointerException
            if (kdNode.isLeaf()) {
                // If leaf, add to final list if they intersect with searchbox
                for (T el : kdNode.getLeafElements()) {
                    if (searchBox.intersects(el)) rl.add(el);
                }
            } else {
                if (searchBox.intersects(kdNode.element)) rl.add(kdNode.element);
                // Go down the branches of the tree
                if (depth%2 == 0) {
                    if (searchBox.getMinX() <= kdNode.getMaxX()) {
                        depth++;
                        search(searchBox, kdNode.getLeftChild(), rl);
                        depth--;
                    }
                    if (searchBox.getMaxX() >= kdNode.getMinX()) {
                        depth++;
                        search(searchBox, kdNode.getRightChild(), rl);
                        depth--;
                    }
                } else {
                    if (searchBox.getMinY() <= kdNode.getMaxY()) {
                        depth++;
                        search(searchBox, kdNode.getLeftChild(), rl);
                        depth--;
                    }
                    if (searchBox.getMaxY() >= kdNode.getMinY()) {
                        depth++;
                        search(searchBox, kdNode.getRightChild(), rl);
                        depth--;
                    }
                }
            }
        }
    }

    public int getLayer() {
        return layer;
    }

    public static class SearchBox<T extends OsmElement> {
        private float minX, maxX, minY, maxY;
        public SearchBox(float _minX, float _maxX, float _minY, float _maxY) {
            minX = _minX;
            maxX = _maxX;
            minY = _minY;
            maxY = _maxY;
        }

        /**
         * @param element to be compared
         * @return true if element is in box
         */
        public boolean intersects(T element) {
            return
                    // Element surrounds box
                    (element.getMinX() <= minX && element.getMaxX() >= maxX && element.getMinY() <= minY && element.getMaxY() >= maxY) ||
                            // Element either Y in box
                            (((element.getMinY() >= minY && element.getMinY() <= maxY) || (element.getMaxY() >= minY && element.getMaxY() <= maxY)) &&
                                    (element.getMaxX() >= minX && element.getMinX() <= maxX)) ||
                            // Element either X in box
                            (((element.getMinX() >= minX && element.getMinX() <= maxX) || (element.getMaxX() >= minX && element.getMaxX() <= maxX)) &&
                                    (element.getMaxY() >= minY && element.getMinY() <= maxY)) ||
                            // Element top left corner in box
                            (element.getMinX() >= minX && element.getMinX() <= maxX && element.getMaxY() >= minY && element.getMaxY() <= maxY) ||
                            // Element top right corner in box
                            (element.getMaxX() >= minX && element.getMaxX() <= maxX && element.getMaxY() >= minY && element.getMaxY() <= maxY) ||
                            // Element bottom left corner in box
                            (element.getMinX() >= minX && element.getMinX() <= maxX && element.getMinY() >= minY && element.getMinY() <= maxY) ||
                            // Element bottom right corner in box
                            (element.getMaxX() >= minX && element.getMaxX() <= maxX && element.getMinY() >= minY && element.getMinY() <= maxY);
        }

        public float getMinX() {
            return minX;
        }

        public float getMaxX() {
            return maxX;
        }

        public float getMinY() {
            return minY;
        }

        public float getMaxY() {
            return maxY;
        }

        public void setNewBounds(float _minX, float _maxX, float _minY, float _maxY) {
            minX = _minX;
            maxX = _maxX;
            minY = _minY;
            maxY = _maxY;
        }
    }

    protected class KdNode {
        private final T element;
        private KdNode leftChild, rightChild;

        private boolean isLeaf;
        private T[] leafElements;

        public KdNode(T _element) {
            element = _element;
        }

        public void setLeftChild(KdNode _leftChild) {
            leftChild = _leftChild;
        }

        public void setRightChild(KdNode _rightChild) {
            rightChild = _rightChild;
        }

        public void setAsLeafNode(T[] _leafElements) {
            leafElements = _leafElements;
            isLeaf = true;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public T[] getLeafElements() {
            return leafElements;
        }

        public float getMinX() {
            return element.getMinX();
        }

        public float getMaxX() {
            return element.getMaxX();
        }

        public float getMinY() {
            return element.getMinY();
        }

        public float getMaxY() {
            return element.getMaxY();
        }

        public KdNode getLeftChild() {
            return leftChild;
        }

        public KdNode getRightChild() {
            return rightChild;
        }
    }
}
