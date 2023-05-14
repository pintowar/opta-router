package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import org.jooq.DSLContext
import org.jooq.generated.public.tables.references.VRP_SOLVER_REQUEST
import java.time.Instant
import java.util.*

class VrpSolverRequestJooqAdapter(
    private val dsl: DSLContext
) : VrpSolverRequestPort {

    override fun createRequest(request: VrpSolverRequest): VrpSolverRequest? {
        val numEnqueued = dsl.selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(request.problemId))
            .and(VRP_SOLVER_REQUEST.STATUS.`in`(SolverState.ENQUEUED.name, SolverState.RUNNING.name))
            .count()

        if (numEnqueued > 0) return null

        val now = Instant.now()
        val result = dsl.insertInto(VRP_SOLVER_REQUEST)
            .set(VRP_SOLVER_REQUEST.REQUEST_KEY, request.requestKey)
            .set(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID, request.problemId)
            .set(VRP_SOLVER_REQUEST.SOLVER, request.solver)
            .set(VRP_SOLVER_REQUEST.STATUS, request.status.name)
            .set(VRP_SOLVER_REQUEST.CREATED_AT, now)
            .set(VRP_SOLVER_REQUEST.UPDATED_AT, now)
            .execute()

        return if (result == 1) request else null
    }

    override fun currentSolverStatus(problemId: Long): VrpSolverRequest? {
        return dsl.selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(problemId))
            .orderBy(VRP_SOLVER_REQUEST.UPDATED_AT.desc())
            .limit(1)
            .fetchOne {
                VrpSolverRequest(it.requestKey, it.vrpProblemId, it.solver, SolverState.valueOf(it.status))
            }
    }

    override fun updateSolverStatus(solverKey: UUID, solverState: SolverState) {
        dsl.update(VRP_SOLVER_REQUEST)
            .set(VRP_SOLVER_REQUEST.STATUS, solverState.name)
            .set(VRP_SOLVER_REQUEST.UPDATED_AT, Instant.now())
            .where(VRP_SOLVER_REQUEST.REQUEST_KEY.eq(solverKey))
            .execute()
    }
}