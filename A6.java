
package diver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import graph.Edge;
import graph.Node;

/**
* Author: Arushi Nety
* methods written by Arushi: shortestPath(Node, Node), pathSum(List<Node>), coinSum(List<Node>)

/** This class contains the solution to A6, shortest-path algorithm, <br>
 * and other methods needed for diver. */
public class A6 {
    
    /** Return the shortest path from node v to node last <br>
     * ---or the empty list if a path does not exist. <br>
     * Note: The empty list is a list with 0 elements. */
    public static List<Node> shortestPath(Node v, Node last) {
        // Contains an entry for each node in the frontier set. The priority of a node
        // is the length of the shortest known path from v to the node using only settled
        // node except for the last node, which is in F
        Heap<Node> F= new Heap<>(true);
        F.add(v, 0); // adds first node to frontier set; F = {v}; maintains invariant

        // contains both settled nodes and frontier nodes
        HashMap<Node, NodeData> SandF= new HashMap<>();
        NodeData vd= new NodeData(null, 0);

        SandF.put(v, vd);
        //just adding a comment to see if github works
        while (F.size != 0) {
            // f = node in F with minimum d value
            Node f= F.poll();
            // return once last node is reached
            if (f == last) { return path(SandF, last); }

            int fDist= SandF.get(f).dist;

            for (Edge ed : f.getExits()) { // each neighbor of w of f
                Node w= ed.getOther(f);
                int len= fDist + ed.length; // distance from starting node -> f -> w
                NodeData wnd= SandF.get(w);

                if (wnd == null) {
                    // add w to F
                    F.add(w, len);
                    SandF.put(w, new NodeData(f, len));

                } else if (len < wnd.dist) {
                    wnd.bkptr= f;
                    wnd.dist= len; // set to smallest of the two; in this case, that's 'len'

                    // change priority to update heap
                    F.changePriority(w, len);
                }

            }

        }
        // no path from v to last
        return new LinkedList<>();
    }

   
    /** An instance contains information about a node: <br>
     * the Distance of this node from the start node and <br>
     * its Backpointer: the previous node on a shortest path <br>
     * from the first node to this node (null for the start node). 
     * (Not written by Arushi)
     */
    public static class NodeData {
        /** shortest known distance from the start node to this one. */
        private int dist;
        /** backpointer on path (with shortest known distance) from start node to this one */
        private Node bkptr;

        /** Constructor: an instance with dist d from the start node and<br>
         * backpointer p. */
        private NodeData(Node p, int d) {
            dist= d;     // Distance from start node to this one.
            bkptr= p;    // Backpointer on the path (null if start node)
        }

        /** return a representation of this instance. */
        @Override
        public String toString() {
            return "dist " + dist + ", bckptr " + bkptr;
        }
    }

    /** = the path from the start node to node last.<br>
     * Precondition: mapSF contains all the necessary information about<br>
     * ............. the path. */
    public static List<Node> path(HashMap<Node, NodeData> mapSF, Node last) {
        List<Node> path= new LinkedList<>();
        Node p= last;
        // invariant: All the nodes from p's successor to node last are in
        // path, in reverse order.
        while (p != null) {
            path.add(0, p);
            p= mapSF.get(p).bkptr;
        }
        return path;
    }

    /** Return the sum of the weights of the edges on path p. <br>
     * Precondition: pa contains at least 1 node. <br>
     * If 1 node, it's a path of length 0, i.e. with no edges.
     */
    public static int pathSum(List<Node> p) {
        synchronized (p) {
            Node w= null;
            int sum= 0;
            // invariant: if w is null, n is the start node of the path.<br>
            // .......... if w is not null, w is the predecessor of n on the path.
            // .......... sum = sum of weights on edges from first node to v
            for (Node n : p) {
                if (w != null) sum= sum + w.getEdge(n).length;
                w= n;
            }
            return sum;
        }
    }

    /**
    * Return the sum of the coins along the path 'p'. <br>
    */
    public static int coinSum(List<Node> p) {
        int sum= 0;
        for (Node n : p) {
            if (n != null) {
                sum+= n.getTile().coins();
            }
        }
        return sum;
    }

}
