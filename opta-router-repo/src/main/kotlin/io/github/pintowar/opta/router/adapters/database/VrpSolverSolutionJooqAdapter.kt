package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.VrpSolverObjective
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.serialization.Serde
import io.github.pintowar.opta.router.core.serialization.fromJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.Record3
import org.jooq.generated.tables.records.VrpProblemRecord
import org.jooq.generated.tables.records.VrpSolutionRecord
import org.jooq.generated.tables.records.VrpSolverRequestRecord
import org.jooq.generated.tables.references.VRP_PROBLEM
import org.jooq.generated.tables.references.VRP_SOLUTION
import org.jooq.generated.tables.references.VRP_SOLVER_REQUEST
import org.jooq.generated.tables.references.VRP_SOLVER_SOLUTION
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.Instant
import java.util.*

class VrpSolverSolutionJooqAdapter(
    private val serde: Serde,
    private val dsl: DSLContext
) : VrpSolverSolutionPort {
    override suspend fun currentSolution(problemId: Long): List<Route> =
        dsl
            .selectFrom(VRP_SOLUTION)
            .where(VRP_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
            .limit(1)
            .awaitFirstOrNull()
            ?.let { sol ->
                serde.fromJson(sol.paths.data())
            } ?: emptyList()

    override suspend fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? =
        currentSolutionRequestQuery(dsl, problemId)
            .awaitFirstOrNull()
            ?.let(::convertRecordToSolutionRequest)

    override suspend fun upsertSolution(
        problemId: Long,
        solverStatus: SolverStatus,
        paths: List<Route>,
        objective: Double,
        clear: Boolean,
        uuid: UUID
    ): VrpSolutionRequest {
        val now = Instant.now()
        val jsonPaths = if (clear) JSON.json("[]") else JSON.json(serde.toJson(paths))

        return dsl.transactionCoroutine { trx ->
            trx
                .dsl()
                .update(VRP_SOLVER_REQUEST)
                .set(VRP_SOLVER_REQUEST.STATUS, if (clear) SolverStatus.NOT_SOLVED.name else solverStatus.name)
                .set(VRP_SOLVER_REQUEST.UPDATED_AT, now)
                .where(VRP_SOLVER_REQUEST.REQUEST_KEY.eq(uuid))
                .awaitSingle()

            val (numSolutions) =
                dsl
                    .selectCount()
                    .from(VRP_SOLUTION)
                    .where(VRP_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
                    .awaitSingle()

            if (numSolutions == 0) {
                trx
                    .dsl()
                    .insertInto(VRP_SOLUTION)
                    .set(VRP_SOLUTION.VRP_PROBLEM_ID, problemId)
                    .set(VRP_SOLUTION.PATHS, jsonPaths)
                    .set(VRP_SOLUTION.CREATED_AT, now)
                    .set(VRP_SOLUTION.UPDATED_AT, now)
                    .awaitSingle()
            } else {
                trx
                    .dsl()
                    .update(VRP_SOLUTION)
                    .set(VRP_SOLUTION.PATHS, jsonPaths)
                    .set(VRP_SOLUTION.UPDATED_AT, now)
                    .where(VRP_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
                    .awaitSingle()
            }

            if (!clear) {
                trx
                    .dsl()
                    .insertInto(VRP_SOLVER_SOLUTION)
                    .set(VRP_SOLVER_SOLUTION.REQUEST_KEY, uuid)
                    .set(VRP_SOLVER_SOLUTION.VRP_PROBLEM_ID, problemId)
                    .set(VRP_SOLVER_SOLUTION.STATUS, solverStatus.name)
                    .set(VRP_SOLVER_SOLUTION.OBJECTIVE, objective)
                    .set(VRP_SOLVER_SOLUTION.PATHS, jsonPaths)
                    .set(VRP_SOLVER_SOLUTION.CREATED_AT, now)
                    .set(VRP_SOLVER_SOLUTION.UPDATED_AT, now)
                    .awaitSingle()
            }

            currentSolutionRequestQuery(trx.dsl(), problemId)
                .awaitSingle()
                .let(::convertRecordToSolutionRequest)
        }
    }

    override fun solutionHistory(problemId: Long): Flow<VrpSolverObjective> =
        dsl
            .select(VRP_SOLVER_SOLUTION, VRP_SOLVER_REQUEST)
            .from(
                VRP_SOLVER_SOLUTION
                    .leftJoin(VRP_SOLVER_REQUEST)
                    .on(VRP_SOLVER_SOLUTION.REQUEST_KEY.eq(VRP_SOLVER_REQUEST.REQUEST_KEY))
            ).where(VRP_SOLVER_SOLUTION.VRP_PROBLEM_ID.eq(problemId))
            .orderBy(VRP_SOLVER_SOLUTION.CREATED_AT)
            .asFlow()
            .map { (sol, req) ->
                VrpSolverObjective(
                    sol.objective,
                    req.solver,
                    SolverStatus.valueOf(sol.status),
                    sol.requestKey!!,
                    sol.createdAt
                )
            }

    /**
     * Constructs a JOOQ query to retrieve the current VRP solution request along with problem and solver request details.
     *
     * This is a private helper function used to build the common query for fetching solution request information.
     *
     * @param dsl The [DSLContext] for building the query.
     * @param problemId The ID of the VRP problem.
     * @return A [org.jooq.SelectLimitStep] representing the JOOQ query.
     */
    private fun currentSolutionRequestQuery(
        dsl: DSLContext,
        problemId: Long
    ) = dsl
        .select(VRP_PROBLEM, VRP_SOLUTION, VRP_SOLVER_REQUEST)
        .from(VRP_PROBLEM)
        .leftJoin(VRP_SOLUTION)
        .on(VRP_SOLUTION.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID))
        .leftJoin(VRP_SOLVER_REQUEST)
        .on(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID.eq(VRP_PROBLEM.ID))
        .where(VRP_PROBLEM.ID.eq(problemId))
        .orderBy(VRP_SOLVER_REQUEST.UPDATED_AT.desc())
        .limit(1)

    /**
     * Converts a JOOQ [Record3] containing VRP problem, solution, and solver request records into a [VrpSolutionRequest] domain object.
     *
     * This private helper function deserializes JSON data from the database records to construct the domain objects.
     *
     * @param record The [Record3] containing [VrpProblemRecord], [VrpSolutionRecord], and [VrpSolverRequestRecord].
     * @return The converted [VrpSolutionRequest] object.
     */
    private fun convertRecordToSolutionRequest(
        record: Record3<VrpProblemRecord, VrpSolutionRecord, VrpSolverRequestRecord>
    ): VrpSolutionRequest {
        val (problem, solution, solverRequest) = record

        return VrpSolutionRequest(
            VrpSolution(
                VrpProblem(
                    problem.id!!,
                    problem.name,
                    serde.fromJson(problem.vehicles.data()),
                    serde.fromJson(problem.customers.data())
                ),
                solution.get(VRP_SOLUTION.PATHS)?.let { serde.fromJson(it.data()) } ?: emptyList()
            ),
            solverRequest.get(VRP_SOLVER_REQUEST.STATUS)?.let(SolverStatus::valueOf) ?: SolverStatus.NOT_SOLVED,
            solverRequest.get(VRP_SOLVER_REQUEST.REQUEST_KEY)
        )
    }
}