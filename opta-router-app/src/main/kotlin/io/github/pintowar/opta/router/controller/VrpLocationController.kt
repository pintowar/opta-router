package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpLocationPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile(ConfigData.REST_PROFILE)
@RequestMapping("/api/vrp-locations")
class VrpLocationController(
    private val repo: VrpLocationPort
) {
    data class LocationRequest(
        val id: Long,
        val name: String,
        val lat: Double,
        val lng: Double,
        val demand: Int? = null
    ) {
        fun toLocation() = demand?.let { Customer(id, name, lat, lng, it) } ?: Depot(id, name, lat, lng)
    }

    /**
     * Retrieves a paginated list of VRP locations.
     *
     * @param page The page number to retrieve (0-indexed).
     * @param size The number of items per page.
     * @param q A query string to filter locations by name.
     * @return A [Page] of [Location] objects.
     */
    @GetMapping
    suspend fun index(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "25") size: Int,
        @RequestParam("q", defaultValue = "") q: String
    ): Page<Location> {
        val count = repo.count(q)
        val locations = repo.findAll(q, page * size, size).toList()
        return PageImpl(locations, PageRequest.of(page, size), count)
    }

    /**
     * Retrieves a flow of VRP locations filtered by their kind (e.g., "depot" or "customer").
     *
     * @param kind The type of location to retrieve ("depot" or "customer").
     * @return A [Flow] of [Location] objects matching the specified kind.
     */
    @GetMapping("/{kind}")
    fun list(
        @PathVariable kind: String
    ): Flow<Location> = repo.listAllByKind(kind)

    /**
     * Inserts a new VRP location.
     *
     * @param req The [LocationRequest] containing the details of the location to insert.
     * @return A [ResponseEntity] with a 200 OK status if the insertion is successful.
     */
    @PostMapping("/insert", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun insert(
        @RequestBody req: LocationRequest
    ): ResponseEntity<Unit> =
        repo
            .create(req.toLocation())
            .let { ResponseEntity.ok().build() }

    /**
     * Removes a VRP location by its ID.
     *
     * @param id The ID of the location to remove.
     * @return A [ResponseEntity] with a 200 OK status if the removal is successful.
     */
    @DeleteMapping("/{id}/remove", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun remove(
        @PathVariable id: Long
    ): ResponseEntity<Unit> =
        repo
            .deleteById(id)
            .let { ResponseEntity.ok().build() }

    /**
     * Updates an existing VRP location.
     *
     * @param id The ID of the location to update.
     * @param req The [LocationRequest] containing the updated details of the location.
     * @return A [ResponseEntity] with a 200 OK status if the update is successful.
     */
    @PutMapping("/{id}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody req: LocationRequest
    ): ResponseEntity<Unit> =
        repo
            .update(id, req.toLocation())
            .let { ResponseEntity.ok().build() }
}