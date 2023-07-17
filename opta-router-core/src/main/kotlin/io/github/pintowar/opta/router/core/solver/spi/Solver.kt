package io.github.pintowar.opta.router.core.solver.spi

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class SolutionFlow(val body: VrpSolution, val isCompleted: Boolean = false)

abstract class Solver(val key: UUID, val name: String, val config: SolverConfig) {

    abstract fun solutionFlow(initialSolution: VrpSolution, matrix: Matrix, config: SolverConfig): Flow<SolutionFlow>

    abstract fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit)

    abstract fun terminate()

    abstract fun isSolving(): Boolean
}