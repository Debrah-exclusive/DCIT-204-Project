package app;

import data.CSVLoader;
import data.Location;
import graph.Graph;
import algorithms.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

public class UGNavigateApp extends Application {
    private Graph graph;
    private Map<Integer, Location> locations;
    private TextArea output;
    private ComboBox<String> startBox;
    private ComboBox<String> endBox;
    private ComboBox<String> landmarkBox;
    private ComboBox<String> algorithmBox;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load data
        CSVLoader loader = new CSVLoader();
        locations = loader.loadNodes("/data/nodes.csv");
        graph = loader.loadGraph("/data/edges.csv", locations);

        // UI Controls
        startBox = new ComboBox<>();
        endBox = new ComboBox<>();
        landmarkBox = new ComboBox<>();
        algorithmBox = new ComboBox<>();
        
        // Populate location dropdowns
        for (Location loc : locations.values()) {
            startBox.getItems().add(loc.getLabel());
            endBox.getItems().add(loc.getLabel());
        }
        
        // Populate landmark types
        Set<String> landmarkTypes = new HashSet<>();
        for (Location loc : locations.values()) {
            if (loc.getType() != null && !loc.getType().isEmpty()) {
                landmarkTypes.add(loc.getType());
            }
        }
        landmarkBox.getItems().addAll(landmarkTypes);
        landmarkBox.getItems().add(0, "No specific landmarks");
        landmarkBox.setValue("No specific landmarks");
        
        // Populate algorithm selection
        algorithmBox.getItems().addAll(
            "Dijkstra's Algorithm",
            "A* Search Algorithm", 
            "Floyd-Warshall Algorithm",
            "Vogel Approximation Method",
            "Northwest Corner Method",
            "Critical Path Method"
        );
        algorithmBox.setValue("Dijkstra's Algorithm");
        
        startBox.setEditable(true);
        endBox.setEditable(true);

        Button findBtn = new Button("Find Routes");
        findBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        output = new TextArea();
        output.setEditable(false);
        output.setPrefHeight(400);
        output.setFont(Font.font("Monospaced", 12));

        findBtn.setOnAction(e -> findRoutes());

        // Layout
        HBox inputRow1 = new HBox(10, new Label("Start:"), startBox, new Label("End:"), endBox);
        HBox inputRow2 = new HBox(10, new Label("Landmarks:"), landmarkBox, new Label("Algorithm:"), algorithmBox, findBtn);
        inputRow1.setPadding(new Insets(10));
        inputRow2.setPadding(new Insets(10));

        VBox root = new VBox(10, inputRow1, inputRow2, output);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("UG Navigate - Complete Navigation Solution");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void findRoutes() {
        String startLabel = startBox.getValue();
        String endLabel = endBox.getValue();
        String landmarkType = landmarkBox.getValue();
        String algorithm = algorithmBox.getValue();
        
        if (startLabel == null || endLabel == null) {
            output.setText("Please select both start and end locations.");
            return;
        }

        Optional<Location> startLoc = locations.values().stream()
                .filter(l -> l.getLabel().equals(startLabel))
                .findFirst();
        Optional<Location> endLoc = locations.values().stream()
                .filter(l -> l.getLabel().equals(endLabel))
                .findFirst();

        if (startLoc.isEmpty() || endLoc.isEmpty()) {
            output.setText("One or both locations not found. Make sure selection matches the list.");
            return;
        }

        Location start = startLoc.get();
        Location end = endLoc.get();
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== UG Navigate - Route Analysis ===\n\n");
        sb.append("From: ").append(start.getLabel()).append("\n");
        sb.append("To: ").append(end.getLabel()).append("\n");
        sb.append("Algorithm: ").append(algorithm).append("\n");
        sb.append("Landmark Preference: ").append(landmarkType).append("\n\n");
        
        List<List<Location>> allRoutes = new ArrayList<>();
        
        // Generate routes using different algorithms
        switch (algorithm) {
            case "Dijkstra's Algorithm":
                allRoutes.addAll(generateDijkstraRoutes(start, end));
                break;
            case "A* Search Algorithm":
                allRoutes.addAll(generateAStarRoutes(start, end));
                break;
            case "Floyd-Warshall Algorithm":
                allRoutes.addAll(generateFloydWarshallRoutes(start, end));
                break;
            case "Vogel Approximation Method":
                allRoutes.addAll(generateVogelRoutes(start, end));
                break;
            case "Northwest Corner Method":
                allRoutes.addAll(generateNorthwestCornerRoutes(start, end));
                break;
            case "Critical Path Method":
                allRoutes.addAll(generateCriticalPathRoutes(start, end));
                break;
        }
        
        // Add landmark-based routes if landmarks are specified
        if (!landmarkType.equals("No specific landmarks")) {
            allRoutes.addAll(generateLandmarkRoutes(start, end, landmarkType));
        }
        
        // Remove duplicates and sort routes
        allRoutes = removeDuplicateRoutes(allRoutes);
        allRoutes = RouteSorter.sortByDistance(allRoutes, graph);
        
        // Display routes
        if (allRoutes.isEmpty()) {
            sb.append("No routes found between these locations.\n");
        } else {
            sb.append("Found ").append(allRoutes.size()).append(" route(s):\n\n");
            
            for (int i = 0; i < Math.min(allRoutes.size(), 5); i++) {
                List<Location> route = allRoutes.get(i);
                sb.append("--- Route ").append(i + 1).append(" ---\n");
                sb.append("Path: ");
                for (int j = 0; j < route.size(); j++) {
                    sb.append(route.get(j).getLabel());
                    if (j < route.size() - 1) sb.append(" → ");
                }
                sb.append("\n");
                
                double distance = computeDistance(route, graph);
                double time = computeTravelTime(route, graph);
                
                sb.append(String.format("Distance: %.2f km\n", distance / 1000.0));
                sb.append(String.format("Travel Time: %.0f minutes\n", time));
                sb.append("\n");
            }
        }
        
        output.setText(sb.toString());
    }
    
