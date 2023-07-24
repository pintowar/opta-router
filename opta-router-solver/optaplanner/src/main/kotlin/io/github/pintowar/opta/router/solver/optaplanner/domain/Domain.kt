package io.github.pintowar.opta.router.solver.optaplanner.domain


import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable
import org.optaplanner.core.api.domain.variable.NextElementShadowVariable
import org.optaplanner.core.api.domain.variable.PlanningListVariable
import org.optaplanner.core.api.domain.variable.PreviousElementShadowVariable
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore
import kotlin.math.atan2

interface LocationAware {
    val location: RoadLocation
}

class RoadLocation(
    val id: Long = -1,
    val name: String = "None",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    private val travelDistanceMap: Map<Long, Double> = emptyMap()
) {
    /**
     * @param location never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
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

@PlanningEntity //(difficultyWeightFactoryClass = DepotAngleCustomerFactory::class)
class Customer(
    val id: Long = -1,
    val demand: Int = 0,
    override val location: RoadLocation = RoadLocation(-1),
    // Shadow variables
    @InverseRelationShadowVariable(sourceVariableName = "customers")
    var vehicle: Vehicle? = null,
    @PreviousElementShadowVariable(sourceVariableName = "customers")
    var previousCustomer: Customer? = null,
    @NextElementShadowVariable(sourceVariableName = "customers")
    var nextCustomer: Customer? = null
) : LocationAware {
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

    val distanceToDepot: Long
        get() {
            return location.getDistanceTo(vehicle!!.location)
        }
}

class Depot(val id: Long, val location: RoadLocation)

@PlanningEntity
class Vehicle(
    val id: Long = -1,
    val capacity: Int = 0,
    val depot: Depot = Depot(-1, RoadLocation(-1)),
    @PlanningListVariable
    var customers: List<Customer> = emptyList()
) : LocationAware {
    override val location: RoadLocation
        get() = depot.location
}

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