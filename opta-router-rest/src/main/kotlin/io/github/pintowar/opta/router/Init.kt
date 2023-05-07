 package io.github.pintowar.opta.router

 import com.fasterxml.jackson.databind.ObjectMapper
  import io.github.pintowar.opta.router.core.domain.models.*
 import io.github.pintowar.opta.router.core.domain.ports.GeoService
 import org.jooq.DSLContext
 import org.jooq.JSON
 import org.jooq.generated.public.tables.references.*
 import org.springframework.boot.CommandLineRunner
 import org.springframework.stereotype.Component
 import java.time.Instant
 import java.util.*

 @Component
 class Init(
    val geoService: GeoService,
    val objectMapper: ObjectMapper,
//    val solutionRepo: SolutionDummyRepository,
    val dsl: DSLContext
 ) : CommandLineRunner {

    override fun run(vararg args: String?) {
//        val CUSTOMER_LOCATION = LOCATION.`as`("CUSTOMER_LOCATION")
//        val DEPOT_LOCATION = LOCATION.`as`("DEPOT_LOCATION")
//        val routes = dsl
//            .select(
//                ROUTE,
//                CUSTOMER,
//                CUSTOMER_LOCATION,
//                DEPOT,
//                DEPOT_LOCATION,
//                VEHICLE
//            )
//            .from(
//                ROUTE
//                    .leftJoin(ROUTE_CUSTOMER).on(ROUTE_CUSTOMER.ROUTE_ID.eq(ROUTE.ID))
//                    .leftJoin(CUSTOMER).on(ROUTE_CUSTOMER.CUSTOMER_ID.eq(CUSTOMER.ID))
//                    .leftJoin(CUSTOMER_LOCATION).on(CUSTOMER_LOCATION.ID.eq(CUSTOMER.LOCATION_ID))
//                    .leftJoin(DEPOT).on(DEPOT.ID.eq(ROUTE.DEPOT_ID))
//                    .leftJoin(DEPOT_LOCATION).on(DEPOT_LOCATION.ID.eq(DEPOT.LOCATION_ID))
//                    .leftJoin(VEHICLE).on(VEHICLE.DEPOT_ID.eq(ROUTE.DEPOT_ID))
//            )
//            .fetch()
//            .groupingBy { (route, _, _, _, _) -> route }
//            .fold(Pair(emptySet<Customer>(), emptySet<Vehicle>())) { (customers, vehicles), (_, c, l, d, dl, v) ->
//                val accC = customers + Customer(c.id!!, c.name, c.demand, Location(l.id!!, l.latitude.toDouble(), l.longitude.toDouble(), l.name, c.demand))
//                val accV = vehicles + Vehicle(v.id!!, v.name, v.capacity, Depot(d.id!!, d.name,
//                    Location(dl.id!!, dl.latitude.toDouble(), dl.longitude.toDouble(), dl.name, 0)
//                )
//                )
//
//                accC to accV
//            }.map { (k, v) ->
//                val (customers, vehicles) = v
//                RouteInterface(k.id!!, k.name, vehicles.toList(), customers.toList())
//            }
//
        println("eita")
    }
 }
