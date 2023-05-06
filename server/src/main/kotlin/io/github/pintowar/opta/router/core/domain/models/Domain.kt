package io.github.pintowar.opta.router.core.domain.models

import java.math.BigDecimal

/**
 * DTO class with the representation of a VRP instance. This class is used as the application input data representation.
 */
interface Instance {
    val id: Long
    val name: String
    val nLocations: Int
    val nVehicles: Int
    val capacity: Int
    val stops: List<Location>
    val depots: List<Long>
}

data class DummyInstance(
    override val id: Long,
    override val name: String,
    override val nLocations: Int,
    override val nVehicles: Int,
    override val capacity: Int,
    override val stops: List<Location>,
    override val depots: List<Long>
) : Instance

data class RouteInstance(
    override val id: Long,
    override val name: String,
    val vehicles: List<Vehicle>,
    val customers: List<Customer>
) : Instance {
    override val nLocations: Int = customers.size + 1
    override val nVehicles: Int = vehicles.size
    override val capacity: Int = vehicles.maxOf { it.capacity }
    override val stops: List<Location> =
        (vehicles.map { it.depot.location }.toSet() + customers.map { it.location }.toSet()).toList()
    override val depots: List<Long> = vehicles.map { it.depot.location.id }
}

data class Vehicle(val id: Long, val name: String, val capacity: Int, val depot: Depot)

data class Customer(val id: Long, val name: String, val demand: Int, val location: Location)

data class Depot(val id: Long, val name: String, val location: Location)

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
data class VrpSolution(val instance: Instance, val routes: List<Route>) {

    companion object {
        fun emptyFromInstance(instance: Instance) = VrpSolution(instance, emptyList())
    }

    fun isEmpty(): Boolean = routes.isEmpty() || routes.all { it.order.isEmpty() }

    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    fun getTotalTime() = routes.maxOfOrNull { it.time } ?: 0
}

enum class SolverState {
    NOT_SOLVED, RUNNING, TERMINATED
}

data class VrpSolutionState(val solution: VrpSolution, val state: SolverState)

data class SolverPanel(val isDetailedPath: Boolean = false)