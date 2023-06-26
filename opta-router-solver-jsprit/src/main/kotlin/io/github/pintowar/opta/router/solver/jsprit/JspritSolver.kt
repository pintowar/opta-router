package io.github.pintowar.opta.router.solver.jsprit

import com.graphhopper.jsprit.core.algorithm.SearchStrategy
import com.graphhopper.jsprit.core.algorithm.box.Jsprit
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener
import com.graphhopper.jsprit.core.algorithm.termination.PrematureAlgorithmTermination
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution
import com.graphhopper.jsprit.core.util.Solutions
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import java.time.Duration
import java.util.*


class JspritSolver(key: UUID, name: String, config: SolverConfig) : Solver(key, name, config) {

    @Volatile
    private var running = false

    internal class TimeTermination(private val totalTimeMs: Duration) : PrematureAlgorithmTermination {
        private val beginning = System.currentTimeMillis()

        override fun isPrematureBreak(discoveredSolution: SearchStrategy.DiscoveredSolution) =
            System.currentTimeMillis() - beginning >= totalTimeMs.toMillis()
    }

    override fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit) {
        val initialProblem = initialSolution.problem
        val vrp = initialProblem.toProblem(matrix)
        var best = initialSolution
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
                if (best.isEmpty() || actual.getTotalDistance() < best.getTotalDistance())
                    best = actual
                callback(VrpSolutionRequest(best, SolverStatus.RUNNING, key))
            }
        })
        algorithm.addTerminationCriterion(TimeTermination(config.timeLimit))
        algorithm.addTerminationCriterion { !running }
        running = true
        val solutions = algorithm.searchSolutions()
        best = Solutions.bestOf(solutions).toDTO(initialProblem, matrix)
        callback(VrpSolutionRequest(best, SolverStatus.TERMINATED, key))
    }

    override fun terminate() {
        running = false
    }

    override fun isSolving() = running
}