package algorithms;

import data.Location;
import data.Edge;
import graph.Graph;
import java.util.*;

/**
 * Implementation of A* (A Star) search algorithm for finding optimal paths
 * using heuristic estimates to guide the search.
 */
public class AStarSearch {
    private final Graph graph;
    private final Map<Location, Double> gScore = new HashMap<>();
    private final Map<Location, Double> fScore = new HashMap<>();
    private final Map<Location, Location> cameFrom = new HashMap<>();
    
    public AStarSearch(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Finds the optimal path from source to destination using A* search.
     * @param source Source location
     * @param destination Destination location
     * @return List of locations in the optimal path, or empty list if no path exists
     */
    public List<Location> findOptimalPath(Location source, Location destination) {
        gScore.clear();
        fScore.clear();
        cameFrom.clear();
        
        // Priority queue for open set, ordered by f-score
        PriorityQueue<Location> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(loc -> fScore.getOrDefault(loc, Double.POSITIVE_INFINITY))
        );
        
        Set<Location> closedSet = new HashSet<>();
        
        // Initialize scores
        gScore.put(source, 0.0);
        fScore.put(source, heuristicEstimate(source, destination));
        openSet.add(source);
        
        while (!openSet.isEmpty()) {
            Location current = openSet.poll();
            
            if (current.equals(destination)) {
                return reconstructPath(cameFrom, current);
            }
            
            closedSet.add(current);
            
            // Explore neighbors
            for (Edge edge : graph.getEdgesFrom(current.getId())) {
                Location neighbor = findLocationById(edge.getDst());
                if (neighbor == null || closedSet.contains(neighbor)) {
                    continue;
                }
                
                double tentativeGScore = gScore.get(current) + edge.getDistanceMeters();
                
                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    continue;
                }
                
                // This path is the best so far
                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                fScore.put(neighbor, tentativeGScore + heuristicEstimate(neighbor, destination));
            }
        }
        
        // No path found
        return new ArrayList<>();
    }
    
    /**
     * Heuristic function: Euclidean distance between two locations.
     * This is admissible (never overestimates) for road networks.
     * @param from Source location
     * @param to Destination location
     * @return Heuristic estimate of distance
     */
    private double heuristicEstimate(Location from, Location to) {
        double dx = from.getLat() - to.getLat();
        double dy = from.getLon() - to.getLon();
        return Math.sqrt(dx * dx + dy * dy) * 111000; // Convert to meters (roughly)
    }
    
    /**
     * Reconstructs the path from the cameFrom map.
     * @param cameFrom Map of locations to their predecessors
     * @param current Current location
     * @return List of locations in the path
     */
    private List<Location> reconstructPath(Map<Location, Location> cameFrom, Location current) {
        List<Location> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Helper method to find location by ID.
     * @param id Location ID
     * @return Location object, or null if not found
     */
    private Location findLocationById(int id) {
        for (Location loc : graph.getNodes()) {
            if (loc.getId() == id) {
                return loc;
            }
        }
        return null;
    }
    
    /**
     * Gets the g-score (actual cost from start) for a location.
     * @param location The location
     * @return G-score, or Double.POSITIVE_INFINITY if not computed
     */
    public double getGScore(Location location) {
        return gScore.getOrDefault(location, Double.POSITIVE_INFINITY);
    }
    
    /**
     * Gets the f-score (estimated total cost) for a location.
     * @param location The location
     * @return F-score, or Double.POSITIVE_INFINITY if not computed
     */
    public double getFScore(Location location) {
        return fScore.getOrDefault(location, Double.POSITIVE_INFINITY);
    }
}
