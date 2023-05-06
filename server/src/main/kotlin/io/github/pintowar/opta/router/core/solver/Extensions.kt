package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Instance
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import org.optaplanner.examples.vehiclerouting.domain.Customer
import org.optaplanner.examples.vehiclerouting.domain.Depot
import org.optaplanner.examples.vehiclerouting.domain.Vehicle
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.optaplanner.examples.vehiclerouting.domain.location.DistanceType
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts the DTO into the VRP Solution representation. (Used on the VRP Solver).
 *
 * @param dist distance calculator instance.
 * @return solution representation used by the solver.
 */
fun Instance.toSolution(dist: Matrix): VehicleRoutingSolution {
    val sol = VehicleRoutingSolution(id)
    sol.name = this.name
    val locs = this.stops.map {
        RoadLocation(it.id, it.lat, it.lng).apply { name = it.name }
    }

    val deps = this.depots.distinct().mapIndexed { idx, it ->
        Depot(it, locs[idx])
    }.associateBy { it.id }

    locs.forEach { a ->
        a.travelDistanceMap = locs
            .map { b -> b to dist.distance(a.id, b.id) }
            .filter { (b, _) -> a != b }
            .toMap()
    }
    sol.locationList = locs
    val depsLocs = deps.map { it.value.location.id }.toSet()
    sol.customerList = this.stops.mapIndexed { idx, it ->
        Customer(it.id, sol.locationList[idx], it.demand)
    }.filter { !depsLocs.contains(it.location.id) }
    sol.depotList = deps.values.toList()

    sol.vehicleList = this.depots.mapIndexed { idx, it ->
        Vehicle(idx.toLong(), this.capacity, deps[it])
    }
    sol.distanceType = DistanceType.ROAD_DISTANCE
    sol.distanceUnitOfMeasurement = "m"
    return sol
}

fun VrpSolution.toSolverSolution(distances: Matrix): VehicleRoutingSolution {
    val solution = instance.toSolution(distances)
    val keys = solution.customerList.associateBy { it.id }

    routes.forEachIndexed { rIdx, route ->
        val customers = route.customerIds.map { keys[it] }
        customers.forEachIndexed { idx, customer ->
            if (idx > 0) customer?.previousCustomer = customers[idx - 1]
            if (idx < customers.size - 1) customer?.nextCustomer = customers[idx + 1]
            customer?.vehicle = solution.vehicleList[rIdx]
        }
        solution.vehicleList[rIdx].customers = customers
    }
    return solution
}

/**
 * Convert the solver VRP Solution representation into the DTO representation.
 *
 * @param graph graphwrapper to calculate the distance/time took to complete paths.
 * @return the DTO solution representation.
 */
fun VehicleRoutingSolution.toDTO(instance: Instance, matrix: Matrix): VrpSolution {
    val vehicles = this.vehicleList
    val routes = vehicles?.map { v ->
        val origin = v.depot.location.let { Coordinate(it.latitude, it.longitude) }

        var dist = 0.0
        var time = 0.0
        var locations = emptyList<Coordinate>()
        var customerIds = emptyList<Long>()
        var toOriginDist = 0.0
        var toOriginTime = 0L
        var customer = v.customers.firstOrNull()
        while (customer != null) {
            locations += Coordinate(customer.location.latitude, customer.location.longitude)
            customerIds += customer.id

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
            rep,
            customerIds
        )
    } ?: emptyList()

    return VrpSolution(instance, routes)
}