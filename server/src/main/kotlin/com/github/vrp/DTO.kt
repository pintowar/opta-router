package com.github.vrp

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.util.GraphWrapper
import com.github.vrp.dist.Distance
import org.optaplanner.examples.vehiclerouting.domain.Customer
import org.optaplanner.examples.vehiclerouting.domain.Depot
import org.optaplanner.examples.vehiclerouting.domain.Vehicle
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.optaplanner.examples.vehiclerouting.domain.location.DistanceType
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation
import java.math.BigDecimal
import java.math.RoundingMode

data class Instance @JsonCreator constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("nLocations") val nLocations: Int,
        @JsonProperty("nVehicles") val nVehicles: Int,
        @JsonProperty("capacity") val capacity: Int,
        @JsonProperty("stops") val stops: List<Point>,
        @JsonProperty("depots") val depots: List<Long>
) {
    fun toSolution(dist: Distance): VehicleRoutingSolution {
        val sol = VehicleRoutingSolution()
        sol.id = 0
        sol.name = this.id
        val locs = this.stops.map {
            val loc = RoadLocation(it.id, it.lat, it.lon)
            loc.name = it.name
            loc
        }
        val idxLocs = locs.map { it.id to it }.toMap()

        val deps = this.depots.distinct().map {
            val d = Depot()
            d.id = it
            d.location = idxLocs[it]
            d.id to d
        }.toMap()

        locs.forEachIndexed { idxa, a ->
            a.travelDistanceMap = locs.mapIndexed { idxb, b -> b to dist.distance(idxa, idxb) }
                    .filter { (b, _) -> a != b }.toMap()
        }
        sol.locationList = locs
        val depsLocs = deps.map { it.value.location.id }.toSet()
        sol.customerList = this.stops.mapIndexed { idx, it ->
            val c = Customer()
            c.id = it.id
            c.demand = it.demand
            c.location = sol.locationList[idx]
            c
        }.filter { !depsLocs.contains(it.location.id) }
        sol.depotList = deps.values.toList()

        sol.vehicleList = this.depots.mapIndexed { idx, it ->
            val v = Vehicle()
            v.id = idx.toLong()
            v.capacity = this.capacity
            v.depot = deps[it]
            v
        }
        sol.distanceType = DistanceType.ROAD_DISTANCE
        sol.distanceUnitOfMeasurement = "m"
        return sol
    }
}

data class Point @JsonCreator constructor(
        @JsonProperty("id") val id: Long,
        @JsonProperty("lat") val lat: Double,
        @JsonProperty("lon") val lon: Double,
        @JsonProperty("name") val name: String,
        @JsonProperty("demand") val demand: Int) {
    fun toPair() = lat to lon
}

data class Route(val distance: BigDecimal, val time: BigDecimal, val order: List<Point>)

data class VrpSolution(val routes: List<Route>) {
    fun getTotalDistance() = routes.map { it.distance }.fold(BigDecimal(0)) { a, b -> a + b }

    fun getTotalTime() = routes.map { it.time }.max() ?: 0
}

fun VehicleRoutingSolution.convertSolution(graph: GraphWrapper? = null): VrpSolution {
    val vehicles = this.vehicleList
    val routes = vehicles?.map { v ->
        val origin = Point(v.depot.location.id, v.depot.location.latitude, v.depot.location.longitude, v.depot.location.name, 0)

        var dist = BigDecimal(0)
        var points = emptyList<Point>()
        var toOrigin = 0L
        var customer = v.nextCustomer
        while (customer != null) {
            points += Point(customer.id, customer.location.latitude, customer.location.longitude, customer.location.name, customer.demand)
            dist += BigDecimal(customer.distanceFromPreviousStandstill.toDouble() / (1000 * 1000))
            toOrigin = customer.location.getDistanceTo(v.depot.location)
            customer = customer.nextCustomer
        }
        dist = (dist + BigDecimal(toOrigin / (1000 * 1000))).setScale(2, RoundingMode.HALF_UP)
        var time = dist
        var rep = (listOf(origin) + points + listOf(origin))
        if (graph != null) {
            val aux = rep.windowed(2, 1, false)
                    .map { (a, b) -> graph.simplePath(a.toPair(), b.toPair()) }
            rep = aux.flatMap { it.points }.mapIndexed { idx, it ->
                Point(lat = it.lat, lon = it.lon, id = idx.toLong(), demand = 0, name = "None")
            }
            dist = BigDecimal(aux.sumByDouble { it.distance / 1000 }).setScale(2, RoundingMode.HALF_UP)
            time = BigDecimal(aux.sumByDouble { it.time.toDouble() / (60 * 1000) }).setScale(2, RoundingMode.HALF_UP)
        }

        Route(dist, time, rep)
    } ?: emptyList()

    return VrpSolution(routes)
}