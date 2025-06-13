package io.github.pintowar.opta.router.core.domain.models.matrix

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class VrpCachedMatrixTest : FunSpec({
    val mockMatrix: Matrix = mockk()
    val cachedMatrix = VrpCachedMatrix(mockMatrix)

    afterTest {
        clearMocks(mockMatrix)
    }

    test("distance should cache result and call underlying matrix only once") {
        val originId = 1L
        val targetId = 2L
        val expectedDistance = 100.0

        // Step 4: Configure mock to return a value and verify call count
        every { mockMatrix.distance(originId, targetId) } returns expectedDistance

        // First call - should call underlying matrix
        val distance1 = cachedMatrix.distance(originId, targetId)
        expectedDistance shouldBe distance1

        // Second call with same IDs - should use cache, not call underlying matrix again
        val distance2 = cachedMatrix.distance(originId, targetId)
        expectedDistance shouldBe distance2

        // Verify that the underlying matrix.distance was called exactly once for this pair
        verify(exactly = 1) { mockMatrix.distance(originId, targetId) }

        // Test with a different pair to ensure independent caching
        val originId2 = 3L
        val targetId2 = 4L
        val expectedDistance2 = 250.0
        every { mockMatrix.distance(originId2, targetId2) } returns expectedDistance2

        val distance3 = cachedMatrix.distance(originId2, targetId2)
        expectedDistance2 shouldBe distance3

        verify(exactly = 1) { mockMatrix.distance(originId2, targetId2) }
        verify(exactly = 1) { mockMatrix.distance(originId, targetId) } // Ensure first call count is unchanged
    }

    test("time should cache result and call underlying matrix only once") {
        val originId = 5L
        val targetId = 6L
        val expectedTime = 500L

        // Step 4: Configure mock to return a value and verify call count
        every { mockMatrix.time(originId, targetId) } returns expectedTime

        // First call - should call underlying matrix
        val time1 = cachedMatrix.time(originId, targetId)
        expectedTime shouldBe time1

        // Second call with same IDs - should use cache, not call underlying matrix again
        val time2 = cachedMatrix.time(originId, targetId)
        expectedTime shouldBe time2

        // Verify that the underlying matrix.time was called exactly once for this pair
        verify(exactly = 1) { mockMatrix.time(originId, targetId) }

        // Test with a different pair to ensure independent caching
        val originId2 = 7L
        val targetId2 = 8L
        val expectedTime2 = 750L
        every { mockMatrix.time(originId2, targetId2) } returns expectedTime2

        val time3 = cachedMatrix.time(originId2, targetId2)
        expectedTime2 shouldBe time3

        verify(exactly = 1) { mockMatrix.time(originId2, targetId2) }
        verify(exactly = 1) { mockMatrix.time(originId, targetId) } // Ensure first call count is unchanged
    }

    test("caching should handle different pairs independently") {
        val pair1Origin = 10L
        val pair1Target = 11L
        val pair2Origin = 12L
        val pair2Target = 13L
        val expectedDistance1 = 150.0
        val expectedDistance2 = 300.0

        every { mockMatrix.distance(pair1Origin, pair1Target) } returns expectedDistance1
        every { mockMatrix.distance(pair2Origin, pair2Target) } returns expectedDistance2

        // Call both pairs once
        cachedMatrix.distance(pair1Origin, pair1Target)
        cachedMatrix.distance(pair2Origin, pair2Target)

        // Call both pairs again
        cachedMatrix.distance(pair1Origin, pair1Target)
        cachedMatrix.distance(pair2Origin, pair2Target)

        // Verify each underlying call was made exactly once
        verify(exactly = 1) { mockMatrix.distance(pair1Origin, pair1Target) }
        verify(exactly = 1) { mockMatrix.distance(pair2Origin, pair2Target) }
    }

    test("caching should handle same origin and target IDs") {
        val nodeId = 20L
        val expectedDistance = 0.0 // Typically distance to self is 0
        val expectedTime = 0L // Typically time to self is 0

        every { mockMatrix.distance(nodeId, nodeId) } returns expectedDistance
        every { mockMatrix.time(nodeId, nodeId) } returns expectedTime

        // Test distance
        cachedMatrix.distance(nodeId, nodeId)
        cachedMatrix.distance(nodeId, nodeId)
        verify(exactly = 1) { mockMatrix.distance(nodeId, nodeId) }

        // Test time
        cachedMatrix.time(nodeId, nodeId)
        cachedMatrix.time(nodeId, nodeId)
        verify(exactly = 1) { mockMatrix.time(nodeId, nodeId) }
    }
})