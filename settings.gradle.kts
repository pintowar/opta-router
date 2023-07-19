rootProject.name = "opta-router"

include("opta-router-core", "opta-router-geo", "opta-router-repo")
include(
//    "opta-router-solver-optaplanner",
    "opta-router-solver-timefold",
    "opta-router-solver-jenetics",
    "opta-router-solver-jsprit",
    "opta-router-solver-ortools"
)
include("opta-router-webcli", "opta-router-app")
