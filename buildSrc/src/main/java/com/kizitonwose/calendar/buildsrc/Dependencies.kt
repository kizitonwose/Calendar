@file:Suppress("unused", "ConstPropertyName")

package com.kizitonwose.calendar.buildsrc

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import kotlin.math.max

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_17
    val compatibleJavaLanguageVersion = JavaLanguageVersion.of(compatibleJavaVersion.majorVersion.toInt())
}

object Android {
    const val minSdkViewLibrary = 15
    const val minSdkComposeLibrary = 21
    val minSdkSampleApp = max(minSdkViewLibrary, minSdkComposeLibrary)
    const val targetSdk = 34
    const val compileSdk = 34

    // See compose/kotlin version mapping
    // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    const val composeCompiler = "1.5.14"
}

object Plugins {
    private const val agpVersion = "8.4.0"
    const val androidApp = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinJvm = "org.jetbrains.kotlin.jvm"
    const val kotlinAndroid = "org.jetbrains.kotlin.android"
    const val kotlinter = "org.jmailen.kotlinter"
    const val mavenPublish = "com.vanniktech.maven.publish"
    const val versionCheck = "com.github.ben-manes.versions"

    fun PluginDependenciesSpecScope.applyRootPlugins() {
        id(androidApp).version(agpVersion).apply(false)
        id(androidLibrary).version(agpVersion).apply(false)
        id(kotlinAndroid).version(Kotlin.version).apply(false)
        id(kotlinter).version("4.3.0").apply(false)
        id(mavenPublish).version("0.28.0").apply(false)
        id(versionCheck).version("0.51.0").apply(true)
    }
}

object Kotlin {
    internal const val version = "1.9.24"
    const val stdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
}

object Libs {
    object Core {
        const val deSugar = "com.android.tools:desugar_jdk_libs:2.0.4"

        object Test {
            const val junit = "junit:junit:4.13.2"
        }
    }

    object View {
        const val legacySupport = "androidx.legacy:legacy-support-v4:1.0.0"
        const val appCompat = "androidx.appcompat:appcompat:1.6.1"
        const val coreKtx = "androidx.core:core-ktx:1.13.1"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val cardView = "androidx.cardview:cardview:1.0.0"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.3.2"
        const val material = "com.google.android.material:material:1.12.0"

        object Test {
            private const val espressoVersion = "3.5.1"
            const val espressoCore = "androidx.test.espresso:espresso-core:$espressoVersion"
            const val espressoContrib = "androidx.test.espresso:espresso-contrib:$espressoVersion"
            const val runner = "androidx.test:runner:1.5.2"
            const val rules = "androidx.test:rules:1.5.0"
            const val extJunit = "androidx.test.ext:junit:1.1.5"
        }
    }

    object Compose {
        private const val composeVersion = "1.6.1"
        const val ui = "androidx.compose.ui:ui:$composeVersion"
        const val foundation = "androidx.compose.foundation:foundation:$composeVersion"
        const val tooling = "androidx.compose.ui:ui-tooling:$composeVersion"
        const val runtime = "androidx.compose.runtime:runtime:$composeVersion"
        const val material = "androidx.compose.material:material:$composeVersion"
        const val activity = "androidx.activity:activity-compose:1.9.0"
        const val navigation = "androidx.navigation:navigation-compose:2.7.7"

        object Test {
            const val uiJunit = "androidx.compose.ui:ui-test-junit4:$composeVersion"
            const val uiManifest = "androidx.compose.ui:ui-test-manifest:$composeVersion"
        }
    }
}
