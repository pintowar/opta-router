package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.generated.tables.records.LocationRecord
import org.jooq.generated.tables.records.VehicleRecord
import org.jooq.generated.tables.references.LOCATION
import org.jooq.generated.tables.references.VEHICLE
import org.jooq.generated.tables.references.VRP_PROBLEM
import org.jooq.generated.tables.references.VRP_PROBLEM_LOCATION
import org.jooq.generated.tables.references.VRP_PROBLEM_MATRIX
import org.jooq.generated.tables.references.VRP_SOLVER_REQUEST
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select
import org.jooq.impl.DSL.selectCount

class VrpProblemJooqAdapter(
    private val dsl: DSLContext
) : VrpProblemPort {

    companion object {
        fun problemSelect(dsl: DSLContext) = dsl
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
                ).convertFrom { r -> r.map(Records.mapping(convertVehicle)) },
                field(selectCount().from(VRP_SOLVER_REQUEST).where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID)))
            )

        private fun problemQuery(dsl: DSLContext) = problemSelect(dsl).from(VRP_PROBLEM)

        private val convertCustomer: (l: LocationRecord) -> Customer = { l ->
            Customer(l.id!!, l.name, l.latitude, l.longitude, l.demand)
        }
        private val convertVehicle: (v: VehicleRecord, l: LocationRecord) -> Vehicle = { v, l ->
            Vehicle(v.id!!, v.name, v.capacity, Depot(l.id!!, l.name, l.latitude, l.longitude))
        }
    }

    override fun findAll(query: String, offset: Int, limit: Int): Flow<VrpProblemSummary> {
        return problemQuery(dsl)
            .where(VRP_PROBLEM.NAME.likeIgnoreCase("${query.trim()}%"))
            .limit(offset, limit).asFlow().map { (p, c, v, t) ->
                VrpProblem(p.id!!, p.name, v, c).let {
                    VrpProblemSummary(it.id, it.name, it.nLocations, it.nVehicles, t)
                }
            }
    }

    override suspend fun count(query: String): Long {
        val (total) = dsl.selectCount().from(VRP_PROBLEM).where(VRP_PROBLEM.NAME.likeIgnoreCase("${query.trim()}%"))
            .awaitSingle()
        return total.toLong()
    }

    override suspend fun getById(problemId: Long): VrpProblem? {
        return problemQuery(dsl).where(VRP_PROBLEM.ID.eq(problemId))
            .awaitFirstOrNull()
            ?.let { (r, c, v) ->
                VrpProblem(r.id!!, r.name, v, c)
            }
    }

    override suspend fun deleteById(problemId: Long) {
        dsl.deleteFrom(VRP_PROBLEM)
            .where(VRP_PROBLEM.ID.eq(problemId))
            .awaitFirstOrNull()
    }

    override suspend fun getMatrixById(problemId: Long): VrpProblemMatrix? {
        val matrix = dsl
            .selectFrom(VRP_PROBLEM_MATRIX)
            .where(VRP_PROBLEM_MATRIX.VRP_PROBLEM_ID.eq(problemId))
            .awaitFirstOrNull()

        return matrix?.let {
            VrpProblemMatrix(
                it.locationIds.filterNotNull(),
                it.travelDistances.filterNotNull(),
                it.travelTimes.filterNotNull()
            )
        }
    }
}