package io.github.pintowar.opta.router.solver.timefold.domain

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore
import io.github.pintowar.opta.router.solver.timefold.domain.weights.DepotAngleCustomerFactory
import kotlin.math.atan2

/**
 * Represents a location on a road network with pre-calculated travel distances to other locations.
 *
 * @property id The unique identifier of the location.
 * @property name The name of the location.
 * @property latitude The latitude coordinate of the location.
 * @property longitude The longitude coordinate of the location.
 * @property travelDistanceMap A map where keys are other location IDs and values are the travel distances to those locations.
 */
class RoadLocation(
    val id: Long = -1,
    val name: String = "None",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    private val travelDistanceMap: Map<Long, Double> = emptyMap()
) {
    /**
     * Calculates the travel distance from this location to another specified location.
     *
     * The distance is retrieved from the pre-calculated `travelDistanceMap` and multiplied by 1000
     * to convert it to a Long, avoiding floating-point precision issues.
     *
     * @param location The target [RoadLocation] to calculate the distance to. Must not be null.
     * @return A positive number representing the distance in scaled integer format (distance * 1000).
     */
    fun getDistanceTo(location: RoadLocation): Long {
        if (this === location) {
            return 0L
        }
        val distance = travelDistanceMap.getValue(location.id)
        // Multiplied by 1000 to avoid floating point arithmetic rounding errors
        return (distance * 1000.0 + 0.5).toLong()
    }

    /**
     * The angle relative to the direction EAST.
     *
     * @param location never null
     * @return in Cartesian coordinates
     */
    fun getAngle(location: RoadLocation): Double {
        // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
        val latitudeDifference = location.latitude - latitude
        val longitudeDifference = location.longitude - longitude
        return atan2(latitudeDifference, longitudeDifference)
    }
}

/**
 * Represents a customer in the Vehicle Routing Problem, which is a planning entity in Timefold Solver.
 *
 * A customer has a demand for goods and is associated with a specific [RoadLocation].
 * It also includes shadow variables for its assigned [Vehicle], and its [previousCustomer] and [nextCustomer]
 * in the vehicle's route, which are managed by Timefold Solver's list variable functionality.
 *
 * @property id The unique identifier of the customer.
 * @property demand The demand of the customer (e.g., quantity of goods to be delivered).
 * @property location The [RoadLocation] of the customer.
 * @property vehicle The [Vehicle] that serves this customer. This is a shadow variable.
 * @property previousCustomer The [Customer] visited immediately before this customer in the route. This is a shadow variable.
 * @property nextCustomer The [Customer] visited immediately after this customer in the route. This is a shadow variable.
 */
@PlanningEntity(difficultyWeightFactoryClass = DepotAngleCustomerFactory::class)
class Customer(
    val id: Long = -1,
    val demand: Int = 0,
    val location: RoadLocation = RoadLocation(-1),
    // Shadow variables
    @InverseRelationShadowVariable(sourceVariableName = "customers")
    var vehicle: Vehicle? = null,
    @PreviousElementShadowVariable(sourceVariableName = "customers")
    var previousCustomer: Customer? = null,
    @NextElementShadowVariable(sourceVariableName = "customers")
    var nextCustomer: Customer? = null
) {
    /**
     * Calculates the distance from the previous standstill (either the vehicle's depot or the previous customer)
     * to this customer's location.
     *
     * @return The distance in scaled integer format (distance * 1000).
     * @throws IllegalStateException if the `vehicle` shadow variable is not initialized.
     */
    val distanceFromPreviousStandstill: Long
        get() {
            checkNotNull(vehicle) {
                "This method must not be called when the shadow variables are not initialized yet."
            }
            return if (previousCustomer == null) {
                vehicle!!.location.getDistanceTo(location)
            } else {
                previousCustomer!!.location.getDistanceTo(location)
            }
        }

    /**
     * Calculates the distance from this customer's location back to its assigned vehicle's depot.
     *
     * @return The distance in scaled integer format (distance * 1000).
     */
    val distanceToDepot: Long
        get() {
            return location.getDistanceTo(vehicle!!.location)
        }
}

/**
 * Represents a depot where vehicles start and end their routes.
 *
 * @property id The unique identifier of the depot.
 * @property location The [RoadLocation] of the depot.
 */
class Depot(
    val id: Long,
    val location: RoadLocation
)

/**
 * Represents a vehicle in the Vehicle Routing Problem, which is a planning entity in Timefold Solver.
 *
 * A vehicle has a carrying capacity and is associated with a [Depot]. It also contains a list variable
 * `customers` which represents the ordered list of customers assigned to this vehicle.
 *
 * @property id The unique identifier of the vehicle.
 * @property capacity The maximum demand the vehicle can carry.
 * @property depot The [Depot] where the vehicle starts and ends its route.
 * @property customers The [List] of [Customer]s assigned to this vehicle, representing its route. This is a planning list variable.
 */
@PlanningEntity
class Vehicle(
    val id: Long = -1,
    val capacity: Int = 0,
    val depot: Depot = Depot(-1, RoadLocation(-1)),
    @PlanningListVariable
    var customers: List<Customer> = emptyList()
) {
    /**
     * Returns the [RoadLocation] of the vehicle, which is the location of its associated depot.
     */
    val location: RoadLocation
        get() = depot.location
}

/**
 * Represents the entire Vehicle Routing Problem solution, which is the planning solution in Timefold Solver.
 *
 * This class holds all problem facts (locations, depots, customers) and the planning entities (vehicles)
 * with their assigned customer routes. It also contains the [HardSoftLongScore] representing the quality of the solution.
 *
 * @property id The unique identifier of the solution.
 * @property name The name of the solution.
 * @property locationList A [List] of all [RoadLocation]s involved in the problem.
 * @property depotList A [List] of all [Depot]s in the problem.
 * @property vehicleList A [List] of all [Vehicle]s, which are the planning entities whose `customers` list Timefold Solver will optimize.
 * @property customerList A [List] of all [Customer]s to be visited.
 * @property score The [HardSoftLongScore] of the solution, indicating its feasibility and quality.
 */
@PlanningSolution
class VehicleRoutingSolution(
    val id: Long = -1,
    val name: String = "None",
    @ProblemFactCollectionProperty
    val locationList: List<RoadLocation> = emptyList(),
    @ProblemFactCollectionProperty
    val depotList: List<Depot> = emptyList(),
    @PlanningEntityCollectionProperty
    val vehicleList: List<Vehicle> = emptyList(),
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    val customerList: List<Customer> = emptyList(),
    @PlanningScore
    val score: HardSoftLongScore? = null
)