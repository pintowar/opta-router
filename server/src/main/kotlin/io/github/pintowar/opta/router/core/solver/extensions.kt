package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Convert the solver VRP Solution representation into the DTO representation.
 *
 * @param graph graphwrapper to calculate the distance/time took to complete paths.
 * @return the DTO solution representation.
 */
fun VehicleRoutingSolution.toDTO(graph: GeoService? = null): VrpSolution {
    val vehicles = this.vehicleList
    val routes = vehicles?.map { v ->
        val origin = v.depot.location.let { Location(it.id, it.latitude, it.longitude, it.name, 0) }

        var dist = BigDecimal(0)
        var locations = emptyList<Location>()
        var toOrigin = 0L
        var customer = v.customers.firstOrNull()
        while (customer != null) {
            locations += Location(
                customer.id,
                customer.location.latitude,
                customer.location.longitude,
                customer.location.name,
                customer.demand
            )
            dist += BigDecimal(customer.distanceFromPreviousStandstill.toDouble() / (1000 * 1000))
            toOrigin = customer.location.getDistanceTo(v.depot.location)
            customer = customer.nextCustomer
        }
        dist = (dist + BigDecimal(toOrigin / (1000 * 1000))).setScale(2, RoundingMode.HALF_UP)
        var time = dist
        var rep = (listOf(origin) + locations + listOf(origin))
        if (graph != null) {
            val aux = rep.windowed(2, 1, false)
                .map { (a, b) -> graph.detailedSimplePath(a.toCoordinate(), b.toCoordinate()) }
            rep = aux.flatMap { it.coordinates }.mapIndexed { idx, it ->
                Location(lat = it.lat, lng = it.lng, id = idx.toLong(), demand = 0, name = "None")
            }
            dist = BigDecimal(aux.sumOf { it.distance / 1000 }).setScale(2, RoundingMode.HALF_UP)
            time = BigDecimal(aux.sumOf { it.time.toDouble() / (60 * 1000) }).setScale(2, RoundingMode.HALF_UP)
        }

        Route(dist, time, rep, locations.map { it.id })
    } ?: emptyList()

    return VrpSolution(this.id, routes)
}