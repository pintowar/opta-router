package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.ports.VrpLocationPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.generated.tables.references.LOCATION

class VrpLocationJooqAdapter(
    private val dsl: DSLContext
) : VrpLocationPort {

    override fun findAll(offset: Int, limit: Int): Flow<Location> {
        return dsl
            .selectFrom(LOCATION)
            .limit(offset, limit)
            .asFlow()
            .map { loc ->
                when (loc.kind) {
                    "depot" -> Depot(loc.id!!, loc.name, loc.latitude, loc.longitude)
                    else -> Customer(loc.id!!, loc.name, loc.latitude, loc.longitude, loc.demand)
                }
            }
    }

    override suspend fun count(): Long {
        val (total) = dsl.selectCount().from(LOCATION).awaitSingle()
        return total.toLong()
    }

    override suspend fun deleteById(locationId: Long) {
        dsl
            .deleteFrom(LOCATION)
            .where(LOCATION.ID.eq(locationId))
            .awaitFirstOrNull()
    }

    override suspend fun update(id: Long, location: Location) {
        val (kind, demand) = if (location is Customer) "customer" to location.demand else "depot" to 0

        dsl.update(LOCATION)
            .set(LOCATION.NAME, location.name)
            .set(LOCATION.LATITUDE, location.lat)
            .set(LOCATION.LONGITUDE, location.lng)
            .set(LOCATION.KIND, kind)
            .set(LOCATION.DEMAND, demand)
            .where(LOCATION.ID.eq(id))
            .awaitSingle()
    }

    override fun listAllByKind(kind: String): Flow<Location> {
        return dsl
            .selectFrom(LOCATION)
            .where(LOCATION.KIND.eq(kind))
            .asFlow()
            .map { loc ->
                when (kind) {
                    "depot" -> Depot(loc.id!!, loc.name, loc.latitude, loc.longitude)
                    else -> Customer(loc.id!!, loc.name, loc.latitude, loc.longitude, loc.demand)
                }
            }
    }
}