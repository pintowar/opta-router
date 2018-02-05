package com.github.util

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopperAPI
import com.graphhopper.PathWrapper

import java.util.Locale

class GraphWrapper(private val graph: GraphHopperAPI) {

    fun simplePath(origin: Pair<Double, Double>, target: Pair<Double, Double>): PathWrapper {
        val req = GHRequest(origin.first, origin.second, target.first, target.second)
                .setWeighting("fastest").setVehicle("car").setLocale(Locale.US)
        return graph.route(req).best
    }
}