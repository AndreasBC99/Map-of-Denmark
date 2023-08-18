package dk.itu.datastructure.deprecated;

import dk.itu.datastructure.OsmSearchTree;
import dk.itu.model.OsmElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Deprecated
public class OsmAvlTree<T extends OsmElement> implements OsmSearchTree<T> {
    private Node root;
    private int size;
    private class Node {
        T data;
        Node left;
        Node right;

        int height;

        public Node(T _data) {
            data = _data;
        }
    }

    public T get(long id) {
        Node retNode = get(id, root);
        if (retNode == null) return null;
        return retNode.data;
    }

    private Node get(long id, Node node) {
        if (node == null) {
            return null;
        }

        if (id == node.data.getId()) {
            return node;
        } else if (id < node.data.getId()) {
            return get(id, node.left);
        } else {
            return get(id, node.right);
        }
    }

    public void add(T element) {
        root = add(element, root);
    }

    private Node add(T element, Node node) {
        if (node == null) {
            node = new Node(element);
            size++;
        }

        // Otherwise, traverse the tree to the left or right depending on the key
        else if (element.getId() < node.data.getId()) {
            node.left = add(element, node.left);
        } else if (element.getId() > node.data.getId()) {
            node.right = add(element, node.right);
        }

        updateHeight(node);

        return rebalance(node);
    }

    public void replace(T eFrom, T eTo) {
        replace(root, eFrom, eTo);
    }

    private void replace(Node node, T eFrom, T eTo) {
        if (node == null) {
            return;
        }

        if (eFrom.getId() == node.data.getId()) {
            node.data = eTo;
        } else if (eFrom.getId() < node.data.getId()) {
            replace(node.left, eFrom, eTo);
        } else {
            replace(node.right, eFrom, eTo);
        }
    }

    private void updateHeight(Node node) {
        int leftChildHeight = height(node.left);
        int rightChildHeight = height(node.right);
        node.height = Math.max(leftChildHeight, rightChildHeight) + 1;
    }

    private Node rebalance(Node node) {
        int balanceFactor = balanceFactor(node);

        // Left-heavy?
        if (balanceFactor < -1) {
            if (balanceFactor(node.left) <= 0) {
                // Rotate right
                node = rotateRight(node);
            } else {
                // Rotate left-right
                node.left = rotateLeft(node.left);
                node = rotateRight(node);
            }
        }

        // Right-heavy?
        if (balanceFactor > 1) {
            if (balanceFactor(node.right) >= 0) {
                // Rotate left
                node = rotateLeft(node);
            } else {
                // Rotate right-left
                node.right = rotateRight(node.right);
                node = rotateLeft(node);
            }
        }

        return node;
    }

    private Node rotateRight(Node node) {
        Node leftChild = node.left;

        node.left = leftChild.right;
        leftChild.right = node;

        updateHeight(node);
        updateHeight(leftChild);

        return leftChild;
    }

    private Node rotateLeft(Node node) {
        Node rightChild = node.right;

        node.right = rightChild.left;
        rightChild.left = node;

        updateHeight(node);
        updateHeight(rightChild);

        return rightChild;
    }

    private int balanceFactor(Node node) {
        return height(node.right) - height(node.left);
    }

    private int height(Node node) {
        return node != null ? node.height : -1;
    }

    public List<T> toListFilter(Predicate<? super T> predicate) {
        List<T> retList = new ArrayList<>(size);
        addToListFilter(predicate, root, retList);
        return retList;
    }

    private void addToListFilter(Predicate<? super T> predicate, Node node, List<T> retList) {
        if (node != null) {
            if (predicate.test(node.data)) retList.add(node.data);
            if (node.left != null) addToListFilter(predicate, node.left, retList);
            if (node.right != null) addToListFilter(predicate, node.right, retList);
        }
    }

    public void clear() {
        root = null;
    }
}