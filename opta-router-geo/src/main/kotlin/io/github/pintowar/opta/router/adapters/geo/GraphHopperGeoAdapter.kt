package io.github.pintowar.opta.router.adapters.geo

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import com.graphhopper.json.Statement
import com.graphhopper.util.CustomModel
import com.graphhopper.util.Parameters
import com.graphhopper.util.TurnCostsConfig
import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class GraphHopperGeoAdapter(
    private val path: String,
    private val location: String
) : GeoPort {
    companion object {
        const val NAME = "car"
    }

    private val graph: GraphHopper =
        GraphHopper()
            .apply {
                val statements =
                    listOf(
                        Statement.If("!car_access", Statement.Op.MULTIPLY, "0"),
                        Statement.Else(Statement.Op.LIMIT, "car_average_speed")
                    )
                val profs =
                    listOf(
                        Profile(NAME).apply {
                            turnCostsConfig = TurnCostsConfig.car()
                            customModel =
                                CustomModel().apply {
                                    distanceInfluence = 90.0
                                    priority.addAll(statements)
                                    speed.addAll(statements)
                                }
                        }
                    )

                osmFile = path
                graphHopperLocation = location
                profiles = profs
                chPreparationHandler.preparationThreads = Runtime.getRuntime().availableProcessors()
                chPreparationHandler.setCHProfiles(profs.map { CHProfile(it.name) })

                setEncodedValuesString("car_access,car_average_speed")
                setMinNetworkSize(200)
            }.importOrLoad()

    /**
     * Generates a PathWrapper containing the best route between origin and target points.
     * @param origin
     * @param target
     * @return
     */
    override suspend fun simplePath(
        origin: Coordinate,
        target: Coordinate
    ): Path {
        val req =
            GHRequest(origin.lat, origin.lng, target.lat, target.lng)
                .setProfile(NAME)
                .putHint(Parameters.Routing.INSTRUCTIONS, false)
                .putHint(Parameters.Routing.CALC_POINTS, false)
                .setLocale(Locale.US)
        return graph.route(req).best.let {
            Path(it.distance, it.time, listOf(origin, target))
        }
    }

    override suspend fun detailedPaths(routes: List<Route>): List<Route> =
        routes.map { route ->
            val aux =
                route.order
                    .windowed(2, 1, false)
                    .map { (a, b) -> detailedSimplePath(a, b) }

            val rep = aux.flatMap { it.coordinates }.map { LatLng(it.lat, it.lng) }
            val dist = BigDecimal(aux.sumOf { it.distance / 1000 }).setScale(2, RoundingMode.HALF_UP)
            val time = BigDecimal(aux.sumOf { it.time.toDouble() / (60 * 1000) }).setScale(2, RoundingMode.HALF_UP)

            Route(dist, time, route.totalDemand, rep, route.customerIds)
        }

    override suspend fun generateMatrix(locations: Set<Location>): VrpProblemMatrix {
        val ids = locations.map { it.id }
        val (travelDistances, travelTimes) =
            locations
                .flatMap { a ->
                    locations.map { b -> simplePath(a, b).let { it.distance to it.time } }
                }.unzip()
        return VrpProblemMatrix(ids, travelDistances, travelTimes)
    }

    private fun detailedSimplePath(
        origin: Coordinate,
        target: Coordinate
    ): Path {
        val req =
            GHRequest(origin.lat, origin.lng, target.lat, target.lng)
                .setProfile(NAME)
                .putHint(Parameters.Routing.INSTRUCTIONS, false)
                .setLocale(Locale.US)
        return graph.route(req).best.let {
            Path(it.distance, it.time, it.points.map { p -> LatLng(p.lat, p.lon) })
        }
    }
}