package algorithms;

import data.Location;
import graph.Graph;

import java.util.*;

/**
 * Implementation of Northwest Corner Method for route optimization.
 * This is a transportation problem algorithm adapted for route selection.
 */
public class NorthwestCorner {
    private final Map<Location, Map<Location, Double>> costMatrix = new HashMap<>();
    private final Map<Location, Double> supply = new HashMap<>();
    private final Map<Location, Double> demand = new HashMap<>();
    private final Map<Location, Location> allocation = new HashMap<>();

    /**
     * Initializes the Northwest Corner algorithm with a graph and potential sources/destinations.
     * @param graph The campus graph
     * @param sources Potential starting locations
     * @param destinations Potential destination locations
     */
    public void initialize(Graph graph, List<Location> sources, List<Location> destinations) {
        // Clear previous data
        costMatrix.clear();
        supply.clear();
        demand.clear();
        allocation.clear();
        
        // Initialize cost matrix with travel times between locations
        for (Location source : sources) {
            costMatrix.put(source, new HashMap<>());
            // Run Dijkstra from this source to compute costs to all destinations
            Dijkstra dijkstra = new Dijkstra();
            dijkstra.computeShortestPaths(graph, source);
            
            for (Location dest : destinations) {
                double cost = dijkstra.getDistanceTo(dest);
                costMatrix.get(source).put(dest, cost);
            }
            
            // For simplicity, we set equal supply for all sources
            supply.put(source, 1.0);
        }
        
        // For simplicity, we set equal demand for all destinations
        for (Location dest : destinations) {
            demand.put(dest, 1.0 / destinations.size() * sources.size());
        }
    }
    
    /**
     * Executes the Northwest Corner Method to find route allocations.
     * Unlike Vogel's method, this doesn't consider costs during initial allocation.
     * @return Map of source locations to their allocated destinations
     */
    public Map<Location, Location> solve() {
        // Create working copies of supply and demand
        Map<Location, Double> remainingSupply = new HashMap<>(supply);
        Map<Location, Double> remainingDemand = new HashMap<>(demand);
        
        // Convert to lists to maintain order
        List<Location> sources = new ArrayList<>(remainingSupply.keySet());
        List<Location> destinations = new ArrayList<>(remainingDemand.keySet());
        
        int i = 0; // source index
        int j = 0; // destination index
        
        // Continue until all supply is allocated
        while (i < sources.size() && j < destinations.size()) {
            Location source = sources.get(i);
            Location destination = destinations.get(j);
            
            double supplyValue = remainingSupply.get(source);
            double demandValue = remainingDemand.get(destination);
            
            // Allocate as much as possible
            double allocateAmount = Math.min(supplyValue, demandValue);
            allocation.put(source, destination);
            
            // Update remaining supply and demand
            supplyValue -= allocateAmount;
            demandValue -= allocateAmount;
            
            // If supply is exhausted, move to next source
            if (supplyValue <= 0.001) { // Using a small epsilon for floating point comparison
                i++;
            } else {
                remainingSupply.put(source, supplyValue);
            }
            
            // If demand is satisfied, move to next destination
            if (demandValue <= 0.001) { // Using a small epsilon for floating point comparison
                j++;
            } else {
                remainingDemand.put(destination, demandValue);
            }
        }
        
        return allocation;
    }
    
    /**
     * Calculates the total cost of the current allocation.
     * @return The total cost
     */
    public double calculateTotalCost() {
        double totalCost = 0.0;
        
        for (Map.Entry<Location, Location> entry : allocation.entrySet()) {
            Location source = entry.getKey();
            Location destination = entry.getValue();
            totalCost += costMatrix.get(source).get(destination);
        }
        
        return totalCost;
    }
    
    /**
     * Gets the cost matrix for inspection.
     * @return The cost matrix
     */
    public Map<Location, Map<Location, Double>> getCostMatrix() {
        return costMatrix;
    }
}