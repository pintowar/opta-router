package io.github.pintowar.opta.router.core.domain.ports.repo

import io.github.pintowar.opta.router.core.domain.models.Vehicle
import kotlinx.coroutines.flow.Flow

interface VrpVehiclePort {

    fun findAll(query: String = "", offset: Int = 0, limit: Int = 25): Flow<Vehicle>

    suspend fun count(query: String = ""): Long

    suspend fun create(vehicle: Vehicle)

    suspend fun deleteById(id: Long)

    suspend fun update(id: Long, vehicle: Vehicle)

    fun listByDepots(depotIds: List<Long>): Flow<Vehicle>
}