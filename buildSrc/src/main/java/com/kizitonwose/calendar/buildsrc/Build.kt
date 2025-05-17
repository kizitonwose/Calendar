@file:Suppress("unused", "ConstPropertyName")

package com.kizitonwose.calendar.buildsrc

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_17
    val compatibleJavaLanguageVersion = JavaLanguageVersion.of(compatibleJavaVersion.majorVersion)
}

object Version {
    val android = "2.7.0"
    val multiplatfrom = "2.7.0"

    fun String.isNoPublish() = this == VERSION_NO_PUBLISH
}

private val VERSION_NO_PUBLISH = "NO_PUBLISH"

object Android {
    const val minSdk = 21
    const val targetSdk = 35
    const val compileSdk = 35
}

val multiplatformProjects = listOf("library")
val androidProjects = listOf("core", "data", "view", "compose")
