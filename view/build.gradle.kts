import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config
import com.kizitonwose.calendar.buildsrc.Version

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.mavenPublish)
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kizitonwose.calendar.view"
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
}

dependencies {
    api(project(":core"))
    implementation(project(":data"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)

    // Expose RecyclerView which is CalendarView"s superclass.
    api(libs.androidx.recyclerview)
}

mavenPublishing {
    coordinates(version = Version.android)
}
