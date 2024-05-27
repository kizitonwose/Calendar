import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config
import com.kizitonwose.calendar.buildsrc.Kotlin
import com.kizitonwose.calendar.buildsrc.Libs

plugins {
    with(com.kizitonwose.calendar.buildsrc.Plugins) {
        id(androidLibrary)
        id(kotlinAndroid)
        id(mavenPublish)
    }
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kizitonwose.calendar.view"
    defaultConfig {
        minSdk = Android.minSdkViewLibrary
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
    implementation(Kotlin.stdLib)
    implementation(Libs.View.coreKtx)

    // Expose RecyclerView which is CalendarView"s superclass.
    api(Libs.View.recyclerView)
}
