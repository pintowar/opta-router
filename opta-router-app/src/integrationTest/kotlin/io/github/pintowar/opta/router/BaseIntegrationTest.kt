package io.github.pintowar.opta.router

import com.ninjasquad.springmockk.MockkBean
import io.github.pintowar.opta.router.adapters.database.util.TestUtils
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseIntegrationTest : FunSpec() {
    @Autowired
    lateinit var client: WebTestClient

    @MockkBean
    lateinit var geoPort: GeoPort

    init {
        extensions(SpringExtension())
//        coroutineTestScope = true

        val dsl =
            TestUtils.initDB(
                "jdbc:h2:file:~/.opta.router/test.h2.db",
                "r2dbc:h2:file:///~/.opta.router/test.h2.db"
            )

        beforeEach {
            TestUtils.cleanTables(dsl)
            TestUtils.runInitScript(dsl)
        }
    }
}