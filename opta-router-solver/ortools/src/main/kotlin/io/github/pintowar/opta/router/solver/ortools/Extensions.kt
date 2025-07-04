package io.github.pintowar.opta.router.solver.ortools

import com.google.ortools.constraintsolver.Assignment
import com.google.ortools.constraintsolver.RoutingIndexManager
import com.google.ortools.constraintsolver.RoutingModel
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

private class DistanceEval(
    val matrix: Matrix,
    val manager: RoutingIndexManager,
    val idxLocations: Map<Int, Location>,
    val k: Long = 100
) : LongBinaryOperator {
    override fun applyAsLong(
        p1: Long,
        p2: Long
    ): Long =
        try {
            val fromNode = idxLocations.getValue(manager.indexToNode(p1)).id
            val toNode = idxLocations.getValue(manager.indexToNode(p2)).id

            (matrix.distance(fromNode, toNode) * k).toLong()
        } catch (e: Throwable) {
            0L
        }
}

private class DemandEval(
    val manager: RoutingIndexManager,
    val idxLocations: Map<Int, Location>
) : LongUnaryOperator {
    override fun applyAsLong(fromIndex: Long): Long {
        val fromNode = manager.indexToNode(fromIndex)
        return when (val loc = idxLocations[fromNode]) {
            is Customer -> loc.demand.toLong()
            else -> 0L
        }
    }
}

/**
 * A data class that summarizes key problem details for OR-Tools, derived from a [VrpProblem].
 *
 * @property nVehicles The number of vehicles in the problem.
 * @property vehiclesCapacities An array of capacities for each vehicle.
 * @property idLocations A map from location ID to [Location] object.
 * @property idxLocations A map from internal OR-Tools index to [Location] object.
 * @property locationsIdx A map from [Location] object to internal OR-Tools index.
 * @property nLocations The total number of locations (customers + depots).
 * @property depots An array of internal OR-Tools indices representing the depot locations.
 */
class ProblemSummary(
    problem: VrpProblem
) {
    val nVehicles = problem.vehicles.size
    val vehiclesCapacities = problem.vehicles.map { it.capacity.toLong() }.toLongArray()
    val idLocations = problem.locations().associateBy { it.id }
    val idxLocations = problem.locations().withIndex().associate { it.index to it.value }
    val locationsIdx = idxLocations.map { it.value to it.key }.toMap()
    val nLocations = idxLocations.size
    val depots = problem.vehicles.mapNotNull { locationsIdx[it.depot] }.toIntArray()

    /**
     * Retrieves the internal OR-Tools index for a given customer ID.
     *
     * @param customerId The ID of the customer.
     * @return The internal OR-Tools index corresponding to the customer's location.
     */
    fun locationIdxFromCustomer(customerId: Long) = locationsIdx.getValue(idLocations.getValue(customerId)).toLong()
}

/**
 * A data class that wraps the OR-Tools [RoutingModel], [RoutingIndexManager], and [ProblemSummary].
 *
 * @property model The OR-Tools [RoutingModel] instance.
 * @property manager The OR-Tools [RoutingIndexManager] instance.
 * @property summary The [ProblemSummary] containing high-level problem details.
 */
data class ProblemWrapper(
    val model: RoutingModel,
    val manager: RoutingIndexManager,
    val summary: ProblemSummary
)

/**
 * Converts a [VrpProblem] domain object into an OR-Tools [ProblemWrapper] representation.
 * This involves setting up the routing model, index manager, and registering callbacks for distances and demands.
 *
 * @receiver The [VrpProblem] to convert.
 * @param matrix The [Matrix] containing travel distances between locations.
 * @return A [ProblemWrapper] containing the OR-Tools model, manager, and problem summary.
 */
fun VrpProblem.toProblem(matrix: Matrix): ProblemWrapper {
    val summary = ProblemSummary(this)

    val manager = RoutingIndexManager(summary.nLocations, summary.nVehicles, summary.depots, summary.depots)
    val model = RoutingModel(manager)

    val transitRegistry =
        model.registerTransitCallback(
            DistanceEval(
                matrix,
                manager,
                summary.idxLocations
            )
        )
    model.setArcCostEvaluatorOfAllVehicles(transitRegistry)

    val demandCallbackIndex: Int =
        model.registerUnaryTransitCallback(
            DemandEval(
                manager,
                summary.idxLocations
            )
        )
    model.addDimensionWithVehicleCapacity(demandCallbackIndex, 0, summary.vehiclesCapacities, true, "Capacity")
    return ProblemWrapper(model, manager, summary)
}

/**
 * Converts an OR-Tools [RoutingModel] solution into a [VrpSolution] domain object.
 * This extracts the routes from the OR-Tools assignment, calculates their distances, times, and demands,
 * and constructs the domain solution.
 *
 * @receiver The OR-Tools [RoutingModel] that contains the solution.
 * @param manager The OR-Tools [RoutingIndexManager] associated with the model.
 * @param problem The original [VrpProblem] associated with this solution.
 * @param idxLocations A map from internal OR-Tools index to [Location] object.
 * @param matrix The [Matrix] containing travel distances and times for calculating route metrics.
 * @param assignment The OR-Tools [Assignment] object representing the solved routes. If null, it tries to get the current solution from the model.
 * @return A [VrpSolution] object representing the solution derived from the OR-Tools solution.
 */
fun RoutingModel.toDTO(
    manager: RoutingIndexManager,
    problem: VrpProblem,
    idxLocations: Map<Int, Location>,
    matrix: Matrix,
    assignment: Assignment? = null
): VrpSolution {
    val subRoutes =
        problem.vehicles.indices.map { vehicleIdx ->
            val nodes =
                sequence {
                    var index = start(vehicleIdx)
                    yield(index)
                    while (!isEnd(index)) {
                        index = assignment?.value(nextVar(index)) ?: nextVar(index).value()
                        yield(index)
                    }
                }.map(manager::indexToNode)

            val locations = nodes.map(idxLocations::getValue).toList()
            val dist = locations.windowed(2, 1).sumOf { (i, j) -> matrix.distance(i.id, j.id) }
            val time = locations.windowed(2, 1).sumOf { (i, j) -> matrix.time(i.id, j.id).toDouble() }
            val customers = locations.mapNotNull { if (it is Customer) it else null }

            Route(
                BigDecimal(dist / 1000).setScale(2, RoundingMode.HALF_UP),
                BigDecimal(time / (60 * 1000)).setScale(2, RoundingMode.HALF_UP),
                customers.sumOf { it.demand },
                locations.map { LatLng(it.lat, it.lng) },
                customers.map { it.id }
            )
        }
    return VrpSolution(problem, subRoutes)
}