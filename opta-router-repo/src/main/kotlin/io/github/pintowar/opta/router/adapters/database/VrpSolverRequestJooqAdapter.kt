package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.generated.tables.references.VRP_SOLVER_REQUEST
import java.time.Duration
import java.time.Instant
import java.util.UUID

class VrpSolverRequestJooqAdapter(
    private val dsl: DSLContext
) : VrpSolverRequestPort {

    override suspend fun refreshSolverRequests(timeout: Duration): Int {
        return dsl
            .update(VRP_SOLVER_REQUEST)
            .set(VRP_SOLVER_REQUEST.STATUS, SolverStatus.TERMINATED.name)
            .where(VRP_SOLVER_REQUEST.STATUS.eq(SolverStatus.RUNNING.name))
            .and(VRP_SOLVER_REQUEST.UPDATED_AT.lt(Instant.now() - timeout))
            .awaitSingle()
//            .execute()
    }

    override suspend fun createRequest(request: VrpSolverRequest): VrpSolverRequest? {
        val numEnqueued = dsl.selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(request.problemId))
            .and(VRP_SOLVER_REQUEST.STATUS.`in`(SolverStatus.ENQUEUED.name, SolverStatus.RUNNING.name))
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
            .awaitSingle()
//            .execute()

        return if (result == 1) request else null
    }

    override suspend fun currentSolverRequest(problemId: Long): VrpSolverRequest? {
        return dsl.selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(problemId))
            .orderBy(VRP_SOLVER_REQUEST.UPDATED_AT.desc())
            .limit(1)
            .awaitFirstOrNull()
            ?.let {
                VrpSolverRequest(it.requestKey, it.vrpProblemId, it.solver, SolverStatus.valueOf(it.status))
            }
    }

    override suspend fun currentSolverRequest(solverKey: UUID): VrpSolverRequest? {
        return dsl.selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.REQUEST_KEY.eq(solverKey))
            .orderBy(VRP_SOLVER_REQUEST.UPDATED_AT.desc())
            .limit(1)
            .awaitFirstOrNull()
            ?.let {
                VrpSolverRequest(it.requestKey, it.vrpProblemId, it.solver, SolverStatus.valueOf(it.status))
            }
    }

    override fun requestsByProblemIdAndSolverName(problemId: Long, solverName: String): Flow<VrpSolverRequest> {
        return dsl.selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(problemId))
            .and(VRP_SOLVER_REQUEST.SOLVER.eq(solverName))
            .orderBy(VRP_SOLVER_REQUEST.CREATED_AT)
            .asFlow()
            .map {
                VrpSolverRequest(it.requestKey, it.vrpProblemId, it.solver, SolverStatus.valueOf(it.status))
            }
    }
}