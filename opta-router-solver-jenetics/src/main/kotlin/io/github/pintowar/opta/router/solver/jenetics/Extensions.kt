package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.jenetics.EnumGene
import io.jenetics.Genotype
import io.jenetics.PermutationChromosome
import io.jenetics.Phenotype
import io.jenetics.engine.Codec
import io.jenetics.engine.Constraint
import io.jenetics.engine.EvolutionStart
import io.jenetics.engine.Problem
import io.jenetics.util.ISeq
import java.math.BigDecimal
import java.math.RoundingMode

data class DummyLocation(override val id: Long) : Location {
    override val name: String = "Dummy $id"
    override val lat: Double = 0.0
    override val lng: Double = 0.0
}

private fun genToSubRoutes(genotype: Genotype<EnumGene<Location>>): List<List<Customer>> {
    val chromosome = genotype.chromosome().toList()
    val indices = chromosome.withIndex().filter { (_, it) -> it.allele() is DummyLocation }.map { (idx, _) -> idx }
    return (listOf(0) + indices + listOf(chromosome.size))
        .windowed(2)
        .map { (b, e) ->
            chromosome.subList(b, e)
                .asSequence()
                .map { it.allele() }
                .filterIsInstance<Customer>()
                .toList()
        }
}

private fun problemCodec(problem: VrpProblem) = problem.let { prob ->
    val nVehicles = prob.nVehicles
    val locations = prob.customers + (1 until nVehicles).map { DummyLocation(it.toLong()) }
    val chromosomeLocations = PermutationChromosome.of(ISeq.of(locations))
    Codec.of(Genotype.of(chromosomeLocations), ::genToSubRoutes)
}

fun VrpSolution.toInitialSolution(): EvolutionStart<EnumGene<Location>, Double> {
    val idxCustomers = this.problem.customers.associateBy { it.id }
    val customers = this.routes.map { it.customerIds.map(idxCustomers::getValue) }
    val dummies = (1 until this.problem.nVehicles).map { DummyLocation(it.toLong()) }
    val orderedLocations = customers.withIndex().fold(emptyList<Location>()) { acc, (idx, customers) ->
        val dummy = if (idx < dummies.size) listOf(dummies[idx]) else emptyList()
        acc + customers + dummy
    }

    val geneIdx = (problem.customers + dummies).withIndex().associate { (k, v) -> v to k }
    val chromosome = PermutationChromosome(
        ISeq.of(orderedLocations.map { EnumGene.of(geneIdx.getValue(it), ISeq.of(problem.customers + dummies)) })
    )

    val gen = 1L
    val phenotype = Genotype.of(chromosome).let { gt -> Phenotype.of(gt, gen, this.getTotalDistance().toDouble()) }
    return EvolutionStart.of(ISeq.of(phenotype), gen)
}

private fun problemConstraint(problem: VrpProblem): Constraint<EnumGene<Location>, Double> =
    Constraint.of { individual ->
        val subRoutes = genToSubRoutes(individual.genotype())
        val validCapacities by lazy {
            subRoutes.indices.all { idx ->
                val subRoute = subRoutes[idx]
                val (demand, customersIds) = subRoute
                    .fold(0 to emptySet<Long>()) { (allDemand, allIds), it ->
                        (allDemand + it.demand) to (allIds + it.id)
                    }
                problem.vehicles[idx].capacity >= demand && customersIds.size == problem.customers.size
            }
        }
        subRoutes.size <= problem.nVehicles && validCapacities
    }

fun VrpProblem.toProblem(matrix: Matrix): Problem<List<List<Customer>>, EnumGene<Location>, Double> {
    val codec = problemCodec(this)
    return Problem.of(
        { gene ->
            gene.mapIndexed { idx, customers ->
                val depot = this.vehicles[idx].depot
                (listOf(depot) + customers + listOf(depot)).windowed(2)
                    .sumOf { (a, b) -> matrix.distance(a.id, b.id) }
            }.sum()
        },
        codec,
        problemConstraint(this)
    )
}

fun Genotype<EnumGene<Location>>.toDto(problem: VrpProblem, matrix: Matrix): VrpSolution {
    val subRoutes = genToSubRoutes(this).mapIndexed { idx, customers ->
        val depot = problem.vehicles[idx].depot
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