package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.github.pintowar.opta.router.core.domain.ports.VrpVehiclePort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.generated.tables.references.LOCATION
import org.jooq.generated.tables.references.VEHICLE
import org.jooq.generated.tables.references.VRP_PROBLEM
import org.jooq.generated.tables.references.VRP_PROBLEM_LOCATION

class VrpVehicleJooqAdapter(
    private val dsl: DSLContext
) : VrpVehiclePort {

    override fun findAll(query: String, offset: Int, limit: Int): Flow<Vehicle> {
        return dsl
            .select(VEHICLE, LOCATION)
            .from(
                VEHICLE
                    .leftJoin(LOCATION).on(LOCATION.ID.eq(VEHICLE.DEPOT_ID))
            )
            .where(
                LOCATION.KIND.eq("depot")
                    .and(VEHICLE.NAME.likeIgnoreCase("${query.trim()}%"))
            )
            .limit(offset, limit)
            .asFlow()
            .map { (vehicle, loc) ->
                Depot(loc.id!!, loc.name, loc.latitude, loc.longitude).let { depot ->
                    Vehicle(vehicle.id!!, vehicle.name, vehicle.capacity, depot)
                }
            }
    }

    override suspend fun count(query: String): Long {
        val (total) = dsl.selectCount().from(VEHICLE).where(VEHICLE.NAME.likeIgnoreCase("${query.trim()}%"))
            .awaitSingle()
        return total.toLong()
    }

    override suspend fun deleteById(id: Long) {
        dsl
            .deleteFrom(VEHICLE)
            .where(VEHICLE.ID.eq(id))
            .awaitFirstOrNull()
    }

    override suspend fun update(id: Long, vehicle: Vehicle) {
        dsl.update(VEHICLE)
            .set(VEHICLE.NAME, vehicle.name)
            .set(VEHICLE.CAPACITY, vehicle.capacity)
            .set(VEHICLE.DEPOT_ID, vehicle.depot.id)
            .where(VEHICLE.ID.eq(id))
            .awaitSingle()
    }
}