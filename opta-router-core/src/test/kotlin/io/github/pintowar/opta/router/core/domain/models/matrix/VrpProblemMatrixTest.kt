package io.github.pintowar.opta.router.core.domain.models.matrix

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.NoSuchElementException

class VrpProblemMatrixTest :
    FunSpec({

        val locationIds = longArrayOf(10L, 20L, 30L)

        val travelDistances =
            doubleArrayOf(
                // 0->0, 0->1, 0->2
                0.0,
                10.5,
                20.1,
                // 1->0, 1->1, 1->2
                11.2,
                0.0,
                21.3,
                // 2->0, 2->1, 2->2
                22.4,
                12.5,
                0.0
            )
        val travelTimes =
            longArrayOf(
                // 0->0, 0->1, 0->2
                0L,
                100L,
                200L,
                // 1->0, 1->1, 1->2
                110L,
                0L,
                210L,
                // 2->0, 2->1, 2->2
                220L,
                120L,
                0L
            )
        val matrix = VrpProblemMatrix(locationIds, travelDistances, travelTimes)

        test("distance should return correct distance between different locations") {
            matrix.distance(10L, 20L) shouldBe 10.5 // 0->1
            matrix.distance(20L, 10L) shouldBe 11.2 // 1->0
            matrix.distance(10L, 30L) shouldBe 20.1 // 0->2
            matrix.distance(30L, 20L) shouldBe 12.5 // 2->1
        }

        test("distance should return correct distance from a location to itself") {
            matrix.distance(10L, 10L) shouldBe 0.0 // 0->0
            matrix.distance(20L, 20L) shouldBe 0.0 // 1->1
            matrix.distance(30L, 30L) shouldBe 0.0 // 2->2
        }

        test("distance should throw NoSuchElementException for invalid location ID") {
            shouldThrow<NoSuchElementException> {
                matrix.distance(10L, 99L) // 99L is not in locationIdxById
            }
            shouldThrow<NoSuchElementException> {
                matrix.distance(99L, 20L) // 99L is not in locationIdxById
            }
        }

        test("time should return correct time between different locations") {
            matrix.time(10L, 20L) shouldBe 100L // 0->1
            matrix.time(20L, 10L) shouldBe 110L // 1->0
            matrix.time(10L, 30L) shouldBe 200L // 0->2
            matrix.time(30L, 20L) shouldBe 120L // 2->1
        }

        test("time should return correct time from a location to itself") {
            matrix.time(10L, 10L) shouldBe 0L // 0->0
            matrix.time(20L, 20L) shouldBe 0L // 1->1
            matrix.time(30L, 30L) shouldBe 0L // 2->2
        }

        test("time should throw NoSuchElementException for invalid location ID") {
            shouldThrow<NoSuchElementException> {
                matrix.time(10L, 99L) // 99L is not in locationIdxById
            }
            shouldThrow<NoSuchElementException> {
                matrix.time(99L, 20L) // 99L is not in locationIdxById
            }
        }

        test("test getters") {
            matrix.getLocationIds() shouldBe locationIds
            matrix.getTravelTimes() shouldBe travelTimes
            matrix.getTravelDistances() shouldBe travelDistances
        }

        test("invalid arguments on constructor") {
            shouldThrow<IllegalArgumentException> {
                VrpProblemMatrix(locationIds, travelDistances, travelTimes.dropLast(3).toLongArray())
            }
        }
    })