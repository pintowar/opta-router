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

        val subSamples = listOf(2, 5)
            .flatMapIndexed { i, splitBy ->
                val stopSamples = sample.stops.windowed(sample.stops.size / splitBy, sample.stops.size / splitBy)

                stopSamples.mapIndexed { j, stop ->
                    val baseIdx = i * stopSamples.size
                    Instance(
                        id = baseIdx + sample.id + 1 + j,
                        name = "sub-sample-${baseIdx + 1 + j}",
                        capacity = sample.capacity / splitBy,
                        depots = List(sample.depots.size / splitBy) { 0 },
                        nVehicles = sample.nVehicles / splitBy,
                        nLocations = sample.nLocations / splitBy,
                        stops = stop
                    )
                }
            }

        return listOf(sample) + subSamples
    }
}