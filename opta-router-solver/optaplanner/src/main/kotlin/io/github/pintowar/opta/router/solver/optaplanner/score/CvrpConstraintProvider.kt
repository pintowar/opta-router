package io.github.pintowar.opta.router.solver.optaplanner.score

import io.github.pintowar.opta.router.solver.optaplanner.domain.Customer
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore
import org.optaplanner.core.api.score.stream.Constraint
import org.optaplanner.core.api.score.stream.ConstraintCollectors
import org.optaplanner.core.api.score.stream.ConstraintFactory
import org.optaplanner.core.api.score.stream.ConstraintProvider

class CvrpConstraintProvider : ConstraintProvider {

    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> {
        return arrayOf(
            vehicleCapacity(factory),
            distanceToPreviousStandstill(factory),
            distanceFromLastCustomerToDepot(factory)
        )
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************
    private fun vehicleCapacity(factory: ConstraintFactory): Constraint = factory
        .forEach(Customer::class.java)
        .filter { customer -> customer.vehicle != null }
        .groupBy({ it.vehicle }, ConstraintCollectors.sum { it: Customer -> it.demand })
        .filter { vehicle, demand -> demand > vehicle!!.capacity }
        .penalizeLong(HardSoftLongScore.ONE_HARD) { vehicle, demand -> (demand - vehicle!!.capacity).toLong() }
        .asConstraint("vehicleCapacity")

    // ************************************************************************
    // Soft constraints
    // ************************************************************************
    private fun distanceToPreviousStandstill(factory: ConstraintFactory): Constraint = factory
        .forEach(Customer::class.java)
        .filter { customer: Customer -> customer.vehicle != null }
        .penalizeLong(HardSoftLongScore.ONE_SOFT) { it.distanceFromPreviousStandstill }
        .asConstraint("distanceToPreviousStandstill")

    private fun distanceFromLastCustomerToDepot(factory: ConstraintFactory): Constraint = factory
        .forEach(Customer::class.java)
        .filter { it.vehicle != null && it.nextCustomer == null }
        .penalizeLong(HardSoftLongScore.ONE_SOFT) { it.distanceToDepot }
        .asConstraint("distanceFromLastCustomerToDepot")
}