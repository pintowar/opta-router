package com.github.vrp.dist;

public interface Distance {
    double distance(int i, int j);

    double time(int i, int j);
}
