package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
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

/**
 * Represents a dummy location used in Jenetics for internal representation of vehicle routes.
 * This is used to mark the end of a vehicle's route within a chromosome.
 *
 * @property id The unique identifier of the dummy location.
 */
data class DummyLocation(
    override val id: Long
) : Location {
    override val name: String = "Dummy $id"
    override val lat: Double = 0.0
    override val lng: Double = 0.0
}

/**
 * Converts a Jenetics [Genotype] of [EnumGene]s representing locations into a list of customer sub-routes.
 * Dummy locations are used as delimiters to separate routes for different vehicles.
 *
 * @param genotype The [Genotype] containing the permutation of locations and dummy locations.
 * @return A [List] of [List]s of [Customer]s, where each inner list represents a vehicle's route.
 */
private fun genToSubRoutes(genotype: Genotype<EnumGene<Location>>): List<List<Customer>> {
    val chromosome = genotype.chromosome().toList()
    val indices =
        chromosome
            .withIndex()
            .filter { (_, it) -> it.allele() is DummyLocation }
            .sortedBy { (_, it) -> it.alleleIndex() }
            .map { (idx, _) -> idx }
    val result =
        (listOf(0) + indices.sorted() + listOf(chromosome.size))
            .windowed(2)
            .map { (b, e) ->
                chromosome
                    .subList(b, e)
                    .asSequence()
                    .map { it.allele() }
                    .filterIsInstance<Customer>()
                    .toList()
            }
    return (listOf(-1) + indices).withIndex().sortedBy { (_, v) -> v }.map { (i, _) -> result[i] }
}

/**
 * Creates a Jenetics [Codec] for the VRP problem.
 * The codec defines how the problem's genotype (permutation of locations) is converted into a phenotype (list of customer sub-routes).
 *
 * @param problem The [VrpProblem] to create the codec for.
 * @return A [Codec] that maps a [Genotype] of [EnumGene]s to a [List] of [List]s of [Customer]s.
 */
private fun problemCodec(problem: VrpProblem) =
    problem.let { prob ->
        val nVehicles = prob.numVehicles()
        val locations = prob.customers + (1 until nVehicles).map { DummyLocation(it.toLong()) }
        val chromosomeLocations = PermutationChromosome.of(ISeq.of(locations))
        Codec.of(Genotype.of(chromosomeLocations), ::genToSubRoutes)
    }

/**
 * Creates a fitness function for the VRP problem, used by Jenetics to evaluate solutions.
 * The fitness is calculated as the total travel distance for all vehicles based on their assigned sub-routes.
 *
 * @param problem The [VrpProblem] for which to calculate fitness.
 * @param matrix The [Matrix] containing travel distances between locations.
 * @return A function that takes a [List] of [List]s of [Customer]s (sub-routes) and returns their total travel distance as a [Double].
 */
private fun fitnessFactory(
    problem: VrpProblem,
    matrix: Matrix
): (List<List<Customer>>) -> Double =
    { subRoutes: List<List<Customer>> ->
        subRoutes
            .mapIndexed { idx, customers ->
                val depot = problem.vehicles[idx].depot
                (listOf(depot) + customers + listOf(depot))
                    .windowed(2)
                    .sumOf { (a, b) -> matrix.distance(a.id, b.id) }
            }.sum()
    }

/**
 * Converts a list of customer sub-routes back into a Jenetics [PermutationChromosome] of [Location]s.
 * This function reintroduces dummy locations to separate the routes for different vehicles within the chromosome.
 *
 * @param problem The [VrpProblem] associated with the sub-routes.
 * @param subRoutes A [List] of [List]s of [Customer]s, where each inner list represents a vehicle's route.
 * @return A [PermutationChromosome] representing the ordered locations for Jenetics.
 */
private fun toChromosome(
    problem: VrpProblem,
    subRoutes: List<List<Customer>>
): PermutationChromosome<Location> {
    val dummies = (1 until problem.numVehicles()).map { DummyLocation(it.toLong()) }
    val orderedLocations =
        subRoutes.withIndex().fold(emptyList<Location>()) { acc, (idx, customers) ->
            val dummy = if (idx < dummies.size) listOf(dummies[idx]) else emptyList()
            acc + customers + dummy
        }
    val locations = problem.customers + dummies
    val geneIdx = locations.withIndex().associate { (k, v) -> v to k }
    return PermutationChromosome(
        ISeq.of(orderedLocations.map { EnumGene.of(geneIdx.getValue(it), ISeq.of(locations)) })
    )
}

/**
 * Converts a [VrpSolution] into a Jenetics [EvolutionStart] object.
 * This is used to initialize the Jenetics evolutionary algorithm with a pre-existing solution.
 *
 * @receiver The [VrpSolution] to convert.
 * @return An [EvolutionStart] object representing the initial state for the Jenetics solver.
 */
