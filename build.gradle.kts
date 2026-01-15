import com.kizitonwose.calendar.buildsrc.Version
import com.kizitonwose.calendar.buildsrc.Version.isNoPublish
import com.kizitonwose.calendar.buildsrc.androidLibProjects
import com.kizitonwose.calendar.buildsrc.multiplatformLibProjects
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationVariantSpec
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.versionCheck)
}

allprojects {
    apply(plugin = rootProject.libs.plugins.kotlinter.get().pluginId)

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xsuppress-warning=NOTHING_TO_INLINE")
        }
    }
    plugins.withType<KotlinBasePlugin> {
        @OptIn(ExperimentalAbiValidation::class)
        extensions.configure<KotlinProjectExtension> {
            if ("sample" !in project.name) {
                when (this) {
                    is KotlinJvmProjectExtension -> extensions.configure<AbiValidationExtension> {
                        enabled = true
                        applyFilters()
                        configureAbiTask()
                    }

                    is KotlinMultiplatformExtension -> extensions.configure<AbiValidationMultiplatformExtension> {
                        enabled = true
                        klib.enabled = true
                        applyFilters()
                        configureAbiTask()
                    }
                }
            }
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        // https://docs.gradle.org/8.8/userguide/performance.html#execute_tests_in_parallel
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    }
    afterEvaluate {
        // Android and Multiplatform libraries are published separately
        // See https://github.com/kizitonwose/Calendar/pull/561
        disableMavenPublicationsIfNeeded(multiplatformLibProjects, Version.multiplatform)
        disableMavenPublicationsIfNeeded(androidLibProjects, Version.android)
    }
}

private fun Project.disableMavenPublicationsIfNeeded(
    projects: List<String>,
    version: String,
) {
    if (version.isNoPublish() && project.name in projects) {
        tasks.withType<AbstractPublishToMaven> {
            enabled = false
        }
    }
}

@ExperimentalAbiValidation
private fun AbiValidationVariantSpec.applyFilters() {
    filters {
        excluded {
            annotatedWith.add("com.kizitonwose.calendar.core.ExperimentalCalendarApi")
        }
    }
}

private fun Project.configureAbiTask() {
    // Retain previous task names from the old lib.
    tasks.register("apiDump") {
        dependsOn("updateLegacyAbi")
    }
    tasks.register("apiCheck") {
        dependsOn("checkLegacyAbi")
    }
}

// tasks.register<Delete>("clean").configure {
//    delete(rootProject.layout.buildDirectory)
// }
