package io.github.pintowar.opta.router.adapters.geo

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import com.graphhopper.util.PointList
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Route
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP

class GraphHopperGeoAdapterTest :
    FunSpec({

        val path = "some/path"
        val location = "some/location"

        val graphHopperMock = mockk<GraphHopper>(relaxed = true)
        val ghResponseMock = mockk<GHResponse>(relaxed = true)
        val responsePathMock = mockk<ResponsePath>(relaxed = true)

        beforeEach {
            mockkConstructor(GraphHopper::class)
            every { anyConstructed<GraphHopper>().importOrLoad() } returns graphHopperMock
            every { graphHopperMock.route(any()) } returns ghResponseMock
            every { ghResponseMock.best } returns responsePathMock
        }

        afterEach {
            clearMocks(graphHopperMock, ghResponseMock, responsePathMock)
        }

        test("simplePath should return a path with correct properties") {
            val origin = LatLng(1.0, 1.0)
            val target = LatLng(2.0, 2.0)
            val expectedDistance = 1000.0
            val expectedTime = 60000L // 1 minute

            every { responsePathMock.distance } returns expectedDistance
            every { responsePathMock.time } returns expectedTime
            every { responsePathMock.points } returns
                PointList(2, false).apply {
                    add(origin.lat, origin.lng)
                    add(target.lat, target.lng)
                }

            val adapter = GraphHopperGeoAdapter(path, location)
            val result = adapter.simplePath(origin, target)

            result.distance shouldBe expectedDistance
            result.time shouldBe expectedTime
            result.coordinates shouldBe listOf(origin, target)

            verify(exactly = 1) { graphHopperMock.route(any<GHRequest>()) }
        }

        test("detailedPaths should process routes correctly") {
            val origin1 = LatLng(1.0, 1.0)
            val target1 = LatLng(2.0, 2.0)
            val origin2 = LatLng(2.0, 2.0)
            val target2 = LatLng(3.0, 3.0)

            val route1 =
                Route(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    listOf(LatLng(origin1.lat, origin1.lng), LatLng(target1.lat, target1.lng)),
                    listOf(1L, 2L)
                )
            val route2 =
                Route(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    listOf(LatLng(origin2.lat, origin2.lng), LatLng(target2.lat, target2.lng)),
                    listOf(2L, 3L)
                )

            val routes = listOf(route1, route2)

            val expectedDistance1 = 1000.0
            val expectedTime1 = 60000L
            val expectedDistance2 = 2000.0
            val expectedTime2 = 120000L

            val pointList1 =
                PointList(2, false).apply {
                    add(origin1.lat, origin1.lng)
                    add(target1.lat, target1.lng)
                }
            val pointList2 =
                PointList(2, false).apply {
                    add(origin2.lat, origin2.lng)
                    add(target2.lat, target2.lng)
                }

            every { responsePathMock.distance } returnsMany listOf(expectedDistance1, expectedDistance2)
            every { responsePathMock.time } returnsMany listOf(expectedTime1, expectedTime2)
            every { responsePathMock.points } returnsMany listOf(pointList1, pointList2)

            val adapter = GraphHopperGeoAdapter(path, location)
            val results = adapter.detailedPaths(routes)

            results.size shouldBe 2

            results[0].distance shouldBe BigDecimal(expectedDistance1 / 1000).setScale(2, HALF_UP)
            results[0].time shouldBe BigDecimal(expectedTime1.toDouble() / (60 * 1000)).setScale(2, HALF_UP)
            results[0].order shouldBe listOf(LatLng(origin1.lat, origin1.lng), LatLng(target1.lat, target1.lng))

            results[1].distance shouldBe BigDecimal(expectedDistance2 / 1000).setScale(2, HALF_UP)
            results[1].time shouldBe BigDecimal(expectedTime2.toDouble() / (60 * 1000)).setScale(2, HALF_UP)
            results[1].order shouldBe listOf(LatLng(origin2.lat, origin2.lng), LatLng(target2.lat, target2.lng))

            verify(exactly = 2) { graphHopperMock.route(any<GHRequest>()) }
        }

        test("generateMatrix should create a correct VrpProblemMatrix") {
            val loc1 = Depot(1L, "Loc1", 1.0, 1.0)
            val loc2 = Depot(2L, "Loc2", 2.0, 2.0)
            val locations = setOf(loc1, loc2)

            val dist11 = 0.0
            val time11 = 0L
            val dist12 = 1000.0
            val time12 = 60000L
            val dist21 = 1000.0
            val time21 = 60000L
            val dist22 = 0.0
            val time22 = 0L

            every { responsePathMock.distance } returnsMany listOf(dist11, dist12, dist21, dist22)
            every { responsePathMock.time } returnsMany listOf(time11, time12, time21, time22)

            val adapter = GraphHopperGeoAdapter(path, location)
            val matrix = adapter.generateMatrix(locations)

            matrix.getLocationIds() shouldBe listOf(1L, 2L)
            matrix.getTravelDistances() shouldBe listOf(dist11, dist12, dist21, dist22)
            matrix.getTravelTimes() shouldBe listOf(time11, time12, time21, time22)

            verify(exactly = 4) { graphHopperMock.route(any<GHRequest>()) }
        }
    })