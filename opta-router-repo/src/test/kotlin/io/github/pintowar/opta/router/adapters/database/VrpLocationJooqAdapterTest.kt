package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.adapters.database.util.TestUtils
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.runBlocking
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import org.jooq.exception.IntegrityConstraintViolationException

class VrpLocationJooqAdapterTest :
    FunSpec({

        coroutineTestScope = true

        val dsl = TestUtils.initDB()
        val adapter = VrpLocationJooqAdapter(dsl)

        beforeSpec {
            runBlocking { TestUtils.cleanTables(dsl) }
        }

        beforeEach {
            runBlocking { TestUtils.runInitScript(dsl) }
        }

        afterEach {
            runBlocking { TestUtils.cleanTables(dsl) }
        }

        test("findAll should return locations based on query, offset, and limit") {
            val locations = adapter.findAll("chie", 0, 10).toList()
            locations.size shouldBe 1
            locations.first().name shouldBe "CHIEVRES"
        }

        test("count should return the correct number of locations based on query") {
            val count = adapter.count("chie")
            count shouldBe 1
        }

        test("create should insert a new location") {
            val newLocation = Customer(0, "New Customer", 1.0, 1.0, 10)
            adapter.create(newLocation)

            val found = adapter.findAll("New Customer", 0, 1).toList()
            found.size shouldBe 1
            found.first().name shouldBe "New Customer"
        }

        test("update should modify an existing location") {
            val locationToUpdateId = 2L
            val updatedLocation = Depot(locationToUpdateId, "Updated Depot", 2.0, 2.0)
            adapter.update(locationToUpdateId, updatedLocation)

            val found = adapter.findAll("Updated Depot", 0, 1).toList()
            found.size shouldBe 1
            found.first().name shouldBe "Updated Depot"
            found.first().lat shouldBe 2.0
        }

        test("listAllByKind should return locations of a specific kind") {
            val depots = adapter.listAllByKind("depot").toList()
            depots.all { it is Depot } shouldBe true
        }

        context("deleteById should remove a location") {
            test("failed deletion") {
                val locationToDeleteId = 1L
                shouldThrow<IntegrityConstraintViolationException> {
                    adapter.deleteById(locationToDeleteId)
                }
            }

            test("successful deletion") {
                val newLocation = Customer(-1, "New Customer", 1.0, 1.0, 10)
                adapter.create(newLocation)

                val newCustomer = adapter.findAll("New Customer", 0, 1).first()
                adapter.deleteById(newCustomer.id)

                adapter.findAll("New Customer", 0, 1).count() shouldBe 0
            }
        }
    })