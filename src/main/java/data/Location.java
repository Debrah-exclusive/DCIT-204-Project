package data;

public class Location {
    private final int id;
    private final String label;
    private final double lat;
    private final double lon;
    private final String type;

    public Location(int id, String label, double lat, double lon, String type) {
        this.id = id;
        this.label = label;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getType() { return type; }
}