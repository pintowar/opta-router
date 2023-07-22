import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

val Project.isSnapshotVersion: Boolean
    get() = version.toString().endsWith("SNAPSHOT")

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

val Project.isProdProfile: Boolean
    get() = (project.property("environmentName")?.toString() ?: "").split(",").contains("prod")