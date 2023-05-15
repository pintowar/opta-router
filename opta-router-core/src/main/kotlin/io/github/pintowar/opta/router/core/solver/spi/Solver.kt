package io.github.pintowar.opta.router.core.solver.spi

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import java.util.UUID

abstract class Solver(val key: UUID, val name: String, val config: SolverConfig) {

    abstract fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRegistry) -> Unit)

    abstract fun terminate()
}