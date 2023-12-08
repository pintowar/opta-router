package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.ports.VrpLocationPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
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
    )

    @GetMapping
    suspend fun index(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "25") size: Int
    ): Page<Location> {
        val count = repo.count()
        val locations = repo.findAll(page * size, size).toList()
        return PageImpl(locations, PageRequest.of(page, size), count)
    }

    @GetMapping("/{kind}")
    suspend fun list(@PathVariable kind: String): Flow<Location> {
        return repo.listAllByKind(kind)
    }

    @DeleteMapping("/{id}/remove", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun remove(@PathVariable id: Long): ResponseEntity<Unit> {
        return repo.deleteById(id)
            .let { ResponseEntity.ok().build() }
    }

    @PutMapping("/{id}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun update(@PathVariable id: Long, @RequestBody req: LocationRequest): ResponseEntity<Unit> {
        val location = req.demand
            ?.let { Customer(req.id, req.name, req.lat, req.lng, it) }
            ?: Depot(req.id, req.name, req.lat, req.lng)

        return repo.update(id, location)
            .let { ResponseEntity.ok().build() }
    }

}