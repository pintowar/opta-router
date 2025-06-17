package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.adapters.database.util.TestUtils
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import io.github.pintowar.opta.router.core.serialization.Serde
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.runBlocking
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList

class VrpProblemJooqAdapterTest : FunSpec({

    coroutineTestScope = true

    val geoPort: GeoPort = mockk(relaxed = true)
    val serde: Serde = TestUtils.serde()

    val dsl = TestUtils.initDB()
    val adapter = VrpProblemJooqAdapter(dsl, geoPort, serde)

    beforeSpec {
        runBlocking { TestUtils.cleanTables(dsl) }
    }

    beforeEach {
        runBlocking { TestUtils.runInitScript(dsl) }

        // Mock geoPort behavior
        coEvery { geoPort.generateMatrix(any()) } returns VrpProblemMatrix(
            listOf(1L, 2L),
            listOf(0.0, 10.0, 10.0, 0.0),
            listOf(0L, 100L, 100L, 0L)
        )
    }

    afterEach {
        clearMocks(geoPort)
        runBlocking { TestUtils.cleanTables(dsl) }
    }

    test("findAll should return problem summaries") {
        val summaries = adapter.findAll("", 0, 10).toList()
        summaries.size shouldBe 8
    }

    test("count should return the correct number of problems") {
        val count = adapter.count("")
        count shouldBe 8
    }

    test("getById should return a VrpProblem if found") {
        val problem = adapter.getById(1L)
        problem?.name shouldBe "sample-1"
    }

    test("create should insert a new VrpProblem and its matrix") {
        val depot = Depot(1L, "Depot 1", 0.0, 0.0)
        val customer = Customer(2L, "Customer 1", 1.0, 1.0, 10)
        val vehicle = Vehicle(1L, "Vehicle 1", 100, depot)
        val newProblem = VrpProblem(-1, "Test Problem", listOf(vehicle), listOf(customer))

        adapter.create(newProblem)

        val found = adapter.findAll("Test Problem", 0, 1).toList()
        found.size shouldBe 1
        found.first().name shouldBe "Test Problem"

        val matrix = adapter.getMatrixById(found.first().id)
        matrix?.getLocationIds() shouldBe arrayOf(1L, 2L)
        matrix?.getTravelDistances() shouldBe arrayOf(0.0, 10.0, 10.0, 0.0)
        matrix?.getTravelTimes() shouldBe arrayOf(0L, 100L, 100L, 0L)
    }

    test("deleteById should remove a VrpProblem") {
        // Create a problem first to delete
        val depot = Depot(1L, "Depot 1", 0.0, 0.0)
        val customer = Customer(2L, "Customer 1", 1.0, 1.0, 10)
        val vehicle = Vehicle(1L, "Vehicle 1", 100, depot)
        val problemToDelete = VrpProblem(0, "Problem to Delete", listOf(vehicle), listOf(customer))
        adapter.create(problemToDelete)

        val createdProblem = adapter.findAll("Problem to Delete", 0, 1).toList().first()
        adapter.deleteById(createdProblem.id)

        val found = adapter.getById(createdProblem.id)
        found shouldBe null
    }

    test("update should modify an existing VrpProblem and its matrix") {
        val depot = Depot(1L, "Depot 1", 0.0, 0.0)
        val customer = Customer(2L, "Customer 1", 1.0, 1.0, 10)
        val vehicle = Vehicle(1L, "Vehicle 1", 100, depot)
        val problemToUpdate = VrpProblem(0, "Problem to Update", listOf(vehicle), listOf(customer))
        adapter.create(problemToUpdate)

        val createdProblem = adapter.findAll("Problem to Update", 0, 1).toList().first()

        val updatedCustomer = Customer(3L, "Updated Customer", 2.0, 2.0, 20)
        val updatedProblem =
            VrpProblem(createdProblem.id, "Updated Problem", listOf(vehicle), listOf(updatedCustomer))

        coEvery { geoPort.generateMatrix(any()) } returns VrpProblemMatrix(
            listOf(1L, 3L),
            listOf(0.0, 20.0, 20.0, 0.0),
            listOf(0L, 200L, 200L, 0L)
        )

        adapter.update(createdProblem.id, updatedProblem)

        val found = adapter.getById(createdProblem.id)
        found?.name shouldBe "Updated Problem"
        found?.customers?.first()?.name shouldBe "Updated Customer"

        val matrix = adapter.getMatrixById(createdProblem.id)
        matrix?.getLocationIds() shouldBe arrayOf(1L, 3L)
        matrix?.getTravelDistances() shouldBe arrayOf(0.0, 20.0, 20.0, 0.0)
        matrix?.getTravelTimes() shouldBe arrayOf(0L, 200L, 200L, 0L)
    }

    test("getMatrixById should return the problem matrix") {
        val depot = Depot(1L, "Depot 1", 0.0, 0.0)
        val customer = Customer(2L, "Customer 1", 1.0, 1.0, 10)
        val vehicle = Vehicle(1L, "Vehicle 1", 100, depot)
        val problemWithMatrix = VrpProblem(0, "Problem With Matrix", listOf(vehicle), listOf(customer))
        adapter.create(problemWithMatrix)

        val createdProblem = adapter.findAll("Problem With Matrix", 0, 1).toList().first()
        val matrix = adapter.getMatrixById(createdProblem.id)
        matrix?.getLocationIds() shouldBe arrayOf(1L, 2L)
        matrix?.getTravelDistances() shouldBe arrayOf(0.0, 10.0, 10.0, 0.0)
        matrix?.getTravelTimes() shouldBe arrayOf(0L, 100L, 100L, 0L)
    }
})
