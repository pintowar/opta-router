package io.github.pintowar.opta.router.solver.timefold.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import io.github.pintowar.opta.router.solver.timefold.domain.location.Location;

import java.util.ArrayList;
import java.util.List;

@PlanningEntity
public class Vehicle implements LocationAware {

    private long id;
    private int capacity;
    private Depot depot;

    @PlanningListVariable
    private List<Customer> customers = new ArrayList<>();

    public Vehicle() {
    }

    public Vehicle(long id, int capacity, Depot depot) {
        this.id = id;
        this.capacity = capacity;
        this.depot = depot;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public Location getLocation() {
        return depot.getLocation();
    }

    @Override
    public String toString() {
        Location location = getLocation();
        if (location.getName() == null) {
            return super.toString();
        }
        return location.getName() + "/" + super.toString();
    }
}
