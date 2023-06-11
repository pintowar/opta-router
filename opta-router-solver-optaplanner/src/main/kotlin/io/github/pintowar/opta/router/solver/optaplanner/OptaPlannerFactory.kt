package io.github.pintowar.opta.router.solver.optaplanner

import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.spi.SolverFactory
import java.util.UUID

class OptaPlannerFactory : SolverFactory {

    override val name: String = "optaplanner"

    override fun createSolver(key: UUID, config: SolverConfig): Solver {
        return OptaSolver(key, name, config)
    }
}