package io.github.pintowar.opta.router.core.solver.spi

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.scan
import java.util.*

/**
 * The Solver class is an abstract base class for all VRP solvers.
 * It provides a common interface for solving VRP problems and for discovering available solver implementations.
 */
abstract class Solver {
    companion object {
        /**
         * Retrieves a map of all available solvers, where the key is the solver's name and the value is the solver instance.
         *
         * @return A map of named solvers.
         */
        fun getNamedSolvers(): Map<String, Solver> {
            val solverFactories = mutableMapOf<String, Solver>()
            ServiceLoader
                .load(Solver::class.java)
                .iterator()
                .forEachRemaining { solverFactories[it.name] = it }
            return solverFactories
        }

        /**
         * Retrieves a solver by its name.
         *
         * @param solverName The name of the solver to retrieve.
         * @return The solver instance.
         * @throws IllegalArgumentException if no solver with the given name is found.
         */
        fun getSolverByName(solverName: String): Solver =
            getNamedSolvers()[solverName]
                ?: throw IllegalArgumentException("No solver $solverName was found")
    }

    /**
     * The name of the solver.
     */
    abstract val name: String

    /**
     * Solves a VRP problem and returns a flow of solutions.
     * The flow will only emit solutions that are better than the previous one.
     *
     * @param initialSolution The initial solution to start the solver from.
     * @param matrix The travel matrix for the problem.
     * @param config The configuration for the solver.
     * @return A [Flow] of [VrpSolution]s.
     */
    fun solve(
        initialSolution: VrpSolution,
        matrix: Matrix,
        config: SolverConfig
    ): Flow<VrpSolution> =
        solveFlow(initialSolution, matrix, config)
            .scan(initialSolution) { acc, sol ->
                if (!acc.isEmpty() && sol.getTotalDistance() > acc.getTotalDistance()) acc else sol
            }.filterNot { it.isEmpty() }
            .distinctUntilChangedBy { it.getTotalDistance() }

    /**
     * The main logic for the solver, which should be implemented by subclasses.
     *
     * @param initialSolution The initial solution to start the solver from.
     * @param matrix The travel matrix for the problem.
     * @param config The configuration for the solver.
     * @return A [Flow] of [VrpSolution]s.
     */
    protected abstract fun solveFlow(
        initialSolution: VrpSolution,
        matrix: Matrix,
        config: SolverConfig
    ): Flow<VrpSolution>
}