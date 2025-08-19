package algorithms;

import data.Location;
import graph.Graph;

import java.util.*;

/**
 * Implementation of landmark-based routing to find paths that pass through specific landmarks.
 */
public class LandmarkRouting {
    private final Graph graph;
    
    public LandmarkRouting(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Finds routes from source to destination that pass through or near specified landmarks.
     * @param source Source location
     * @param destination Destination location
     * @param landmarks List of landmarks to include in the route
     * @param maxRoutes Maximum number of routes to return
     * @return List of routes (each route is a list of locations)
     */
    public List<List<Location>> findRoutesViaLandmarks(Location source, Location destination, 
                                                     List<Location> landmarks, int maxRoutes) {
        List<List<Location>> routes = new ArrayList<>();
        
        // If no landmarks specified, just find the shortest path
        if (landmarks.isEmpty()) {
            Dijkstra dijkstra = new Dijkstra();
            dijkstra.computeShortestPaths(graph, source);
            List<Location> shortestPath = dijkstra.getShortestPathTo(destination);
            if (!shortestPath.isEmpty()) {
                routes.add(shortestPath);
            }
            return routes;
        }
        
        // Generate all permutations of landmarks to try different orders
        List<List<Location>> landmarkPermutations = generatePermutations(landmarks, maxRoutes);
        
        for (List<Location> landmarkOrder : landmarkPermutations) {
            // Build a route through all landmarks in the given order
            List<Location> route = new ArrayList<>();
            route.add(source);
            
            Location current = source;
            double totalDistance = 0;
            boolean validRoute = true;
            
            // Find shortest paths between consecutive landmarks
            for (Location landmark : landmarkOrder) {
                Dijkstra dijkstra = new Dijkstra();
                dijkstra.computeShortestPaths(graph, current);
                List<Location> segment = dijkstra.getShortestPathTo(landmark);
                
                if (segment.isEmpty()) {
                    validRoute = false;
                    break;
                }
                
                // Skip the first location as it's already in the route
                for (int i = 1; i < segment.size(); i++) {
                    route.add(segment.get(i));
                }
                
                current = landmark;
            }
            
            // Add final segment from last landmark to destination
            if (validRoute) {
                Dijkstra dijkstra = new Dijkstra();
                dijkstra.computeShortestPaths(graph, current);
                List<Location> segment = dijkstra.getShortestPathTo(destination);
                
                if (segment.isEmpty()) {
                    validRoute = false;
                } else {
                    // Skip the first location as it's already in the route
                    for (int i = 1; i < segment.size(); i++) {
                        route.add(segment.get(i));
                    }
                }
            }
            
            if (validRoute) {
                routes.add(route);
                if (routes.size() >= maxRoutes) {
                    break;
                }
            }
        }
        
        // If we couldn't find routes through all landmarks, try with subsets
        if (routes.isEmpty() && landmarks.size() > 1) {
            for (Location landmark : landmarks) {
                List<Location> singleLandmark = new ArrayList<>();
                singleLandmark.add(landmark);
                List<List<Location>> subRoutes = findRoutesViaLandmarks(source, destination, singleLandmark, 1);
                routes.addAll(subRoutes);
                
                if (routes.size() >= maxRoutes) {
                    break;
                }
            }
        }
        
        // Sort routes by total distance
        routes.sort(Comparator.comparingDouble(this::calculateRouteDistance));
        
        // Return at most maxRoutes routes
        return routes.size() <= maxRoutes ? routes : routes.subList(0, maxRoutes);
    }
    
    /**
     * Finds routes that pass through or near landmarks of a specific type.
     * @param source Source location
     * @param destination Destination location
     * @param landmarkType Type of landmarks to include (e.g., "library", "cafeteria")
     * @param maxRoutes Maximum number of routes to return
     * @return List of routes (each route is a list of locations)
     */
    public List<List<Location>> findRoutesViaLandmarkType(Location source, Location destination, 
                                                        String landmarkType, int maxRoutes) {
        // Find all landmarks of the specified type
        List<Location> typeLandmarks = new ArrayList<>();
        for (Location loc : graph.getNodes()) {
            if (loc.getType().equalsIgnoreCase(landmarkType)) {
                typeLandmarks.add(loc);
            }
        }
        
        // If no landmarks of this type, return empty list
        if (typeLandmarks.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Find routes via these landmarks
        return findRoutesViaLandmarks(source, destination, typeLandmarks, maxRoutes);
    }
    
    /**
     * Generates permutations of landmarks to try different orders.
     * Limits the number of permutations to avoid combinatorial explosion.
     */
    private List<List<Location>> generatePermutations(List<Location> landmarks, int maxPermutations) {
        List<List<Location>> result = new ArrayList<>();
        
        // If there are too many landmarks, just use them in the given order
        // to avoid generating too many permutations
        if (landmarks.size() > 4) {
            result.add(new ArrayList<>(landmarks));
            return result;
        }
        
        // Generate all permutations
        generatePermutationsHelper(new ArrayList<>(), new ArrayList<>(landmarks), result);
        
        // Limit the number of permutations
        if (result.size() > maxPermutations) {
            return result.subList(0, maxPermutations);
        }
        
        return result;
    }
    
    /**
     * Helper method for generating permutations.
     */
    private void generatePermutationsHelper(List<Location> current, List<Location> remaining, 
                                          List<List<Location>> result) {
        if (remaining.isEmpty()) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = 0; i < remaining.size(); i++) {
            Location landmark = remaining.get(i);
            current.add(landmark);
            List<Location> newRemaining = new ArrayList<>(remaining);
            newRemaining.remove(i);
            
            generatePermutationsHelper(current, newRemaining, result);
            
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Calculates the total distance of a route.
     */
    private double calculateRouteDistance(List<Location> route) {
        double totalDistance = 0;
        
        for (int i = 0; i < route.size() - 1; i++) {
            Location current = route.get(i);
            Location next = route.get(i + 1);
            
            Dijkstra dijkstra = new Dijkstra();
            dijkstra.computeShortestPaths(graph, current);
            totalDistance += dijkstra.getDistanceTo(next);
        }
        
        return totalDistance;
    }
}