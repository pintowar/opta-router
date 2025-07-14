package io.github.pintowar.opta.router.solver.jsprit

import com.graphhopper.jsprit.core.algorithm.SearchStrategy
import com.graphhopper.jsprit.core.algorithm.box.Jsprit
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener
import com.graphhopper.jsprit.core.algorithm.termination.PrematureAlgorithmTermination
import com.graphhopper.jsprit.core.util.Solutions
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import java.time.Duration

class JspritSolver : Solver() {
    override val name: String = "jsprit"

    /**
     * A termination criterion for jsprit algorithms based on a time limit.
     *
     * This class implements [PrematureAlgorithmTermination] to stop the jsprit solver
     * once a specified total time duration has elapsed since the algorithm's start.
     *
     * @property totalTimeMs The maximum [Duration] for which the algorithm is allowed to run.
     */
    internal class TimeTermination(
        private val totalTimeMs: Duration
    ) : PrematureAlgorithmTermination {
        private val beginning = System.currentTimeMillis()

        /**
         * Checks if the algorithm should be prematurely terminated.
         *
         * @param discoveredSolution The currently discovered solution (not used in this time-based termination).
         * @return `true` if the elapsed time exceeds `totalTimeMs`, `false` otherwise.
         */
        override fun isPrematureBreak(discoveredSolution: SearchStrategy.DiscoveredSolution) =
            System.currentTimeMillis() - beginning >= totalTimeMs.toMillis()
    }

    /**
     * Solves the VRP problem using the jsprit library and emits solutions as they are found.
     *
     * This function sets up a jsprit algorithm, optionally initializes it with a given solution,
     * and adds listeners to emit the best solution found at the end of each iteration.
     * The solver runs until a specified time limit is reached or the coroutine is cancelled.
     *
     * @param initialSolution The initial [VrpSolution] to start the solver from.
     * @param matrix The [Matrix] containing travel distances between locations.
     * @param config The [SolverConfig] containing parameters like the time limit for the solver.
     * @return A [Flow] of [VrpSolution] objects, representing the best solution found at different stages of the solving process.
     */
    override fun solveFlow(
        initialSolution: VrpSolution,
        matrix: Matrix,
        config: SolverConfig
    ): Flow<VrpSolution> =
        callbackFlow {
            val ctx = currentCoroutineContext()

            val initialProblem = initialSolution.problem
            val vrp = initialProblem.toProblem(matrix)
            val algorithm =
                Jsprit.Builder
                    .newInstance(vrp)
                    .setProperty(Jsprit.Parameter.ITERATIONS.toString(), "${config.timeLimit.toSeconds() * 70}")
                    .buildAlgorithm()

            algorithm.addInitialSolution(initialSolution.toSolverSolution(vrp))
            algorithm.addListener(
                IterationEndsListener { _, _, solutions ->
                    val actual = Solutions.bestOf(solutions).toDTO(initialProblem, matrix)
                    trySendBlocking(actual)
                }
            )
            algorithm.addTerminationCriterion(TimeTermination(config.timeLimit))
            algorithm.addTerminationCriterion { !ctx.isActive }

            val solutions = algorithm.searchSolutions()
            val result = Solutions.bestOf(solutions).toDTO(initialProblem, matrix)
            send(result)
            close()
        }
}