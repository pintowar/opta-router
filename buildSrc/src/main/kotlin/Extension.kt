import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/**
 * Checks if the project version is a snapshot version.
 *
 * @return `true` if the project version ends with "SNAPSHOT", `false` otherwise.
 */
val Project.isSnapshotVersion: Boolean
    get() = version.toString().endsWith("SNAPSHOT")

/**
 * Provides access to the `libs` version catalog.
 *
 * @return The `LibrariesForLibs` accessor.
 */
val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()


/**
 * Gets the build environment name.
 *
 * @return The build environment name, or an empty string if not set.
 */
val Project.buildEnv: String
    get() = project.property("environmentName")?.toString() ?: ""

/**
 * Checks if the build environment is the "dist" profile.
 *
 * @return `true` if the build environment is "dist", `false` otherwise.
 */
val Project.isDistProfile: Boolean
    get() = project.buildEnv == "dist"

/**
 * Checks if the build environment is the "local" profile.
 *
 * @return `true` if the build environment is "local", `false` otherwise.
 */
val Project.isLocalProfile: Boolean
    get() = project.buildEnv == "local"

/**
 * Gets all subprojects that should be included in the JaCoCo report.
 *
 * @return A list of subprojects to be included in the JaCoCo report.
 */
val Project.allJacocoSubModules: List<Project>
    get() = this.rootProject
        .subprojects
        .filterNot { sub -> listOf("webcli", "solver").any { sub.name.contains(it) } }

/**
 * Gets the packages to be excluded from the JaCoCo report.
 *
 * @return A list of packages to be excluded from the JaCoCo report.
 */
val Project.excludedJacocoPackages: List<String>
    get() = listOf("org/jooq/generated/**", "io/github/pintowar/opta/router/config/**")
