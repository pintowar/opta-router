package io.github.pintowar.opta.router.solver.jenetics

import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import java.math.BigDecimal
import java.time.Duration

class JeneticsSolverTest :
    FunSpec({

        val problem = Fixtures.problem("sample-4")
        val matrix = Fixtures.matrix("sample-4")
        val config = SolverConfig(Duration.ofSeconds(5))

        test("JeneticsSolver should return a solution flow") {
            val initialSolution = VrpSolution(problem, emptyList())
            val expectedRoutes =
                Fixtures
                    .solution("sample-4")
                    .last()
                    .routes
                    .find { it.distance > BigDecimal.ZERO }

            val solver = JeneticsSolver()
            val result = solver.solve(initialSolution, matrix, config).take(20).last()
            val validRoute = result.routes.find { it.distance > BigDecimal.ZERO }

            validRoute shouldBe expectedRoutes
        }
    })