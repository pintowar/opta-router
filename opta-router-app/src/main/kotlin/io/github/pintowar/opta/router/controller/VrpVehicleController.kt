package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpVehiclePort
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
@RequestMapping("/api/vrp-vehicles")
class VrpVehicleController(
    private val repo: VrpVehiclePort
) {
    @GetMapping
    suspend fun index(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "25") size: Int,
        @RequestParam("q", defaultValue = "") q: String
    ): Page<Vehicle> {
        val count = repo.count(q)
        val locations = repo.findAll(q, page * size, size).toList()
        return PageImpl(locations, PageRequest.of(page, size), count)
    }

    @GetMapping("/by-depots")
    fun listByDepot(
        @RequestParam("ids", defaultValue = "") ids: List<Long>
    ): Flow<Vehicle> = repo.listByDepots(ids)

    @PostMapping("/insert", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun insert(
        @RequestBody vehicle: Vehicle
    ): ResponseEntity<Unit> =
        repo
            .create(vehicle)
            .let { ResponseEntity.ok().build() }

    @DeleteMapping("/{id}/remove", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun remove(
        @PathVariable id: Long
    ): ResponseEntity<Unit> =
        repo
            .deleteById(id)
            .let { ResponseEntity.ok().build() }

    @PutMapping("/{id}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody vehicle: Vehicle
    ): ResponseEntity<Unit> =
        repo
            .update(id, vehicle)
            .let { ResponseEntity.ok().build() }
}