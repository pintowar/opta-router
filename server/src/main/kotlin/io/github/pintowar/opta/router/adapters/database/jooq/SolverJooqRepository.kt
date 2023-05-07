package io.github.pintowar.opta.router.adapters.database.jooq

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import io.github.pintowar.opta.router.core.domain.ports.SolverRepository
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.generated.public.tables.references.SOLUTION
import java.time.Instant
import java.util.*

class SolverJooqRepository(
    private val mapper: ObjectMapper,
    private val dsl: DSLContext,
    private val solutionRepo: SolutionRepository
) : SolverRepository {

    override fun listAllSolutionIds(): Set<Long> {
        return dsl.select(SOLUTION).from(SOLUTION).fetch { (it) -> it.id!! }.toSet()
    }

    override fun addNewSolution(sol: VrpSolution, uuid: UUID, solverState: SolverState) {
        createNewSolutionFromInstance(sol.instance, solverState, sol.routes, uuid)
    }

    override fun currentOrNewSolutionRegistry(instanceId: Long): VrpSolutionRegistry? {
        return solutionRepo.getByInstanceId(instanceId)?.instance?.let { instance ->
            currentSolution(instance) ?: createNewSolutionFromInstance(instance)
        }
    }

    override fun currentMatrix(instanceId: Long): Matrix? {
        return solutionRepo.getByMatrixInstanceId(instanceId)
    }

    override fun clearSolution(instanceId: Long) {
        createNewSolution(instanceId)
    }

    private fun currentSolution(instance: RouteInstance) = dsl.selectFrom(SOLUTION)
        .where(SOLUTION.ROUTE_ID.eq(instance.id))
        .orderBy(SOLUTION.UPDATED_AT.desc())
        .limit(1)
        .fetchOne()
        ?.let { sol ->
            val routes = mapper.readValue<List<Route>>(sol.paths.data())
            VrpSolutionRegistry(VrpSolution(instance, routes), SolverState.valueOf(sol.status))
        }

    private fun createNewSolution(
        instanceId: Long,
        solverState: SolverState = SolverState.NOT_SOLVED,
        paths: List<Route> = emptyList(),
        uuid: UUID? = null,
    ) {
        val now = Instant.now()
        dsl.insertInto(SOLUTION)
            .set(SOLUTION.SOLVER_KEY, uuid)
            .set(SOLUTION.ROUTE_ID, instanceId)
            .set(SOLUTION.STATUS, solverState.name)
            .set(SOLUTION.PATHS, JSON.json(mapper.writeValueAsString(paths)))
            .set(SOLUTION.CREATED_AT, now)
            .set(SOLUTION.UPDATED_AT, now)
            .execute()
    }

    private fun createNewSolutionFromInstance(
        instance: RouteInstance,
        solverState: SolverState = SolverState.NOT_SOLVED,
        paths: List<Route> = emptyList(),
        uuid: UUID? = null,
    ): VrpSolutionRegistry {
        createNewSolution(instance.id, solverState, paths, uuid)
        return VrpSolutionRegistry(VrpSolution(instance, paths), solverState, uuid)
    }

}