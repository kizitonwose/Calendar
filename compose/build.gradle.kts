
import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config

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
        minSdk = Android.minSdkComposeLibrary
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

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)

    testImplementation(libs.test.junit)
}
