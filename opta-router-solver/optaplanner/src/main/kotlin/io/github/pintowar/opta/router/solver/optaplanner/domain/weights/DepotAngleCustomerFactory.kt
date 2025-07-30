package io.github.pintowar.opta.router.solver.optaplanner.domain.weights

import io.github.pintowar.opta.router.solver.optaplanner.domain.Customer
import io.github.pintowar.opta.router.solver.optaplanner.domain.VehicleRoutingSolution
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory

class DepotAngleCustomerFactory : SelectionSorterWeightFactory<VehicleRoutingSolution, Customer> {
    /**
     * Creates a [DepotAngleCustomer] sorter weight for a given customer.
     *
     * This factory is used by OptaPlanner to sort customers based on their angle relative to the depot
     * and their round-trip distance to the depot. This can influence the order in which customers
     * are selected during the solving process.
     *
     * @param vehicleRoutingSolution The current [VehicleRoutingSolution].
     * @param customer The [Customer] for which to create the sorter weight.
     * @return A [DepotAngleCustomer] object containing the calculated angle and distance.
     */
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

    /**
     * Represents a sorter weight for a customer based on its angle and round-trip distance to the depot.
     *
     * This class implements [Comparable] to allow sorting of customers. The primary sorting key is the
     * angle to the depot, followed by the round-trip distance, and finally the customer ID for tie-breaking.
     *
     * @property customer The [Customer] associated with this weight.
     * @property depotAngle The angle of the customer's location relative to the depot's location.
     * @property depotRoundTripDistance The round-trip distance from the customer to the depot and back.
     */
    class DepotAngleCustomer(
        private val customer: Customer,
        private val depotAngle: Double,
        private val depotRoundTripDistance: Long
    ) : Comparable<DepotAngleCustomer> {
        /**
         * Compares this [DepotAngleCustomer] with another for ordering.
         *
         * The comparison is based on `depotAngle` (ascending), then `depotRoundTripDistance` (ascending),
         * and finally `customer.id` (ascending).
         *
         * @param other The other [DepotAngleCustomer] to compare with.
         * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
         */
        override operator fun compareTo(other: DepotAngleCustomer): Int = COMPARATOR.compare(this, other)

        companion object {
            private val COMPARATOR =
                Comparator
                    .comparingDouble { weight: DepotAngleCustomer -> weight.depotAngle }
                    .thenComparingLong { weight ->
                        weight.depotRoundTripDistance
                    } // Ascending (further from the depot are more difficult)
                    .thenComparing(
                        { weight -> weight.customer },
                        Comparator.comparingLong(Customer::id)
                    )
        }
    }
}