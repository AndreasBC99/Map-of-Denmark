package dk.itu.datastructure;

import dk.itu.model.OsmElement;

public interface OsmSearchTree<T extends OsmElement> {
    T get(long id);
    void add(T element);
    void replace(T eFrom, T eTo);
}
