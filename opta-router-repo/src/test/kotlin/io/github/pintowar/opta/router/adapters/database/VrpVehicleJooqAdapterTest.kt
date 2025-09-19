package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.jooq.generated.tables.references.LOCATION

class VrpVehicleJooqAdapterTest : BaseJooqTest() {
    val adapter = VrpVehicleJooqAdapter(dsl)

    init {

        test("findAll should return vehicles based on query, offset, and limit") {
            val vehicles = adapter.findAll("Vehicle", 0, 50).toList()
            vehicles.size shouldBe 10
            vehicles.first().name shouldBe "Vehicle 0"
        }

        test("count should return the correct number of vehicles based on query") {
            val count = adapter.count("Vehicle")
            count shouldBe 10
        }

        test("create should insert a new vehicle") {
            val depot = Depot(1L, "Test Depot", 0.0, 0.0)
            val newVehicle = Vehicle(0, "New Vehicle", 200, depot)
            adapter.create(newVehicle)

            val found = adapter.findAll("New Vehicle", 0, 1).toList()
            found.size shouldBe 1
            found.first().name shouldBe "New Vehicle"
        }

        test("deleteById should remove a vehicle") {
            val depot = Depot(1L, "Test Depot", 0.0, 0.0)
            val vehicleToDelete = Vehicle(0, "Vehicle To Delete", 50, depot)
            adapter.create(vehicleToDelete)

            val createdVehicle = adapter.findAll("Vehicle To Delete", 0, 1).toList().first()
            adapter.deleteById(createdVehicle.id!!)

            val found = adapter.findAll("Vehicle To Delete", 0, 10).toList()
            found.none { it.id == createdVehicle.id } shouldBe true
        }

        test("update should modify an existing vehicle") {
            val depot = Depot(1L, "Test Depot", 0.0, 0.0)
            val vehicleToUpdate = Vehicle(0, "Old Vehicle Name", 100, depot)
            adapter.create(vehicleToUpdate)

            val createdVehicle = adapter.findAll("Old Vehicle Name", 0, 1).toList().first()
            val updatedVehicle = Vehicle(createdVehicle.id, "Updated Vehicle Name", 250, depot)
            adapter.update(createdVehicle.id, updatedVehicle)

            val found = adapter.findAll("Updated Vehicle Name", 0, 1).toList()
            found.size shouldBe 1
            found.first().name shouldBe "Updated Vehicle Name"
            found.first().capacity shouldBe 250
        }

        test("listByDepots should return vehicles associated with given depot IDs") {
            val (depotId) =
                dsl
                    .select(LOCATION.ID)
                    .from(LOCATION)
                    .where(LOCATION.KIND.eq("depot"))
                    .awaitFirst()

            val vehicles = adapter.listByDepots(listOf(depotId!!)).toList()
            vehicles.size shouldBe 10
            vehicles.first().name shouldBe "Vehicle 0"
        }
    }
}