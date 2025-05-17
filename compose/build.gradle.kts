
import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config
import com.kizitonwose.calendar.buildsrc.Version

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kizitonwose.calendar.compose"
    defaultConfig {
        minSdk = Android.minSdk
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
}

dependencies {
    api(project(":core"))
    implementation(project(":data"))
    implementation(libs.kotlin.stdlib)

    implementation(libs.compose.ui.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)

    testImplementation(platform(libs.test.junit5.bom))
    testImplementation(libs.test.junit5.api)
    testRuntimeOnly(libs.test.junit5.engine)
    testRuntimeOnly(libs.test.junit.platform.launcher)
}

mavenPublishing {
    coordinates(version = Version.android)
}
