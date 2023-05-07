package io.github.pintowar.opta.router.adapters.database.jooq

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.generated.public.tables.records.CustomerRecord
import org.jooq.generated.public.tables.records.DepotRecord
import org.jooq.generated.public.tables.records.LocationRecord
import org.jooq.generated.public.tables.records.VehicleRecord
import org.jooq.generated.public.tables.references.*
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select

class SolutionJooqRepository(
    private val mapper: ObjectMapper,
    private val dsl: DSLContext
) : SolutionRepository {

    override fun listAll(): List<VrpSolution> {
        return query().limit(0, 10).fetch { (r, c, v) ->
            VrpSolution.emptyFromInstance(RouteInstance(r.id!!, r.name, v, c))
        }
    }

    override fun getByInstanceId(instanceId: Long): VrpSolution? {
        return query().where(ROUTE.ID.eq(instanceId)).fetch { (r, c, v) ->
            VrpSolution.emptyFromInstance(RouteInstance(r.id!!, r.name, v, c))
        }.firstOrNull()
    }

    override fun getByMatrixInstanceId(instanceId: Long): Matrix? {
        val matrix = dsl
            .selectFrom(ROUTE_MATRIX)
            .where(ROUTE_MATRIX.ROUTE_ID.eq(1))
            .fetchOne()?.matrix?.data()

        return matrix?.let {
            object : Matrix {
                val tuples = mapper.readValue<List<MatrixTuple>>(it).associate { tup ->
                    (tup.originId to tup.targetId) to (tup.distance to tup.time)
                }

                override fun distance(originId: Long, targetId: Long): Double =
                    tuples[originId to targetId]?.first ?: 0.0

                override fun time(originId: Long, targetId: Long): Long = tuples[originId to targetId]?.second ?: 0L
            }
        }
    }

    data class MatrixTuple(val originId: Long, val targetId: Long, val time: Long, val distance: Double)

    private val convertCustomer: (c: CustomerRecord, l: LocationRecord) -> Customer = { c, l ->
        Customer(c.id!!, c.name, c.demand, Location(l.id!!, l.name, l.latitude.toDouble(), l.longitude.toDouble()))
    }
    private val convertVehicle: (v: VehicleRecord, d: DepotRecord, l: LocationRecord) -> Vehicle = { v, d, l ->
        val depot = Depot(d.id!!, d.name, Location(l.id!!, l.name, l.latitude.toDouble(), l.longitude.toDouble()))
        Vehicle(v.id!!, v.name, v.capacity, depot)
    }

    private fun query() = dsl
        .select(
            ROUTE,
            multiset(
                select(CUSTOMER, LOCATION)
                    .from(
                        CUSTOMER.leftJoin(ROUTE_CUSTOMER).on(ROUTE_CUSTOMER.CUSTOMER_ID.eq(CUSTOMER.ID))
                            .leftJoin(LOCATION).on(CUSTOMER.LOCATION_ID.eq(LOCATION.ID))
                    )
                    .where(ROUTE_CUSTOMER.ROUTE_ID.eq(ROUTE.ID))
            ).`as`("CUSTOMERS").convertFrom { r -> r.map(Records.mapping(convertCustomer)) },
            multiset(
                select(VEHICLE, DEPOT, LOCATION)
                    .from(
                        VEHICLE.leftJoin(DEPOT).on(VEHICLE.DEPOT_ID.eq(DEPOT.ID))
                            .leftJoin(LOCATION).on(LOCATION.ID.eq(DEPOT.LOCATION_ID))
                    )
                    .where(DEPOT.ID.eq(ROUTE.DEPOT_ID))
            ).`as`("VEHICLES").convertFrom { r -> r.map(Records.mapping(convertVehicle)) },
        )
        .from(ROUTE)

}
