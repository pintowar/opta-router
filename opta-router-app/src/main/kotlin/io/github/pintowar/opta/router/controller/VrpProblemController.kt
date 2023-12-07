package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vrp-problems")
class VrpProblemController(val repo: VrpProblemPort) {

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun index(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "25") size: Int
    ): Page<VrpProblem> {
        val count = repo.count()
        val problems = repo.findAll(page * size, size).toList()
        return PageImpl(problems, PageRequest.of(page, size), count)
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun show(@PathVariable id: Long): ResponseEntity<VrpProblem> {
        return repo.getById(id)
            ?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun update(@PathVariable id: Long, @RequestBody problem: VrpProblem): ResponseEntity<Unit> {
        println(problem.name)
        delay(1000)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}/remove", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun remove(@PathVariable id: Long): ResponseEntity<Unit> {
        return repo.deleteById(id)
            .let { ResponseEntity.ok().build() }
    }
}