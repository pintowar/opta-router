package io.github.pintowar.opta.router.solver.timefold

import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.solver.timefold.domain.Customer
import io.github.pintowar.opta.router.solver.timefold.domain.Depot
import io.github.pintowar.opta.router.solver.timefold.domain.RoadLocation
import io.github.pintowar.opta.router.solver.timefold.domain.Vehicle
import io.github.pintowar.opta.router.solver.timefold.domain.VehicleRoutingSolution
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts a [VrpProblem] domain object into a Timefold Solver [VehicleRoutingSolution] representation.
 * This involves transforming locations, vehicles, and customers into Timefold Solver-specific domain objects,
 * including pre-calculating travel distances between all locations.
 *
 * @receiver The [VrpProblem] to convert.
 * @param dist The [Matrix] containing travel distances between locations.
 * @return A Timefold Solver [VehicleRoutingSolution] instance.
 */
fun VrpProblem.toSolution(dist: Matrix): VehicleRoutingSolution {
    val roadLocations =
        this.locations().map { a ->
            val distances =
                locations()
                    .asSequence()
                    .map { b -> b.id to dist.distance(a.id, b.id) }
                    .filter { (bId, _) -> a.id != bId }
                    .toMap()
            RoadLocation(a.id, a.name, a.lat, a.lng, distances)
        }

    val roadLocationIds = roadLocations.associateBy { it.id }
    val depotIds = this.depots().map { Depot(it.id, roadLocationIds.getValue(it.id)) }.associateBy { it.id }
    return VehicleRoutingSolution(
        id,
        name,
        roadLocations,
        depotIds.values.toList(),
        this.vehicles.map { Vehicle(it.id, it.capacity, depotIds.getValue(it.depot.id)) },
        this.customers.map { Customer(it.id, it.demand, roadLocationIds.getValue(it.id)) }
    )
}

/**
 * Converts a [VrpSolution] domain object into a Timefold Solver [VehicleRoutingSolution] for warm-starting the solver.
 * This function takes an existing solution and populates the Timefold Solver domain objects with the routes
 * and their associated shadow variables (previous/next customer, vehicle).
 *
 * @receiver The [VrpSolution] to convert.
 * @param distances The [Matrix] containing travel distances between locations.
 * @return A Timefold Solver [VehicleRoutingSolution] instance initialized with the provided solution.
 */
fun VrpSolution.toSolverSolution(distances: Matrix): VehicleRoutingSolution {
    val solution = problem.toSolution(distances)
    val keys = solution.customerList.associateBy { it.id }

    routes.forEachIndexed { rIdx, route ->
        val customers = route.customerIds.mapNotNull { keys[it] }
        customers.forEachIndexed { idx, customer ->
            if (idx > 0) customer.previousCustomer = customers[idx - 1]
            if (idx < customers.size - 1) customer.nextCustomer = customers[idx + 1]
            customer.vehicle = solution.vehicleList[rIdx]
        }
        solution.vehicleList[rIdx].customers = customers
    }
    return solution
}

/**
 * Converts a Timefold Solver [VehicleRoutingSolution] into a [VrpSolution] domain object.
 * This extracts the routes from the solved Timefold Solver solution, calculates their distances, times, and demands,
 * and constructs the domain solution.
 *
 * @receiver The Timefold Solver [VehicleRoutingSolution] to convert.
 * @param instance The original [VrpProblem] instance associated with this solution.
 * @param matrix The [Matrix] containing travel distances and times for calculating route metrics.
 * @return A [VrpSolution] object representing the solution derived from the Timefold Solver solution.
 */
fun VehicleRoutingSolution.toDTO(
    instance: VrpProblem,
    matrix: Matrix
): VrpSolution {
    val vehicles = this.vehicleList
    val routes =
        vehicles.map { v ->
            val origin = v.depot.location.let { LatLng(it.latitude, it.longitude) }

            var dist = 0.0
            var time = 0.0
            var locations = emptyList<LatLng>()
            var customerIds = emptyList<Long>()
            var totalDemand = 0
            var toOriginDist = 0.0
            var toOriginTime = 0L
            var customer = v.customers.firstOrNull()
            while (customer != null) {
                locations += LatLng(customer.location.latitude, customer.location.longitude)
                customerIds += customer.id
                totalDemand += customer.demand

                val previousLocationId = customer.previousCustomer?.location?.id ?: v.depot.location.id
                dist += matrix.distance(previousLocationId, customer.location.id)
                time += matrix.time(previousLocationId, customer.location.id)
                toOriginDist = matrix.distance(customer.location.id, v.depot.location.id)
                toOriginTime = matrix.time(customer.location.id, v.depot.location.id)
                customer = customer.nextCustomer
            }
            dist += toOriginDist
            time += toOriginTime
            val rep = (listOf(origin) + locations + listOf(origin))

            Route(
                BigDecimal(dist / 1000).setScale(2, RoundingMode.HALF_UP),
                BigDecimal(time / (60 * 1000)).setScale(2, RoundingMode.HALF_UP),
                totalDemand,
                rep,
                customerIds
            )
        }

    return VrpSolution(instance, routes)
}