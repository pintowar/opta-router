package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.jenetics.*
import io.jenetics.engine.Codec
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.Limits
import io.jenetics.engine.Problem
import io.jenetics.util.ISeq
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class JeneticsSolver(key: UUID, name: String, config: SolverConfig) : Solver(key, name, config) {

    private val running = AtomicBoolean(false)

    override fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit) {
        var best = initialSolution
        val engine = Engine.builder(toProblem(initialSolution.problem, matrix))
            .optimize(Optimize.MINIMUM)
            .maximalPhenotypeAge(10)
            .populationSize(100)
            .alterers(
                PartiallyMatchedCrossover(0.8),
                SwapMutator(0.1)
            )
            .build()

        running.set(true)
        callback(VrpSolutionRequest(initialSolution, SolverStatus.RUNNING, key))
        val result = engine.stream()
            .limit(Limits.byExecutionTime(config.timeLimit))
            .limit { running.get() }
            .peek {
                val actual = toDto(it.bestPhenotype().genotype(), initialSolution.problem, matrix)
                if (best.isEmpty() || actual.getTotalDistance() < best.getTotalDistance()) {
                    best = actual
                    callback(VrpSolutionRequest(best, SolverStatus.RUNNING, key))
                }
            }
            .collect(EvolutionResult.toBestGenotype())
        callback(VrpSolutionRequest(toDto(result, initialSolution.problem, matrix), SolverStatus.TERMINATED, key))
        running.set(false)
    }

    override fun terminate() {
        running.set(false)
    }

    override fun isSolving() = running.get()

    fun problemCodec(problem: VrpProblem, seed: Random = Random()) = problem.let { prob ->
        val nCustomers = prob.customers.size
        val nVehicles = prob.nVehicles

        val customerOrder = (0 until nCustomers).toList().shuffled(seed)
        val subRoutes = (listOf(0) + customerOrder.shuffled(seed).take(nVehicles - 1).sorted() + listOf(nCustomers))
            .windowed(2)
            .map { (b, e) -> customerOrder.subList(b, e) }
            .map { PermutationChromosome.of(ISeq.of(it)) }

        Codec.of(Genotype.of(subRoutes)) { gt ->
            ISeq.of(gt.map { ISeq.of(it.toList()) })
        }
    }

    fun toProblem(problem: VrpProblem, matrix: Matrix) = Problem.of(
        { gene ->
            gene.mapIndexed { idx, seq ->
                val depot = problem.vehicles[idx].depot
                val customers = seq.map { problem.customers[it.allele()] }.toList()
                (listOf(depot) + customers + listOf(depot)).windowed(2).sumOf { (a, b) -> matrix.distance(a.id, b.id) }
            }.sum()
        },
        problemCodec(problem)
    )

    fun toDto(genotype: Genotype<EnumGene<Int>>, problem: VrpProblem, matrix: Matrix): VrpSolution {
        val subRoutes = genotype.mapIndexed { idx, gen ->
            val depot = problem.vehicles[idx].depot
            val customers = gen.map { problem.customers[it.allele()] }.toList()
            val locations = (listOf(depot) + customers + listOf(depot))

            val dist = locations.windowed(2, 1).sumOf { (i, j) -> matrix.distance(i.id, j.id) }
            val time = locations.windowed(2, 1).sumOf { (i, j) -> matrix.time(i.id, j.id).toDouble() }

            Route(
                BigDecimal(dist / 1000).setScale(2, RoundingMode.HALF_UP),
                BigDecimal(time / (60 * 1000)).setScale(2, RoundingMode.HALF_UP),
                customers.sumOf { it.demand },
                locations.map { LatLng(it.lat, it.lng) },
                customers.map { it.id }
            )
        }

        return VrpSolution(problem, subRoutes)
    }
}