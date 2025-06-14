package io.github.pintowar.opta.router.core.domain.models

import java.math.BigDecimal

object Fixtures {
    private val depot =
        Depot(
            id = 1L,
            name = "BRUSSEL",
            lat = 50.84275,
            lng = 4.35155
        )

    private val vehicles =
        mapOf(
            "vehicle-0" to
                Vehicle(
                    id = 1L,
                    name = "Vehicle 0",
                    capacity = 31,
                    depot = depot
                ),
            "vehicle-1" to
                Vehicle(
                    id = 2L,
                    name = "Vehicle 1",
                    capacity = 31,
                    depot = depot
                )
        )

    private val customers =
        mapOf(
            "sample-4" to
                listOf(
                    Customer(2L, "ANTHISNES", 50.481299, 5.519805, 3),
                    Customer(3L, "AVE-ET-AUFFE", 50.108568, 5.142958, 3),
                    Customer(4L, "BAVIKHOVE", 50.875358, 3.311374, 6),
                    Customer(5L, "BERSILLIES-L'ABBAYE", 50.262495, 4.152304, 2),
                    Customer(6L, "BLERET", 50.688785, 5.285913, 6),
                    Customer(7L, "BOURLERS", 50.025208, 4.340981, 1),
                    Customer(8L, "BULSKAMP", 51.043247, 2.650135, 1),
                    Customer(9L, "CHIEVRES", 50.566135, 3.785317, 3),
                    Customer(10L, "DAMME", 51.236583, 3.340076, 6)
                ),
            "sample-5" to
                listOf(
                    Customer(11, "DONSTIENNES", 50.284996, 4.310697, 5),
                    Customer(12, "ELVERDINGE", 50.884867, 2.816207, 3),
                    Customer(13, "EVELETTE", 50.411922, 5.173705, 2),
                    Customer(14, "FONTAINE-L'EVEQUE", 50.410056, 4.324953, 6),
                    Customer(15, "GELINDEN", 50.767056, 5.262893, 5),
                    Customer(16, "GONDREGNIES", 50.627105, 3.91157, 2),
                    Customer(17, "GUIGNIES", 50.549981, 3.372729, 6),
                    Customer(18, "HAREN_BRUSSEL", 50.891958, 4.418294, 6),
                    Customer(19, "HEPPIGNIES", 50.481412, 4.49326, 6)
                ),
            "sample-6" to
                listOf(
                    Customer(20, "HOFSTADE_BT.", 50.991291, 4.492735, 5),
                    Customer(21, "HUMBEEK", 50.966766, 4.383436, 3),
                    Customer(22, "JURBISE", 50.520819, 3.901708, 5),
                    Customer(23, "KOMEN", 50.77614, 3.007366, 1),
                    Customer(24, "LANDEGEM", 51.056602, 3.573045, 5),
                    Customer(25, "LES_BONS_VILLERS", 50.542746, 4.44768, 2),
                    Customer(26, "LISSEWEGE", 51.294838, 3.199415, 1),
                    Customer(27, "MAFFLE", 50.620081, 3.80181, 5),
                    Customer(28, "MAULDE", 50.616574, 3.547085, 1)
                ),
            "sample-7" to
                listOf(
                    Customer(29, "MERKSPLAS", 51.361787, 4.861625, 4),
                    Customer(30, "MONT_NAM.", 50.353471, 4.901419, 1),
                    Customer(31, "NAAST", 50.550432, 4.09648, 4),
                    Customer(32, "NIEUWKERKEN-WAAS", 51.193616, 4.178782, 1),
                    Customer(33, "OLLIGNIES", 50.687617, 3.857423, 1),
                    Customer(34, "ORET", 50.299871, 4.615565, 3),
                    Customer(35, "PEPINGEN", 50.736029, 4.136086, 1),
                    Customer(36, "POUPEHAN", 49.812224, 5.0029, 1),
                    Customer(37, "RENINGE", 50.947788, 2.789172, 1)
                ),
            "sample-8" to
                listOf(
                    Customer(38, "ROSMEER", 50.846085, 5.575024, 3),
                    Customer(39, "SAINT-MARTIN", 50.500951, 4.647366, 5),
                    Customer(40, "SELANGE", 49.609647, 5.856027, 3),
                    Customer(41, "SINT-KWINTENS-LENNIK", 50.806866, 4.153828, 3),
                    Customer(42, "SOMZEE", 50.295067, 4.483164, 5),
                    Customer(43, "TAILLES", 50.227388, 5.743751, 5),
                    Customer(44, "TILFF", 50.56952, 5.584406, 6),
                    Customer(45, "VAUX-LEZ-ROSIERES", 49.910279, 5.565905, 3),
                    Customer(46, "VILLERS-L'EVEQUE", 50.704425, 5.439973, 6),
                    Customer(47, "VOSSEM", 50.834607, 4.557627, 3),
                    Customer(48, "WARNANT-DREYE", 50.595165, 5.227629, 4),
                    Customer(49, "WETTEREN", 51.000574, 3.869354, 3),
                    Customer(50, "XHENDELESSE", 50.606152, 5.783008, 6)
                )
        )

