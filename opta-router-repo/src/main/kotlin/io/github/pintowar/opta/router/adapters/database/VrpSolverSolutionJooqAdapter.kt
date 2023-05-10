package io.github.pintowar.opta.router.adapters.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolverSolution
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.generated.public.tables.references.VRP_SOLVER_SOLUTION
import java.time.Instant
import java.util.*

class VrpSolverSolutionJooqAdapter(
    private val mapper: ObjectMapper,
    private val dsl: DSLContext
) : VrpSolverSolutionPort {

    override fun clearSolution(problemId: Long) {
        createNewSolution(problemId, "none")
    }

    override fun currentSolution(problemId: Long): VrpSolverSolution? = dsl
        .selectFrom(VRP_SOLVER_SOLUTION)
        .where(VRP_SOLVER_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
        .orderBy(VRP_SOLVER_SOLUTION.UPDATED_AT.desc())
        .limit(1)
        .fetchOne { sol ->
            val routes = mapper.readValue<List<Route>>(sol.paths.data())
            VrpSolverSolution(
                sol.vrpProblemId,
                sol.solver,
                routes,
                SolverState.valueOf(sol.status),
                sol.solutionKey
            )
        }

    override fun createNewSolution(
        instanceId: Long,
        solver: String,
        solverState: SolverState,
        paths: List<Route>,
        uuid: UUID?
    ) {
        val now = Instant.now()
        dsl.insertInto(VRP_SOLVER_SOLUTION)
            .set(VRP_SOLVER_SOLUTION.SOLUTION_KEY, uuid)
            .set(VRP_SOLVER_SOLUTION.VRP_PROBLEM_ID, instanceId)
            .set(VRP_SOLVER_SOLUTION.SOLVER, solver)
            .set(VRP_SOLVER_SOLUTION.STATUS, solverState.name)
            .set(VRP_SOLVER_SOLUTION.PATHS, JSON.json(mapper.writeValueAsString(paths)))
            .set(VRP_SOLVER_SOLUTION.CREATED_AT, now)
            .set(VRP_SOLVER_SOLUTION.UPDATED_AT, now)
            .execute()
    }
}