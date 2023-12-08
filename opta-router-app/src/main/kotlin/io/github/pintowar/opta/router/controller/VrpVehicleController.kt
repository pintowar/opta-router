package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.models.Vehicle
import io.github.pintowar.opta.router.core.domain.ports.VrpVehiclePort
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vrp-vehicles")
class VrpVehicleController(
    private val repo: VrpVehiclePort
) {

    @GetMapping
    suspend fun index(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "25") size: Int
    ): Page<Vehicle> {
        val count = repo.count()
        val locations = repo.findAll(page * size, size).toList()
        return PageImpl(locations, PageRequest.of(page, size), count)
    }

    @DeleteMapping("/{id}/remove", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun remove(@PathVariable id: Long): ResponseEntity<Unit> {
        return repo.deleteById(id)
            .let { ResponseEntity.ok().build() }
    }

    @PutMapping("/{id}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun update(@PathVariable id: Long, @RequestBody vehicle: Vehicle): ResponseEntity<Unit> {
        return repo.update(id, vehicle)
            .let { ResponseEntity.ok().build() }
    }

}