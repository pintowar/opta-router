package io.github.pintowar.opta.router.controller

import com.ninjasquad.springmockk.MockkBean
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(VrpProblemController::class)
class VrpProblemControllerTest : FunSpec() {

    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var repo: VrpProblemPort

    init {
        extensions(SpringExtension())

        beforeEach {
            clearMocks(repo)
        }

        context("GET") {
            test("/api/vrp-problems with no params") {
                coEvery { repo.count(any()) } returns 0
                coEvery { repo.findAll(any()) } returns emptyFlow()

                client.get().uri("/api/vrp-problems")
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.count("") }
                verify(exactly = 1) { repo.findAll("", 0, 25) }
            }

            test("/api/vrp-problems with params") {
                val problems = Fixtures.problems()
                coEvery { repo.count(any()) } returns problems.size.toLong()
                every { repo.findAll(any(), any(), any()) } returns problems.asFlow().map { it.toSummary() }

                client.get().uri("/api/vrp-problems?page=1&size=5&q=sample")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$.totalPages").isEqualTo(2)
                    .jsonPath("$.totalElements").isEqualTo(5 + problems.size)
                    .jsonPath("$.first").isEqualTo(false)
                    .jsonPath("$.content[0].id").isEqualTo(4)
                    .jsonPath("$.content[0].name").isEqualTo("sample-4")
                    .jsonPath("$.content[0].totalCapacity").isEqualTo(62)
                    .jsonPath("$.content[0].totalDemand").isEqualTo(31)

                coVerify(exactly = 1) { repo.count("sample") }
                verify(exactly = 1) { repo.findAll("sample", 5, 5) }
            }

            test("/api/vrp-problems/:id") {
                val problem = Fixtures.problem("sample-4")
                coEvery { repo.getById(any()) } returns problem

                client.get().uri("/api/vrp-problems/4")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(4)
                    .jsonPath("$.name").isEqualTo("sample-4")

                coVerify(exactly = 1) { repo.getById(4) }
            }
        }

        context("POST") {
            test("/api/vrp-problems") {
                val problem = Fixtures.problem("sample-4")
                coEvery { repo.create(any()) } just runs

                client.post().uri("/api/vrp-problems")
                    .bodyValue(problem.copy(id = -1))
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.create(problem.copy(id = -1)) }
            }

            test("/api/vrp-problems/:id/copy") {
                val problem = Fixtures.problem("sample-4")
                coEvery { repo.create(any()) } just runs

                client.post().uri("/api/vrp-problems/4/copy")
                    .bodyValue(problem)
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.create(problem) }
            }
        }

        context("DELETE") {
            test("/api/vrp-problems/:id/remove") {
                coEvery { repo.deleteById(any()) } just runs

                client.delete().uri("/api/vrp-problems/1/remove")
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.deleteById(1) }
            }
        }

        context("PUT") {
            test("/api/vrp-problems/:id/update") {
                val problem = Fixtures.problem("sample-4")
                coEvery { repo.update(any(), any()) } just runs

                client.put().uri("/api/vrp-problems/4/update")
                    .bodyValue(problem)
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.update(4, problem) }
            }
        }
    }
}