fun VrpSolution.toInitialSolution(): EvolutionStart<EnumGene<Location>, Double> {
    val idxCustomers = this.problem.customers.associateBy { it.id }
    val subRoutes =
        (0 until this.problem.numVehicles()).map {
            if (it < this.routes.size) this.routes[it].customerIds.map(idxCustomers::getValue) else emptyList()
        }
    val chromosome = toChromosome(this.problem, subRoutes)

    val gen = 1L
    val phenotype = Genotype.of(chromosome).let { gt -> Phenotype.of(gt, gen, this.getTotalDistance().toDouble()) }
    return EvolutionStart.of(ISeq.of(phenotype), gen)
}

/**
 * Defines a constraint for the VRP problem within Jenetics, ensuring that vehicle capacities are not exceeded.
 * It provides a `test` function to check constraint violations and an `enforce` function to repair invalid solutions.
 *
 * @param problem The [VrpProblem] to apply constraints to.
 * @param matrix The [Matrix] containing travel distances (used in the enforce function for re-calculating fitness).
 * @return A [Constraint] object for Jenetics, handling capacity constraints.
 */
private fun problemConstraint(
    problem: VrpProblem,
    matrix: Matrix
): Constraint<EnumGene<Location>, Double> =
    Constraint.of(
        { individual ->
            val subRoutes = genToSubRoutes(individual.genotype())
            subRoutes.indices.all { idx ->
                val demand = subRoutes[idx].sumOf { it.demand }
                problem.vehicles[idx].capacity >= demand
            }
        },
        { individual, gen ->
            val subRoutes = genToSubRoutes(individual.genotype())

            val (full, dropped) =
                subRoutes.indices
                    .fold(emptyList<Boolean>() to emptySet<Customer>()) { (full, dropped), idx ->
                        val splitIndex =
                            subRoutes[idx]
                                .asSequence()
                                .scan(0) { acc, c -> acc + c.demand }
                                .indexOfLast { it <= problem.vehicles[idx].capacity }
                        full + (splitIndex < subRoutes[idx].size) to dropped + subRoutes[idx].drop(splitIndex)
                    }

            val initial = MutableList<List<Customer>>(subRoutes.size) { emptyList() } to dropped
            val (adjustedSubRoutes, _) =
                subRoutes.foldIndexed(initial) { idx, (newSubRoutes, used), subRoute ->
                    if (full[idx]) {
                        newSubRoutes[idx] = subRoute.filter { c -> c !in dropped }
                        newSubRoutes to used
                    } else {
                        val currentDemand = subRoute.sumOf { c -> c.demand }
                        val validSubRoute =
                            used
                                .scan(Pair<Int, Customer?>(currentDemand, null)) { (acc, _), c ->
                                    val accDemand = acc + c.demand
                                    if (accDemand > problem.vehicles[idx].capacity) {
                                        acc to null
                                    } else {
                                        accDemand to c
                                    }
                                }.drop(1)
                                .mapNotNull { (_, c) -> c }
                        newSubRoutes[idx] = subRoute + validSubRoute
                        newSubRoutes to (used - validSubRoute.toSet())
                    }
                }
            val distance = fitnessFactory(problem, matrix)(adjustedSubRoutes)
            val chromosome = toChromosome(problem, adjustedSubRoutes)
            Genotype.of(chromosome).let { gt -> Phenotype.of(gt, gen, distance) }
        }
    )

/**
 * Converts a [VrpProblem] and its associated [Matrix] into a Jenetics [Problem] definition.
 * This sets up the problem for the Jenetics evolutionary algorithm, including the fitness function, codec, and constraints.
 *
 * @receiver The [VrpProblem] to convert.
 * @param matrix The [Matrix] containing travel distances and times for the problem's locations.
 * @return A Jenetics [Problem] object ready for solving.
 */
fun VrpProblem.toProblem(matrix: Matrix): Problem<List<List<Customer>>, EnumGene<Location>, Double> {
    val codec = problemCodec(this)
    return Problem.of(fitnessFactory(this, matrix), codec, problemConstraint(this, matrix))
}

/**
 * Converts a Jenetics [Genotype] into a [VrpSolution] domain object.
 * This function extracts the routes from the genotype, calculates their distances, times, and demands,
 * and constructs a [VrpSolution] object.
 *
 * @receiver The [Genotype] to convert.
 * @param problem The original [VrpProblem] associated with this solution.
 * @param matrix The [Matrix] containing travel distances and times for calculating route metrics.
 * @return A [VrpSolution] object representing the solution derived from the genotype.
 */
fun Genotype<EnumGene<Location>>.toDto(
    problem: VrpProblem,
    matrix: Matrix
): VrpSolution {
    val subRoutes =
        genToSubRoutes(this).mapIndexed { idx, customers ->
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