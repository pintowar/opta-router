package io.github.pintowar.opta.router.adapters.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpProblemSummary
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
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
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.selectCount
import java.time.Instant

class VrpProblemJooqAdapter(
    private val dsl: DSLContext,
    private val mapper: ObjectMapper
) : VrpProblemPort {

    override fun findAll(query: String, offset: Int, limit: Int): Flow<VrpProblemSummary> {
        return dsl
            .select(
                VRP_PROBLEM,
                field(
                    selectCount().from(VRP_SOLVER_REQUEST).where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID))
                )
            )
            .from(VRP_PROBLEM)
            .where(VRP_PROBLEM.NAME.likeIgnoreCase("${query.trim()}%"))
            .limit(offset, limit).asFlow().map { (p, t) ->
                toProblem(p).let {
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
        return dsl.selectFrom(VRP_PROBLEM)
            .where(VRP_PROBLEM.ID.eq(problemId))
            .awaitFirstOrNull()
            ?.let(::toProblem)
    }

    override suspend fun create(problem: VrpProblem) {
        val now = Instant.now()

        dsl.insertInto(VRP_PROBLEM)
            .set(VRP_PROBLEM.NAME, problem.name)
            .set(VRP_PROBLEM.CUSTOMERS, JSON.json(mapper.writeValueAsString(problem.customers)))
            .set(VRP_PROBLEM.VEHICLES, JSON.json(mapper.writeValueAsString(problem.vehicles)))
            .set(VRP_PROBLEM.CREATED_AT, now)
            .set(VRP_PROBLEM.UPDATED_AT, now)
            .awaitSingle()
    }

    override suspend fun deleteById(problemId: Long) {
        dsl.deleteFrom(VRP_PROBLEM)
            .where(VRP_PROBLEM.ID.eq(problemId))
            .awaitFirstOrNull()
    }

    override suspend fun update(id: Long, problem: VrpProblem) {
        val now = Instant.now()

        dsl.update(VRP_PROBLEM)
            .set(VRP_PROBLEM.NAME, problem.name)
            .set(VRP_PROBLEM.CUSTOMERS, JSON.json(mapper.writeValueAsString(problem.customers)))
            .set(VRP_PROBLEM.VEHICLES, JSON.json(mapper.writeValueAsString(problem.vehicles)))
            .set(VRP_PROBLEM.UPDATED_AT, now)
            .where(VRP_PROBLEM.ID.eq(id))
            .awaitSingle()
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

    private fun toProblem(problem: VrpProblemRecord): VrpProblem {
        return VrpProblem(
            problem.id!!,
            problem.name,
            mapper.readValue<List<Vehicle>>(problem.vehicles.data()),
            mapper.readValue<List<Customer>>(problem.customers.data())
        )
    }
}