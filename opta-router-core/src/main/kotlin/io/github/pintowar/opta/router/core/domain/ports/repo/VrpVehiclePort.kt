package io.github.pintowar.opta.router.core.domain.ports.repo

import io.github.pintowar.opta.router.core.domain.models.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * The VrpVehiclePort is responsible for managing the persistence of [Vehicle] data.
 */
interface VrpVehiclePort {
    /**
     * Finds all vehicles matching a given query, with pagination, and their associated depot information.
     *
     * This function retrieves a paginated list of vehicles, joining with the `LOCATION` table
     * to include details about their depot. Results are filtered by vehicle name (case-insensitive).
     *
     * @param query The search query string to filter vehicles by name (case-insensitive).
     * @param offset The starting offset for pagination.
     * @param limit The maximum number of results to return.
     * @return A [Flow] of [Vehicle] objects that match the criteria.
     */
    fun findAll(
        query: String = "",
        offset: Int = 0,
        limit: Int = 25
    ): Flow<Vehicle>

    /**
     * Counts the number of vehicles matching a given query.
     *
     * @param query The search query string to filter vehicles by name (case-insensitive).
     * @return The total number of vehicles that match the criteria.
     */
    suspend fun count(query: String = ""): Long

    /**
     * Creates a new vehicle in the database.
     *
     * @param vehicle The [Vehicle] object to be created.
     */
    suspend fun create(vehicle: Vehicle)

    /**
     * Deletes a vehicle by its ID.
     *
     * @param id The ID of the vehicle to be deleted.
     */
    suspend fun deleteById(id: Long)

    /**
     * Updates an existing vehicle in the database.
     *
     * @param id The ID of the vehicle to be updated.
     * @param vehicle The [Vehicle] object containing the updated details.
     */
    suspend fun update(
        id: Long,
        vehicle: Vehicle
    )

    /**
     * Lists all vehicles associated with a given list of depot IDs.
     *
     * This function retrieves vehicles that are linked to any of the specified depot IDs.
     *
     * @param depotIds A [List] of depot IDs to filter vehicles by.
     * @return A [Flow] of [Vehicle] objects associated with the provided depot IDs.
     */
    fun listByDepots(depotIds: List<Long>): Flow<Vehicle>
}