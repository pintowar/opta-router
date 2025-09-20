package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
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
    override suspend fun refreshCreatedSolverRequests(timeout: Duration): Int =
        terminateByStatusRequests(SolverStatus.CREATED, timeout)

    override suspend fun refreshRunningSolverRequests(timeout: Duration): Int =
        terminateByStatusRequests(SolverStatus.RUNNING, timeout)

    override suspend fun createRequest(request: VrpSolverRequest): VrpSolverRequest? {
        val (numEnqueued) =
            dsl
                .selectCount()
                .from(VRP_SOLVER_REQUEST)
                .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(request.problemId))
                .and(
                    VRP_SOLVER_REQUEST.STATUS.`in`(
                        SolverStatus.CREATED.name,
                        SolverStatus.ENQUEUED.name,
                        SolverStatus.RUNNING.name
                    )
                ).awaitSingle()

        if (numEnqueued > 0) return null

        val now = Instant.now()
        val result =
            dsl
                .insertInto(VRP_SOLVER_REQUEST)
                .set(VRP_SOLVER_REQUEST.REQUEST_KEY, request.requestKey)
                .set(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID, request.problemId)
                .set(VRP_SOLVER_REQUEST.SOLVER, request.solver)
                .set(VRP_SOLVER_REQUEST.STATUS, request.status.name)
                .set(VRP_SOLVER_REQUEST.CREATED_AT, now)
                .set(VRP_SOLVER_REQUEST.UPDATED_AT, now)
                .returning()
                .awaitFirstOrNull()

        return request.takeIf { result != null }
    }

    override suspend fun enqueueRequest(solverKey: UUID) {
        dsl
            .update(VRP_SOLVER_REQUEST)
            .set(VRP_SOLVER_REQUEST.STATUS, SolverStatus.ENQUEUED.name)
            .where(VRP_SOLVER_REQUEST.STATUS.eq(SolverStatus.CREATED.name))
            .and(VRP_SOLVER_REQUEST.REQUEST_KEY.eq(solverKey))
            .awaitSingle()
    }

    override suspend fun currentSolverRequest(problemId: Long): VrpSolverRequest? =
        dsl
            .selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(problemId))
            .orderBy(VRP_SOLVER_REQUEST.UPDATED_AT.desc())
            .limit(1)
            .awaitFirstOrNull()
            ?.let {
                VrpSolverRequest(it.requestKey, it.vrpProblemId, it.solver, SolverStatus.valueOf(it.status))
            }

    override suspend fun currentSolverRequest(solverKey: UUID): VrpSolverRequest? =
        dsl
            .selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.REQUEST_KEY.eq(solverKey))
            .orderBy(VRP_SOLVER_REQUEST.UPDATED_AT.desc())
            .limit(1)
            .awaitFirstOrNull()
            ?.let {
                VrpSolverRequest(it.requestKey, it.vrpProblemId, it.solver, SolverStatus.valueOf(it.status))
            }

    override fun requestsByProblemIdAndSolverName(
        problemId: Long,
        solverName: String
    ): Flow<VrpSolverRequest> =
        dsl
            .selectFrom(VRP_SOLVER_REQUEST)
            .where(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(problemId))
            .and(VRP_SOLVER_REQUEST.SOLVER.eq(solverName))
            .orderBy(VRP_SOLVER_REQUEST.CREATED_AT)
            .asFlow()
            .map {
                VrpSolverRequest(it.requestKey, it.vrpProblemId, it.solver, SolverStatus.valueOf(it.status))
            }

    private suspend fun terminateByStatusRequests(
        status: SolverStatus,
        timeout: Duration
    ): Int =
        dsl
            .update(VRP_SOLVER_REQUEST)
            .set(VRP_SOLVER_REQUEST.STATUS, SolverStatus.TERMINATED.name)
            .where(VRP_SOLVER_REQUEST.STATUS.eq(status.name))
            .and(VRP_SOLVER_REQUEST.UPDATED_AT.lt(Instant.now() - timeout))
            .awaitSingle()
}