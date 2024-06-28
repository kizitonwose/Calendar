import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "calendar"
    }

    androidTarget {}

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

        androidMain.dependencies {
            implementation(compose.preview)
        }
        jvmMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            api(project(":compose-multiplatform:lib-compose-core"))
            api(project(":compose-multiplatform:lib-core-jvm"))
            implementation(project(":compose-multiplatform:lib-core-kmp"))
            implementation(project(":compose-multiplatform:lib-data"))
            implementation(libs.kotlinx.datetime)
        }
        androidMain.get().dependsOn(jvmMain)
        val nonJvmMain by creating {
            dependsOn(commonMain)
            nativeMain.dependsOn(this)
            wasmJsMain.dependsOn(this)
            dependencies {}
        }
//        desktopMain.dependencies {
//            implementation(compose.desktop.currentOs)
//        }
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "com.kizitonwose.calendarx"
    compileSdk = Android.compileSdk

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = Android.minSdkComposeLibrary
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    java {
        toolchain {
            languageVersion.set(Config.compatibleJavaLanguageVersion)
        }
    }
    kotlin {
        jvmToolchain {
            languageVersion.set(Config.compatibleJavaLanguageVersion)
        }
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}
