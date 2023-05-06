package io.github.pintowar.opta.router.adapters.database.jooq

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import io.github.pintowar.opta.router.core.domain.ports.SolverRepository
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.generated.public.tables.references.SOLUTION

class SolverJooqRepository(
    private val mapper: ObjectMapper,
    private val dsl: DSLContext,
    private val solutionRepo: SolutionRepository
) : SolverRepository {

    override fun listAllSolutionIds(): Set<Long> {
        return dsl.select(SOLUTION).from(SOLUTION).fetch { (it) -> it.id!! }.toSet()
    }

    override fun updateSolution(sol: VrpSolution, solverState: SolverState) {
        dsl.update(SOLUTION)
            .set(SOLUTION.PATHS, JSON.json(mapper.writeValueAsString(sol.routes)))
            .set(SOLUTION.STATUS, solverState.name)
            .where(SOLUTION.ROUTE_ID.eq(sol.instance.id))
            .execute()
    }

    override fun updateStatus(instanceId: Long, solverState: SolverState) {
        dsl.update(SOLUTION)
            .set(SOLUTION.STATUS, solverState.name)
            .where(SOLUTION.ROUTE_ID.eq(instanceId))
            .execute()
    }

    override fun currentSolutionState(instanceId: Long): VrpSolutionState? {
        return solutionRepo.getByInstanceId(instanceId)?.instance?.let { instance ->
            dsl.selectFrom(SOLUTION)
                .where(SOLUTION.ROUTE_ID.eq(instanceId))
                .orderBy(SOLUTION.UPDATED_AT.desc())
                .limit(1)
                .fetchOne()
                ?.let { sol ->
                    val routes = mapper.readValue<List<Route>>(sol.paths.data())

                    VrpSolutionState(VrpSolution(instance, routes), SolverState.valueOf(sol.status))
                }
        }
    }

    override fun currentMatrix(instanceId: Long): Matrix? {
        return solutionRepo.getByMatrixInstanceId(instanceId)
    }

    override fun clearSolution(instanceId: Long) {
        dsl.update(SOLUTION)
            .set(SOLUTION.PATHS, JSON.json(mapper.writeValueAsString(emptyList<Route>())))
            .set(SOLUTION.STATUS, SolverState.NOT_SOLVED.name)
            .where(SOLUTION.ROUTE_ID.eq(instanceId))
            .execute()
    }
}