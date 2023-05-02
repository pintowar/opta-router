package io.github.pintowar.opta.router.adapters.geo

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.util.Parameters
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Path
import java.util.*

/**
 * A Simple Wrapper to the GraphHopper class.
 * @param graph A GraphHopper instance.
 */
class GraphHopperGeoService(private val graph: GraphHopper) : GeoService {

    companion object {
        const val VEHICLE = "car"
        const val WEIGHTING = "shortest"
        const val PROFILE = "${VEHICLE}_$WEIGHTING"
    }

    /**
     * Generates a PathWrapper containing the best route between origin and target points.
     * @param origin
     * @param target
     * @return
     */
    override fun simplePath(origin: Coordinate, target: Coordinate): Path {
        val req = GHRequest(origin.lat, origin.lng, target.lat, target.lng)
            .setProfile(PROFILE)
            .putHint(Parameters.Routing.INSTRUCTIONS, false)
            .putHint(Parameters.Routing.CALC_POINTS, false)
            .setLocale(Locale.US)
        return graph.route(req).best.let {
            Path(it.distance, it.time, listOf(origin, target))
        }
    }

    override fun detailedSimplePath(origin: Coordinate, target: Coordinate): Path {
        val req = GHRequest(origin.lat, origin.lng, target.lat, target.lng)
            .setProfile(PROFILE)
            .putHint(Parameters.Routing.INSTRUCTIONS, false)
            .setLocale(Locale.US)
        return graph.route(req).best.let {
            Path(it.distance, it.time, it.points.map { p -> Coordinate(p.lat, p.lon) })
        }
    }
}