    private val problems =
        mapOf(
            "sample-4" to
                VrpProblem(
                    id = 4L,
                    name = "sample-4",
                    vehicles = vehicles.values.toList(),
                    customers = customers.getValue("sample-4")
                ),
            "sample-5" to
                VrpProblem(
                    id = 5L,
                    name = "sample-5",
                    vehicles = vehicles.values.toList(),
                    customers = customers.getValue("sample-5")
                ),
            "sample-6" to
                VrpProblem(
                    id = 6L,
                    name = "sample-6",
                    vehicles = vehicles.values.toList(),
                    customers = customers.getValue("sample-6")
                ),
            "sample-7" to
                VrpProblem(
                    id = 7L,
                    name = "sample-7",
                    vehicles = vehicles.values.toList(),
                    customers = customers.getValue("sample-7")
                ),
            "sample-8" to
                VrpProblem(
                    id = 8L,
                    name = "sample-8",
                    vehicles = vehicles.values.toList(),
                    customers = customers.getValue("sample-8")
                )
        )

    private val solutions =
        mapOf(
            "sample-4" to
                listOf(
                    VrpSolution(
                        problem("sample-4"),
                        listOf(
                            Route(
                                distance = BigDecimal.valueOf(690.05),
                                time = BigDecimal.valueOf(920.91),
                                totalDemand = 31,
                                order =
                                    listOf(
                                        LatLng(lat = 50.84275, lng = 4.35155),
                                        LatLng(lat = 50.262495, lng = 4.152304),
                                        LatLng(lat = 50.688785, lng = 5.285913),
                                        LatLng(lat = 50.481299, lng = 5.519805),
                                        LatLng(lat = 50.108568, lng = 5.142958),
                                        LatLng(lat = 50.025208, lng = 4.340981),
                                        LatLng(lat = 50.566135, lng = 3.785317),
                                        LatLng(lat = 50.875358, lng = 3.311374),
                                        LatLng(lat = 51.043247, lng = 2.650135),
                                        LatLng(lat = 51.236583, lng = 3.340076),
                                        LatLng(lat = 50.84275, lng = 4.35155)
                                    ),
                                customerIds = listOf(5L, 6L, 2L, 3L, 7L, 9L, 4L, 8L, 10L)
                            ),
                            Route(
                                distance = BigDecimal("0"),
                                time = BigDecimal("0"),
                                totalDemand = 0,
                                order = emptyList(),
                                customerIds = emptyList()
                            )
                        )
                    ),
                    VrpSolution(
                        problem("sample-4"),
                        listOf(
                            Route(
                                distance = BigDecimal("592.17"),
                                time = BigDecimal("751.72"),
                                totalDemand = 31,
                                order =
                                    listOf(
                                        LatLng(lat = 50.84275, lng = 4.35155),
                                        LatLng(lat = 50.688785, lng = 5.285913),
                                        LatLng(lat = 50.481299, lng = 5.519805),
                                        LatLng(lat = 50.108568, lng = 5.142958),
                                        LatLng(lat = 50.025208, lng = 4.340981),
                                        LatLng(lat = 50.262495, lng = 4.152304),
                                        LatLng(lat = 50.566135, lng = 3.785317),
                                        LatLng(lat = 50.875358, lng = 3.311374),
                                        LatLng(lat = 51.043247, lng = 2.650135),
                                        LatLng(lat = 51.236583, lng = 3.340076),
                                        LatLng(lat = 50.84275, lng = 4.35155)
                                    ),
                                customerIds = listOf(6L, 2L, 3L, 7L, 5L, 9L, 4L, 8L, 10L)
                            ),
                            Route(
                                distance = BigDecimal("0"),
                                time = BigDecimal("0"),
                                totalDemand = 0,
                                order = emptyList(),
                                customerIds = emptyList()
                            )
                        )
                    )
                )
        )

    fun depot() = depot

    fun vehicle(vehicle: String) = vehicles.getValue(vehicle)

    fun customer(sample: String) = customers.getValue(sample)

    fun problem(sample: String) = problems.getValue(sample)

    fun solution(sample: String) = solutions.getValue(sample)
}