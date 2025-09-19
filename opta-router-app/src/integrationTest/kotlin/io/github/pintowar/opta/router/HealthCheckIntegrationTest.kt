package io.github.pintowar.opta.router

class HealthCheckIntegrationTest : BaseIntegrationTest() {
    init {

        test("should return health status") {
            val exchange = client.get().uri("/actuator/health").exchange()

            exchange.expectStatus().isOk
        }
    }
}