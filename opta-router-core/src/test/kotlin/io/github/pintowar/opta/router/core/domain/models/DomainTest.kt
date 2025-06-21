package io.github.pintowar.opta.router.core.domain.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class DomainTest :
    FunSpec({

        test("VrpProblem getters") {
            val problem = Fixtures.problem("sample-4")

            problem.depots() shouldContain Fixtures.depot()
            problem.depots() shouldHaveSize 1
            problem.numLocations() shouldBe 10
            problem.numVehicles() shouldBe 2
        }

        test("VrpSolution getters") {
            val (firstSolution, secondSolution) = Fixtures.solution("sample-4")

            firstSolution.isFeasible() shouldBe true
            firstSolution.isEmpty() shouldBe false
            firstSolution.getTotalDistance() shouldBe BigDecimal.valueOf(690.05)
            firstSolution.getTotalTime() shouldBe BigDecimal.valueOf(920.91)

            secondSolution.isFeasible() shouldBe true
            secondSolution.isEmpty() shouldBe false
            secondSolution.getTotalDistance() shouldBe BigDecimal.valueOf(592.17)
            secondSolution.getTotalTime() shouldBe BigDecimal.valueOf(751.72)
        }
    })