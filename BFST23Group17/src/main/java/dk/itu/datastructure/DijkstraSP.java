package dk.itu.datastructure;


import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Stack;

// read more about exact the specific algs4 library code here:
// https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/EdgeWeightedGraph.java.html
// https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/DirectedEdge.java.html
// https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/IndexMinPQ.java.html
public class DijkstraSP {
    private double[] distTo;
    private DirectedEdge[] edgeTo;

    private IndexMinPQ<Double> pq;

    public DijkstraSP(EdgeWeightedDirectedGraph G, int s) {
        distTo = new double[G.V()];
        edgeTo = new DirectedEdge[G.V()];

        for (int v = 0; v < G.V(); v++) {
            distTo[v] = Double.POSITIVE_INFINITY;//Set the initial distance value
        }

        distTo[s] = 0.0;//The distance to the starting point should be 0.0

        pq = new IndexMinPQ<Double>(G.V());
        pq.insert(s, distTo[s]);
        while(!pq.isEmpty()){
            int v = pq.delMin(); // Deletes smallest distance, and returns the vertex index
            for(DirectedEdge e : G.adj(v)){
                relax(e);
            }
        }
    }

    private void relax(DirectedEdge e) {
        int v = e.from(), w = e.to();
        if (distTo[w] > distTo[v] + e.weight()) {
            distTo[w] = distTo[v] + e.weight();
            edgeTo[w] = e;
            if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
            else pq.insert(w, distTo[w]);
        }
    }

    public double distTo(int v){
        return distTo[v];
    }

    public boolean hasPathTo(int v){
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    public Iterable<DirectedEdge> pathTo(int v){
        if(!hasPathTo(v)) return null;
        Stack<DirectedEdge> path = new Stack<DirectedEdge>();
        for (DirectedEdge e = edgeTo[v]; e != null; e = edgeTo[e.from()]){
            path.push(e);
        }
        return path;
    }
}