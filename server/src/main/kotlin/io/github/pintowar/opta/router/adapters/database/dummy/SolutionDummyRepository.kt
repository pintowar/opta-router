package io.github.pintowar.opta.router.adapters.database.dummy

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.GeoMatrix
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.nio.charset.StandardCharsets.UTF_8

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

    private data class DummyCustomer(val id: Long, val name: String, val lat: Double, val lng: Double, val demand: Int)
    private data class DummyDepot(val id: Long, val name: String, val lat: Double, val lng: Double)

    private data class DummyVehicle(val depotId: Long, val capacity: Int)

    private data class DummyInstance(
        val id: Long,
        val name: String,
        val customers: List<DummyCustomer>,
        val depots: List<DummyDepot>,
        val vehicles: List<DummyVehicle>
    )

    private fun readInstances(resource: Resource): List<RouteInstance> {
        val sample = mapper.readValue(resource.getContentAsString(UTF_8), DummyInstance::class.java).let { dummy ->
            val customers = dummy.customers.map { Customer(it.id, it.name, it.demand, it.lat, it.lng) }
            val depotsIds = dummy.depots.associate { it.id to Depot(it.id, it.name, it.lat, it.lng) }
            val vehicles = dummy.vehicles.mapIndexed { idx, it ->
                Vehicle((idx + 1).toLong(), "Vehicle ${idx + 1}", it.capacity, depotsIds.getValue(it.depotId))
            }

            RouteInstance(dummy.id, dummy.name, vehicles, customers)
        }

        val subSamples = listOf(2, 5)
            .flatMapIndexed { i, splitBy ->
                val customerSamples =
                    sample.customers.windowed(sample.customers.size / splitBy, sample.customers.size / splitBy)
                val vehiclesSample = sample.vehicles.take(sample.vehicles.size / splitBy)
                    .map { it.copy(capacity = it.capacity / splitBy) }

                customerSamples.mapIndexed { j, stop ->
                    val baseIdx = i * customerSamples.size
                    RouteInstance(
                        id = baseIdx + sample.id + 1 + j,
                        name = "sub-sample-${baseIdx + 1 + j}",
                        vehicles = vehiclesSample,
                        customers = stop
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
            vrpSolution.instance.locations,
            geoService
        )
    }
}