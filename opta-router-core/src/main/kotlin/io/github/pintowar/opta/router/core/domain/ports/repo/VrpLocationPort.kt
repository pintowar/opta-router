package io.github.pintowar.opta.router.core.domain.ports.repo

import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Location
import kotlinx.coroutines.flow.Flow

/**
 * The VrpLocationPort is responsible for managing the persistence of [Location] data.
 */
interface VrpLocationPort {
    /**
     * Finds all locations matching a given query, with pagination.
     *
     * @param query The search query string to filter locations by name (case-insensitive).
     * @param offset The starting offset for pagination.
     * @param limit The maximum number of results to return.
     * @return A [Flow] of [Location] objects that match the criteria.
     */
    fun findAll(
        query: String = "",
        offset: Int = 0,
        limit: Int = 25
    ): Flow<Location>

    /**
     * Counts the number of locations matching a given query.
     *
     * @param query The search query string to filter locations by name (case-insensitive).
     * @return The total number of locations that match the criteria.
     */
    suspend fun count(query: String = ""): Long

    /**
     * Creates a new location in the database.
     *
     * @param location The [Location] object to be created. It can be either a [Customer] or a [Depot].
     */
    suspend fun create(location: Location)

    /**
     * Deletes a location by its ID.
     *
     * @param locationId The ID of the location to be deleted.
     */
    suspend fun deleteById(locationId: Long)

    /**
     * Updates an existing location in the database.
     *
     * @param id The ID of the location to be updated.
     * @param location The [Location] object containing the updated details. It can be either a [Customer] or a [Depot].
     */
    suspend fun update(
        id: Long,
        location: Location
    )

    /**
     * Lists all locations of a specific kind (e.g., "depot" or "customer").
     *
     * @param kind The type of location to retrieve ("depot" or "customer").
     * @return A [Flow] of [Location] objects that match the specified kind.
     */
    fun listAllByKind(kind: String): Flow<Location>
}