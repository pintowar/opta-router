package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vrp-problems")
class VrpProblemController(val repo: VrpProblemPort) {

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun index(): Flow<VrpProblem> = repo.listAll()

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun show(@PathVariable id: Long): ResponseEntity<VrpProblem> {
        val current = repo.getById(id)
        return if (current != null) {
            ResponseEntity.ok(current)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}