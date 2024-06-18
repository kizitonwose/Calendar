import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config
import com.kizitonwose.calendar.buildsrc.Kotlin
import com.kizitonwose.calendar.buildsrc.Libs

plugins {
    with(com.kizitonwose.calendar.buildsrc.Plugins) {
        id(androidLibrary)
        id(kotlinAndroid)
        id(composeCompiler)
        id(mavenPublish)
    }
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
    implementation(Kotlin.stdLib)

    implementation(Libs.Compose.ui)
    implementation(Libs.Compose.tooling)
    implementation(Libs.Compose.foundation)

    testImplementation(Libs.Core.Test.junit)
}
