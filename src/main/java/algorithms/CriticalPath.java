package algorithms;

import data.Edge;
import data.Location;
import graph.Graph;

import java.util.*;

/**
 * Implementation of Critical Path Method (CPM) for route optimization.
 * CPM is typically used for project scheduling but adapted here for finding
 * critical paths in the campus navigation network.
 */
public class CriticalPath {
    private final Map<Integer, Double> earliestStart = new HashMap<>();
    private final Map<Integer, Double> latestStart = new HashMap<>();
    private final Map<Integer, Double> slack = new HashMap<>();
    private final List<Integer> criticalPath = new ArrayList<>();
    
    /**
     * Computes the critical path from source to destination in the graph.
     * @param graph The campus graph
     * @param source Source location
     * @param destination Destination location
     */
    public void computeCriticalPath(Graph graph, Location source, Location destination) {
        // Clear previous data
        earliestStart.clear();
        latestStart.clear();
        slack.clear();
        criticalPath.clear();
        
        // Initialize earliest start times
        for (Location loc : graph.getNodes()) {
            earliestStart.put(loc.getId(), Double.NEGATIVE_INFINITY);
        }
        earliestStart.put(source.getId(), 0.0);
        
        // Compute earliest start times (forward pass)
        boolean updated = true;
        while (updated) {
            updated = false;
            for (Location loc : graph.getNodes()) {
                int nodeId = loc.getId();
                double es = earliestStart.get(nodeId);
                if (es == Double.NEGATIVE_INFINITY) continue;
                
                for (Edge edge : graph.getEdgesFrom(nodeId)) {
                    int nextId = edge.getDst();
                    double duration = edge.travelTimeMinutes(edge.getDefaultSpeedKmph());
                    double newEs = es + duration;
                    
                    if (newEs > earliestStart.getOrDefault(nextId, Double.NEGATIVE_INFINITY)) {
                        earliestStart.put(nextId, newEs);
                        updated = true;
                    }
                }
            }
        }
        
        // If destination is unreachable, return
        if (earliestStart.get(destination.getId()) == Double.NEGATIVE_INFINITY) {
            return;
        }
        
        // Initialize latest start times
        for (Location loc : graph.getNodes()) {
            latestStart.put(loc.getId(), Double.POSITIVE_INFINITY);
        }
        latestStart.put(destination.getId(), earliestStart.get(destination.getId()));
        
        // Compute latest start times (backward pass)
        updated = true;
        while (updated) {
            updated = false;
            for (Location loc : graph.getNodes()) {
                int nodeId = loc.getId();
                
                // Skip nodes that don't have incoming edges or haven't been reached
                if (earliestStart.get(nodeId) == Double.NEGATIVE_INFINITY) continue;
                
                // For each incoming edge
                for (Location prevLoc : graph.getNodes()) {
                    int prevId = prevLoc.getId();
                    for (Edge edge : graph.getEdgesFrom(prevId)) {
                        if (edge.getDst() == nodeId) {
                            double duration = edge.travelTimeMinutes(edge.getDefaultSpeedKmph());
                            double ls = latestStart.get(nodeId);
                            double newLs = ls - duration;
                            
                            if (newLs < latestStart.getOrDefault(prevId, Double.POSITIVE_INFINITY)) {
                                latestStart.put(prevId, newLs);
                                updated = true;
                            }
                        }
                    }
                }
            }
        }
        
        // Compute slack and identify critical path
        for (Location loc : graph.getNodes()) {
            int nodeId = loc.getId();
            if (earliestStart.get(nodeId) == Double.NEGATIVE_INFINITY) continue;
            
            double es = earliestStart.get(nodeId);
            double ls = latestStart.get(nodeId);
            double slackTime = ls - es;
            slack.put(nodeId, slackTime);
            
            // Nodes with zero slack are on the critical path
            if (Math.abs(slackTime) < 0.001) { // Using a small epsilon for floating point comparison
                criticalPath.add(nodeId);
            }
        }
    }
    
    /**
     * Gets the critical path as a list of location IDs.
     * @return List of location IDs on the critical path
     */
    public List<Integer> getCriticalPath() {
        return new ArrayList<>(criticalPath);
    }
    
    /**
     * Gets the earliest start time for a location.
     * @param locationId The location ID
     * @return The earliest start time
     */
    public double getEarliestStart(int locationId) {
        return earliestStart.getOrDefault(locationId, Double.NEGATIVE_INFINITY);
    }
    
    /**
     * Gets the latest start time for a location.
     * @param locationId The location ID
     * @return The latest start time
     */
    public double getLatestStart(int locationId) {
        return latestStart.getOrDefault(locationId, Double.POSITIVE_INFINITY);
    }
    
    /**
     * Gets the slack time for a location.
     * @param locationId The location ID
     * @return The slack time
     */
    public double getSlack(int locationId) {
        return slack.getOrDefault(locationId, Double.POSITIVE_INFINITY);
    }
    
    /**
     * Gets the total duration of the critical path.
     * @return The total duration
     */
    public double getTotalDuration() {
        return earliestStart.getOrDefault(criticalPath.get(criticalPath.size() - 1), 0.0);
    }
}