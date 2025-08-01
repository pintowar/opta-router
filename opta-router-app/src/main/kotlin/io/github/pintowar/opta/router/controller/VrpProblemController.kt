package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpProblemSummary
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
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
@RequestMapping("/api/vrp-problems")
class VrpProblemController(
    val repo: VrpProblemPort
) {
    /**
     * Retrieves a paginated list of VRP problem summaries.
     *
     * @param page The page number to retrieve (0-indexed).
     * @param size The number of items per page.
     * @param q A query string to filter problems by name.
     * @return A [Page] of [VrpProblemSummary] objects.
     */
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun index(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "25") size: Int,
        @RequestParam("q", defaultValue = "") q: String
    ): Page<VrpProblemSummary> {
        val count = repo.count(q)
        val problems = repo.findAll(q, page * size, size).toList()
        return PageImpl(problems, PageRequest.of(page, size), count)
    }

    /**
     * Retrieves a VRP problem by its ID.
     *
     * @param id The ID of the VRP problem to retrieve.
     * @return A [ResponseEntity] with the [VrpProblem] object if found, or a 404 Not Found response.
     */
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun show(
        @PathVariable id: Long
    ): ResponseEntity<VrpProblem> =
        repo
            .getById(id)
            ?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    /**
     * Creates a new VRP problem.
     *
     * @param problem The [VrpProblem] object to create.
     * @return A [ResponseEntity] with a 200 OK status if the creation is successful.
     */
    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun create(
        @RequestBody problem: VrpProblem
    ): ResponseEntity<Unit> =
        repo
            .create(problem)
            .let { ResponseEntity.ok().build() }

    /**
     * Copies an existing VRP problem.
     *
     * @param id The ID of the VRP problem to copy (currently unused in the implementation).
     * @param problem The [VrpProblem] object to create as a copy.
     * @return A [ResponseEntity] with a 200 OK status if the copy is successful.
     */
    @PostMapping("/{id}/copy", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun copy(
        @PathVariable id: Long,
        @RequestBody problem: VrpProblem
    ): ResponseEntity<Unit> =
        repo
            .create(problem)
            .let { ResponseEntity.ok().build() }

    /**
     * Updates an existing VRP problem.
     *
     * @param id The ID of the VRP problem to update.
     * @param problem The [VrpProblem] object containing the updated details.
     * @return A [ResponseEntity] with a 200 OK status if the update is successful.
     */
    @PutMapping("/{id}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody problem: VrpProblem
    ): ResponseEntity<Unit> =
        repo
            .update(id, problem)
            .let { ResponseEntity.ok().build() }

    /**
     * Removes a VRP problem by its ID.
     *
     * @param id The ID of the VRP problem to remove.
     * @return A [ResponseEntity] with a 200 OK status if the removal is successful.
     */
    @DeleteMapping("/{id}/remove", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun remove(
        @PathVariable id: Long
    ): ResponseEntity<Unit> =
        repo
            .deleteById(id)
            .let { ResponseEntity.ok().build() }
}