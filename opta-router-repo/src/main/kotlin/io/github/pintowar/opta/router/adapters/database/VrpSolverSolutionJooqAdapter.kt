package io.github.pintowar.opta.router.adapters.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.Record
import org.jooq.generated.public.tables.records.VrpProblemRecord
import org.jooq.generated.public.tables.records.VrpSolutionRecord
import org.jooq.generated.public.tables.records.VrpSolverRequestRecord
import org.jooq.generated.public.tables.references.VRP_PROBLEM
import org.jooq.generated.public.tables.references.VRP_SOLUTION
import org.jooq.generated.public.tables.references.VRP_SOLVER_REQUEST
import org.jooq.generated.public.tables.references.VRP_SOLVER_SOLUTION
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class VrpSolverSolutionJooqAdapter(
    private val mapper: ObjectMapper,
    private val dsl: DSLContext
) : VrpSolverSolutionPort {

    override fun currentSolution(problemId: Long): List<Route> = dsl
        .selectFrom(VRP_SOLUTION)
        .where(VRP_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
        .limit(1)
        .fetchOne { sol ->
            mapper.readValue<List<Route>>(sol.paths.data())
        } ?: emptyList()

    override fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? {
        return currentSolutionRequestQuery(dsl, problemId)
            .fetchOne(::convertRecordToSolutionRequest)
    }

    override fun upsertSolution(
        problemId: Long,
        solverStatus: SolverStatus,
        paths: List<Route>,
        objective: Double,
        clear: Boolean,
        uuid: UUID
    ): VrpSolutionRequest {
        val now = Instant.now()
        val jsonPaths = if (clear) JSON.json("[]") else JSON.json(mapper.writeValueAsString(paths))

        return dsl.transactionResult { trx ->
            trx.dsl()
                .update(VRP_SOLVER_REQUEST)
                .set(VRP_SOLVER_REQUEST.STATUS, if (clear) SolverStatus.NOT_SOLVED.name else solverStatus.name)
                .set(VRP_SOLVER_REQUEST.UPDATED_AT, now)
                .where(VRP_SOLVER_REQUEST.REQUEST_KEY.eq(uuid))
                .execute()

            val numSolutions = dsl.selectFrom(VRP_SOLUTION)
                .where(VRP_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
                .count()

            if (numSolutions == 0) {
                trx.dsl()
                    .insertInto(VRP_SOLUTION)
                    .set(VRP_SOLUTION.VRP_PROBLEM_ID, problemId)
                    .set(VRP_SOLUTION.PATHS, jsonPaths)
                    .set(VRP_SOLUTION.CREATED_AT, now)
                    .set(VRP_SOLUTION.UPDATED_AT, now)
                    .execute()
            } else {
                trx.dsl()
                    .update(VRP_SOLUTION)
                    .set(VRP_SOLUTION.PATHS, jsonPaths)
                    .set(VRP_SOLUTION.UPDATED_AT, now)
                    .where(VRP_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
                    .execute()
            }

            if (!clear) {
                trx.dsl()
                    .insertInto(VRP_SOLVER_SOLUTION)
                    .set(VRP_SOLVER_SOLUTION.REQUEST_KEY, uuid)
                    .set(VRP_SOLVER_SOLUTION.VRP_PROBLEM_ID, problemId)
                    .set(VRP_SOLVER_SOLUTION.STATUS, solverStatus.name)
                    .set(VRP_SOLVER_SOLUTION.OBJECTIVE, objective)
                    .set(VRP_SOLVER_SOLUTION.PATHS, jsonPaths)
                    .set(VRP_SOLVER_SOLUTION.CREATED_AT, now)
                    .set(VRP_SOLVER_SOLUTION.UPDATED_AT, now)
                    .execute()
            }

            currentSolutionRequestQuery(trx.dsl(), problemId)
                .fetchOne(::convertRecordToSolutionRequest)!!
        }
    }

    override fun solutionHistory(problemId: Long, requestId: UUID): List<VrpSolverObjective> {
        return dsl.selectFrom(VRP_SOLVER_SOLUTION)
            .where(VRP_SOLVER_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
            .and(VRP_SOLVER_SOLUTION.REQUEST_KEY.eq(requestId))
            .orderBy(VRP_SOLVER_SOLUTION.CREATED_AT)
            .fetch {
                VrpSolverObjective(it.objective, SolverStatus.valueOf(it.status), it.requestKey!!, it.createdAt)
            }
    }

    private fun currentSolutionRequestQuery(dsl: DSLContext, problemId: Long) = VrpProblemJooqAdapter
        .problemSelect(dsl)
        .select(VRP_SOLUTION, VRP_SOLVER_REQUEST)
        .from(VRP_PROBLEM)
        .leftJoin(VRP_SOLUTION).on(VRP_SOLUTION.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID))
        .leftJoin(VRP_SOLVER_REQUEST).on(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID))
        .where(VRP_PROBLEM.ID.eq(problemId))
        .orderBy(VRP_SOLVER_REQUEST.UPDATED_AT.desc())
        .limit(1)

    private fun convertRecordToSolutionRequest(record: Record): VrpSolutionRequest {
        val problem = record.get(0, VrpProblemRecord::class.java).let {
            VrpProblem(it.id!!, it.name, emptyList(), emptyList())
        }.copy(
            customers = record.get(1, List::class.java).filterIsInstance<Customer>(),
            vehicles = record.get(2, List::class.java).filterIsInstance<Vehicle>()
        )
        val solution = record.get(3, VrpSolutionRecord::class.java)
        val solverRequest = record.get(4, VrpSolverRequestRecord::class.java)

        return VrpSolutionRequest(
            VrpSolution(
                problem,
                solution.get(solution.field2())?.let { mapper.readValue<List<Route>>(it.data()) } ?: emptyList()
            ),
            solverRequest.get(solverRequest.field4())?.let(SolverStatus::valueOf) ?: SolverStatus.NOT_SOLVED,
            solverRequest.get(solverRequest.field1())
        )
    }
}