package io.github.pintowar.opta.router.solver.optaplanner.domain.nearby

import io.github.pintowar.opta.router.solver.optaplanner.domain.Customer
import io.github.pintowar.opta.router.solver.optaplanner.domain.LocationAware
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter

class CustomerNearbyDistanceMeter : NearbyDistanceMeter<Customer, LocationAware> {
    override fun getNearbyDistance(origin: Customer, destination: LocationAware): Double {
        return origin.location.getDistanceTo(destination.location).toDouble()
        // If arriving early also inflicts a cost (more than just not using the vehicle more), such as the driver's wage, use this:
        //        if (origin instanceof TimeWindowedCustomer && destination instanceof TimeWindowedCustomer) {
        //            distance += ((TimeWindowedCustomer) origin).getTimeWindowGapTo((TimeWindowedCustomer) destination);
        //        }
        // return distance;
    }
}