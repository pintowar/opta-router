package io.github.pintowar.opta.router.controller

import com.ninjasquad.springmockk.MockkBean
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpVehiclePort
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
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(VrpVehicleController::class)
class VrpVehicleControllerTest : FunSpec() {
    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var repo: VrpVehiclePort

    init {
        extensions(SpringExtension())

        beforeEach {
            clearMocks(repo)
        }

        context("GET") {
            test("/api/vrp-vehicles with no params") {
                coEvery { repo.count(any()) } returns 0
                coEvery { repo.findAll(any()) } returns emptyFlow()

                client
                    .get()
                    .uri("/api/vrp-vehicles")
                    .exchange()
                    .expectStatus()
                    .isOk

                coVerify(exactly = 1) { repo.count("") }
                verify(exactly = 1) { repo.findAll("", 0, 25) }
            }

            test("/api/vrp-vehicles with params") {
                val vehicles = Fixtures.vehicles()
                coEvery { repo.count(any()) } returns vehicles.size.toLong()
                every { repo.findAll(any(), any(), any()) } returns vehicles.asFlow()

                client
                    .get()
                    .uri("/api/vrp-vehicles?page=1&size=5&q=sample")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$.totalPages")
                    .isEqualTo(2)
                    .jsonPath("$.totalElements")
                    .isEqualTo(5 + vehicles.size)
                    .jsonPath("$.first")
                    .isEqualTo(false)
                    .jsonPath("$.content[0].id")
                    .isEqualTo(1)
                    .jsonPath("$.content[0].name")
                    .isEqualTo("Vehicle 0")
                    .jsonPath("$.content[0].capacity")
                    .isEqualTo(31)

                coVerify(exactly = 1) { repo.count("sample") }
                verify(exactly = 1) { repo.findAll("sample", 5, 5) }
            }

            test("/api/vrp-vehicles/by-depots") {
                val vehicles = Fixtures.vehicles()
                coEvery { repo.listByDepots(any()) } returns vehicles.asFlow()

                client
                    .get()
                    .uri("/api/vrp-vehicles/by-depots?ids=1,2")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$[0].id")
                    .isEqualTo(1)
                    .jsonPath("$[0].name")
                    .isEqualTo("Vehicle 0")
                    .jsonPath("$[0].capacity")
                    .isEqualTo(31)

                verify(exactly = 1) { repo.listByDepots(listOf(1, 2)) }
            }
        }

        context("POST") {
            test("/api/vrp-vehicles/insert") {
                val vehicle = Fixtures.vehicle("vehicle-0")
                coEvery { repo.create(any()) } just runs

                client
                    .post()
                    .uri("/api/vrp-vehicles/insert")
                    .bodyValue(vehicle.copy(id = -1))
                    .exchange()
                    .expectStatus()
                    .isOk

                coVerify(exactly = 1) { repo.create(vehicle.copy(id = -1)) }
            }
        }

        context("DELETE") {
            test("/api/vrp-vehicles/:id/remove") {
                coEvery { repo.deleteById(any()) } just runs

                client
                    .delete()
                    .uri("/api/vrp-vehicles/1/remove")
                    .exchange()
                    .expectStatus()
                    .isOk

                coVerify(exactly = 1) { repo.deleteById(1) }
            }
        }

        context("PUT") {
            test("/api/vrp-vehicles/:id/update") {
                val vehicle = Fixtures.vehicle("vehicle-0")
                coEvery { repo.update(any(), any()) } just runs

                client
                    .put()
                    .uri("/api/vrp-vehicles/1/update")
                    .bodyValue(vehicle)
                    .exchange()
                    .expectStatus()
                    .isOk

                coVerify(exactly = 1) { repo.update(1, vehicle) }
            }
        }
    }
}