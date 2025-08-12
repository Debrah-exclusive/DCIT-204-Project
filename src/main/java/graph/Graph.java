package graph;

import data.Edge;
import data.Location;

import java.util.*;

public class Graph {
    private final Map<Integer, Location> nodes = new HashMap<>();
    private final Map<Integer, List<Edge>> adj = new HashMap<>();

    /** Add a node (Location) to the graph */
    public void addNode(Location n) {
        nodes.put(n.getId(), n);
        adj.putIfAbsent(n.getId(), new ArrayList<>());
    }

    /** Add an edge object directly */
    public void addEdge(Edge e) {
        adj.putIfAbsent(e.getSrc(), new ArrayList<>());
        adj.get(e.getSrc()).add(e);
    }

    /** Add an edge by specifying details */
    public void addEdge(int src, int dst, double distanceMeters, double speedKmph) {
        adj.putIfAbsent(src, new ArrayList<>());
        adj.get(src).add(new Edge(src, dst, distanceMeters, speedKmph));
    }

    /** Return all nodes (used by Dijkstra as getNodes()) */
    public Collection<Location> getNodes() {
        return nodes.values();
    }

    /** Return a specific node by id (used by Dijkstra as getNode()) */
    public Location getNode(int id) {
        return nodes.get(id);
    }

    /** Return edges from a node id (used by Dijkstra as getEdgesFrom(int)) */
    public List<Edge> getEdgesFrom(int nodeId) {
        return adj.getOrDefault(nodeId, Collections.emptyList());
    }

    /** Optional: get all nodes map */
    public Map<Integer, Location> getNodesMap() {
        return nodes;
    }
}
