package io.github.pintowar.opta.router

import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType

class VrpProblemIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var problemRepo: VrpProblemPort

    init {
        test("should list vrp problems") {
            val uri = "/api/vrp-problems"
            val exchange = client.get().uri(uri).exchange()

            exchange.expectStatus().isOk
            exchange.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)

            val body = exchange.expectBody()
            body.jsonPath("$.content.length()").isEqualTo(problemRepo.count())
            body.jsonPath("$.totalPages").isEqualTo(1)
        }

        test("should return vrp problems") {
            val id = 4
            val uri = "/api/vrp-problems/{id}"
            val exchange = client.get().uri(uri, id).exchange()

            exchange.expectStatus().isOk
            exchange.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)

            val body = exchange.expectBody()
            body.jsonPath("$.id").isEqualTo(id)
            body.jsonPath("$.name").isEqualTo("sample-4")
        }

        test("should create vrp problems") {
            val problem = Fixtures.problem("sample-4")
            coEvery { geoPort.generateMatrix(any()) } returns Fixtures.matrix("sample-4")

            problemRepo.count() shouldBe 8

            val exchange =
                client
                    .post()
                    .uri("/api/vrp-problems")
                    .bodyValue(problem)
                    .exchange()

            exchange.expectStatus().isOk
            problemRepo.count() shouldBe 9
        }

        test("should copy vrp problems") {
            val problem = Fixtures.problem("sample-4")
            coEvery { geoPort.generateMatrix(any()) } returns Fixtures.matrix("sample-4")

            problemRepo.count() shouldBe 8

            val exchange =
                client
                    .post()
                    .uri("/api/vrp-problems/{id}/copy", problem.id)
                    .bodyValue(problem)
                    .exchange()

            exchange.expectStatus().isOk
            problemRepo.count() shouldBe 9
        }

        test("should update vrp problems") {
            val problem = Fixtures.problem("sample-4")
            coEvery { geoPort.generateMatrix(any()) } returns Fixtures.matrix("sample-4")

            val exchange =
                client
                    .put()
                    .uri("/api/vrp-problems/{id}/update", problem.id)
                    .bodyValue(problem.copy(name = "new-sample-4"))
                    .exchange()

            exchange.expectStatus().isOk
            problemRepo.getById(problem.id)?.name shouldBe "new-sample-4"
        }

        test("should remove vrp problems") {
            problemRepo.count() shouldBe 8

            val id = 4
            val uri = "/api/vrp-problems/{id}/remove"
            val exchange = client.delete().uri(uri, id).exchange()

            exchange.expectStatus().isOk
            problemRepo.count() shouldBe 7
        }
    }
}