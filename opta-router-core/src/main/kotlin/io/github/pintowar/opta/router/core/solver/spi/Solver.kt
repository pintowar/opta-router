package io.github.pintowar.opta.router.core.solver.spi

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.scan
import java.util.*

abstract class Solver {

    companion object {

        fun getNamedSolvers(): Map<String, Solver> {
            val solverFactories = mutableMapOf<String, Solver>()
            ServiceLoader.load(Solver::class.java)
                .iterator()
                .forEachRemaining { solverFactories[it.name] = it }
            return solverFactories
        }

        fun getSolverByName(solverName: String): Solver = getNamedSolvers()[solverName]
            ?: throw IllegalArgumentException("No solver $solverName was found")
    }

    abstract val name: String

    fun solve(initialSolution: VrpSolution, matrix: Matrix, config: SolverConfig): Flow<VrpSolution> =
        solveFlow(initialSolution, matrix, config)
            .scan(initialSolution) { acc, sol ->
                if (!acc.isEmpty() && sol.getTotalDistance() > acc.getTotalDistance()) acc else sol
            }
            .filterNot { it.isEmpty() }
            .distinctUntilChangedBy { it.getTotalDistance() }

    protected abstract fun solveFlow(
        initialSolution: VrpSolution,
        matrix: Matrix,
        config: SolverConfig
    ): Flow<VrpSolution>
}