package io.github.pintowar.opta.router.core.domain.models

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
                    Customer(
                        12,
                        "ELVERDINGE",
                        50.884867,
                        2.816207,
                        3
                    ),
                    Customer(
                        13,
                        "EVELETTE",
                        50.411922,
                        5.173705,
                        2
                    ),
                    Customer(
                        id = 14,
                        name = "FONTAINE-L'EVEQUE",
                        lat = 50.410056,
                        lng = 4.324953,
                        demand = 6
                    ),
                    Customer(
                        id = 15,
                        name = "GELINDEN",
                        lat = 50.767056,
                        lng = 5.262893,
                        demand = 5
                    ),
                    Customer(
                        id = 16,
                        name = "GONDREGNIES",
                        lat = 50.627105,
                        lng = 3.91157,
                        demand = 2
                    ),
                    Customer(
                        id = 17,
                        name = "GUIGNIES",
                        lat = 50.549981,
                        lng = 3.372729,
                        demand = 6
                    ),
                    Customer(
                        id = 18,
                        name = "HAREN_BRUSSEL",
                        lat = 50.891958,
                        lng = 4.418294,
                        demand = 6
                    ),
                    Customer(
                        id = 19,
                        name = "HEPPIGNIES",
                        lat = 50.481412,
                        lng = 4.49326,
                        demand = 6
                    )
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
                    Customer(
                        id = 29,
                        name = "MERKSPLAS",
                        lat = 51.361787,
                        lng = 4.861625,
                        demand = 4
                    ),
                    Customer(
                        id = 30,
                        name = "MONT_NAM.",
                        lat = 50.353471,
                        lng = 4.901419,
                        demand = 1
                    ),
                    Customer(
                        id = 31,
                        name = "NAAST",
                        lat = 50.550432,
                        lng = 4.09648,
                        demand = 4
                    ),
                    Customer(
                        id = 32,
                        name = "NIEUWKERKEN-WAAS",
                        lat = 51.193616,
                        lng = 4.178782,
                        demand = 1
                    ),
                    Customer(
                        id = 33,
                        name = "OLLIGNIES",
                        lat = 50.687617,
                        lng = 3.857423,
                        demand = 1
                    ),
                    Customer(
                        id = 34,
                        name = "ORET",
                        lat = 50.299871,
                        lng = 4.615565,
                        demand = 3
                    ),
                    Customer(
                        id = 35,
                        name = "PEPINGEN",
                        lat = 50.736029,
                        lng = 4.136086,
                        demand = 1
                    ),
                    Customer(
                        id = 36,
                        name = "POUPEHAN",
                        lat = 49.812224,
                        lng = 5.0029,
                        demand = 1
                    ),
                    Customer(
                        id = 37,
                        name = "RENINGE",
                        lat = 50.947788,
                        lng = 2.789172,
                        demand = 1
                    )
                ),
            "sample-8" to
                listOf(
                    Customer(
                        id = 38,
                        name = "ROSMEER",
                        lat = 50.846085,
                        lng = 5.575024,
                        demand = 3
                    ),
                    Customer(
                        id = 39,
                        name = "SAINT-MARTIN",
                        lat = 50.500951,
                        lng = 4.647366,
                        demand = 5
                    ),
                    Customer(
                        id = 40,
                        name = "SELANGE",
                        lat = 49.609647,
                        lng = 5.856027,
                        demand = 3
                    ),
                    Customer(
                        id = 41,
                        name = "SINT-KWINTENS-LENNIK",
                        lat = 50.806866,
                        lng = 4.153828,
                        demand = 3
                    ),
                    Customer(
                        id = 42,
                        name = "SOMZEE",
                        lat = 50.295067,
                        lng = 4.483164,
                        demand = 5
                    ),
                    Customer(
                        id = 43,
                        name = "TAILLES",
                        lat = 50.227388,
                        lng = 5.743751,
                        demand = 5
                    ),
                    Customer(
                        id = 44,
                        name = "TILFF",
                        lat = 50.56952,
                        lng = 5.584406,
                        demand = 6
                    ),
                    Customer(
                        id = 45,
                        name = "VAUX-LEZ-ROSIERES",
                        lat = 49.910279,
                        lng = 5.565905,
                        demand = 3
                    ),
                    Customer(
                        id = 46,
                        name = "VILLERS-L'EVEQUE",
                        lat = 50.704425,
                        lng = 5.439973,
                        demand = 6
                    ),
                    Customer(
                        id = 47,
                        name = "VOSSEM",
                        lat = 50.834607,
                        lng = 4.557627,
                        demand = 3
                    ),
                    Customer(
                        id = 48,
                        name = "WARNANT-DREYE",
                        lat = 50.595165,
                        lng = 5.227629,
                        demand = 4
                    ),
                    Customer(
                        id = 49,
                        name = "WETTEREN",
                        lat = 51.000574,
                        lng = 3.869354,
                        demand = 3
                    ),
                    Customer(
                        id = 50,
                        name = "XHENDELESSE",
                        lat = 50.606152,
                        lng = 5.783008,
                        demand = 6
                    )
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

    fun depot() = depot

    fun vehicle(vehicle: String) = vehicles.getValue(vehicle)

    fun customer(sample: String) = customers.getValue(sample)

    fun problem(sample: String) = problems.getValue(sample)
}