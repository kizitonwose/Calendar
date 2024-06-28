pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev/")
    }
}
include(":core")
include(":data")
include(":view")
include(":compose")
include(":sample")
include(":compose-multiplatform:lib")
include(":compose-multiplatform:lib-core")
include(":compose-multiplatform:lib-core-jvm")
include(":compose-multiplatform:lib-core-kmp")
include(":compose-multiplatform:lib-data")
include(":compose-multiplatform:lib-jvm")
include(":compose-multiplatform:sample")
