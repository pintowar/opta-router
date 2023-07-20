import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

val Project.isSnapshotVersion: Boolean
    get() = version.toString().endsWith("SNAPSHOT")

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

val Project.profile: String
    get() = project
        .findProperty("spring.profiles.active")?.toString()
        ?: System.getenv("SPRING_PROFILES_ACTIVE")
        ?: "dev"

val Project.isProdProfile: Boolean
    get() = project.profile.split(",").contains("prod")