package dk.itu.datastructure.deprecated;

import dk.itu.model.OsmElement;

import java.lang.reflect.Array;
@Deprecated
public class OsmTrie<T extends OsmElement> {
    class Node {
        Node[] ns;
        T el;

        private Node() {
            ns = (Node[]) Array.newInstance(this.getClass(), 10);
        }

        public void put(CharSequence num, T val) {
            if (num.isEmpty()) {
                el = val;
            } else {
                int i = Character.getNumericValue(num.charAt(0));
                if (ns[i] == null) ns[i] = new Node();
                ns[i].put(num.subSequence(1, num.length()), val);
            }
        }

        public T get(CharSequence num) {
            if (num.isEmpty()) return el;
            int i = Character.getNumericValue(num.charAt(0));
            return ns[i].get(num.subSequence(1, num.length()));
        }
    }

    private final Node root = new Node();
    private int size = 0;

    public void put(Long id, T val) {
        root.put(id.toString(), val);
        size++;
    }

    public void put(String id, T val) {
        root.put(id, val);
        size++;
    }

    public T get(Long id) {
        return root.get(id.toString());
    }

    public T get(String id) {
        return root.get(id);
    }
}
