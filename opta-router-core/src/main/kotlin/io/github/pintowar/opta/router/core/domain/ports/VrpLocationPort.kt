package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Location
import kotlinx.coroutines.flow.Flow

interface VrpLocationPort {

    fun locations(offset: Int = 0, limit: Int = 25): Flow<Location>

    suspend fun totalLocations(): Long

    suspend fun deleteById(locationId: Long)
}