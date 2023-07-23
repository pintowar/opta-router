package io.github.pintowar.opta.router.solver.timefold.domain.location;

import java.util.Map;

public class RoadLocation extends Location {

    // Prefer Map over array or List because customers might be added and removed in real-time planning.
    private Map<RoadLocation, Double> travelDistanceMap;

    public RoadLocation() {
    }

    public RoadLocation(long id) {
        super(id);
    }

    public RoadLocation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
    }

    public Map<RoadLocation, Double> getTravelDistanceMap() {
        return travelDistanceMap;
    }

    public void setTravelDistanceMap(Map<RoadLocation, Double> travelDistanceMap) {
        this.travelDistanceMap = travelDistanceMap;
    }

    @Override
    public long getDistanceTo(Location location) {
        if (this == location) {
            return 0L;
        }
        double distance = travelDistanceMap.get((RoadLocation) location);
        // Multiplied by 1000 to avoid floating point arithmetic rounding errors
        return (long) (distance * 1000.0 + 0.5);
    }

}
