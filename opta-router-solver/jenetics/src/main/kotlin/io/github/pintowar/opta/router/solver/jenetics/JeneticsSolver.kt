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

    private fun buildEngine(
        problem: VrpProblem,
        matrix: Matrix
    ) = Engine.builder(problem.toProblem(matrix))
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
        )
        .build()

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
                    }
                    .collect(EvolutionResult.toBestGenotype())
            send(result.toDto(initialSolution.problem, matrix))
            close()
        }
    }
}