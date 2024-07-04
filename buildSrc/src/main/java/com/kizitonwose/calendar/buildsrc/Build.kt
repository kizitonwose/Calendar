@file:Suppress("unused", "ConstPropertyName")

package com.kizitonwose.calendar.buildsrc

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion
import kotlin.math.max

object Config {
    val compatibleJavaVersion = JavaVersion.VERSION_17
    val compatibleJavaLanguageVersion = JavaLanguageVersion.of(compatibleJavaVersion.majorVersion.toInt())
}

object Versions {
    val core = "2.6.0-SNAPSHOT"
    val multiplatfrom = "2.6.0-alpha01"
}

object Android {
    const val minSdkViewLibrary = 19
    const val minSdkComposeLibrary = 21
    val minSdkSampleApp = max(minSdkViewLibrary, minSdkComposeLibrary)
    const val targetSdk = 34
    const val compileSdk = 34
}
