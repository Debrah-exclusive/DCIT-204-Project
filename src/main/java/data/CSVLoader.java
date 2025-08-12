package data;

import graph.Graph;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CSVLoader {

    public Map<Integer, Location> loadNodes(String resourcePath) throws Exception {
        Map<Integer, Location> nodes = new HashMap<>();
        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) throw new Exception("Resource not found: " + resourcePath);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0].trim());
                String label = parts[1].trim();
                double lat = Double.parseDouble(parts[2].trim());
                double lon = Double.parseDouble(parts[3].trim());
                String type = parts[4].trim();
                nodes.put(id, new Location(id, label, lat, lon, type));
            }
        }
        return nodes;
    }

    public Graph loadGraph(String resourcePath, Map<Integer, Location> nodes) throws Exception {
        Graph g = new Graph();
        for (Location l : nodes.values()) g.addNode(l);

        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) throw new Exception("Resource not found: " + resourcePath);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                int src = Integer.parseInt(parts[0].trim());
                int dst = Integer.parseInt(parts[1].trim());
                double dist = Double.parseDouble(parts[2].trim());
                double speed = Double.parseDouble(parts[3].trim());
                g.addEdge(new data.Edge(src, dst, dist, speed));
            }
        }
        return g;
    }
}