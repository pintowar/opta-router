package io.github.pintowar.opta.router.solver.ortools

import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.spi.SolverFactory
import java.util.UUID

class OrToolsFactory : SolverFactory {

    override val name: String = "or-tools"

    override fun createSolver(key: UUID, config: SolverConfig): Solver {
        return OrToolsSolver(key, name, config)
    }
}