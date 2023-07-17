package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.SolutionFlow
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.jenetics.EliteSelector
import io.jenetics.PartiallyMatchedCrossover
import io.jenetics.RouletteWheelSelector
import io.jenetics.SwapMutator
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.Limits
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class JeneticsSolver(key: UUID, name: String, config: SolverConfig) : Solver(key, name, config) {

    private fun buildEngine(problem: VrpProblem, matrix: Matrix) = Engine.builder(problem.toProblem(matrix))
        .minimizing()
        .survivorsSelector(EliteSelector(5))
        .offspringSelector(RouletteWheelSelector())
        .offspringFraction(0.8)
        .maximalPhenotypeAge(100)
        .populationSize(500)
        .alterers(
            PartiallyMatchedCrossover(0.8),
            SwapMutator(0.05)
        )
        .build()

    override fun solutionFlow(initialSolution: VrpSolution, matrix: Matrix, config: SolverConfig): Flow<SolutionFlow> {
        val engine = buildEngine(initialSolution.problem, matrix)

        return callbackFlow {
            val ctx = currentCoroutineContext()
            val emptySol = initialSolution.isEmpty()
            val evoStream = if (emptySol) engine.stream() else engine.stream(initialSolution.toInitialSolution())
            val result = evoStream
                .limit(Limits.byExecutionTime(config.timeLimit))
                .limit { ctx.isActive }
                .peek { result ->
                    val actual = result.bestPhenotype().genotype().toDto(initialSolution.problem, matrix)
                    trySendBlocking(SolutionFlow(actual))
                }
                .collect(EvolutionResult.toBestGenotype())
            val actual = result.toDto(initialSolution.problem, matrix)
            send(SolutionFlow(actual, true))
            close()
        }
    }

    private val running = AtomicBoolean(false)

    override fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit) {
        var best = initialSolution
        val engine = buildEngine(initialSolution.problem, matrix)

        running.set(true)
        val emptySol = initialSolution.isEmpty()
        if (!emptySol) callback(VrpSolutionRequest(initialSolution, SolverStatus.RUNNING, key))
        val evoStream = if (emptySol) engine.stream() else engine.stream(initialSolution.toInitialSolution())
        evoStream
            .limit(Limits.byExecutionTime(config.timeLimit))
            .limit { running.get() }
            .forEach { result ->
                val actual = result.bestPhenotype().genotype().toDto(initialSolution.problem, matrix)
                if (best.isEmpty() || actual.getTotalDistance() < best.getTotalDistance()) {
                    best = actual
                    callback(VrpSolutionRequest(best, SolverStatus.RUNNING, key))
                }
            }
//            .collect(EvolutionResult.toBestGenotype())
        callback(VrpSolutionRequest(best, SolverStatus.TERMINATED, key))
        running.set(false)
    }

    override fun terminate() {
        running.set(false)
    }

    override fun isSolving() = running.get()
}