package io.github.pintowar.opta.router.solver.jsprit

import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.spi.SolverFactory
import java.util.UUID

class JspritFactory : SolverFactory {

    override val name: String = "jsprit"

    override fun createSolver(key: UUID, config: SolverConfig): Solver {
        return JspritSolver(key, name, config)
    }
}