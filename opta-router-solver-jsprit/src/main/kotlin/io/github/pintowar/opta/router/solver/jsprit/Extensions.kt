package io.github.pintowar.opta.router.solver.jsprit

import com.graphhopper.jsprit.core.problem.Location as JsLocation
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.job.Service
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
import com.graphhopper.jsprit.core.util.Coordinate
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import java.math.BigDecimal
import java.math.RoundingMode

private fun toJsLocations(locations: List<Location>): List<JsLocation> {
    return locations.mapIndexed { idx, it ->
        JsLocation.Builder
            .newInstance()
            .setIndex(idx)
            .setId("${it.id}")
            .setName(it.name)
            .setCoordinate(Coordinate.newInstance(it.lat, it.lng))
            .build()
    }
}

private fun toJsVehicles(vehicles: List<Vehicle>, locationsIds: Map<String, JsLocation>): List<VehicleImpl> {
    return vehicles.map {
        VehicleImpl.Builder
            .newInstance("${it.id}")
            .setType(
                VehicleTypeImpl.Builder
                    .newInstance("vehicleType")
                    .addCapacityDimension(0, it.capacity)
                    .setCostPerDistance(1.0)
                    .build()
            )
            .setUserData(mapOf("name" to it.name))
            .setStartLocation(locationsIds.getValue("${it.depot.id}"))
            .setEndLocation(locationsIds.getValue("${it.depot.id}"))
            .setReturnToDepot(true)
            .build()
    }
}

private fun toJsServices(customers: List<Customer>, locationsIds: Map<String, JsLocation>): List<Service> {
    return customers.map {
        Service.Builder
            .newInstance("${it.id}")
            .setName(it.name)
            .setLocation(locationsIds.getValue("${it.id}"))
            .addSizeDimension(0, it.demand)
            .build()
    }
}

/**
 * Converts the DTO into the VRP Solution representation. (Used on the VRP Solver).
 *
 * @param dist distance calculator instance.
 * @return solution representation used by the solver.
 */
fun VrpProblem.toProblem(dist: Matrix): VehicleRoutingProblem {
    val jspritLocationsId = toJsLocations(locations).associateBy { it.id }
    val jspritVehicles = toJsVehicles(vehicles, jspritLocationsId)
    val jspritServices = toJsServices(customers, jspritLocationsId)

    val jspritMatrix = jspritLocationsId
        .flatMap { (_, i) -> jspritLocationsId.map { (_, j) -> i to j } }
        .fold(VehicleRoutingTransportCostsMatrix.Builder.newInstance(false)) { acc, (i, j) ->
            acc.addTransportDistance(i.id, j.id, dist.distance(i.id.toLong(), j.id.toLong()))
        }
        .build()

    return VehicleRoutingProblem.Builder.newInstance()
        .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
        .addAllVehicles(jspritVehicles)
        .addAllJobs(jspritServices)
        .setRoutingCost(jspritMatrix)
        .build()
}

fun VrpSolution.toSolverSolution(vrp: VehicleRoutingProblem): VehicleRoutingProblemSolution {
    val vehicles = vrp.vehicles.toList()

    val jspritRoutes = routes.mapIndexed { idx, route ->
        val builder = VehicleRoute.Builder.newInstance(vehicles[idx]).setJobActivityFactory(vrp.jobActivityFactory)
        route.customerIds
            .asSequence()
            .map { vrp.jobs["$it"] as Service }
            .fold(builder) { acc, it -> acc.addService(it) }
            .build()
    }
    return VehicleRoutingProblemSolution(jspritRoutes, routes.sumOf { it.distance }.toDouble() / 1000)
}

/**
 * Convert the solver VRP Solution representation into the DTO representation.
 *
 * @return the DTO solution representation.
 */
fun VehicleRoutingProblemSolution.toDTO(instance: VrpProblem, matrix: Matrix): VrpSolution {
    val locationIds = instance.locations.associateBy { it.id }

    val subRoutes = routes.map { route ->
        val tour = listOf(route.start) + route.activities + listOf(route.end)
        val dist = tour.windowed(2, 1).sumOf { (i, j) ->
            matrix.distance(i.location.id.toLong(), j.location.id.toLong())
        }
        val time = tour.windowed(2, 1).sumOf { (i, j) ->
            matrix.time(i.location.id.toLong(), j.location.id.toLong()).toDouble()
        }
        val coordinates = tour.mapNotNull { locationIds[it.location.id.toLong()] }
        val customers = coordinates.mapNotNull { if (it is Customer) it else null }

        Route(
            BigDecimal(dist / 1000).setScale(2, RoundingMode.HALF_UP),
            BigDecimal(time / (60 * 1000)).setScale(2, RoundingMode.HALF_UP),
            customers.sumOf { it.demand },
            coordinates.map { LatLng(it.lat, it.lng) },
            customers.map { it.id }
        )
    }

    return VrpSolution(instance, subRoutes)
}