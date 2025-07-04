package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort

/**
 * The SolverPanelStorage is responsible for storing and retrieving [SolverPanel] instances,
 * and for converting [VrpSolution] objects based on the panel's settings.
 *
 * @param sessionPanel A mutable map that stores the [SolverPanel] instances.
 * @param geoPort The [GeoPort] used to calculate detailed paths.
 */
class SolverPanelStorage(
    private val sessionPanel: MutableMap<String, SolverPanel>,
    private val geoPort: GeoPort
) {
    /**
     * Stores a [SolverPanel] in the storage.
     *
     * @param key The key to store the panel under.
     * @param panel The [SolverPanel] to store.
     */
    fun store(
        key: String,
        panel: SolverPanel
    ) = sessionPanel.put(key, panel)

    /**
     * Retrieves a [SolverPanel] from the storage, or creates a new one if it doesn't exist.
     *
     * @param key The key of the panel to retrieve.
     * @return The [SolverPanel] associated with the given key.
     */
    fun getOrDefault(key: String) = sessionPanel.getOrPut(key) { SolverPanel() }

    /**
     * Converts a [VrpSolution] for a given panel ID.
     * If the panel's `isDetailedPath` property is true, it calculates detailed paths for the solution's routes.
     *
     * @param key The key of the panel to use for the conversion.
     * @param solution The [VrpSolution] to convert.
     * @return The converted [VrpSolution].
     */
    suspend fun convertSolutionForPanelId(
        key: String,
        solution: VrpSolution
    ): VrpSolution {
        val panel = getOrDefault(key)
        val routes = if (panel.isDetailedPath) geoPort.detailedPaths(solution.routes) else solution.routes
        return solution.copy(routes = routes)
    }
}