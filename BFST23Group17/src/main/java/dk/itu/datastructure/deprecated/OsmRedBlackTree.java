package dk.itu.datastructure.deprecated;

import dk.itu.datastructure.OsmSearchTree;
import dk.itu.model.OsmElement;

@Deprecated
public class OsmRedBlackTree<T extends OsmElement> implements OsmSearchTree<T> {
    private static final boolean RED   = true;
    private static final boolean BLACK = false;
    private Node root;

    private class Node {
        private T data;
        private Node left, right;  // links to left and right subtrees
        private boolean color;     // color of parent link

        public Node(T data, boolean color) {
            this.data = data;
            this.color = color;
        }
    }

    @Override
    public T get(long id) {
        return get(root, id);
    }

    private T get(Node node, long id) {
        if (node == null) {
            return null;
        }

        if (id == node.data.getId()) {
            return node.data;
        } else if (id < node.data.getId()) {
            return get(node.left, id);
        } else {
            return get(node.right, id);
        }
    }

    @Override
    public void replace(T eFrom, T eTo) {
        replace(root, eFrom, eTo);
    }

    private void replace(Node node, T eFrom, T eTo) {
        if (node == null) return;

        if (eFrom.getId() == node.data.getId()) {
            node.data = eTo;
        } else if (eFrom.getId() < node.data.getId()) {
            replace(node.left, eFrom, eTo);
        } else {
            replace(node.right, eFrom, eTo);
        }
    }

    @Override
    public void add(T el) {
        root = insert(root, el);
        root.color = BLACK;
    }

    private Node insert(Node h, T el) {
        if (h == null) {
            return new Node(el, RED);
        }

        if (el.getId() == h.data.getId()) {
            h.data = el;
        } else if (el.getId() < h.data.getId()) {
            h.left = insert(h.left, el);
        } else {
            h.right = insert(h.right, el);
        }

        // fix-up any right-leaning links
        if (isRed(h.right) && !isRed(h.left))      h = rotateLeft(h);
        if (isRed(h.left)  &&  isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left)  &&  isRed(h.right))     flipColors(h);

        return h;
    }

    private boolean isRed(Node x) {
        if (x == null) return false;
        return x.color == RED;
    }

    // rotate right
    private Node rotateRight(Node h) {
        assert (h != null) && isRed(h.left);
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    // rotate left
    private Node rotateLeft(Node h) {
        assert (h != null) && isRed(h.right);
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    // precondition: two children are red, node is black
    // postcondition: two children are black, node is red
    private void flipColors(Node h) {
        assert !isRed(h) && isRed(h.left) && isRed(h.right);
        h.color = RED;
        h.left.color = BLACK;
        h.right.color = BLACK;
    }
}
