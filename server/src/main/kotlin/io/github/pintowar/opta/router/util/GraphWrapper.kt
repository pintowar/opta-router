package io.github.pintowar.opta.router.util

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import com.graphhopper.util.Parameters
import java.util.Locale

/**
 * A Simple Wrapper to the GraphHopperAPI class.
 * @param graph A GraphHopperAPI instance.
 */
class GraphWrapper(private val graph: GraphHopper) {

    /**
     * Generates a PathWrapper containing the best route between origin and target points.
     * @param origin
     * @param target
     * @return
     */
    fun simplePath(origin: Pair<Double, Double>, target: Pair<Double, Double>): ResponsePath {
        val req = GHRequest(origin.first, origin.second, target.first, target.second)
            .setProfile("car_shortest")
            .putHint(Parameters.Routing.INSTRUCTIONS, false)
            .putHint(Parameters.Routing.CALC_POINTS, false)
            .setLocale(Locale.US)
        return graph.route(req).best
    }

    fun detailedSimplePath(origin: Pair<Double, Double>, target: Pair<Double, Double>): ResponsePath {
        val req = GHRequest(origin.first, origin.second, target.first, target.second)
            .setProfile("car_shortest")
            .putHint(Parameters.Routing.INSTRUCTIONS, false)
            .setLocale(Locale.US)
        return graph.route(req).best
    }
}