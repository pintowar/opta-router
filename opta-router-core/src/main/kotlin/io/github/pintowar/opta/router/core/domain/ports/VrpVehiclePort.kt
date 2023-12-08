package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import kotlinx.coroutines.flow.Flow

interface VrpVehiclePort {

    fun findAll(offset: Int = 0, limit: Int = 25): Flow<Vehicle>

    suspend fun count(): Long

    suspend fun deleteById(id: Long)

    suspend fun update(id: Long, vehicle: Vehicle)
}