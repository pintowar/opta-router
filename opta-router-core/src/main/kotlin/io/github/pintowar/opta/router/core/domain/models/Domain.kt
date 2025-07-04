package io.github.pintowar.opta.router.core.domain.models

import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

interface Coordinate {
    val lat: Double
    val lng: Double
}

interface Location : Coordinate {
    val id: Long
    val name: String
    override val lat: Double
    override val lng: Double
}

data class LatLng(
    override val lat: Double,
    override val lng: Double
) : Coordinate

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
    /**
     * Returns a distinct list of depots from the vehicles in the problem.
     *
     * @return A list of [Depot]s.
     */
    fun depots(): List<Depot> = vehicles.map { it.depot }.distinct()

    /**
     * Returns a list of all locations in the problem, including depots and customers.
     *
     * @return A list of [Location]s.
     */
    fun locations(): List<Location> = depots() + customers

    /**
     * Returns the total number of locations in the problem.
     *
     * @return The number of locations.
     */
    fun numLocations(): Int = locations().size

    /**
     * Returns the total number of vehicles in the problem.
     *
     * @return The number of vehicles.
     */
    fun numVehicles(): Int = vehicles.size
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

data class Vehicle(
    val id: Long,
    val name: String,
    val capacity: Int,
    val depot: Depot
)

data class Path(
    val distance: Double,
    val time: Long,
    val coordinates: List<Coordinate>
)

data class Route(
    val distance: BigDecimal,
    val time: BigDecimal,
    val totalDemand: Int,
    val order: List<LatLng>,
    // TODO rename to locationIds
    val customerIds: List<Long>
) {
    companion object {
        val EMPTY = Route(BigDecimal.ZERO, BigDecimal.ZERO, 0, emptyList(), emptyList())
    }
}

data class VrpSolution(
    val problem: VrpProblem,
    val routes: List<Route>
) {
    companion object {
        /**
         * Creates an empty VrpSolution from a VrpProblem instance.
         *
         * @param problem The VrpProblem instance.
         * @return An empty VrpSolution.
         */
        fun emptyFromInstance(problem: VrpProblem) = VrpSolution(problem, emptyList())
    }

    /**
     * Checks if the solution is feasible.
     * A solution is feasible if the total demand of each route does not exceed the capacity of the vehicle assigned to it.
     *
     * @return `true` if the solution is feasible, `false` otherwise.
     */
    fun isFeasible(): Boolean =
        problem.vehicles.zip(routes).all { (vehicle, route) ->
            vehicle.capacity >= route.totalDemand
        }

    /**
     * Checks if the solution is empty.
     * A solution is empty if there are no routes or if all routes are empty.
     *
     * @return `true` if the solution is empty, `false` otherwise.
     */
    fun isEmpty(): Boolean = routes.isEmpty() || routes.all { it.order.isEmpty() }

    /**
     * Calculates the total distance of all routes in the solution.
     *
     * @return The total distance as a BigDecimal.
     */
    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    /**
     * Calculates the total time of the solution, which is the time of the longest route.
     *
     * @return The total time as a BigDecimal.
     */
    fun getTotalTime(): BigDecimal = routes.maxOfOrNull { it.time } ?: BigDecimal.ZERO
}

data class VrpDetailedSolution(
    val solution: VrpSolution,
    val matrix: VrpProblemMatrix
)

enum class SolverStatus {
    ENQUEUED,
    NOT_SOLVED,
    RUNNING,
    TERMINATED
}

data class VrpSolverRequest(
    val requestKey: UUID,
    val problemId: Long,
    val solver: String,
    val status: SolverStatus
)

data class VrpSolutionRequest(
    val solution: VrpSolution,
    val status: SolverStatus,
    val solverKey: UUID? = null
)

data class VrpSolverObjective(
    val objective: Double,
    val solver: String,
    val status: SolverStatus,
    val solverKey: UUID,
    val createdAt: Instant
)

data class SolverPanel(
    val isDetailedPath: Boolean = false
)