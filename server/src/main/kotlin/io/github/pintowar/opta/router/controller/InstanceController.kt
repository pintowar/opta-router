package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.ports.InstanceRepository
import io.github.pintowar.opta.router.core.domain.models.Instance
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/instances")
class InstanceController(val repo: InstanceRepository) {

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun index(): List<Instance> = repo.listAll()

    @GetMapping("/{id}/show", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun show(@PathVariable id: Long): ResponseEntity<Instance> {
        val current = repo.getById(id)
        return if (current != null) {
            ResponseEntity.ok(current)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}