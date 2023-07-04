package io.github.pintowar.opta.router.solver.timefold

import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.spi.SolverFactory
import java.util.UUID

class TimefoldPlannerFactory : SolverFactory {

    override val name: String = "timefold"

    override fun createSolver(key: UUID, config: SolverConfig): Solver {
        return TimefoldSolver(key, name, config)
    }
}