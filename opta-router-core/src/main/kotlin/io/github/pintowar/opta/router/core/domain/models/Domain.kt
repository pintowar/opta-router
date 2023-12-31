package io.github.pintowar.opta.router.core.domain.models

import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

interface Coordinate {
    val lat: Double
    val lng: Double
}

/**
 * DTO class with the representation of locations.
 */
interface Location : Coordinate {
    val id: Long
    val name: String
    override val lat: Double
    override val lng: Double
}

data class LatLng(override val lat: Double, override val lng: Double) : Coordinate

/**
 * DTO class with the representation of a VRP instance. This class is used as the application input data representation.
 */
data class VrpProblemSummary(
    val id: Long,
    val name: String,
    val nLocations: Int,
    val nVehicles: Int,
    val totalCapacity: Int,
    val totalDemand: Int,
    val numEnqueuedRequests: Int,
    val numRunningRequests: Int,
    val numTerminatedRequests: Int,
    val numNotSolvedRequests: Int,
    val numSolverRequests: Int
)

data class VrpProblem(
    val id: Long,
    val name: String,
    val vehicles: List<Vehicle>,
    val customers: List<Customer>
) {
    val depots: List<Depot> = vehicles.map { it.depot }.distinct()
    val locations: List<Location>
        get() = depots + customers
    val nLocations: Int = locations.size
    val nVehicles: Int = vehicles.size
}

data class Customer(
    override val id: Long,
    override val name: String,
    override val lat: Double,
    override val lng: Double,
    val demand: Int
) : Location

data class Depot(
    override val id: Long,
    override val name: String,
    override val lat: Double,
    override val lng: Double
) : Location

data class Vehicle(val id: Long, val name: String, val capacity: Int, val depot: Depot)

data class Path(val distance: Double, val time: Long, val coordinates: List<Coordinate>)

/**
 * Route representation containing the route distance, time and list of points.
 */
data class Route(
    val distance: BigDecimal,
    val time: BigDecimal,
    val totalDemand: Int,
    val order: List<LatLng>,
    val customerIds: List<Long> // TODO rename to locationIds
) {
    companion object {
        val EMPTY = Route(BigDecimal.ZERO, BigDecimal.ZERO, 0, emptyList(), emptyList())
    }
}

/**
 * DTO class with the representation of the VRP solution.
 * This class is used as the application output data representation.
 */
data class VrpSolution(val problem: VrpProblem, val routes: List<Route>) {

    companion object {
        fun emptyFromInstance(problem: VrpProblem) = VrpSolution(problem, emptyList())
    }

    fun isFeasible(): Boolean {
        return problem.vehicles.zip(routes).all { (vehicle, route) ->
            vehicle.capacity >= route.totalDemand
        }
    }

    fun isEmpty(): Boolean = routes.isEmpty() || routes.all { it.order.isEmpty() }

    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    fun getTotalTime() = routes.maxOfOrNull { it.time } ?: 0
}

data class VrpDetailedSolution(val solution: VrpSolution, val matrix: VrpProblemMatrix)

enum class SolverStatus {
    ENQUEUED, NOT_SOLVED, RUNNING, TERMINATED
}

data class VrpSolverRequest(
    val requestKey: UUID,
    val problemId: Long,
    val solver: String,
    val status: SolverStatus
)

data class VrpSolutionRequest(val solution: VrpSolution, val status: SolverStatus, val solverKey: UUID? = null)

data class VrpSolverObjective(
    val objective: Double,
    val solver: String,
    val status: SolverStatus,
    val solverKey: UUID,
    val createdAt: Instant
)

data class SolverPanel(val isDetailedPath: Boolean = false)