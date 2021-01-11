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

/** netid: apn29 */

public class McDiver extends SewerDiver {

    /** Get to the ring in as few steps as possible. Once there, <br>
     * McDiver must return from this function in order to pick<br>
     * it up. If McDiver continues to move after finding the ring rather <br>
     * than returning, it will not count.<br>
     * If McDiver returns from this function while not standing on top of the ring, <br>
     * it will count as a failure.
     *
     * There is no limit to how many steps McDiver can take, but you will receive<br>
     * a score bonus multiplier for finding the ring in fewer steps.
     *
     * At every step, McDiver knows only the current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the ring at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * currentLocation(), neighbors(), and distanceToRing() in state.<br>
     * You know McDiver is standing on the ring when distanceToRing() is 0.
     *
     * Use function moveTo(long id) in state to move McDiver to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position. */
    @Override
    public void seek(SeekState state) {
        // TODO : Find the ring and return.

        // holds the visited nodes
        HashMap<Long, Boolean> visit= new HashMap<>();
        dfsWalk(state, visit);
    }

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
     *
     * You now have access to the entire underlying graph, which can be accessed<br>
     * through ScramState. currentNode() and getExit() will return Node objects<br>
     * of interest, and getNodes() will return a collection of all nodes on the graph.
     *
     * You have to get out of the sewer system in the number of steps given by<br>
     * stepsToGo(); for each move along an edge, this number is <br>
     * decremented by the weight of the edge taken.
     *
     * Use moveTo(n) to move to a node n that is adjacent to the current node.<br>
     * When n is moved-to, coins on node n are automatically picked up.
     *
     * McDiver must return from this function while standing at the exit. Failing <br>
     * to do so before steps run out or returning from the wrong node will be<br>
     * considered a failed run.
     *
     * Initially, there are enough steps to get from the starting point to the<br>
     * exit using the shortest path, although this will not collect many coins.<br>
     * For this reason, a good starting solution is to use the shortest path to<br>
     * the exit. */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
        // We say this because it makes it easier for you to try different
        // possibilities, always keeping at least one method that always scrams
        // in the prescribed number of steps.

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
            if (curr.getNeighbors().contains(x)) {
                if (st > curr.getEdge(x).length() + A6.pathSum(shp.get(x))) {
                    if (curr.getNeighbors().contains(x)) {
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
