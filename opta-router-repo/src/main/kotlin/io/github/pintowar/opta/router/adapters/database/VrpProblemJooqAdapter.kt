package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpProblemSummary
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import io.github.pintowar.opta.router.core.serialization.Serde
import io.github.pintowar.opta.router.core.serialization.fromJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.generated.tables.records.VrpProblemRecord
import org.jooq.generated.tables.references.VRP_PROBLEM
import org.jooq.generated.tables.references.VRP_PROBLEM_MATRIX
import org.jooq.generated.tables.references.VRP_SOLVER_REQUEST
import org.jooq.impl.DSL.sum
import org.jooq.impl.DSL.`when`
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.Instant
import org.jooq.impl.DSL.count as dslCount

class VrpProblemJooqAdapter(
    private val dsl: DSLContext,
    private val geo: GeoPort,
    private val mapper: Serde
) : VrpProblemPort {
    override fun findAll(
        query: String,
        offset: Int,
        limit: Int
    ): Flow<VrpProblemSummary> {
        val sumStatuses = { status: SolverStatus ->
            sum(`when`(VRP_SOLVER_REQUEST.STATUS.eq(status.name), 1).otherwise(0)).cast(Int::class.java)
        }

        return dsl
            .select(
                VRP_PROBLEM,
                dslCount(VRP_SOLVER_REQUEST),
                sumStatuses(SolverStatus.ENQUEUED),
                sumStatuses(SolverStatus.RUNNING),
                sumStatuses(SolverStatus.TERMINATED),
                sumStatuses(SolverStatus.NOT_SOLVED)
            )
            .from(VRP_PROBLEM)
            .leftJoin(VRP_SOLVER_REQUEST).on(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID))
            .where(VRP_PROBLEM.NAME.likeIgnoreCase("${query.trim()}%"))
            .groupBy(VRP_PROBLEM)
            .limit(offset, limit).asFlow().map { (p, t, e, r, f, c) ->
                toProblem(p).let {
                    val totalCapacity = it.vehicles.sumOf { v -> v.capacity }
                    val totalDemand = it.customers.sumOf { c -> c.demand }
                    val (nl, nv) = it.numLocations() to it.numVehicles()
                    VrpProblemSummary(it.id, it.name, nl, nv, totalCapacity, totalDemand, e, r, f, c, t)
                }
            }
    }

    override suspend fun count(query: String): Long {
        val (total) =
            dsl.selectCount().from(VRP_PROBLEM).where(VRP_PROBLEM.NAME.likeIgnoreCase("${query.trim()}%"))
                .awaitSingle()
        return total.toLong()
    }

    override suspend fun getById(problemId: Long): VrpProblem? {
        return dsl.selectFrom(VRP_PROBLEM)
            .where(VRP_PROBLEM.ID.eq(problemId))
            .awaitFirstOrNull()
            ?.let(::toProblem)
    }

    override suspend fun create(problem: VrpProblem) {
        val now = Instant.now()

        val matrix = geo.generateMatrix(problem.locations().toSet())

        dsl.transactionCoroutine { trx ->
            val result =
                trx.dsl()
                    .insertInto(VRP_PROBLEM)
                    .set(VRP_PROBLEM.NAME, problem.name)
                    .set(VRP_PROBLEM.CUSTOMERS, JSON.json(mapper.toJson(problem.customers)))
                    .set(VRP_PROBLEM.VEHICLES, JSON.json(mapper.toJson(problem.vehicles)))
                    .set(VRP_PROBLEM.CREATED_AT, now)
                    .set(VRP_PROBLEM.UPDATED_AT, now)
                    .returning()
                    .awaitSingle()

            trx.dsl()
                .insertInto(VRP_PROBLEM_MATRIX)
                .set(VRP_PROBLEM_MATRIX.VRP_PROBLEM_ID, result.id)
                .set(VRP_PROBLEM_MATRIX.LOCATION_IDS, matrix.getLocationIds())
                .set(VRP_PROBLEM_MATRIX.TRAVEL_DISTANCES, matrix.getTravelDistances())
                .set(VRP_PROBLEM_MATRIX.TRAVEL_TIMES, matrix.getTravelTimes())
                .set(VRP_PROBLEM_MATRIX.CREATED_AT, now)
                .set(VRP_PROBLEM_MATRIX.UPDATED_AT, now)
                .awaitSingle()
        }
    }

    override suspend fun deleteById(problemId: Long) {
        dsl.deleteFrom(VRP_PROBLEM)
            .where(VRP_PROBLEM.ID.eq(problemId))
            .awaitFirstOrNull()
    }

    override suspend fun update(
        id: Long,
        problem: VrpProblem
    ) {
        val now = Instant.now()

        val matrix = geo.generateMatrix(problem.locations().toSet())

        dsl.transactionCoroutine { trx ->
            dsl.update(VRP_PROBLEM)
                .set(VRP_PROBLEM.NAME, problem.name)
                .set(VRP_PROBLEM.CUSTOMERS, JSON.json(mapper.toJson(problem.customers)))
                .set(VRP_PROBLEM.VEHICLES, JSON.json(mapper.toJson(problem.vehicles)))
                .set(VRP_PROBLEM.UPDATED_AT, now)
                .where(VRP_PROBLEM.ID.eq(id))
                .awaitSingle()

            trx.dsl()
                .update(VRP_PROBLEM_MATRIX)
                .set(VRP_PROBLEM_MATRIX.LOCATION_IDS, matrix.getLocationIds())
                .set(VRP_PROBLEM_MATRIX.TRAVEL_DISTANCES, matrix.getTravelDistances())
                .set(VRP_PROBLEM_MATRIX.TRAVEL_TIMES, matrix.getTravelTimes())
                .set(VRP_PROBLEM_MATRIX.UPDATED_AT, now)
                .where(VRP_PROBLEM_MATRIX.VRP_PROBLEM_ID.eq(id))
                .awaitSingle()
        }
    }

    override suspend fun getMatrixById(problemId: Long): VrpProblemMatrix? {
        val matrix =
            dsl
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

    private fun toProblem(problem: VrpProblemRecord): VrpProblem {
        return VrpProblem(
            problem.id!!,
            problem.name,
            mapper.fromJson(problem.vehicles.data()),
            mapper.fromJson(problem.customers.data())
        )
    }
}