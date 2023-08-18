/*
    THIS IMPLEMENTATION IS BASED ON ALGS4 LIBRARY:
    https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/DirectedEdge.java.html
*/

package dk.itu.datastructure;

import dk.itu.model.OsmElement;

public class DirectedEdge {
    private final int v;
    private final int w;

    private final String name;
    private final long parentId;
    private final double weight;

    /**
     * Initializes a directed edge from vertex {@code v} to vertex {@code w} with
     * the given {@code weight}.
     * @param v the tail vertex
     * @param w the head vertex
     * @param weight the weight of the directed edge
     * @throws IllegalArgumentException if either {@code v} or {@code w}
     *    is a negative integer
     * @throws IllegalArgumentException if {@code weight} is {@code NaN}
     */
    public DirectedEdge(int v, int w, String name, long parentId, double weight) {
        if (v < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers");
        if (w < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers");
        if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN");
        this.v = v;
        this.w = w;
        this.name = name.intern();
        this.parentId = parentId;
        this.weight = weight;
    }

    public int from() {
        return v;
    }

    public int to() {
        return w;
    }

    public String getName(){return name;}
    public long getParentId(){return parentId;}

    public double weight() {
        return weight;
    }

    public String toString() {
        return v + "->" + w + " " + String.format("%5.2f", weight);
    }
}