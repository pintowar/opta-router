package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.VrpSolution

data class SolutionFlow(val body: VrpSolution, val isCompleted: Boolean = false)
