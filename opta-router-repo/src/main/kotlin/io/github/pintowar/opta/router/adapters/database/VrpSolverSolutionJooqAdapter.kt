package io.github.pintowar.opta.router.adapters.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.generated.public.tables.references.VRP_SOLUTION
import org.jooq.generated.public.tables.references.VRP_SOLVER_REQUEST
import org.jooq.generated.public.tables.references.VRP_SOLVER_SOLUTION
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

    override fun upsertSolution(
        problemId: Long,
        solverStatus: SolverStatus,
        paths: List<Route>,
        objective: Double,
        clear: Boolean,
        uuid: UUID
    ) {
        val now = Instant.now()

        dsl.transaction { trx ->
            trx.dsl()
                .update(VRP_SOLVER_REQUEST)
                .set(VRP_SOLVER_REQUEST.STATUS, if (clear) SolverStatus.NOT_SOLVED.name else solverStatus.name)
                .set(VRP_SOLVER_REQUEST.UPDATED_AT, now)
                .where(VRP_SOLVER_REQUEST.REQUEST_KEY.eq(uuid))
                .execute()

            val jsonPaths = if (clear) JSON.json("[]") else JSON.json(mapper.writeValueAsString(paths))

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

            if (!clear) trx.dsl()
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
    }
}