package io.github.pintowar.opta.router.core.domain.models

import java.math.BigDecimal
import java.util.*

/**
 * DTO class with the representation of a VRP instance. This class is used as the application input data representation.
 */
data class RouteInstance(
    val id: Long,
    val name: String,
    val vehicles: List<Vehicle>,
    val customers: List<Customer>
) {
    val nLocations: Int = customers.size + 1
    val nVehicles: Int = vehicles.size
    val depots: List<Depot> = vehicles.map { it.depot }.distinct()
    val locations: List<Location> =
        depots.map { it.location } + customers.map { it.location }

}

data class Vehicle(val id: Long, val name: String, val capacity: Int, val depot: Depot)

data class Customer(val id: Long, val name: String, val demand: Int, val location: Location)

data class Depot(val id: Long, val name: String, val location: Location)

data class Coordinate(val lat: Double, val lng: Double)

/**
 * DTO class with the representation of locations.
 */
data class Location(val id: Long, val name: String, val lat: Double, val lng: Double) {
    fun toCoordinate() = Coordinate(lat, lng)
}

data class Path(val distance: Double, val time: Long, val coordinates: List<Coordinate>)

/**
 * Route representation containing the route distance, time and list of points.
 */
data class Route(
    val distance: BigDecimal,
    val time: BigDecimal,
    val order: List<Coordinate>,
    val customerIds: List<Long>
)

/**
 * DTO class with the representation of the VRP solution.
 * This class is used as the application output data representation.
 */
data class VrpSolution(val instance: RouteInstance, val routes: List<Route>) {

    companion object {
        fun emptyFromInstance(instance: RouteInstance) = VrpSolution(instance, emptyList())
    }

    fun isEmpty(): Boolean = routes.isEmpty() || routes.all { it.order.isEmpty() }

    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    fun getTotalTime() = routes.maxOfOrNull { it.time } ?: 0
}

enum class SolverState {
    ENQUEUED, NOT_SOLVED, RUNNING, TERMINATED
}

data class VrpSolutionRegistry(val solution: VrpSolution, val state: SolverState, val solverKey: UUID? = null)

data class SolverPanel(val isDetailedPath: Boolean = false)