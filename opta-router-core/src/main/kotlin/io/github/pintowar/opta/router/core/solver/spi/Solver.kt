package io.github.pintowar.opta.router.core.solver.spi

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolutionFlow
import io.github.pintowar.opta.router.core.solver.SolverConfig
import kotlinx.coroutines.flow.Flow
import java.util.*

interface Solver {

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

    val name: String

    fun solutionFlow(initialSolution: VrpSolution, matrix: Matrix, config: SolverConfig): Flow<SolutionFlow>

}