package io.github.pintowar.opta.router.adapters.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import org.jooq.DSLContext
import org.jooq.Record6
import org.jooq.Result
import org.jooq.SelectJoinStep
import org.jooq.generated.public.tables.records.*
import org.jooq.generated.public.tables.references.*

class SolutionJooqRepository(
    private val mapper: ObjectMapper,
    private val dsl: DSLContext
) : SolutionRepository {

    override fun listAll(): List<VrpSolution> {
        return groupRoutes(allRoutes().fetch()).map {
            VrpSolution.emptyFromInstance(it)
        }
    }

    override fun getByInstanceId(instanceId: Long): VrpSolution? {
        return groupRoutes(routeById(instanceId).fetch()).map {
            VrpSolution.emptyFromInstance(it)
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

                override fun distance(originId: Long, targetId: Long): Double = tuples[originId to targetId]?.first ?: 0.0

                override fun time(originId: Long, targetId: Long): Long = tuples[originId to targetId]?.second ?: 0L
            }
        }
    }

    data class MatrixTuple(val originId: Long, val targetId: Long, val time: Long, val distance: Double)

    private fun groupRoutes(tuples: Result<RouteTuple>) = tuples
        .groupingBy { (route, _, _, _, _) -> route }
        .fold(Pair(emptySet<Customer>(), emptySet<Vehicle>())) { (customers, vehicles), (_, c, l, d, dl, v) ->
            val accC = customers + Customer(
                c.id!!,
                c.name,
                c.demand,
                Location(l.id!!, l.latitude.toDouble(), l.longitude.toDouble(), l.name, c.demand)
            )
            val accV = vehicles + Vehicle(
                v.id!!, v.name, v.capacity, Depot(
                    d.id!!, d.name,
                    Location(dl.id!!, dl.latitude.toDouble(), dl.longitude.toDouble(), dl.name, 0)
                )
            )

            accC to accV
        }.map { (k, v) ->
            val (customers, vehicles) = v
            RouteInstance(k.id!!, k.name, vehicles.toList(), customers.toList())
        }

    private fun routeById(id: Long) = allRoutes().where(ROUTE.ID.eq(id))

    private fun allRoutes(): SelectJoinStep<RouteTuple> {
        val CUSTOMER_LOCATION = LOCATION.`as`("CUSTOMER_LOCATION")
        val DEPOT_LOCATION = LOCATION.`as`("DEPOT_LOCATION")

        return dsl
            .select(ROUTE, CUSTOMER, CUSTOMER_LOCATION, DEPOT, DEPOT_LOCATION, VEHICLE)
            .from(
                ROUTE
                    .leftJoin(ROUTE_CUSTOMER).on(ROUTE_CUSTOMER.ROUTE_ID.eq(ROUTE.ID))
                    .leftJoin(CUSTOMER).on(ROUTE_CUSTOMER.CUSTOMER_ID.eq(CUSTOMER.ID))
                    .leftJoin(CUSTOMER_LOCATION).on(CUSTOMER_LOCATION.ID.eq(CUSTOMER.LOCATION_ID))
                    .leftJoin(DEPOT).on(DEPOT.ID.eq(ROUTE.DEPOT_ID))
                    .leftJoin(DEPOT_LOCATION).on(DEPOT_LOCATION.ID.eq(DEPOT.LOCATION_ID))
                    .leftJoin(VEHICLE).on(VEHICLE.DEPOT_ID.eq(ROUTE.DEPOT_ID))
            )
    }

}

typealias RouteTuple = Record6<RouteRecord, CustomerRecord, LocationRecord, DepotRecord, LocationRecord, VehicleRecord>