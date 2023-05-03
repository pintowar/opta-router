package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/solutions")
class SolutionController(val repo: SolutionRepository) {

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun index(): List<VrpSolution> = repo.listAll()

    @GetMapping("/by-instance-id/{id}/show", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun show(@PathVariable id: Long): ResponseEntity<VrpSolution> {
        val current = repo.getByInstanceId(id)
        return if (current != null) {
            ResponseEntity.ok(current)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}