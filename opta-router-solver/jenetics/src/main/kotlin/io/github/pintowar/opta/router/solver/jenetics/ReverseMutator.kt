package io.github.pintowar.opta.router.solver.jenetics

import io.jenetics.Chromosome
import io.jenetics.Gene
import io.jenetics.Mutator
import io.jenetics.MutatorResult
import io.jenetics.internal.math.Subsets
import io.jenetics.util.ISeq
import io.jenetics.util.RandomRegistry
import java.util.random.RandomGenerator

class ReverseMutator<G : Gene<*, G>?, C : Comparable<C>?>(
    probability: Double = DEFAULT_ALTER_PROBABILITY
) : Mutator<G, C>(probability) {
    /**
     * Mutates a given chromosome by reversing a random sub-sequence of its genes.
     *
     * This mutation operator selects two random points within the chromosome and reverses the order
     * of the genes between these two points. This can help in exploring different permutations of routes.
     *
     * @param chromosome The [Chromosome] to be mutated.
     * @param p The probability of applying this mutation (inherited from [Mutator]).
     * @param random The [RandomGenerator] to use for selecting the sub-sequence.
     * @return A [MutatorResult] containing the new, mutated chromosome and the number of mutations performed.
     */
    override fun mutate(
        chromosome: Chromosome<G>,
        p: Double,
        random: RandomGenerator
    ): MutatorResult<Chromosome<G>> =
        if (chromosome.length() > 1) {
            val genes = chromosome.toMutableList()
            val points = Subsets.next(RandomRegistry.random(), genes.size, 2)

            val newSeq = genes.subList(points[0], points[1]).reversed()
            val mutations =
                (points[0]..<points[1])
                    .onEachIndexed { idx, i ->
                        genes[i] = newSeq[idx]
                    }.count()

            MutatorResult(
                chromosome.newInstance(ISeq.of(genes)),
                mutations
            )
        } else {
            MutatorResult(chromosome, 0)
        }
}