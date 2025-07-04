package io.github.pintowar.opta.router.solver.optaplanner.score

import io.github.pintowar.opta.router.solver.optaplanner.domain.Customer
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore
import org.optaplanner.core.api.score.stream.Constraint
import org.optaplanner.core.api.score.stream.ConstraintCollectors
import org.optaplanner.core.api.score.stream.ConstraintFactory
import org.optaplanner.core.api.score.stream.ConstraintProvider

class CvrpConstraintProvider : ConstraintProvider {
    /**
     * Defines all constraints for the Capacitated Vehicle Routing Problem (CVRP).
     *
     * This function aggregates all hard and soft constraints that OptaPlanner will use
     * to evaluate the quality of a [VehicleRoutingSolution].
     *
     * @param factory The [ConstraintFactory] used to build constraints.
     * @return An [Array] of [Constraint]s applicable to the CVRP.
     */
    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> =
        arrayOf(
            vehicleCapacity(factory),
            distanceToPreviousStandstill(factory),
            distanceFromLastCustomerToDepot(factory)
        )

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    /**
     * Defines a hard constraint that penalizes solutions where a vehicle's capacity is exceeded.
     *
     * The penalty is proportional to the amount by which the demand exceeds the vehicle's capacity.
     *
     * @param factory The [ConstraintFactory] used to build the constraint.
     * @return A [Constraint] representing the vehicle capacity hard constraint.
     */
    private fun vehicleCapacity(factory: ConstraintFactory): Constraint =
        factory
            .forEach(Customer::class.java)
            .filter { customer -> customer.vehicle != null }
            .groupBy({ it.vehicle }, ConstraintCollectors.sum { it: Customer -> it.demand })
            .filter { vehicle, demand -> demand > vehicle!!.capacity }
            .penalizeLong(HardSoftLongScore.ONE_HARD) { vehicle, demand -> (demand - vehicle!!.capacity).toLong() }
            .asConstraint("vehicleCapacity")

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    /**
     * Defines a soft constraint that penalizes the total travel distance from each customer
     * to its previous standstill (either the depot or the preceding customer).
     *
     * This constraint aims to minimize the overall travel distance of all vehicles.
     *
     * @param factory The [ConstraintFactory] used to build the constraint.
     * @return A [Constraint] representing the distance to previous standstill soft constraint.
     */
    private fun distanceToPreviousStandstill(factory: ConstraintFactory): Constraint =
        factory
            .forEach(Customer::class.java)
            .filter { customer: Customer -> customer.vehicle != null }
            .penalizeLong(HardSoftLongScore.ONE_SOFT) { it.distanceFromPreviousStandstill }
            .asConstraint("distanceToPreviousStandstill")

    /**
     * Defines a soft constraint that penalizes the travel distance from the last customer
     * in a vehicle's route back to its depot.
     *
     * This constraint ensures that the return trip to the depot is also considered in the total distance minimization.
     *
     * @param factory The [ConstraintFactory] used to build the constraint.
     * @return A [Constraint] representing the distance from last customer to depot soft constraint.
     */
    private fun distanceFromLastCustomerToDepot(factory: ConstraintFactory): Constraint =
        factory
            .forEach(Customer::class.java)
            .filter { it.vehicle != null && it.nextCustomer == null }
            .penalizeLong(HardSoftLongScore.ONE_SOFT) { it.distanceToDepot }
            .asConstraint("distanceFromLastCustomerToDepot")
}