package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.spi.SolverFactory
import java.util.UUID

class JeneticsFactory : SolverFactory {

    override val name: String = "jenetics"

    override fun createSolver(key: UUID, config: SolverConfig): Solver {
        return JeneticsSolver(key, name, config)
    }
}