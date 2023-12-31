package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.ports.GeoPort

class SolverPanelStorage(
    private val sessionPanel: MutableMap<String, SolverPanel>,
    private val geoPort: GeoPort
) {

    fun store(key: String, panel: SolverPanel) = sessionPanel.put(key, panel)

    fun getOrDefault(key: String) = sessionPanel.getOrPut(key) { SolverPanel() }

    suspend fun convertSolutionForPanelId(key: String, solution: VrpSolution): VrpSolution {
        val panel = getOrDefault(key)
        val routes = if (panel.isDetailedPath) geoPort.detailedPaths(solution.routes) else solution.routes
        return solution.copy(routes = routes)
    }
}