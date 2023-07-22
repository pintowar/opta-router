package io.github.pintowar.opta.router.solver.jsprit

import com.graphhopper.jsprit.core.algorithm.SearchStrategy
import com.graphhopper.jsprit.core.algorithm.box.Jsprit
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener
import com.graphhopper.jsprit.core.algorithm.termination.PrematureAlgorithmTermination
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution
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

class JspritSolver : Solver {

    override val name: String = "jsprit"

    internal class TimeTermination(private val totalTimeMs: Duration) : PrematureAlgorithmTermination {
        private val beginning = System.currentTimeMillis()

        override fun isPrematureBreak(discoveredSolution: SearchStrategy.DiscoveredSolution) =
            System.currentTimeMillis() - beginning >= totalTimeMs.toMillis()
    }

    override fun solutionFlow(initialSolution: VrpSolution, matrix: Matrix, config: SolverConfig): Flow<VrpSolution> {
        return callbackFlow {
            val ctx = currentCoroutineContext()

            val initialProblem = initialSolution.problem
            val vrp = initialProblem.toProblem(matrix)
            val algorithm = Jsprit.Builder
                .newInstance(vrp)
                .setProperty(Jsprit.Parameter.ITERATIONS.toString(), "${config.timeLimit.toSeconds() * 70}")
                .buildAlgorithm()

            algorithm.addInitialSolution(initialSolution.toSolverSolution(vrp))
            algorithm.addListener(object : IterationEndsListener {
                override fun informIterationEnds(
                    i: Int,
                    problem: VehicleRoutingProblem,
                    solutions: MutableCollection<VehicleRoutingProblemSolution>
                ) {
                    val actual = Solutions.bestOf(solutions).toDTO(initialProblem, matrix)
                    trySendBlocking(actual)
                }
            })
            algorithm.addTerminationCriterion(TimeTermination(config.timeLimit))
            algorithm.addTerminationCriterion { !ctx.isActive }

            val solutions = algorithm.searchSolutions()
            val result = Solutions.bestOf(solutions).toDTO(initialProblem, matrix)
            send(result)
            close()
        }
    }
}