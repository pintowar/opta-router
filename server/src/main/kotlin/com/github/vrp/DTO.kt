package com.github.vrp

import com.github.util.GraphWrapper
import com.github.vrp.dist.Distance
import com.github.vrp.dist.PathDistance
import org.optaplanner.examples.vehiclerouting.domain.Customer
import org.optaplanner.examples.vehiclerouting.domain.Depot
import org.optaplanner.examples.vehiclerouting.domain.Vehicle
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.optaplanner.examples.vehiclerouting.domain.location.DistanceType
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * DTO class with the representation of a VRP instance. This class is used as the application input data representation.
 */
data class Instance(
        val id: Long,
        val name: String,
        val nLocations: Int,
        val nVehicles: Int,
        val capacity: Int,
        val stops: List<Point>,
        val depots: List<Long>
) {
    /**
     * Converts the DTO into the VRP Solution representation. (Used on the VRP Solver).
     *
     * @param dist distance calculator instance.
     * @return solution representation used by the solver.
     */
    fun toSolution(dist: Distance): VehicleRoutingSolution {
        val sol = VehicleRoutingSolution(id)
        sol.name = this.name
        val locs = this.stops.map {
            RoadLocation(it.id, it.lat, it.lng).apply { name = it.name }
        }

        val deps = this.depots.distinct().mapIndexed { idx, it ->
            Depot(it, locs[idx])
        }.associateBy { it.id }

        locs.forEachIndexed { idxa, a ->
            a.travelDistanceMap = locs
                    .mapIndexed { idxb, b -> b to dist.distance(idxa, idxb) }
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
}

/**
 * DTO class with the representation of locations.
 */
data class Point(
        val id: Long,
        val lat: Double,
        val lng: Double,
        val name: String,
        val demand: Int
) {
    fun toPair() = lat to lng
}

/**
 * Route representation containing the route distance, time and list of points.
 */
data class Route(val distance: BigDecimal, val time: BigDecimal, val order: List<Point>, val customerIds: List<Long>)

/**
 * DTO class with the representation of the VRP solution.
 * This class is used as the application output data representation.
 */
data class VrpSolution(val instanceId: Long, val routes: List<Route>) {

    companion object {
        fun emptyFromInstanceId(instanceId: Long) = VrpSolution(instanceId, emptyList())
    }

    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    fun getTotalTime() = routes.maxOfOrNull { it.time } ?: 0

    fun toSolverSolution(instance: Instance, distances: Distance): VehicleRoutingSolution {
        val solution = instance.toSolution(distances)
        val keys = solution.customerList.associateBy { it.id }

        routes.forEach { route ->
            val customers = route.customerIds.map { keys[it] }
            customers.forEachIndexed { idx, customer ->
                if (idx > 0) customer?.previousCustomer = customers[idx - 1]
                if (idx < customers.size - 1) customer?.nextCustomer = customers[idx + 1]
            }
        }
        return solution
    }
}

/**
 * DTO class with the representation of solver status data.
 */
data class SolverState(val status: String, val detailedPath: Boolean = false)

data class VrpSolutionState(val solution: VrpSolution, val state: SolverState)

/**
 * Convert the solver VRP Solution representation into the DTO representation.
 *
 * @param graph graphwrapper to calculate the distance/time took to complete paths.
 * @return the DTO solution representation.
 */
fun VehicleRoutingSolution.toDTO(graph: GraphWrapper? = null): VrpSolution {
    val vehicles = this.vehicleList
    val routes = vehicles?.map { v ->
        val origin = Point(v.depot.location.id, v.depot.location.latitude, v.depot.location.longitude, v.depot.location.name, 0)

        var dist = BigDecimal(0)
        var points = emptyList<Point>()
        var toOrigin = 0L
        var customer = v.customers.firstOrNull()
        while (customer != null) {
            points += Point(customer.id, customer.location.latitude, customer.location.longitude, customer.location.name, customer.demand)
            dist += BigDecimal(customer.distanceFromPreviousStandstill.toDouble() / (1000 * 1000))
            toOrigin = customer.location.getDistanceTo(v.depot.location)
            customer = customer.nextCustomer
        }
        dist = (dist + BigDecimal(toOrigin / (1000 * 1000))).setScale(2, RoundingMode.HALF_UP)
        var time = dist
        var rep = (listOf(origin) + points + listOf(origin))
        if (graph != null) {
            val aux = rep.windowed(2, 1, false)
                    .map { (a, b) -> graph.detailedSimplePath(a.toPair(), b.toPair()) }
            rep = aux.flatMap { it.points }.mapIndexed { idx, it ->
                Point(lat = it.lat, lng = it.lon, id = idx.toLong(), demand = 0, name = "None")
            }
            dist = BigDecimal(aux.sumOf { it.distance / 1000 }).setScale(2, RoundingMode.HALF_UP)
            time = BigDecimal(aux.sumOf { it.time.toDouble() / (60 * 1000) }).setScale(2, RoundingMode.HALF_UP)
        }

        Route(dist, time, rep, points.map { it.id })
    } ?: emptyList()

    return VrpSolution(this.id, routes)
}
