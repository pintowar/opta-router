package io.github.pintowar.opta.router.core.domain.models

import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
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
                                distance = BigDecimal.ZERO,
                                time = BigDecimal.ZERO,
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
                    ),
                    VrpSolution(
                        problem("sample-4"),
                        listOf(
                            Route(
                                distance = BigDecimal.valueOf(592.17),
                                time = BigDecimal.valueOf(751.72),
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
                                customerIds = listOf(6, 2, 3, 7, 5, 9, 4, 8, 10)
                            ),
                            Route(
                                distance = BigDecimal.ZERO,
                                time = BigDecimal.ZERO,
                                totalDemand = 0,
                                order = emptyList(),
                                customerIds = emptyList()
                            )
                        )
                    )
                )
        )

    private val matrices =
        mapOf(
            "sample-4" to
                VrpProblemMatrix(
                    longArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                    doubleArrayOf(
                        0.0,
                        101975.74226216275,
                        106790.27479203885,
                        82191.10907698506,
                        73328.06512796407,
                        74322.78354452609,
                        101807.9757950809,
                        134893.7833844354,
                        56564.34935000191,
                        92528.7104042942,
                        102029.00539268937,
                        0.0,
                        56856.10326059388,
                        182116.31545882422,
                        112562.50311282619,
                        36637.8920169034,
                        110922.49026745815,
                        235611.87585681287,
                        137954.77282237925,
                        193246.80287667108,
                        106884.41792256541,
                        56769.411260593835,
                        0.0,
                        178617.44698870025,
                        86049.85064270232,
                        76212.83122059371,
                        71606.9697973344,
                        232209.89938668892,
                        128528.8943522551,
                        198102.21540654727,
                        81185.56420751194,
                        181907.1264588243,
                        177601.57998870045,
                        0.0,
                        100650.59032462545,
                        155124.22683172597,
                        133405.56299174234,
                        58237.83167163507,
                        55758.34470843745,
                        47603.17878605936,
                        73651.21525849083,
                        113044.0361128262,
                        85997.17264270231,
                        101234.09032462568,
                        0.0,
                        105313.86639518956,
                        39186.59964574456,
                        152591.67572261428,
                        48910.67068818071,
                        137027.47674247297,
                        74692.56467505277,
                        36666.9320169034,
                        76220.11922059373,
                        155992.79783172594,
                        105360.50039518965,
                        0.0,
                        112680.24054982173,
                        207540.3821391761,
                        121846.72710474265,
                        165175.30915903486,
                        102613.68092560791,
                        110758.0092674583,
                        71575.38679733439,
                        133949.41399174262,
                        39146.95064574455,
                        112725.57954982168,
                        0.0,
                        185306.99938973098,
                        81625.99435529743,
                        169742.80040958984,
                        134113.20151496207,
                        235515.6468568128,
                        232100.2013866888,
                        58086.393671635065,
                        152534.65572261417,
                        206614.08713917626,
                        185289.6283897309,
                        0.0,
                        109775.0671064257,
                        58706.47670211275,
                        56134.503480528714,
                        137509.23382237917,
                        128142.99735225546,
                        55664.50770843745,
                        48883.82668818072,
                        121713.04010474269,
                        81638.79935529764,
                        109769.79010642579,
                        0.0,
                        93264.89312628457,
                        92047.22153482081,
                        193449.6668766717,
                        198264.19940654724,
                        47798.76478605944,
                        136474.8577424728,
                        164548.10715903508,
                        169229.83040958954,
                        58060.147702112736,
                        93079.14712628468,
                        0.0
                    ),
                    longArrayOf(
                        0,
                        7376609,
                        4638606,
                        6104059,
                        5617093,
                        3488521,
                        8422100,
                        8227670,
                        4873270,
                        6161386,
                        6897879,
                        0,
                        6030981,
                        11377269,
                        10972782,
                        3189843,
                        8027678,
                        14503563,
                        7479010,
                        12437279,
                        4598678,
                        5904928,
                        0,
                        12453490,
                        9288191,
                        7445575,
                        6689687,
                        18990653,
                        12361337,
                        10138078,
                        6354195,
                        11375784,
                        11008003,
                        0,
                        7960351,
                        9683454,
                        9687348,
                        4516712,
                        4739886,
                        4190607,
                        4621768,
                        11152356,
                        9284512,
                        7940776,
                        0,
                        9056790,
                        2760998,
                        10736463,
                        4107147,
                        10570568,
                        3799780,
                        3047464,
                        6268090,
                        9913091,
                        9238784,
                        0,
                        8257498,
                        11593971,
                        9874527,
                        9527687,
                        7905530,
                        8086864,
                        6675961,
                        9613185,
                        2706410,
                        7805525,
                        0,
                        12408872,
                        5779556,
                        12242977,
                        8028734,
                        14861059,
                        18714705,
                        4503899,
                        10464194,
                        10915912,
                        12191191,
                        0,
                        8307415,
                        3833553,
                        4873724,
                        7470820,
                        9896492,
                        4722355,
                        4163932,
                        9371708,
                        5890929,
                        8208078,
                        0,
                        8132589,
                        5960593,
                        12792918,
                        10054915,
                        3864081,
                        10786387,
                        8847771,
                        12513384,
                        3410947,
                        8335904,
                        0
                    )
                )
        )

    fun depot() = depot

    fun vehicle(vehicle: String) = vehicles.getValue(vehicle)

    fun vehicles() = vehicles.values

    fun customer(sample: String) = customers.getValue(sample)

    fun problem(sample: String) = problems.getValue(sample)

    fun problems() = problems.values

    fun solution(sample: String) = solutions.getValue(sample)

    fun matrix(sample: String) = matrices.getValue(sample)
}