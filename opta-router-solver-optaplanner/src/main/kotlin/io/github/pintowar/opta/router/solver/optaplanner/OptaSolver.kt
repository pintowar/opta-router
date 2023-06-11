package io.github.pintowar.opta.router.solver.optaplanner

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.termination.TerminationConfig
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import java.util.UUID
import org.optaplanner.core.config.solver.SolverConfig as SC

class OptaSolver(key: UUID, name: String, config: SolverConfig) : Solver(key, name, config) {
    private val configPath = "org/optaplanner/examples/vehiclerouting/vehicleRoutingSolverConfig.xml"
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