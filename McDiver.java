package diver;

import graph.NodeStatus;
import graph.ScramState;
import graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import graph.SeekState;
import graph.SewerDiver;

/**
* Author: Arushi Nety. 
* This project is in two parts: One part is to find a ring in the sewer system; the other is to pick up as many coins 
* as possible and safely exit without running out of steps. 
*/

public class McDiver extends SewerDiver {

    /** Get to the ring in as few steps as possible. Once there, <br>
     * McDiver must return from this function in order to pick<br>
     * it up. If McDiver continues to move after finding the ring rather <br>
     * than returning, it will not count.<br>
     * If McDiver returns from this function while not standing on top of the ring, <br>
     * it will count as a failure. 
     */
    @Override
    public void seek(SeekState state) {
        
        // holds the visited nodes
        HashMap<Long, Boolean> visit= new HashMap<>();
        dfsWalk(state, visit);
    }

    //Abstract implementation of a depth first search walk
    /** Node u= s.standingOn(); Visit u; for each neighbor w of u { if (w is unvisited) {
     * s.moveTo(w); dfsWalk(s); s.moveTo(u); */

    /** The walker is standing on the current node given by State s. Visit every node reachable
     * along paths of unvisited nodes from current node. End with walker standing on ring's
     * location. <br>
     * Precondition: all nodes are unvisited. */
    public void dfsWalk(SeekState s, HashMap<Long, Boolean> visit) {

        // base case: if on ring, terminate- nothing more to find
        if (s.distanceToRing() == 0) return;

        long curr= s.currentLocation();
        visit.put(curr, true);
        ArrayList<NodeStatus> path= new ArrayList<>();

        for (NodeStatus n : s.neighbors()) {
            long id= n.getId();
            Boolean hold= visit.get(id);
            if (hold == null) {
                visit.put(id, false);
            }
            path.add(n);
        }

        Collections.sort(path);

        for (NodeStatus n : path) {
            long id= n.getId();
            Boolean hold= visit.get(id);
            if (hold == false) {
                s.moveTo(id);
                // recursive case: keep going until distance to ring is found
                dfsWalk(s, visit);
                // backtracking case: terminate if zero
                if (s.distanceToRing() == 0) return;
                s.moveTo(curr);
            }
        }

    }

    /** Scram --get out of the sewer system before the steps are all used, trying to <br>
     * collect as many coins as possible along the way. McDiver must ALWAYS <br>
     * get out before the steps are all used, and this should be prioritized above<br>
     * collecting coins.
     */
    @Override
    public void scram(ScramState state) {
        // HashMap<Node, Boolean> visit= new HashMap<>();
        // exitOpt2(state, visit);

        exitOpt3(state);

    }

    /** Uses shortest path along with knowing where the exit is to find quickest way out.
     *
     * @param s */
    public void exitScram(ScramState s) {
        // Node curr= s.currentNode();
        // Node ex= s.exit();

        if (s.currentNode().equals(s.exit())) return;
        List<Node> path= A6.shortestPath(s.currentNode(), s.exit());
        // if node you're on is a neighbor of the node in the path, move to that
        for (Node p : path) {
            if (s.currentNode().getNeighbors().contains(p)) s.moveTo(p);
        }
    }

    /** First, accesses all the nodes and get shortest path from current nodes to nodes with coins.
     * <br>
     * From there, gets coin sum and path sum of each of the shortest paths. Then, gets the ratio of
     * <br>
     * coin sum to path sum and sorts it from highest to lowest. Then, if numbers of steps permits,
     * <br>
     * McDiver follows that path from the current node along the highest ratio path, ultimately
     * ending <br>
     * on a coin. If there's not enough steps for the entire highest ratio path, McDiver traverses
     * <br>
     * part of it until it is absolutely time to scram */
    public void checkAllNodes(ScramState s, Node curr) {
        // get all the nodes
        Collection<Node> allNotFiltered= s.allNodes();
        Collection<Node> all= new ArrayList<>();

        // filter out nodes with no coins
        for (Node n : allNotFiltered) {
            if (n.getTile().coins() != 0) all.add(n);
        }
        // no coins on the board
        if (all.size() == 0) {
            exitScram(s);
            return;
        }

        // list of shortest paths
        List<List<Node>> shp= new ArrayList<>();
        for (Node n : all) {
            shp.add(A6.shortestPath(curr, n));
        }

        // coinsum of shortest paths
        // path sum of shortest paths
        HashMap<List<Node>, Integer> cs= new HashMap<>(); // list of nodes in a path; coin sum
        HashMap<List<Node>, Integer> ps= new HashMap<>(); // list of nodes in a path; path sum
        for (List<Node> ln : shp) {
            cs.put(ln, A6.coinSum(ln));
            ps.put(ln, A6.pathSum(ln));
        }

        // coinsum / path sum, along with nodes
        HashMap<Double, List<Node>> ratio= new HashMap<>();

        // put the ratio into the hashmap
        for (List<Node> n : cs.keySet()) {
            double cSum= cs.get(n);
            double pSum= ps.get(n);
            ratio.put(pSum / cSum, n);
        }

        // sort from highest to lowest
        Set<Double> sh= ratio.keySet();
        List<Double> shList= new ArrayList<>(sh);
        Collections.sort(shList);

        // while enough steps, go to as many as you can

        List<Node> dest= ratio.get(shList.get(0));
        Node lastDest= dest.get(dest.size() - 1);
        int checkPathSum= A6.pathSum(ratio.get(shList.get(0))); // path to 'best' option
        int steps= s.stepsToGo();
        int shPathSum= A6.pathSum(A6.shortestPath(lastDest, s.exit())); // check if there are enough
                                                                        // steps to go

        // if steps - check path sum > shpath sum go forth, otherwise scram
        if (steps - checkPathSum > shPathSum) {
            for (Node x : dest) {
                if (s.currentNode().getNeighbors().contains(x))
                    s.moveTo(x);
            }
        } else {

            // traverses part of the path
            goPartway(s, dest);
            return;
        }

        return;

    }

    /** If not enough steps for the entire path, traverse part of the path until forced to exit.
     *
     * @param s
     * @param ln */
    public void goPartway(ScramState s, List<Node> ln) {
        Node curr= ln.get(0);
        // System.out.println(curr.equals(s.currentNode()));
        // get shortest paths from current node throughout the list node
        HashMap<Node, List<Node>> shp= new HashMap<>();
        for (Node x : ln) {
            shp.put(x, A6.shortestPath(x, s.exit()));
        }

        int st= s.stepsToGo();
        
        // as long as there are enough steps in part of the path, keep going
        for (Node x : ln) {
            Set<Node> ne = curr.getNeighbors(); 
            if (ne.contains(x)) {
                if (st > curr.getEdge(x).length() + A6.pathSum(shp.get(x))) {
                    if (ne.contains(x)) {
                        s.moveTo(x);
                        return;
                    }
                } else {
                    exitScram(s);
                    return;
                }

            }
            curr= x;
        }

    }

    /** While able to continue(has enough steps), McDiver goes to node with the lowest path to coin
     * value ratio. */
    public void exitOpt3(ScramState s) {
        // Node curr = s.currentNode();
        int pSum= A6.pathSum(A6.shortestPath(s.currentNode(), s.exit()));
        int st= s.stepsToGo();

        while (st > pSum) {
            checkAllNodes(s, s.currentNode());
            if (s.currentNode().equals(s.exit())) return;
        }
        return;
    }

}
