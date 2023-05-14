package io.github.pintowar.opta.router.core.solver

import kotlinx.coroutines.flow.Flow

interface Problem {
    val name: String
}

interface Solution<P : Problem> {

    val problem: P

    fun objective(): Double

    fun isFeasible(): Boolean
}

interface Solver<P : Problem, S : Solution<P>> {

    fun solve(problem: P): Flow<S>
}