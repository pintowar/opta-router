package io.github.pintowar.opta.router.adapters.database

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.core.domain.models.Instance
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.GeoMatrix
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class SolutionDummyRepository(
    private val geoService: GeoService,
    private val mapper: ObjectMapper
) : SolutionRepository {

    private val solutions: Map<Long, PersistSolution> = ClassPathResource("/instances/sample.json")
        .let(::readInstances)
        .map { PersistSolution(VrpSolution.emptyFromInstance(it), geoService) }
        .associateBy { it.vrpSolution.instance.id }

    override fun listAll(): List<VrpSolution> {
        return solutions.values.map { it.vrpSolution }.sortedBy { it.instance.id }
    }

    override fun getByInstanceId(instanceId: Long): VrpSolution? {
        return solutions[instanceId]?.vrpSolution
    }

    override fun getByMatrixInstanceId(instanceId: Long): Matrix? {
        return solutions[instanceId]?.matrix
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

private data class PersistSolution(
    val vrpSolution: VrpSolution,
    private val geoService: GeoService

) {
    val matrix: Matrix by lazy {
        GeoMatrix(
            vrpSolution.instance.stops.map { it.toCoordinate() },
            geoService
        )
    }
}