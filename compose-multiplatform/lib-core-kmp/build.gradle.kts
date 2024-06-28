import com.kizitonwose.calendar.buildsrc.Config
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "calendar"
    }

    jvm("jvm")  // fancy name for jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val jvmMain by getting
        val commonMain by getting

        commonMain.dependencies {
            implementation(project(":compose-multiplatform:lib-core"))
            implementation(libs.kotlinx.datetime)
            implementation(compose.runtime)
        }
    }
    jvmToolchain {
        languageVersion.set(Config.compatibleJavaLanguageVersion)
    }
}
