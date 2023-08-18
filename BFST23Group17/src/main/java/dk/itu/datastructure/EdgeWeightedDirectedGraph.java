package dk.itu.datastructure;

import java.util.*;


public class EdgeWeightedDirectedGraph {
    private static final String NEWLINE = System.getProperty("line.separator");

    private int V, E;           // number of vertices and edges in digrap
    private List<HashSet<DirectedEdge>> adj; // adj.get(v) = adjacency set for vertex v
    private List<Integer> indegree; //indegree.get(v) = indegree of vertex v

    // constructor
    public EdgeWeightedDirectedGraph() {
        V = 0;
        E = 0;
        indegree = new ArrayList<>();
        adj = new ArrayList<HashSet<DirectedEdge>>();
    }

    // adds a vertex
    public void addVertex() {
        V++;
        adj.add(new HashSet<DirectedEdge>());
        indegree.add(0);
    }
    // from ALGS4

    // creates edge by using ALGS4 datatype DirectedEdge, that consists of two vertices and a weight. Code description here:
    // https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/DirectedEdge.java.html
    public void addEdge(DirectedEdge e) {
            int v = e.from();
            int w = e.to();
            validateVertex(w);

            HashSet<DirectedEdge> tempSet = adj.get(v);
            tempSet.add(e);
            adj.set(v, tempSet);
            indegree.set(w, indegree.get(w) + 1);
            E++;

    }

    // ALGS4 function for validating the vertex before other method calls
    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
    }

    // display method for testing
    public void display() {
        // System.out.println(indegree.size());
        System.out.println(V + " vertices");
        System.out.println(E + " edges");
    }

    // return methods
    public int V() {
        return V;
    }
    public int E() {
        return E;
    }
    public Iterable<DirectedEdge> adj(int v) {
        validateVertex(v);
        return adj.get(v);
    }
    // if (V < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be non-negative");

}
