package com.github.vrp.dist;

import com.github.util.GraphWrapper;
import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.PathWrapper;
import kotlin.Pair;

import java.util.List;
import java.util.Locale;

public class PathDistance implements Distance {
    private List<Pair<Double, Double>> locations;

    private int n;
    private double[][] distMatrix;
    private long[][] timeMatrix;

    public PathDistance(List<Pair<Double, Double>> locations, GraphWrapper graph) {
        this.locations = locations;
        this.n = this.locations.size();

        createDistMatrix(graph);
    }

    private void createDistMatrix(GraphWrapper graph) {
        this.distMatrix = new double[this.n][this.n];
        this.timeMatrix = new long[this.n][this.n];
        for (int i = 0; i < this.n; i++) {
            Pair<Double, Double> a = locations.get(i);
            for (int j = 0; j < this.n; j++) {
                if (i != j) {
                    Pair<Double, Double> b = locations.get(j);
                    PathWrapper path = graph.simplePath(a, b);
                    this.distMatrix[i][j] = path.getDistance() / 1000;
                    this.timeMatrix[i][j] = path.getTime() / 1000;
                } else {
                    this.distMatrix[i][j] = 0.0;
                    this.timeMatrix[i][j] = 0;
                }
            }
        }
    }

    @Override
    public double distance(int i, int j) {
        return distMatrix[i][j];
    }

    @Override
    public double time(int i, int j) {
        return timeMatrix[i][j];
    }
}
