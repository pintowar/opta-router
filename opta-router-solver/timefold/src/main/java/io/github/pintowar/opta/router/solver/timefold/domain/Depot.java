package io.github.pintowar.opta.router.solver.timefold.domain;

import io.github.pintowar.opta.router.solver.timefold.domain.location.Location;

public class Depot {

    private long id;
    private Location location;

    public Depot() {
    }

    public Depot(long id, Location location) {
        this.id = id;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
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
