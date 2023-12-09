package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Location
import kotlinx.coroutines.flow.Flow

interface VrpLocationPort {

    fun findAll(query: String = "", offset: Int = 0, limit: Int = 25): Flow<Location>

    suspend fun count(query: String = ""): Long

    suspend fun deleteById(locationId: Long)

    suspend fun update(id: Long, location: Location)

    fun listAllByKind(kind: String): Flow<Location>
}