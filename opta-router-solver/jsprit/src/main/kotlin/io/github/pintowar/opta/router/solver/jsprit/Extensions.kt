package io.github.pintowar.opta.router.solver.jsprit

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.job.Service
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
import com.graphhopper.jsprit.core.util.Coordinate
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import java.math.BigDecimal
import java.math.RoundingMode
import com.graphhopper.jsprit.core.problem.Location as JsLocation

private fun toJsLocations(locations: List<Location>): List<JsLocation> =
    locations.mapIndexed { idx, it ->
        JsLocation.Builder
            .newInstance()
            .setIndex(idx)
            .setId("${it.id}")
            .setName(it.name)
            .setCoordinate(Coordinate.newInstance(it.lat, it.lng))
            .build()
    }

private fun toJsVehicles(
    vehicles: List<Vehicle>,
    locationsIds: Map<String, JsLocation>
): List<VehicleImpl> =
    vehicles.map {
        VehicleImpl.Builder
            .newInstance("${it.id}")
            .setType(
                VehicleTypeImpl.Builder
                    .newInstance("vehicleType")
                    .addCapacityDimension(0, it.capacity)
                    .setCostPerDistance(1.0)
                    .build()
            ).setUserData(mapOf("name" to it.name))
            .setStartLocation(locationsIds.getValue("${it.depot.id}"))
            .setEndLocation(locationsIds.getValue("${it.depot.id}"))
            .setReturnToDepot(true)
            .build()
    }

private fun toJsServices(
    customers: List<Customer>,
    locationsIds: Map<String, JsLocation>
): List<Service> =
    customers.map {
        Service.Builder
            .newInstance("${it.id}")
            .setName(it.name)
            .setLocation(locationsIds.getValue("${it.id}"))
            .addSizeDimension(0, it.demand)
            .build()
    }

/**
 * Converts a [VrpProblem] domain object into a jsprit [VehicleRoutingProblem] representation.
 * This involves transforming locations, vehicles, and customers into jsprit-specific objects
 * and building the routing cost matrix.
 *
 * @receiver The [VrpProblem] to convert.
 * @param dist The [Matrix] containing travel distances between locations.
 * @return A jsprit [VehicleRoutingProblem] instance.
 */
fun VrpProblem.toProblem(dist: Matrix): VehicleRoutingProblem {
    val jspritLocationsId = toJsLocations(locations()).associateBy { it.id }
    val jspritVehicles = toJsVehicles(vehicles, jspritLocationsId)
    val jspritServices = toJsServices(customers, jspritLocationsId)

    val jspritMatrix =
        jspritLocationsId
            .flatMap { (_, i) -> jspritLocationsId.map { (_, j) -> i to j } }
            .fold(VehicleRoutingTransportCostsMatrix.Builder.newInstance(false)) { acc, (i, j) ->
                acc.addTransportDistance(i.id, j.id, dist.distance(i.id.toLong(), j.id.toLong()))
            }.build()

    return VehicleRoutingProblem.Builder
        .newInstance()
        .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
        .addAllVehicles(jspritVehicles)
        .addAllJobs(jspritServices)
        .setRoutingCost(jspritMatrix)
        .build()
}

/**
 * Converts a [VrpSolution] domain object into a jsprit [VehicleRoutingProblemSolution] representation.
 * This maps the routes from the domain solution to jsprit vehicle routes.
 *
 * @receiver The [VrpSolution] to convert.
 * @param vrp The jsprit [VehicleRoutingProblem] instance, used to get vehicle and job activity factories.
 * @return A jsprit [VehicleRoutingProblemSolution] instance.
 */
fun VrpSolution.toSolverSolution(vrp: VehicleRoutingProblem): VehicleRoutingProblemSolution {
    val vehicles = vrp.vehicles.toList()

    val jspritRoutes =
        routes.mapIndexed { idx, route ->
            val builder = VehicleRoute.Builder.newInstance(vehicles[idx]).setJobActivityFactory(vrp.jobActivityFactory)
            route.customerIds
                .asSequence()
                .map { vrp.jobs["$it"] as Service }
                .fold(builder) { acc, it -> acc.addService(it) }
                .build()
        }
    return VehicleRoutingProblemSolution(jspritRoutes, routes.sumOf { it.distance }.toDouble() / 1000)
}

/**
 * Converts a jsprit [VehicleRoutingProblemSolution] into a [VrpSolution] domain object.
 * This extracts the routes, calculates distances, times, and demands, and constructs the domain solution.
 *
 * @receiver The jsprit [VehicleRoutingProblemSolution] to convert.
 * @param problem The original [VrpProblem] associated with this solution.
 * @param matrix The [Matrix] containing travel distances and times for calculating route metrics.
 * @return A [VrpSolution] object representing the solution derived from the jsprit solution.
 */
fun VehicleRoutingProblemSolution.toDTO(
    problem: VrpProblem,
    matrix: Matrix
): VrpSolution {
    val locationIds = problem.locations().associateBy { it.id }

    val subRoutes =
        routes.map { route ->
            val tour = listOf(route.start) + route.activities + listOf(route.end)
            val dist =
                tour.windowed(2, 1).sumOf { (i, j) ->
                    matrix.distance(i.location.id.toLong(), j.location.id.toLong())
                }
            val time =
                tour.windowed(2, 1).sumOf { (i, j) ->
                    matrix.time(i.location.id.toLong(), j.location.id.toLong()).toDouble()
                }
            val coordinates = tour.mapNotNull { locationIds[it.location.id.toLong()] }
            val customers = coordinates.mapNotNull { if (it is Customer) it else null }

            Route(
                BigDecimal(dist / 1000).setScale(2, RoundingMode.HALF_UP),
                BigDecimal(time / (60 * 1000)).setScale(2, RoundingMode.HALF_UP),
                customers.sumOf { it.demand },
                coordinates.map { LatLng(it.lat, it.lng) },
                customers.map { it.id }
            )
        }

    val emptyRoutes = List((problem.numVehicles() - subRoutes.size).coerceAtLeast(0)) { Route.EMPTY }
    return VrpSolution(problem, subRoutes + emptyRoutes)
}