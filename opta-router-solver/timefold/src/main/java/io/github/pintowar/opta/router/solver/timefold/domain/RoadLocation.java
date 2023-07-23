package io.github.pintowar.opta.router.solver.timefold.domain;

import java.util.Map;

public class RoadLocation {

    private long id;
    private String name = null;
    private double latitude;
    private double longitude;

    // Prefer Map over array or List because customers might be added and removed in real-time planning.
    private Map<RoadLocation, Double> travelDistanceMap;

    public RoadLocation() {
    }

    public RoadLocation(long id) {
        this.id = id;
    }

    public RoadLocation(long id, double latitude, double longitude) {
        this(id);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Map<RoadLocation, Double> getTravelDistanceMap() {
        return travelDistanceMap;
    }

    public void setTravelDistanceMap(Map<RoadLocation, Double> travelDistanceMap) {
        this.travelDistanceMap = travelDistanceMap;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @param location never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getDistanceTo(RoadLocation location) {
        if (this == location) {
            return 0L;
        }
        double distance = travelDistanceMap.get(location);
        // Multiplied by 1000 to avoid floating point arithmetic rounding errors
        return (long) (distance * 1000.0 + 0.5);
    }

    /**
     * The angle relative to the direction EAST.
     *
     * @param location never null
     * @return in Cartesian coordinates
     */
    public double getAngle(RoadLocation location) {
        // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
        double latitudeDifference = location.latitude - latitude;
        double longitudeDifference = location.longitude - longitude;
        return Math.atan2(latitudeDifference, longitudeDifference);
    }

    @Override
    public String toString() {
        if (name == null) {
            return super.toString();
        }
        return name;
    }

}
