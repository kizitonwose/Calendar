@file:Suppress("unused", "ConstPropertyName")

package com.kizitonwose.calendar.buildsrc

import org.gradle.api.JavaVersion

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_17
}

object Android {
    const val minSdkLibraryCore = 4
    const val minSdkLibraryView = 15
    const val minSdkLibraryCompose = 21
    const val minSdkSample = 21
    const val targetSdk = 33
    const val compileSdk = 34

    // See compose/kotlin version mapping
    // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    const val composeCompiler = "1.5.9"
}

object Plugins {
    const val android = "com.android.tools.build:gradle:8.1.0"
    const val kotlin = Kotlin.gradlePlugin
    const val kotlinter = "org.jmailen.gradle:kotlinter-gradle:4.2.0"
    const val versions = "com.github.ben-manes:gradle-versions-plugin:0.51.0"
    const val mavenPublish = "com.vanniktech:gradle-maven-publish-plugin:0.27.0"
}

object Kotlin {
    private const val version = "1.9.22"
    const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
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
        const val coreKtx = "androidx.core:core-ktx:1.12.0"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val cardView = "androidx.cardview:cardview:1.0.0"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.3.2"
        const val material = "com.google.android.material:material:1.9.0"

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
        const val activity = "androidx.activity:activity-compose:1.8.2"
        const val navigation = "androidx.navigation:navigation-compose:2.7.7"

        object Test {
            const val uiJunit = "androidx.compose.ui:ui-test-junit4:$composeVersion"
            const val uiManifest = "androidx.compose.ui:ui-test-manifest:$composeVersion"
        }
    }
}