    private List<List<Location>> generateDijkstraRoutes(Location start, Location end) {
        List<List<Location>> routes = new ArrayList<>();
        Dijkstra dijkstra = new Dijkstra();
        dijkstra.computeShortestPaths(graph, start);
        List<Location> path = dijkstra.getShortestPathTo(end);
        if (!path.isEmpty()) {
            routes.add(path);
        }
        return routes;
    }
    
    private List<List<Location>> generateAStarRoutes(Location start, Location end) {
        List<List<Location>> routes = new ArrayList<>();
        AStarSearch aStar = new AStarSearch(graph);
        List<Location> path = aStar.findOptimalPath(start, end);
        if (!path.isEmpty()) {
            routes.add(path);
        }
        return routes;
    }
    
    private List<List<Location>> generateFloydWarshallRoutes(Location start, Location end) {
        List<List<Location>> routes = new ArrayList<>();
        FloydWarshall floyd = new FloydWarshall();
        floyd.computeAllPairsShortestPaths(graph);
        List<Location> path = floyd.getShortestPath(start, end);
        if (!path.isEmpty()) {
            routes.add(path);
        }
        return routes;
    }
    
    private List<List<Location>> generateVogelRoutes(Location start, Location end) {
        List<List<Location>> routes = new ArrayList<>();
        VogelApproximation vogel = new VogelApproximation();
        List<Location> sources = Arrays.asList(start);
        List<Location> destinations = Arrays.asList(end);
        vogel.initialize(graph, sources, destinations);
        Map<Location, Location> allocation = vogel.solve();
        if (allocation.containsKey(start)) {
            routes.add(Arrays.asList(start, end));
        }
        return routes;
    }
    
    private List<List<Location>> generateNorthwestCornerRoutes(Location start, Location end) {
        List<List<Location>> routes = new ArrayList<>();
        NorthwestCorner nwc = new NorthwestCorner();
        List<Location> sources = Arrays.asList(start);
        List<Location> destinations = Arrays.asList(end);
        nwc.initialize(graph, sources, destinations);
        Map<Location, Location> allocation = nwc.solve();
        if (allocation.containsKey(start)) {
            routes.add(Arrays.asList(start, end));
        }
        return routes;
    }
    
    private List<List<Location>> generateCriticalPathRoutes(Location start, Location end) {
        List<List<Location>> routes = new ArrayList<>();
        CriticalPath cpm = new CriticalPath();
        cpm.computeCriticalPath(graph, start, end);
        List<Integer> criticalPathIds = cpm.getCriticalPath();
        if (!criticalPathIds.isEmpty()) {
            List<Location> path = new ArrayList<>();
            for (int id : criticalPathIds) {
                Location loc = locations.get(id);
                if (loc != null) path.add(loc);
            }
            if (!path.isEmpty()) routes.add(path);
        }
        return routes;
    }
    
    private List<List<Location>> generateLandmarkRoutes(Location start, Location end, String landmarkType) {
        List<List<Location>> routes = new ArrayList<>();
        LandmarkRouting landmarkRouting = new LandmarkRouting(graph);
        List<List<Location>> landmarkRoutes = landmarkRouting.findRoutesViaLandmarkType(start, end, landmarkType, 3);
        routes.addAll(landmarkRoutes);
        return routes;
    }
    
    private List<List<Location>> removeDuplicateRoutes(List<List<Location>> routes) {
        Set<String> routeStrings = new HashSet<>();
        List<List<Location>> uniqueRoutes = new ArrayList<>();
        
        for (List<Location> route : routes) {
            String routeString = route.stream()
                .map(Location::getId)
                .map(String::valueOf)
                .reduce("", (a, b) -> a + "," + b);
            
            if (!routeStrings.contains(routeString)) {
                routeStrings.add(routeString);
                uniqueRoutes.add(route);
            }
        }
        
        return uniqueRoutes;
    }

    /** Helper method to compute total travel time along the route */
    private double computeTravelTime(List<Location> path, Graph graph) {
        double totalTime = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int src = path.get(i).getId();
            int dst = path.get(i + 1).getId();
            List<data.Edge> edges = graph.getEdgesFrom(src);
            for (data.Edge e : edges) {
                if (e.getDst() == dst) {
                    totalTime += e.travelTimeMinutes(e.getDefaultSpeedKmph());
                    break;
                }
            }
        }
        return totalTime;
    }

    /** Helper method to compute total distance (in meters) along the route */
    private double computeDistance(List<Location> path, Graph graph) {
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int src = path.get(i).getId();
            int dst = path.get(i + 1).getId();
            List<data.Edge> edges = graph.getEdgesFrom(src);
            for (data.Edge e : edges) {
                if (e.getDst() == dst) {
                    totalDistance += e.getDistanceMeters();
                    break;
                }
            }
        }
        return totalDistance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
