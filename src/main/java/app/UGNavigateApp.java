package app;

import data.CSVLoader;
import data.Location;
import graph.Graph;
import algorithms.Dijkstra;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class UGNavigateApp extends Application {
    private Graph graph;
    private Map<Integer, Location> locations;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load data
        CSVLoader loader = new CSVLoader();
        locations = loader.loadNodes("/data/nodes.csv");
        graph = loader.loadGraph("/data/edges.csv", locations);

        // UI Controls
        ComboBox<String> startBox = new ComboBox<>();
        ComboBox<String> endBox = new ComboBox<>();
        for (Location loc : locations.values()) {
            startBox.getItems().add(loc.getLabel());
            endBox.getItems().add(loc.getLabel());
        }
        startBox.setEditable(true);
        endBox.setEditable(true);

        Button findBtn = new Button("Find Shortest Route");
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefHeight(300);

        findBtn.setOnAction(e -> {
            String startLabel = startBox.getValue();
            String endLabel = endBox.getValue();
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

            // Run Dijkstra
            Dijkstra dijkstra = new Dijkstra();
            dijkstra.computeShortestPaths(graph, startLoc.get());
            double dist = dijkstra.getDistances().getOrDefault(endLoc.get(), Double.POSITIVE_INFINITY);

            if (Double.isInfinite(dist)) {
                output.setText("No path found.");
                return;
            }

            // Build output
            List<Location> path = dijkstra.getShortestPathTo(endLoc.get());
            StringBuilder sb = new StringBuilder();
            sb.append("Shortest Path:\n");
            for (Location loc : path) {
                sb.append(loc.getLabel()).append("\n");
            }
            sb.append(String.format("\nTotal distance (meters): %.2f", dist));

            double totalTime = computeTravelTime(path, graph);
            sb.append(String.format("\nEstimated travel time (minutes): %.2f", totalTime));

            output.setText(sb.toString());
        });

        HBox inputRow = new HBox(10, new Label("Start:"), startBox, new Label("End:"), endBox, findBtn);
        inputRow.setPadding(new Insets(10));

        VBox root = new VBox(10, inputRow, output);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 450);
        primaryStage.setTitle("UG Navigate - Project Start!");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    public static void main(String[] args) {
        launch(args);
    }
}
