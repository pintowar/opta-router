package io.github.pintowar.opta.router.solver.ortools

import com.google.ortools.constraintsolver.Assignment
import com.google.ortools.constraintsolver.RoutingIndexManager
import com.google.ortools.constraintsolver.RoutingModel
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

private class DistanceEval(
    val matrix: Matrix, val manager: RoutingIndexManager, val idxLocations: Map<Int, Location>, val k: Long = 100
) : LongBinaryOperator {
    override fun applyAsLong(p1: Long, p2: Long): Long = try {
        val fromNode = idxLocations.getValue(manager.indexToNode(p1)).id
        val toNode = idxLocations.getValue(manager.indexToNode(p2)).id

        (matrix.distance(fromNode, toNode) * k).toLong()
    } catch (e: Throwable) {
        0L
    }
}

private class DemandEval(
    val manager: RoutingIndexManager, val idxLocations: Map<Int, Location>
) : LongUnaryOperator {
    override fun applyAsLong(fromIndex: Long): Long {
        val fromNode = manager.indexToNode(fromIndex)
        return when (val loc = idxLocations[fromNode]) {
            is Customer -> loc.demand.toLong()
            else -> 0L
        }
    }
}

class ProblemSummary(problem: VrpProblem) {
    val nVehicles = problem.vehicles.size
    val vehiclesCapacities = problem.vehicles.map { it.capacity.toLong() }.toLongArray()
    val idLocations = problem.locations.associateBy { it.id }
    val idxLocations = problem.locations.withIndex().associate { it.index to it.value }
    val locationsIdx = idxLocations.map { it.value to it.key }.toMap()
    val nLocations = idxLocations.size
    val depots = problem.vehicles.mapNotNull { locationsIdx[it.depot] }.toIntArray()

    fun locationIdxFromCustomer(customerId: Long) = locationsIdx.getValue(idLocations.getValue(customerId)).toLong()
}

data class ProblemWrapper(val model: RoutingModel, val manager: RoutingIndexManager, val summary: ProblemSummary)

/**
 * Converts the DTO into the VRP Solution representation. (Used on the VRP Solver).
 *
 * @param dist distance calculator instance.
 * @return solution representation used by the solver.
 */
fun VrpProblem.toProblem(matrix: Matrix): ProblemWrapper {
    val summary = ProblemSummary(this)

    val manager = RoutingIndexManager(summary.nLocations, summary.nVehicles, summary.depots, summary.depots)
    val model = RoutingModel(manager)

    val transitRegistry = model.registerTransitCallback(
        DistanceEval(
            matrix,
            manager,
            summary.idxLocations
        )
    )
    model.setArcCostEvaluatorOfAllVehicles(transitRegistry)

    val demandCallbackIndex: Int = model.registerUnaryTransitCallback(
        DemandEval(
            manager,
            summary.idxLocations
        )
    )
    model.addDimensionWithVehicleCapacity(demandCallbackIndex, 0, summary.vehiclesCapacities, true, "Capacity")
    return ProblemWrapper(model, manager, summary)
}

/**
 * Convert the solver VRP Solution representation into the DTO representation.
 *
 * @return the DTO solution representation.
 */
fun RoutingModel.toDTO(
    manager: RoutingIndexManager,
    instance: VrpProblem,
    idxLocations: Map<Int, Location>,
    matrix: Matrix,
    assignment: Assignment? = null,
): VrpSolution {
    val subRoutes = instance.vehicles.indices.map { vehicleIdx ->
        val nodes = sequence {
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
    return VrpSolution(instance, subRoutes)
}