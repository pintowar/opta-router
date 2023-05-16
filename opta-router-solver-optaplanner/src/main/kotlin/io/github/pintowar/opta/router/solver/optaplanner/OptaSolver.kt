package io.github.pintowar.opta.router.solver.optaplanner

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.SolverConfig
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.SolverConfig as SC
import org.optaplanner.core.config.solver.termination.TerminationConfig
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import java.util.UUID

class OptaSolver(key: UUID, name: String, config: SolverConfig) : Solver(key, name, config) {
    val configPath = "org/optaplanner/examples/vehiclerouting/vehicleRoutingSolverConfig.xml"
    val solverConfig = SC.createFromXmlResource(configPath).apply {
        terminationConfig = TerminationConfig().apply {
            overwriteSpentLimit(config.timeLimit)
        }
    }
    val solverFactory = SolverFactory.create<VehicleRoutingSolution>(solverConfig)
    val solver = solverFactory.buildSolver()

    override fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRegistry) -> Unit) {
        val problem = initialSolution.problem

        solver.addEventListener { evt ->
            val sol = evt.newBestSolution
            callback(VrpSolutionRegistry(sol.toDTO(problem, matrix), SolverStatus.RUNNING, key))
        }

        callback(VrpSolutionRegistry(initialSolution, SolverStatus.RUNNING, key))
        val sol = solver.solve(initialSolution.toSolverSolution(matrix))
        callback(VrpSolutionRegistry(sol.toDTO(problem, matrix), SolverStatus.TERMINATED, key))
    }

    override fun terminate() {
        solver.terminateEarly()
    }
}