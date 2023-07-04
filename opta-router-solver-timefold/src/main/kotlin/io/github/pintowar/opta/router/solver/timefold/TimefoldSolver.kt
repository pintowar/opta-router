package io.github.pintowar.opta.router.solver.timefold

import ai.timefold.solver.core.api.solver.SolverFactory
import ai.timefold.solver.core.config.solver.termination.TerminationConfig
import ai.timefold.solver.examples.vehiclerouting.domain.VehicleRoutingSolution
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import java.util.*
import ai.timefold.solver.core.config.solver.SolverConfig as SC

class TimefoldSolver(key: UUID, name: String, config: SolverConfig) : Solver(key, name, config) {
    private val configPath = "ai/timefold/solver/examples/vehiclerouting/vehicleRoutingSolverConfig.xml"
    private val solverConfig = SC.createFromXmlResource(configPath).apply {
        terminationConfig = TerminationConfig().apply {
            overwriteSpentLimit(config.timeLimit)
        }
    }
    private val solverFactory = SolverFactory.create<VehicleRoutingSolution>(solverConfig)
    private val solver = solverFactory.buildSolver()

    override fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit) {
        val problem = initialSolution.problem

        solver.addEventListener { evt ->
            val sol = evt.newBestSolution
            callback(VrpSolutionRequest(sol.toDTO(problem, matrix), SolverStatus.RUNNING, key))
        }

        callback(VrpSolutionRequest(initialSolution, SolverStatus.RUNNING, key))
        val sol = solver.solve(initialSolution.toSolverSolution(matrix))
        callback(VrpSolutionRequest(sol.toDTO(problem, matrix), SolverStatus.TERMINATED, key))
    }

    override fun terminate() {
        solver.terminateEarly()
    }

    override fun isSolving() = solver.isSolving
}