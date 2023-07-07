package io.github.pintowar.opta.router.solver.ortools

import com.google.ortools.constraintsolver.Assignment
import com.google.ortools.constraintsolver.RoutingIndexManager
import com.google.ortools.constraintsolver.RoutingModel
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import java.math.BigDecimal
import java.math.RoundingMode

//import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
//import com.graphhopper.jsprit.core.problem.job.Service
//import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution
//import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute
//import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl
//import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
//import com.graphhopper.jsprit.core.util.Coordinate
//import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix
//import io.github.pintowar.opta.router.core.domain.models.Customer
//import io.github.pintowar.opta.router.core.domain.models.LatLng
//import io.github.pintowar.opta.router.core.domain.models.Location
//import io.github.pintowar.opta.router.core.domain.models.Route
//import io.github.pintowar.opta.router.core.domain.models.Vehicle
//import io.github.pintowar.opta.router.core.domain.models.VrpProblem
//import io.github.pintowar.opta.router.core.domain.models.VrpSolution
//import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
//import java.math.BigDecimal
//import java.math.RoundingMode
//import com.graphhopper.jsprit.core.problem.Location as JsLocation
//

///**
// * Converts the DTO into the VRP Solution representation. (Used on the VRP Solver).
// *
// * @param dist distance calculator instance.
// * @return solution representation used by the solver.
// */
//fun VrpProblem.toProblem(dist: Matrix): VehicleRoutingProblem {
//    val jspritLocationsId = toJsLocations(locations).associateBy { it.id }
//    val jspritVehicles = toJsVehicles(vehicles, jspritLocationsId)
//    val jspritServices = toJsServices(customers, jspritLocationsId)
//
//    val jspritMatrix = jspritLocationsId
//        .flatMap { (_, i) -> jspritLocationsId.map { (_, j) -> i to j } }
//        .fold(VehicleRoutingTransportCostsMatrix.Builder.newInstance(false)) { acc, (i, j) ->
//            acc.addTransportDistance(i.id, j.id, dist.distance(i.id.toLong(), j.id.toLong()))
//        }
//        .build()
//
//    return VehicleRoutingProblem.Builder.newInstance()
//        .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
//        .addAllVehicles(jspritVehicles)
//        .addAllJobs(jspritServices)
//        .setRoutingCost(jspritMatrix)
//        .build()
//}
//
//fun VrpSolution.toSolverSolution(vrp: VehicleRoutingProblem): VehicleRoutingProblemSolution {
//    val vehicles = vrp.vehicles.toList()
//
//    val jspritRoutes = routes.mapIndexed { idx, route ->
//        val builder = VehicleRoute.Builder.newInstance(vehicles[idx]).setJobActivityFactory(vrp.jobActivityFactory)
//        route.customerIds
//            .asSequence()
//            .map { vrp.jobs["$it"] as Service }
//            .fold(builder) { acc, it -> acc.addService(it) }
//            .build()
//    }
//    return VehicleRoutingProblemSolution(jspritRoutes, routes.sumOf { it.distance }.toDouble() / 1000)
//}

/**
 * Convert the solver VRP Solution representation into the DTO representation.
 *
 * @return the DTO solution representation.
 */
fun toDTO(
    model: RoutingModel,
    manager: RoutingIndexManager,
    instance: VrpProblem,
    idxLocations: Map<Int, Location>,
    matrix: Matrix,
    assignment: Assignment? = null,
): VrpSolution {
    val subRoutes = instance.vehicles.indices.map { vehicleIdx ->
        val nodes = sequence {
            var index = model.start(vehicleIdx)
            yield(index)
            while (!model.isEnd(index)) {
                index = assignment?.value(model.nextVar(index)) ?: model.nextVar(index).value()
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