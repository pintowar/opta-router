package com.github.util

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopperAPI
import com.graphhopper.PathWrapper

import java.util.Locale

/**
 * A Simple Wrapper to the GraphHopperAPI class.
 * @param graph A GraphHopperAPI instance.
 */
class GraphWrapper(private val graph: GraphHopperAPI) {

    /**
     * Generates a PathWrapper containing the best route between origin and target points.
     * @param origin
     * @param target
     * @return
     */
    fun simplePath(origin: Pair<Double, Double>, target: Pair<Double, Double>): PathWrapper {
        val req = GHRequest(origin.first, origin.second, target.first, target.second)
                .setWeighting("fastest").setVehicle("car").setLocale(Locale.US)
        return graph.route(req).best
    }
}