package algorithms;

import data.Location;
import graph.Graph;
import java.util.*;

/**
 * Implementation of Floyd-Warshall algorithm for finding shortest paths
 * between all pairs of nodes in the graph.
 */
public class FloydWarshall {
    private double[][] distances;
    private int[][] next;
    private Map<Integer, Integer> idToIndex;
    private Map<Integer, Integer> indexToId;
    private Graph graph;
    
    /**
     * Computes shortest paths between all pairs of nodes.
     * @param graph The campus graph
     */
    public void computeAllPairsShortestPaths(Graph graph) {
        this.graph = graph;
        List<Location> nodes = new ArrayList<>(graph.getNodes());
        int n = nodes.size();
        
        // Initialize mappings
        idToIndex = new HashMap<>();
        indexToId = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            idToIndex.put(nodes.get(i).getId(), i);
            indexToId.put(i, nodes.get(i).getId());
        }
        
        // Initialize distance and next matrices
        distances = new double[n][n];
        next = new int[n][n];
        
        // Initialize with infinity and self-loops with 0
        for (int i = 0; i < n; i++) {
            Arrays.fill(distances[i], Double.POSITIVE_INFINITY);
            Arrays.fill(next[i], -1);
            distances[i][i] = 0;
        }
        
        // Initialize with direct edges
        for (Location node : nodes) {
            int i = idToIndex.get(node.getId());
            for (data.Edge edge : graph.getEdgesFrom(node.getId())) {
                int j = idToIndex.get(edge.getDst());
                double weight = edge.getDistanceMeters();
                distances[i][j] = weight;
                next[i][j] = j;
            }
        }
        
        // Floyd-Warshall algorithm
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distances[i][k] != Double.POSITIVE_INFINITY && 
                        distances[k][j] != Double.POSITIVE_INFINITY) {
                        double newDist = distances[i][k] + distances[k][j];
                        if (newDist < distances[i][j]) {
                            distances[i][j] = newDist;
                            next[i][j] = next[i][k];
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Gets the shortest distance between two locations.
     * @param source Source location
     * @param destination Destination location
     * @return Shortest distance, or Double.POSITIVE_INFINITY if no path exists
     */
    public double getShortestDistance(Location source, Location destination) {
        if (idToIndex == null) {
            throw new IllegalStateException("Must call computeAllPairsShortestPaths first");
        }
        
        Integer i = idToIndex.get(source.getId());
        Integer j = idToIndex.get(destination.getId());
        
        if (i == null || j == null) {
            return Double.POSITIVE_INFINITY;
        }
        
        return distances[i][j];
    }
    
    /**
     * Gets the shortest path between two locations.
     * @param source Source location
     * @param destination Destination location
     * @return List of locations in the shortest path, or empty list if no path exists
     */
    public List<Location> getShortestPath(Location source, Location destination) {
        if (idToIndex == null) {
            throw new IllegalStateException("Must call computeAllPairsShortestPaths first");
        }
        
        Integer i = idToIndex.get(source.getId());
        Integer j = idToIndex.get(destination.getId());
        
        if (i == null || j == null || distances[i][j] == Double.POSITIVE_INFINITY) {
            return new ArrayList<>();
        }
        
        List<Location> path = new ArrayList<>();
        path.add(source);
        
        int current = i;
        while (current != j) {
            current = next[current][j];
            if (current == -1) break;
            
            Integer locationId = indexToId.get(current);
            Location location = findLocationById(locationId);
            if (location != null) {
                path.add(location);
            }
        }
        
        return path;
    }
    
    /**
     * Gets all shortest distances matrix.
     * @return 2D array of shortest distances
     */
    public double[][] getAllDistances() {
        return distances;
    }
    
    /**
     * Gets the next matrix for path reconstruction.
     * @return 2D array of next nodes
     */
    public int[][] getNextMatrix() {
        return next;
    }
    
    /**
     * Helper method to find location by ID.
     */
    private Location findLocationById(int id) {
        for (Location loc : graph.getNodes()) {
            if (loc.getId() == id) {
                return loc;
            }
        }
        return null;
    }
}
