package io.github.pintowar.opta.router.solver.timefold.domain;

public class Depot {

    private long id;
    private RoadLocation location;

    public Depot() {
    }

    public Depot(long id, RoadLocation location) {
        this.id = id;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RoadLocation getLocation() {
        return location;
    }

    public void setLocation(RoadLocation location) {
        this.location = location;
    }

    @Override
    public String toString() {
        if (location.getName() == null) {
            return super.toString();
        }
        return location.getName();
    }

}
