package algorithms;

import data.Location;
import graph.Graph;

import java.util.*;

/**
 * Utility class for sorting routes based on different criteria.
 */
public class RouteSorter {
    
    /**
     * Sorts routes by total distance (shortest first).
     * @param routes List of routes to sort
     * @param graph The campus graph
     * @return Sorted list of routes
     */
    public static List<List<Location>> sortByDistance(List<List<Location>> routes, Graph graph) {
        List<List<Location>> sortedRoutes = new ArrayList<>(routes);
        sortedRoutes.sort(Comparator.comparingDouble(route -> calculateRouteDistance(route, graph)));
        return sortedRoutes;
    }
    
    /**
     * Sorts routes by estimated arrival time (earliest first).
     * @param routes List of routes to sort
     * @param graph The campus graph
     * @return Sorted list of routes
     */
    public static List<List<Location>> sortByArrivalTime(List<List<Location>> routes, Graph graph) {
        List<List<Location>> sortedRoutes = new ArrayList<>(routes);
        sortedRoutes.sort(Comparator.comparingDouble(route -> calculateRouteTravelTime(route, graph)));
        return sortedRoutes;
    }
    
    /**
     * Sorts routes by number of landmarks passed (most first).
     * @param routes List of routes to sort
     * @param landmarkTypes Set of landmark types to count
     * @return Sorted list of routes
     */
    public static List<List<Location>> sortByLandmarks(List<List<Location>> routes, Set<String> landmarkTypes) {
        List<List<Location>> sortedRoutes = new ArrayList<>(routes);
        sortedRoutes.sort((r1, r2) -> Integer.compare(
            countLandmarks(r2, landmarkTypes),  // Note: r2 first for descending order
            countLandmarks(r1, landmarkTypes)
        ));
        return sortedRoutes;
    }
    
    /**
     * Implements merge sort algorithm for sorting routes by distance.
     * @param routes List of routes to sort
     * @param graph The campus graph
     * @return Sorted list of routes
     */
    public static List<List<Location>> mergeSortByDistance(List<List<Location>> routes, Graph graph) {
        if (routes.size() <= 1) {
            return routes;
        }
        
        int mid = routes.size() / 2;
        List<List<Location>> left = mergeSortByDistance(
            new ArrayList<>(routes.subList(0, mid)), graph);
        List<List<Location>> right = mergeSortByDistance(
            new ArrayList<>(routes.subList(mid, routes.size())), graph);
        
        return merge(left, right, graph);
    }
    
    /**
     * Helper method for merge sort.
     */
    private static List<List<Location>> merge(List<List<Location>> left, List<List<Location>> right, Graph graph) {
        List<List<Location>> result = new ArrayList<>();
        int leftIndex = 0, rightIndex = 0;
        
        while (leftIndex < left.size() && rightIndex < right.size()) {
            double leftDist = calculateRouteDistance(left.get(leftIndex), graph);
            double rightDist = calculateRouteDistance(right.get(rightIndex), graph);
            
            if (leftDist <= rightDist) {
                result.add(left.get(leftIndex));
                leftIndex++;
            } else {
                result.add(right.get(rightIndex));
                rightIndex++;
            }
        }
        
        // Add remaining elements
        while (leftIndex < left.size()) {
            result.add(left.get(leftIndex));
            leftIndex++;
        }
        
        while (rightIndex < right.size()) {
            result.add(right.get(rightIndex));
            rightIndex++;
        }
        
        return result;
    }
    
    /**
     * Implements quick sort algorithm for sorting routes by travel time.
     * @param routes List of routes to sort
     * @param graph The campus graph
     * @return Sorted list of routes
     */
    public static List<List<Location>> quickSortByTravelTime(List<List<Location>> routes, Graph graph) {
        if (routes.size() <= 1) {
            return routes;
        }
        
        List<List<Location>> result = new ArrayList<>(routes);
        quickSortHelper(result, 0, result.size() - 1, graph);
        return result;
    }
    
    /**
     * Helper method for quick sort.
     */
    private static void quickSortHelper(List<List<Location>> routes, int low, int high, Graph graph) {
        if (low < high) {
            int pivotIndex = partition(routes, low, high, graph);
            quickSortHelper(routes, low, pivotIndex - 1, graph);
            quickSortHelper(routes, pivotIndex + 1, high, graph);
        }
    }
    
    /**
     * Partition method for quick sort.
     */
    private static int partition(List<List<Location>> routes, int low, int high, Graph graph) {
        List<Location> pivot = routes.get(high);
        double pivotTime = calculateRouteTravelTime(pivot, graph);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            double routeTime = calculateRouteTravelTime(routes.get(j), graph);
            if (routeTime <= pivotTime) {
                i++;
                // Swap routes[i] and routes[j]
                List<Location> temp = routes.get(i);
                routes.set(i, routes.get(j));
                routes.set(j, temp);
            }
        }
        
        // Swap routes[i+1] and routes[high] (pivot)
        List<Location> temp = routes.get(i + 1);
        routes.set(i + 1, routes.get(high));
        routes.set(high, temp);
        
        return i + 1;
    }
    
    /**
     * Calculates the total distance of a route.
     */
    private static double calculateRouteDistance(List<Location> route, Graph graph) {
        double totalDistance = 0;
        
        for (int i = 0; i < route.size() - 1; i++) {
            Location current = route.get(i);
            Location next = route.get(i + 1);
            
            // Find the edge between current and next
            for (data.Edge edge : graph.getEdgesFrom(current.getId())) {
                if (edge.getDst() == next.getId()) {
                    totalDistance += edge.getDistanceMeters();
                    break;
                }
            }
        }
        
        return totalDistance;
    }
    
    /**
     * Calculates the total travel time of a route.
     */
    private static double calculateRouteTravelTime(List<Location> route, Graph graph) {
        double totalTime = 0;
        
        for (int i = 0; i < route.size() - 1; i++) {
            Location current = route.get(i);
            Location next = route.get(i + 1);
            
            // Find the edge between current and next
            for (data.Edge edge : graph.getEdgesFrom(current.getId())) {
                if (edge.getDst() == next.getId()) {
                    totalTime += edge.travelTimeMinutes(edge.getDefaultSpeedKmph());
                    break;
                }
            }
        }
        
        return totalTime;
    }
    
    /**
     * Counts the number of landmarks of specified types in a route.
     */
    private static int countLandmarks(List<Location> route, Set<String> landmarkTypes) {
        int count = 0;
        for (Location loc : route) {
            if (landmarkTypes.contains(loc.getType())) {
                count++;
            }
        }
        return count;
    }
}