import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

val Project.isSnapshotVersion: Boolean
    get() = version.toString().endsWith("SNAPSHOT")

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()


val Project.buildEnv: String
    get() = project.property("environmentName")?.toString() ?: ""

val Project.isDistProfile: Boolean
    get() = project.buildEnv == "dist"

val Project.isLocalProfile: Boolean
    get() = project.buildEnv == "local"

val Project.allJacocoSubModules: List<Project>
    get() = this.rootProject
        .subprojects
        .filterNot { sub -> listOf("webcli", "solver").any { sub.name.contains(it) } }