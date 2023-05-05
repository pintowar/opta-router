package io.github.pintowar.opta.router.adapters.geo

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.util.Parameters
import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import java.math.BigDecimal
import java.math.RoundingMode
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

    override fun detailedPaths(solution: VrpSolution): VrpSolution {
        val newRoutes = solution.routes.map { route ->
            val aux = route.order
                .windowed(2, 1, false)
                .map { (a, b) -> detailedSimplePath(a, b) }

            val rep = aux.flatMap { it.coordinates }.map { Coordinate(it.lat, it.lng) }
            val dist = BigDecimal(aux.sumOf { it.distance / 1000 }).setScale(2, RoundingMode.HALF_UP)
            val time = BigDecimal(aux.sumOf { it.time.toDouble() / (60 * 1000) }).setScale(2, RoundingMode.HALF_UP)

            Route(dist, time, rep, route.customerIds)
        }
        return solution.copy(routes = newRoutes)
    }

    private fun detailedSimplePath(origin: Coordinate, target: Coordinate): Path {
        val req = GHRequest(origin.lat, origin.lng, target.lat, target.lng)
            .setProfile(PROFILE)
            .putHint(Parameters.Routing.INSTRUCTIONS, false)
            .setLocale(Locale.US)
        return graph.route(req).best.let {
            Path(it.distance, it.time, it.points.map { p -> Coordinate(p.lat, p.lon) })
        }
    }
}