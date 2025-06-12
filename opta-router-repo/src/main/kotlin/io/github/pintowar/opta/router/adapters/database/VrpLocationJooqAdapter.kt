package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpLocationPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.generated.tables.references.LOCATION
import java.time.Instant

class VrpLocationJooqAdapter(
    private val dsl: DSLContext
) : VrpLocationPort {
    override fun findAll(
        query: String,
        offset: Int,
        limit: Int
    ): Flow<Location> {
        return dsl
            .selectFrom(LOCATION)
            .where(LOCATION.NAME.likeIgnoreCase("${query.trim()}%"))
            .limit(offset, limit)
            .asFlow()
            .map { loc ->
                when (loc.kind) {
                    "depot" -> Depot(loc.id!!, loc.name, loc.latitude, loc.longitude)
                    else -> Customer(loc.id!!, loc.name, loc.latitude, loc.longitude, loc.demand)
                }
            }
    }

    override suspend fun count(query: String): Long {
        val (total) =
            dsl.selectCount().from(LOCATION).where(LOCATION.NAME.likeIgnoreCase("${query.trim()}%"))
                .awaitSingle()
        return total.toLong()
    }

    override suspend fun create(location: Location) {
        val (kind, demand) = if (location is Customer) "customer" to location.demand else "depot" to 0
        val now = Instant.now()

        dsl.insertInto(LOCATION)
            .set(LOCATION.NAME, location.name)
            .set(LOCATION.LATITUDE, location.lat)
            .set(LOCATION.LONGITUDE, location.lng)
            .set(LOCATION.KIND, kind)
            .set(LOCATION.DEMAND, demand)
            .set(LOCATION.CREATED_AT, now)
            .set(LOCATION.UPDATED_AT, now)
            .awaitSingle()
    }

    override suspend fun deleteById(locationId: Long) {
        dsl
            .deleteFrom(LOCATION)
            .where(LOCATION.ID.eq(locationId))
            .awaitFirstOrNull()
    }

    override suspend fun update(
        id: Long,
        location: Location
    ) {
        val (kind, demand) = if (location is Customer) "customer" to location.demand else "depot" to 0
        val now = Instant.now()

        dsl.update(LOCATION)
            .set(LOCATION.NAME, location.name)
            .set(LOCATION.LATITUDE, location.lat)
            .set(LOCATION.LONGITUDE, location.lng)
            .set(LOCATION.KIND, kind)
            .set(LOCATION.DEMAND, demand)
            .set(LOCATION.UPDATED_AT, now)
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