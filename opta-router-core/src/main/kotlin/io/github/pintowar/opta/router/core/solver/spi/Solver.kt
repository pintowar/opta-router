package io.github.pintowar.opta.router.core.solver.spi

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import java.util.UUID

abstract class Solver(val key: UUID, val name: String, val config: SolverConfig) {

    abstract fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit)

    abstract fun terminate()

    abstract fun isSolving(): Boolean
}