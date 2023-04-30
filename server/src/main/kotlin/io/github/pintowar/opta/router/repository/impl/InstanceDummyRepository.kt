package io.github.pintowar.opta.router.repository.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.repository.InstanceRepository
import io.github.pintowar.opta.router.vrp.Instance
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class InstanceDummyRepository(private val mapper: ObjectMapper) : InstanceRepository {

    private val instances: Map<Long, Instance> = ClassPathResource("/instances/sample.json")
        .let(::readInstances)
        .associateBy { it.id }

    override fun listAll(): List<Instance> {
        return instances.values.sortedBy { it.id }
    }

    override fun getById(id: Long): Instance? {
        return instances[id]
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