import com.kizitonwose.calendar.buildsrc.Config
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.composeCompiler)
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
    applyDefaultHierarchyTemplate()

    sourceSets {
        val jvmMain by getting
        val commonMain by getting
        val wasmJsMain by getting
        val nativeMain by getting
        commonMain.dependencies {
            implementation(compose.runtime)
        }
        val nonJvmMain by creating {
            dependsOn(commonMain)
            nativeMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
            dependencies {}
        }
        jvmToolchain {
            languageVersion.set(Config.compatibleJavaLanguageVersion)
        }
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
