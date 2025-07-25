package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.jenetics.EliteSelector
import io.jenetics.PartiallyMatchedCrossover
import io.jenetics.RouletteWheelSelector
import io.jenetics.SwapMutator
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.Limits
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive

class JeneticsSolver : Solver() {
    override val name: String = "jenetics"

    /**
     * Builds and configures a Jenetics [Engine] for solving the VRP problem.
     *
     * The engine is configured with various genetic algorithm components such as selectors, alterers (crossover and mutators),
     * population size, and phenotype age. It is set to minimize the fitness function (total distance).
     *
     * @param problem The [VrpProblem] to be solved.
     * @param matrix The [Matrix] containing travel distances between locations.
     * @return A configured Jenetics [Engine] instance.
     */
    private fun buildEngine(
        problem: VrpProblem,
        matrix: Matrix
    ) = Engine
        .builder(problem.toProblem(matrix))
        .minimizing()
        .survivorsSelector(EliteSelector(5))
        .offspringSelector(RouletteWheelSelector())
        .offspringFraction(0.8)
        .maximalPhenotypeAge(100)
        .populationSize(500)
        .alterers(
            PartiallyMatchedCrossover(0.8),
            SwapMutator(0.05),
            ReverseMutator(0.05)
        ).build()

    /**
     * Solves the VRP problem using the Jenetics genetic algorithm and emits solutions as they are found.
     *
     * This function creates an evolutionary engine and streams the evolution results. It emits the best solution
     * found at each step of the evolution until the time limit is reached or the coroutine is cancelled.
     *
     * @param initialSolution The initial [VrpSolution] to start the solver from. If empty, the solver starts from a random initial population.
     * @param matrix The [Matrix] containing travel distances between locations.
     * @param config The [SolverConfig] containing parameters like the time limit for the solver.
     * @return A [Flow] of [VrpSolution] objects, representing the best solution found at different stages of the evolution.
     */
    override fun solveFlow(
        initialSolution: VrpSolution,
        matrix: Matrix,
        config: SolverConfig
    ): Flow<VrpSolution> {
        val engine = buildEngine(initialSolution.problem, matrix)

        return callbackFlow {
            val ctx = currentCoroutineContext()
            val emptySol = initialSolution.isEmpty()
            val evoStream = if (emptySol) engine.stream() else engine.stream(initialSolution.toInitialSolution())
            val result =
                evoStream
                    .limit(Limits.byExecutionTime(config.timeLimit))
                    .limit { ctx.isActive }
                    .peek { result ->
                        val actual = result.bestPhenotype().genotype().toDto(initialSolution.problem, matrix)
                        trySendBlocking(actual)
                    }.collect(EvolutionResult.toBestGenotype())
            send(result.toDto(initialSolution.problem, matrix))
            close()
        }
    }
}