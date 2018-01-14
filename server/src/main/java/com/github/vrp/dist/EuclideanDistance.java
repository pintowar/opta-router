package com.github.vrp.dist;

import kotlin.Pair;

import java.util.Collections;
import java.util.List;

public class EuclideanDistance implements Distance {
    private List<Pair<Double, Double>> locations;

    private int distMatrixSize;
    private int n;
    private double[] distMatrix;
    private double avgSpeed;

    public EuclideanDistance(List<Pair<Double, Double>> locations, double avgSpeed) {
        this.locations = Collections.unmodifiableList(locations);
        this.n = this.locations.size();
        this.avgSpeed = avgSpeed;

        this.distMatrixSize = n * (n - 1) / 2;
        this.distMatrix = createDistMatrix();
    }

    public EuclideanDistance(List<Pair<Double, Double>> locations) {
        this(locations, 60);
    }

    private double[] createDistMatrix() {
        double[] matrix = new double[distMatrixSize];
        for (int i = 0; i < this.n; i++) {
            for (int j = i; j < this.n; j++) {
                if (i != j) {
                    matrix[calcIdx(i, j)] = euclideanDistance(i, j);
                }
            }
        }
        return matrix;
    }

    private int calcIdx(int i, int j) {
        return distMatrixSize - (this.n - i) * (this.n - i - 1) / 2 + j - i - 1;
    }

    private double euclideanDistance(int i, int j) {
        Pair<Double, Double> firstLocation = locations.get(i);
        Pair<Double, Double> secondLocation = locations.get(j);
        return Math.sqrt((Math.pow(firstLocation.getFirst() - secondLocation.getFirst(), 2) +
                Math.pow(firstLocation.getSecond() - secondLocation.getSecond(), 2)));
    }

    @Override
    public double distance(int i, int j) {
        if (i == j) return 0.0;
        else if (i < j) return distMatrix[calcIdx(i, j)];
        else return distMatrix[calcIdx(j, i)];
    }

    @Override
    public double time(int i, int j) {
        return this.distance(i, j) / this.avgSpeed;
    }
}
