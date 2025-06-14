package io.github.pintowar.opta.router.core.domain.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class DomainTest : FunSpec({

    test("VrpProblem getters") {
        val problem = Fixtures.problem("sample-4")

        problem.depots() shouldContain Fixtures.depot()
        problem.depots() shouldHaveSize 1
        problem.numLocations() shouldBe 10
        problem.numVehicles() shouldBe 2
    }

})