package io.github.pintowar.opta.router.controller

import com.ninjasquad.springmockk.MockkBean
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@ActiveProfiles(ConfigData.GEO_SERVER_PROFILE)
@WebFluxTest(GeoController::class)
class GeoControllerTest : FunSpec() {
    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var geo: GeoPort

    init {
        extensions(SpringExtension())

        beforeEach {
            clearMocks(geo)
        }

        context("POST") {
            test("/api/geo/simple-path") {
                val path =
                    Fixtures
                        .solution("sample-4")
                        .last()
                        .routes
                        .first()
                        .let { Path(it.distance.toDouble(), it.time.toLong(), it.order) }
                val (origin, target) = path.coordinates.first() to path.coordinates.last()
                coEvery { geo.simplePath(any(), any()) } returns path

                client
                    .post()
                    .uri("/api/geo/simple-path")
                    .bodyValue(GeoController.SimplePathRequest(origin as LatLng, target as LatLng))
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$.distance")
                    .isEqualTo(path.distance)
                    .jsonPath("$.time")
                    .isEqualTo(path.time)

                coVerify(exactly = 1) { geo.simplePath(origin, target) }
            }

            test("/api/geo/generate-matrix") {
                val problem = Fixtures.problem("sample-4")
                val (customers, depots) = problem.customers to problem.vehicles.map { it.depot }.distinct()
                val matrix = Fixtures.matrix("sample-4")

                coEvery { geo.generateMatrix(any()) } returns matrix

                client
                    .post()
                    .uri("/api/geo/generate-matrix")
                    .bodyValue(GeoController.LocationsRequest(depots, customers))
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()

                coVerify(exactly = 1) { geo.generateMatrix((depots + customers).toSet()) }
            }

            test("/api/geo/detailed-paths") {
                val routes = Fixtures.solution("sample-4").last().routes

                coEvery { geo.detailedPaths(any()) } returns routes

                client
                    .post()
                    .uri("/api/geo/detailed-paths")
                    .bodyValue(GeoController.DetailedPathRequest(routes))
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()

                coVerify(exactly = 1) { geo.detailedPaths(routes) }
            }
        }
    }
}