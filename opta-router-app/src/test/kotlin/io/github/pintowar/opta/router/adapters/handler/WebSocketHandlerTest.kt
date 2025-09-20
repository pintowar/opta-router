package io.github.pintowar.opta.router.adapters.handler

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.serialization.Serde
import io.github.pintowar.opta.router.core.solver.SolverPanelStorage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.net.URI

@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketHandlerTest :
    FunSpec({

        val solverPanelStorage = mockk<SolverPanelStorage>()
        val serde = mockk<Serde>()
        val handler = WebSocketHandler(solverPanelStorage, serde)

        val problem = Fixtures.problem("sample-4")
        val solution = Fixtures.solution("sample-4").last()

        beforeEach {
            clearMocks(solverPanelStorage, serde)
        }

        test("fromChannel should filter and map messages").config(coroutineTestScope = true) {
            val instanceId = problem.id.toString()
            val webSessionId = "ws1"
            val request = VrpSolutionRequest(solution, SolverStatus.NOT_SOLVED)
            val convertedSolution = mockk<VrpSolution>()
            val jsonRequest = "some json"
            val otherSolution = solution.copy(problem = solution.problem.copy(id = 2))

            coEvery {
                solverPanelStorage.convertSolutionForPanelId(webSessionId, solution)
            } returns convertedSolution
            every { serde.toJson(request.copy(solution = convertedSolution)) } returns jsonRequest

            val results = mutableListOf<String>()
//        runBlockingTest {
            runTest(UnconfinedTestDispatcher()) {
                val job =
                    launch {
                        handler.fromChannel(webSessionId, instanceId).toList(results)
                    }

                handler.broadcast(request)
                handler.broadcast(request.copy(solution = otherSolution))
                job.cancel()
            }
            results shouldHaveSize 1
            results.first() shouldBe jsonRequest
        }

        test("fromChannel should return empty flow if instanceId is null").config(coroutineTestScope = true) {
            val webSessionId = "ws1"
            val request = VrpSolutionRequest(solution, SolverStatus.NOT_SOLVED)

            val results = mutableListOf<String>()
            val job =
                launch {
                    handler.fromChannel(webSessionId, null).toList(results)
                }

            handler.broadcast(request)

            results.shouldBeEmpty()
            job.cancel()
        }

        test("handle should complete empty if websession id is not present") {
            val session = mockk<WebSocketSession>()
            every { session.attributes } returns emptyMap()

            handler.handle(session) shouldBe Mono.empty()
        }

        test("handle should setup pipeline and send messages") {
            val session = mockk<WebSocketSession>(relaxed = true)
            val handshakeInfo = mockk<HandshakeInfo>()
            val instanceId = problem.id.toString()
            val uri = URI.create("/ws/solution-state/$instanceId")
            val webSessionId = "ws1"

            every { session.attributes } returns mapOf(ConfigData.WEBSESSION_ID to webSessionId)
            every { session.handshakeInfo } returns handshakeInfo
            every { handshakeInfo.uri } returns uri
            every { session.send(any()) } returns Mono.empty()

            val request = VrpSolutionRequest(solution, SolverStatus.NOT_SOLVED)
            val convertedSolution = mockk<VrpSolution>()
            val jsonRequest = "some json"

            coEvery { solverPanelStorage.convertSolutionForPanelId(webSessionId, solution) } returns convertedSolution
            coEvery { serde.toJson(request.copy(solution = convertedSolution)) } returns jsonRequest

            handler.handle(session).subscribe()
        }
    })