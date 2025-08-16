@file:Suppress("unused", "ConstPropertyName")

package com.kizitonwose.calendar.buildsrc

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_17
    val compatibleJavaLanguageVersion = JavaLanguageVersion.of(compatibleJavaVersion.majorVersion)
}

object Version {
    const val android = "2.8.1-SNAPSHOT"
    const val multiplatform = "2.8.1-SNAPSHOT"

    fun String.isNoPublish() = this == VERSION_NO_PUBLISH
}

private const val VERSION_NO_PUBLISH = "NO_PUBLISH"

object Android {
    const val minSdk = 21
    const val targetSdk = 36
    const val compileSdk = 36
}

val multiplatformLibProjects = listOf("library")
val androidLibProjects = listOf("core", "data", "view", "compose")
