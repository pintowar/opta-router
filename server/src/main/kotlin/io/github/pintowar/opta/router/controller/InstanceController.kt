package io.github.pintowar.opta.router.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.vrp.Instance
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/instances")
class RouteController(val mapper: ObjectMapper) {

    private val instances: Map<Long, Instance> = ClassPathResource("/instances/sample.json")
        .let(::readInstances)
        .associateBy { it.id }

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun index(): List<Instance> = instances.values.sortedBy { it.id }

    @GetMapping("/{id}/show", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun show(@PathVariable id: Long): ResponseEntity<Instance> =
        if (instances.containsKey(id)) {
            ResponseEntity.ok(instances[id]!!)
        } else {
            ResponseEntity.notFound().build()
        }

    private fun readInstances(resource: Resource): List<Instance> {
        val sample = mapper.readValue(resource.getContentAsString(StandardCharsets.UTF_8), Instance::class.java)
        val subSamples = sample.stops.windowed(10, 10).mapIndexed { idx, it ->
            Instance(
                id = sample.id + 1 + idx,
                name = "sub-sample-${1 + idx}",
                capacity = sample.capacity / 5,
                depots = listOf(0L, 0L),
                nVehicles = 2,
                nLocations = 10,
                stops = it
            )
        }
        return listOf(sample) + subSamples
    }
}