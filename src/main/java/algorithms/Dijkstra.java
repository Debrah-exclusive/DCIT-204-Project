package algorithms;

import graph.Graph;
import data.Edge;
import data.Location;

import java.util.*;

/**
 * Instance-based Dijkstra that works with:
 * - graph.Graph (methods: getNodes(), getEdgesFrom(int nodeId), getNode(int id))
 * - data.Edge (methods: getSrc(), getDst(), getDistanceMeters(), travelTimeMinutes(...))
 * - data.Location (getId(), getLabel(), etc.)
 */
public class Dijkstra {
    private final Map<Location, Double> dist = new HashMap<>();
    private final Map<Location, Location> prev = new HashMap<>();

    /**
     * Computes shortest distances (by distanceMeters) from source to all nodes in the graph.
     * @param graph Graph instance
     * @param source Source location (data.Location)
     */
    public void computeShortestPaths(Graph graph, Location source) {
        dist.clear();
        prev.clear();

        // initialize
        for (Location loc : graph.getNodes()) {
            dist.put(loc, Double.POSITIVE_INFINITY);
            prev.put(loc, null);
        }

        dist.put(source, 0.0);

        // priority queue of node ids, ordered by current best distance
        PriorityQueue<Integer> pq = new PriorityQueue<>(
                Comparator.comparingDouble(id -> {
                    Location l = graph.getNode(id);
                    return dist.getOrDefault(l, Double.POSITIVE_INFINITY);
                })
        );

        Set<Integer> visited = new HashSet<>();
        pq.add(source.getId());

        while (!pq.isEmpty()) {
            int uId = pq.poll();
            if (visited.contains(uId)) continue;
            visited.add(uId);

            Location u = graph.getNode(uId);
            double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);

            // iterate edges from this node id
            for (Edge e : graph.getEdgesFrom(uId)) {
                int vId = e.getDst();
                Location v = graph.getNode(vId);
                if (v == null) continue; // defensive

                double cost = e.travelTimeMinutes(e.getDefaultSpeedKmph());
                // currently using distance as weight
                double alt = du + cost;

                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(vId);
                }
            }
        }
    }

    /** Return the shortest-path distance (meters) to a target location. */
    public double getDistanceTo(Location target) {
        return dist.getOrDefault(target, Double.POSITIVE_INFINITY);
    }

    /** Accessor for all computed distances. */
    public Map<Location, Double> getDistances() {
        return dist;
    }

    /** Accessor for predecessor map. */
    public Map<Location, Location> getPreviousNodes() {
        return prev;
    }

    /** Reconstruct the shortest path (list of Locations) to the target. Empty list if unreachable. */
    public List<Location> getShortestPathTo(Location target) {
        if (!prev.containsKey(target) && !dist.containsKey(target)) return Collections.emptyList();
        List<Location> path = new ArrayList<>();
        Location cur = target;
        while (cur != null) {
            path.add(cur);
            cur = prev.get(cur);
        }
        Collections.reverse(path);
        // if path's first element is not a source (i.e., disconnected), you may want to check dist
        if (path.isEmpty() || Double.isInfinite(getDistanceTo(target))) return Collections.emptyList();
        return path;
    }
}
