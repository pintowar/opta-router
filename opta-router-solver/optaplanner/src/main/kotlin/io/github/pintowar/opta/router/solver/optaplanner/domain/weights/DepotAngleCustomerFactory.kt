package io.github.pintowar.opta.router.solver.optaplanner.domain.weights

import io.github.pintowar.opta.router.solver.optaplanner.domain.Customer
import io.github.pintowar.opta.router.solver.optaplanner.domain.VehicleRoutingSolution
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory

class DepotAngleCustomerFactory : SelectionSorterWeightFactory<VehicleRoutingSolution, Customer> {
    override fun createSorterWeight(
        vehicleRoutingSolution: VehicleRoutingSolution,
        customer: Customer
    ): DepotAngleCustomer {
        val depot = vehicleRoutingSolution.depotList[0]
        return DepotAngleCustomer(
            customer,
            customer.location.getAngle(depot.location),
            customer.location.getDistanceTo(depot.location) +
                depot.location.getDistanceTo(customer.location)
        )
    }

    class DepotAngleCustomer(
        private val customer: Customer,
        private val depotAngle: Double,
        private val depotRoundTripDistance: Long
    ) : Comparable<DepotAngleCustomer> {
        override operator fun compareTo(other: DepotAngleCustomer): Int {
            return COMPARATOR.compare(this, other)
        }

        companion object {
            private val COMPARATOR =
                Comparator
                    .comparingDouble { weight: DepotAngleCustomer -> weight.depotAngle }
                    .thenComparingLong {
                            weight ->
                        weight.depotRoundTripDistance
                    } // Ascending (further from the depot are more difficult)
                    .thenComparing(
                        { weight -> weight.customer },
                        Comparator.comparingLong(Customer::id)
                    )
        }
    }
}