package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.adapters.database.util.TestUtils
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.runBlocking
import org.jooq.DSLContext

abstract class BaseJooqTest : FunSpec() {
    var dsl: DSLContext

    init {
        coroutineTestScope = true
        dsl = TestUtils.initDB()

        beforeEach {
            runBlocking {
                TestUtils.cleanTables(dsl)
                TestUtils.runInitScript(dsl)
            }
        }
    }
}