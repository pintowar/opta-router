package io.github.pintowar.opta.router.core.domain.models

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
)

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
data class VrpSolution(val instance: Instance, val routes: List<Route>) {

    companion object {
        fun emptyFromInstance(instance: Instance) = VrpSolution(instance, emptyList())
    }

    fun isEmpty(): Boolean = routes.isEmpty() || routes.all { it.order.isEmpty() }

    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    fun getTotalTime() = routes.maxOfOrNull { it.time } ?: 0
}

/**
 * DTO class with the representation of solver status data.
 */
data class SolverState(val status: String, val detailedPath: Boolean = false)

data class VrpSolutionState(val solution: VrpSolution, val state: SolverState)