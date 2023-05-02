package io.github.pintowar.opta.router.core.domain.models

import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import org.optaplanner.examples.vehiclerouting.domain.Customer
import org.optaplanner.examples.vehiclerouting.domain.Depot
import org.optaplanner.examples.vehiclerouting.domain.Vehicle
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.optaplanner.examples.vehiclerouting.domain.location.DistanceType
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation
import java.math.BigDecimal

/**
 * DTO class with the representation of a VRP instance. This class is used as the application input data representation.
 */
data class Instance(
    val id: Long,
    val name: String,
    val nLocations: Int,
    val nVehicles: Int,
    val capacity: Int,
    val stops: List<Location>,
    val depots: List<Long>
) {
    /**
     * Converts the DTO into the VRP Solution representation. (Used on the VRP Solver).
     *
     * @param dist distance calculator instance.
     * @return solution representation used by the solver.
     */
    fun toSolution(dist: Matrix): VehicleRoutingSolution {
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

data class Coordinate(val lat: Double, val lng: Double)

/**
 * DTO class with the representation of locations.
 */
data class Location(
    val id: Long,
    val lat: Double,
    val lng: Double,
    val name: String,
    val demand: Int
) {
    fun toCoordinate() = Coordinate(lat, lng)
}

data class Path(val distance: Double, val time: Long, val coordinates: List<Coordinate>)

/**
 * Route representation containing the route distance, time and list of points.
 */
data class Route(val distance: BigDecimal, val time: BigDecimal, val order: List<Location>, val customerIds: List<Long>)

/**
 * DTO class with the representation of the VRP solution.
 * This class is used as the application output data representation.
 */
data class VrpSolution(val instanceId: Long, val routes: List<Route>) {

    companion object {
        fun emptyFromInstanceId(instanceId: Long) = VrpSolution(instanceId, emptyList())
    }

    fun isEmpty(): Boolean = routes.isEmpty() || routes.all { it.order.isEmpty() }

    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    fun getTotalTime() = routes.maxOfOrNull { it.time } ?: 0

    fun toSolverSolution(instance: Instance, distances: Matrix): VehicleRoutingSolution {
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
}

/**
 * DTO class with the representation of solver status data.
 */
data class SolverState(val status: String, val detailedPath: Boolean = false)

data class VrpSolutionState(val solution: VrpSolution, val state: SolverState)
