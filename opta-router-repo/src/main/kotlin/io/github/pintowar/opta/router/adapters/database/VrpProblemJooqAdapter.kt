package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.generated.public.tables.records.LocationRecord
import org.jooq.generated.public.tables.records.VehicleRecord
import org.jooq.generated.public.tables.references.*
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select

class VrpProblemJooqAdapter(
    private val dsl: DSLContext
) : VrpProblemPort {

    override fun listAll(): List<VrpProblem> {
        return query().limit(0, 10).fetch { (r, c, v) ->
            VrpProblem(r.id!!, r.name, v, c)
        }
    }

    override fun getById(problemId: Long): VrpProblem? {
        return query().where(VRP_PROBLEM.ID.eq(problemId)).fetch { (r, c, v) ->
            VrpProblem(r.id!!, r.name, v, c)
        }.firstOrNull()
    }

    override fun getMatrixById(problemId: Long): Matrix? {
        val matrix = dsl
            .selectFrom(VRP_PROBLEM_MATRIX)
            .where(VRP_PROBLEM_MATRIX.VRP_PROBLEM_ID.eq(problemId))
            .fetchOne()

        return matrix?.let {
            VrpProblemMatrix(
                it.locationIds.filterNotNull(), it.travelDistances.filterNotNull(), it.travelTimes.filterNotNull()
            )
        }
    }

    private val convertCustomer: (l: LocationRecord) -> Customer = { l ->
        Customer(l.id!!, l.name, l.latitude, l.longitude, l.demand)
    }
    private val convertVehicle: (v: VehicleRecord, l: LocationRecord) -> Vehicle = { v, l ->
        Vehicle(v.id!!, v.name, v.capacity, Depot(l.id!!, l.name, l.latitude, l.longitude))
    }

    private fun query() = dsl
        .select(
            VRP_PROBLEM,
            multiset(
                select(LOCATION)
                    .from(
                        VRP_PROBLEM_LOCATION
                            .leftJoin(LOCATION).on(VRP_PROBLEM_LOCATION.LOCATION_ID.eq(LOCATION.ID))
                    )
                    .where(VRP_PROBLEM_LOCATION.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID).and(LOCATION.KIND.eq("customer")))
            ).convertFrom { r -> r.map(Records.mapping(convertCustomer)) },
            multiset(
                select(VEHICLE, LOCATION)
                    .from(
                        VEHICLE
                            .leftJoin(LOCATION).on(LOCATION.ID.eq(VEHICLE.DEPOT_ID))
                            .leftJoin(VRP_PROBLEM_LOCATION).on(VRP_PROBLEM_LOCATION.LOCATION_ID.eq(LOCATION.ID))
                    ).where(VRP_PROBLEM_LOCATION.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID).and(LOCATION.KIND.eq("depot")))
            ).convertFrom { r -> r.map(Records.mapping(convertVehicle)) }
        )
        .from(VRP_PROBLEM)
}
