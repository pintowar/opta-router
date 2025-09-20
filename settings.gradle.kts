rootProject.name = "opta-router"

include("opta-router-core", "opta-router-geo", "opta-router-repo")
include(
    "opta-router-solver:jenetics",
    "opta-router-solver:jsprit",
    "opta-router-solver:ortools",
    "opta-router-solver:timefold"
)
include("opta-router-webcli", "opta-router-app")
