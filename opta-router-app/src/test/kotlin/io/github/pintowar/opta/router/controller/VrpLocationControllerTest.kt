package io.github.pintowar.opta.router.controller

import com.ninjasquad.springmockk.MockkBean
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpLocationPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(VrpLocationController::class)
class VrpLocationControllerTest : FunSpec() {

    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var repo: VrpLocationPort

    init {
        extensions(SpringExtension())

        beforeEach {
            clearMocks(repo)
        }

        context("GET") {
            test("/api/vrp-locations with no params") {
                val customers = Fixtures.customer("sample-4")
                coEvery { repo.count(eq("")) } returns customers.size.toLong()
                every { repo.findAll(eq(""), eq(0), eq(25)) } returns customers.asFlow()

                client.get().uri("/api/vrp-locations")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$.totalPages").isEqualTo(1)
                    .jsonPath("$.totalElements").isEqualTo(customers.size)
                    .jsonPath("$.first").isEqualTo(true)
                    .jsonPath("$.content[0].id").isEqualTo(2)
                    .jsonPath("$.content[0].name").isEqualTo("ANTHISNES")
                    .jsonPath("$.content[0].demand").isEqualTo(3)

                coVerify(exactly = 1) { repo.count("") }
                verify(exactly = 1) { repo.findAll("", 0, 25) }
            }

            test("/api/vrp-locations with params") {
                val customers = Fixtures.customer("sample-4")
                coEvery { repo.count(any()) } returns customers.size.toLong()
                every { repo.findAll(any(), any(), any()) } returns customers.asFlow()

                client.get().uri("/api/vrp-locations?page=1&size=5&q=sample")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$.totalPages").isEqualTo(3)
                    .jsonPath("$.totalElements").isEqualTo(5 + customers.size)
                    .jsonPath("$.first").isEqualTo(false)
                    .jsonPath("$.content[0].id").isEqualTo(2)
                    .jsonPath("$.content[0].name").isEqualTo("ANTHISNES")
                    .jsonPath("$.content[0].demand").isEqualTo(3)

                coVerify(exactly = 1) { repo.count("sample") }
                verify(exactly = 1) { repo.findAll("sample", 5, 5) }
            }

            test("/api/vrp-locations/customer") {
                val customers = Fixtures.customer("sample-4")
                every { repo.listAllByKind(eq("customer")) } returns customers.asFlow()

                client.get().uri("/api/vrp-locations/customer")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$[0].id").isEqualTo(2)
                    .jsonPath("$[0].name").isEqualTo("ANTHISNES")
                    .jsonPath("$[0].demand").isEqualTo(3)
            }
        }

        context("POST") {
            test("/api/vrp-locations/insert") {
                val customer = Fixtures.customer("sample-4").first()
                val req = VrpLocationController.LocationRequest(
                    id = -1L,
                    name = customer.name,
                    lat = customer.lat,
                    lng = customer.lng,
                    demand = customer.demand
                )
                coEvery { repo.create(any()) } just runs

                client.post().uri("/api/vrp-locations/insert")
                    .bodyValue(req)
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.create(req.toLocation()) }
            }
        }

        context("DELETE") {
            test("/api/vrp-locations/:id/remove") {
                coEvery { repo.deleteById(any()) } just runs

                client.delete().uri("/api/vrp-locations/1/remove")
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.deleteById(1) }
            }
        }

        context("PUT") {
            test("/api/vrp-locations/:id/update") {
                val customer = Fixtures.customer("sample-4").first()
                val req = VrpLocationController.LocationRequest(
                    id = -1L,
                    name = customer.name,
                    lat = customer.lat,
                    lng = customer.lng,
                    demand = customer.demand
                )
                coEvery { repo.update(any(), any()) } just runs

                client.put().uri("/api/vrp-locations/2/update")
                    .bodyValue(req)
                    .exchange()
                    .expectStatus().isOk

                coVerify(exactly = 1) { repo.update(2, req.toLocation()) }
            }
        }
    }
}