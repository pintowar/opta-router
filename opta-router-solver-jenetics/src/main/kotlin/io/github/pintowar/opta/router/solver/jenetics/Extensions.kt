package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.jenetics.EnumGene
import io.jenetics.Genotype
import io.jenetics.PermutationChromosome
import io.jenetics.Phenotype
import io.jenetics.engine.Codec
import io.jenetics.engine.EvolutionStart
import io.jenetics.engine.Problem
import io.jenetics.util.ISeq
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

private fun problemCodec(problem: VrpProblem, seed: Random = Random()) = problem.let { prob ->
    val nCustomers = prob.customers.size
    val customersIdx = prob.customers.indices
    val nVehicles = prob.nVehicles

    val customerOrder = (prob.customers).toList().shuffled(seed)
    val indices = customersIdx.drop(1).shuffled(seed).take(nVehicles - 1).sorted()
    println(indices)

    val subRoutes = (listOf(0) + indices + listOf(nCustomers))
        .asSequence()
        .windowed(2)
        .map { (b, e) -> customerOrder.subList(b, e) }
        .map { route -> route.indices.map { EnumGene.of(it, ISeq.of(route)) } }
        .map { PermutationChromosome(ISeq.of(it)) }
        .toList()

    Codec.of(Genotype.of(subRoutes)) { gt ->
        ISeq.of(gt.map { ISeq.of(it.toList()) })
    }
}

fun VrpSolution.toInitialSolution(): EvolutionStart<EnumGene<Customer>, Double> {
    val idxCustomers = this.problem.customers.associateBy { it.id }
    val subRoutes = this.routes.asSequence()
        .map { it.customerIds.map(idxCustomers::getValue) }
        .map { customers -> customers.indices.map { EnumGene.of(it, ISeq.of(customers)) } }
        .map { PermutationChromosome(ISeq.of(it)) }
        .toList()

    val gen = 1L
    val phenotype = Genotype.of(subRoutes).let { gt -> Phenotype.of(gt, gen, this.getTotalDistance().toDouble()) }
    return EvolutionStart.of(ISeq.of(phenotype), gen)
}

fun VrpProblem.toProblem(matrix: Matrix): Problem<ISeq<ISeq<EnumGene<Customer>>>, EnumGene<Customer>, Double> {
    val codec = problemCodec(this, Random())
    return Problem.of(
        { gene ->
            gene.mapIndexed { idx, seq ->
                val depot = this.vehicles[idx].depot
                val customers = seq.map { it.allele() }.toList()
                (listOf(depot) + customers + listOf(depot)).windowed(2).sumOf { (a, b) -> matrix.distance(a.id, b.id) }
            }.sum()
        },
        codec
    )
}

fun Genotype<EnumGene<Customer>>.toDto(problem: VrpProblem, matrix: Matrix): VrpSolution {
    val subRoutes = this.mapIndexed { idx, gen ->
        val depot = problem.vehicles[idx].depot
        val customers = gen.map { it.allele() }.toList()
        val locations = (listOf(depot) + customers + listOf(depot))

        val dist = locations.windowed(2).sumOf { (i, j) -> matrix.distance(i.id, j.id) }
        val time = locations.windowed(2).sumOf { (i, j) -> matrix.time(i.id, j.id).toDouble() }

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