package io.github.pintowar.opta.router.adapters.database.jooq

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.generated.public.tables.records.VrpLocationRecord
import org.jooq.generated.public.tables.records.VrpVehicleRecord
import org.jooq.generated.public.tables.references.*
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select

class SolutionJooqRepository(
    private val dsl: DSLContext
) : SolutionRepository {

    override fun listAll(): List<VrpSolution> {
        return query().limit(0, 10).fetch { (r, c, v) ->
            VrpSolution.emptyFromInstance(VrpProblem(r.id!!, r.name, v, c))
        }
    }

    override fun getByInstanceId(instanceId: Long): VrpSolution? {
        return query().where(VRP_PROBLEM.ID.eq(instanceId)).fetch { (r, c, v) ->
            VrpSolution.emptyFromInstance(VrpProblem(r.id!!, r.name, v, c))
        }.firstOrNull()
    }

    override fun getByMatrixInstanceId(instanceId: Long): Matrix? {
        val matrix = dsl
            .selectFrom(VRP_PROBLEM_MATRIX)
            .where(VRP_PROBLEM_MATRIX.VRP_PROBLEM_ID.eq(instanceId))
            .fetchOne()

        return matrix?.let {
            VrpProblemMatrix(
                it.locationIds.filterNotNull(), it.travelDistances.filterNotNull(), it.travelTimes.filterNotNull()
            )
        }
    }

//    private val convertCustomer: (c: CustomerRecord, l: LocationRecord) -> Customer = { c, l ->
//        Customer(l.id!!, l.name, l.latitude.toDouble(), l.longitude.toDouble(), c.demand)
//    }
//    private val convertVehicle: (v: VehicleRecord, l: LocationRecord) -> Vehicle = { v, l ->
//        Vehicle(v.id!!, v.name, v.capacity, Depot(l.id!!, l.name, l.latitude.toDouble(), l.longitude.toDouble()))
//    }

    private val convertCustomer: (l: VrpLocationRecord) -> Customer = { l ->
        Customer(l.id!!, l.name, l.latitude.toDouble(), l.longitude.toDouble(), l.demand)
    }
    private val convertVehicle: (v: VrpVehicleRecord, l: VrpLocationRecord) -> Vehicle = { v, l ->
        Vehicle(v.id!!, v.name, v.capacity, Depot(l.id!!, l.name, l.latitude.toDouble(), l.longitude.toDouble()))
    }

    private fun query() = dsl
        .select(
            VRP_PROBLEM,
            multiset(
                select(VRP_LOCATION)
                    .from(
                        VRP_PROBLEM_LOCATION
                            .leftJoin(VRP_LOCATION).on(VRP_PROBLEM_LOCATION.LOCATION_ID.eq(VRP_LOCATION.ID))
                    )
                    .where(VRP_PROBLEM_LOCATION.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID).and(VRP_LOCATION.KIND.eq("customer")))
            ).convertFrom { r -> r.map(Records.mapping(convertCustomer)) },
            multiset(
                select(VRP_VEHICLE, VRP_LOCATION)
                    .from(
                        VRP_VEHICLE
                            .leftJoin(VRP_LOCATION).on(VRP_LOCATION.ID.eq(VRP_VEHICLE.DEPOT_ID))
                            .leftJoin(VRP_PROBLEM_LOCATION).on(VRP_PROBLEM_LOCATION.LOCATION_ID.eq(VRP_LOCATION.ID))
                    ).where(VRP_PROBLEM_LOCATION.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID).and(VRP_LOCATION.KIND.eq("depot")))
            ).convertFrom { r -> r.map(Records.mapping(convertVehicle)) }
        )
        .from(VRP_PROBLEM)
//        .select(
//            ROUTE,
//            multiset(
//                select(CUSTOMER, LOCATION)
//                    .from(
//                        CUSTOMER.leftJoin(ROUTE_CUSTOMER).on(ROUTE_CUSTOMER.CUSTOMER_ID.eq(CUSTOMER.ID))
//                            .leftJoin(LOCATION).on(CUSTOMER.LOCATION_ID.eq(LOCATION.ID))
//                    )
//                    .where(ROUTE_CUSTOMER.ROUTE_ID.eq(ROUTE.ID))
//            ).`as`("CUSTOMERS").convertFrom { r -> r.map(Records.mapping(convertCustomer)) },
//            multiset(
//                select(VEHICLE, LOCATION)
//                    .from(
//                        VEHICLE.leftJoin(DEPOT).on(VEHICLE.DEPOT_ID.eq(DEPOT.ID))
//                            .leftJoin(LOCATION).on(LOCATION.ID.eq(DEPOT.LOCATION_ID))
//                    )
//                    .where(DEPOT.ID.eq(ROUTE.DEPOT_ID))
//            ).`as`("VEHICLES").convertFrom { r -> r.map(Records.mapping(convertVehicle)) },
//        )
//        .from(ROUTE)
}
