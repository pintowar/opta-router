package io.github.pintowar.opta.router.core.solver.spi

import io.github.pintowar.opta.router.core.solver.SolverConfig
import java.util.ServiceLoader
import java.util.UUID

interface SolverFactory {
    companion object {

        fun getNamedSolverFactories(): Map<String, SolverFactory> {
            val solverFactories = mutableMapOf<String, SolverFactory>()
            ServiceLoader.load(SolverFactory::class.java)
                .iterator()
                .forEachRemaining { solverFactories[it.name] = it }
            return solverFactories
        }

        fun createSolver(solverName: String, key: UUID, config: SolverConfig): Solver =
            getNamedSolverFactories()[solverName]?.createSolver(key, config)
                ?: throw IllegalArgumentException("No solver $solverName was found")
    }

    val name: String

    fun createSolver(key: UUID, config: SolverConfig): Solver
}