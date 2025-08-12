package data;

public class Edge {
    private final int src;
    private final int dst;
    private final double distanceMeters;
    private final double defaultSpeedKmph;

    public Edge(int src, int dst, double distanceMeters, double defaultSpeedKmph) {
        this.src = src;
        this.dst = dst;
        this.distanceMeters = distanceMeters;
        this.defaultSpeedKmph = defaultSpeedKmph;
    }

    public int getSrc() { return src; }
    public int getDst() { return dst; }
    public double getDistanceMeters() { return distanceMeters; }
    public double getDefaultSpeedKmph() { return defaultSpeedKmph; }

    public double travelTimeMinutes(double speedKmphOverride) {
        double speed = speedKmphOverride > 0 ? speedKmphOverride : defaultSpeedKmph;
        return (distanceMeters / 1000.0) / speed * 60.0;
    }
}