package io.github.pintowar.opta.router.solver.jenetics

import io.jenetics.Chromosome
import io.jenetics.Gene
import io.jenetics.Mutator
import io.jenetics.MutatorResult
import io.jenetics.internal.math.Subset
import io.jenetics.util.ISeq
import io.jenetics.util.RandomRegistry
import java.util.random.RandomGenerator

class ReverseMutator<G : Gene<*, G>?, C : Comparable<C>?>(
    probability: Double = DEFAULT_ALTER_PROBABILITY
) : Mutator<G, C>(probability) {
    override fun mutate(
        chromosome: Chromosome<G>,
        p: Double,
        random: RandomGenerator
    ): MutatorResult<Chromosome<G>> =
        if (chromosome.length() > 1) {
            val genes = chromosome.toMutableList()
            val points = Subset.next(RandomRegistry.random(), genes.size, 2)

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