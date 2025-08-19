package algorithms;

import data.Location;
import graph.Graph;

import java.util.*;

/**
 * Implementation of Vogel Approximation Method (VAM) for route optimization.
 * VAM is typically used for transportation problems but adapted here for route selection.
 */
public class VogelApproximation {
    private final Map<Location, Map<Location, Double>> costMatrix = new HashMap<>();
    private final Map<Location, Double> supply = new HashMap<>();
    private final Map<Location, Double> demand = new HashMap<>();
    private final Map<Location, Location> allocation = new HashMap<>();

    /**
     * Initializes the VAM algorithm with a graph and potential sources/destinations.
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
     * Executes the Vogel Approximation Method to find optimal route allocations.
     * @return Map of source locations to their allocated destinations
     */
    public Map<Location, Location> solve() {
        // Create working copies of supply and demand
        Map<Location, Double> remainingSupply = new HashMap<>(supply);
        Map<Location, Double> remainingDemand = new HashMap<>(demand);
        
        // Continue until all supply is allocated
        while (!remainingSupply.isEmpty()) {
            // Find row and column penalties
            Map<Location, Double> rowPenalties = calculateRowPenalties(remainingSupply.keySet(), 
                                                                     remainingDemand.keySet());
            Map<Location, Double> colPenalties = calculateColumnPenalties(remainingSupply.keySet(), 
                                                                       remainingDemand.keySet());
            
            // Find maximum penalty
            double maxPenalty = -1;
            Location maxPenaltyLoc = null;
            boolean isRow = true;
            
            for (Map.Entry<Location, Double> entry : rowPenalties.entrySet()) {
                if (entry.getValue() > maxPenalty) {
                    maxPenalty = entry.getValue();
                    maxPenaltyLoc = entry.getKey();
                    isRow = true;
                }
            }
            
            for (Map.Entry<Location, Double> entry : colPenalties.entrySet()) {
                if (entry.getValue() > maxPenalty) {
                    maxPenalty = entry.getValue();
                    maxPenaltyLoc = entry.getKey();
                    isRow = false;
                }
            }
            
            // Find minimum cost cell in the selected row/column
            Location source, destination;
            if (isRow) {
                source = maxPenaltyLoc;
                destination = findMinCostDestination(source, remainingDemand.keySet());
            } else {
                destination = maxPenaltyLoc;
                source = findMinCostSource(destination, remainingSupply.keySet());
            }
            
            // Allocate as much as possible
            double allocateAmount = Math.min(remainingSupply.get(source), remainingDemand.get(destination));
            allocation.put(source, destination);
            
            // Update remaining supply and demand
            double newSupply = remainingSupply.get(source) - allocateAmount;
            if (newSupply <= 0.001) { // Using a small epsilon for floating point comparison
                remainingSupply.remove(source);
            } else {
                remainingSupply.put(source, newSupply);
            }
            
            double newDemand = remainingDemand.get(destination) - allocateAmount;
            if (newDemand <= 0.001) { // Using a small epsilon for floating point comparison
                remainingDemand.remove(destination);
            } else {
                remainingDemand.put(destination, newDemand);
            }
        }
        
        return allocation;
    }
    
    /**
     * Calculates penalties for each row (difference between two lowest costs).
     */
    private Map<Location, Double> calculateRowPenalties(Set<Location> sources, Set<Location> destinations) {
        Map<Location, Double> penalties = new HashMap<>();
        
        for (Location source : sources) {
            double lowest = Double.POSITIVE_INFINITY;
            double secondLowest = Double.POSITIVE_INFINITY;
            
            for (Location dest : destinations) {
                double cost = costMatrix.get(source).get(dest);
                if (cost < lowest) {
                    secondLowest = lowest;
                    lowest = cost;
                } else if (cost < secondLowest) {
                    secondLowest = cost;
                }
            }
            
            // If only one destination is left, penalty is just the cost
            double penalty = (secondLowest == Double.POSITIVE_INFINITY) ? lowest : secondLowest - lowest;
            penalties.put(source, penalty);
        }
        
        return penalties;
    }
    
    /**
     * Calculates penalties for each column (difference between two lowest costs).
     */
    private Map<Location, Double> calculateColumnPenalties(Set<Location> sources, Set<Location> destinations) {
        Map<Location, Double> penalties = new HashMap<>();
        
        for (Location dest : destinations) {
            double lowest = Double.POSITIVE_INFINITY;
            double secondLowest = Double.POSITIVE_INFINITY;
            
            for (Location source : sources) {
                double cost = costMatrix.get(source).get(dest);
                if (cost < lowest) {
                    secondLowest = lowest;
                    lowest = cost;
                } else if (cost < secondLowest) {
                    secondLowest = cost;
                }
            }
            
            // If only one source is left, penalty is just the cost
            double penalty = (secondLowest == Double.POSITIVE_INFINITY) ? lowest : secondLowest - lowest;
            penalties.put(dest, penalty);
        }
        
        return penalties;
    }
    
    /**
     * Finds the destination with minimum cost from a given source.
     */
    private Location findMinCostDestination(Location source, Set<Location> destinations) {
        double minCost = Double.POSITIVE_INFINITY;
        Location minDest = null;
        
        for (Location dest : destinations) {
            double cost = costMatrix.get(source).get(dest);
            if (cost < minCost) {
                minCost = cost;
                minDest = dest;
            }
        }
        
        return minDest;
    }
    
    /**
     * Finds the source with minimum cost to a given destination.
     */
    private Location findMinCostSource(Location destination, Set<Location> sources) {
        double minCost = Double.POSITIVE_INFINITY;
        Location minSource = null;
        
        for (Location source : sources) {
            double cost = costMatrix.get(source).get(destination);
            if (cost < minCost) {
                minCost = cost;
                minSource = source;
            }
        }
        
        return minSource;
    }
}