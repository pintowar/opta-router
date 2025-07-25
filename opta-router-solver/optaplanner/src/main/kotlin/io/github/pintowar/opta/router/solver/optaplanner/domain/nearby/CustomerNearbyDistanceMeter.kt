package io.github.pintowar.opta.router.solver.optaplanner.domain.nearby

import io.github.pintowar.opta.router.solver.optaplanner.domain.Customer
import io.github.pintowar.opta.router.solver.optaplanner.domain.LocationAware
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter

class CustomerNearbyDistanceMeter : NearbyDistanceMeter<Customer, LocationAware> {
    /**
     * Calculates the distance between an origin [Customer] and a destination [LocationAware] object.
     *
     * This method is used by OptaPlanner's nearby selection to determine the proximity between planning entities.
     * It returns the travel distance between the locations of the origin customer and the destination.
     *
     * @param origin The origin [Customer] for which to calculate the distance.
     * @param destination The destination [LocationAware] object (e.g., another Customer or a Depot).
     * @return The distance between the origin and destination locations as a [Double].
     */
    override fun getNearbyDistance(
        origin: Customer,
        destination: LocationAware
    ): Double {
        return origin.location.getDistanceTo(destination.location).toDouble()
        // If arriving early also inflicts a cost (more than just not using the vehicle more), such as the driver's wage, use this:
        //        if (origin instanceof TimeWindowedCustomer && destination instanceof TimeWindowedCustomer) {
        //            distance += ((TimeWindowedCustomer) origin).getTimeWindowGapTo((TimeWindowedCustomer) destination);
        //        }
        // return distance;
    }
}