package com.github.util;

import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.PathWrapper;
import kotlin.Pair;

import java.util.Locale;

public class GraphWrapper {

    private final GraphHopperAPI graph;

    public GraphWrapper(GraphHopperAPI graph) {
        this.graph = graph;
    }

    public PathWrapper simplePath(Pair<Double, Double> origin, Pair<Double, Double> target) {
        GHRequest req = new GHRequest(origin.component1(), origin.component2(), target.component1(), target.component2())
                .setWeighting("fastest")
                .setVehicle("car").setLocale(Locale.US);
        return graph.route(req).getBest();
    }
}