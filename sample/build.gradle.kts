
import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config
import com.kizitonwose.calendar.buildsrc.Kotlin
import com.kizitonwose.calendar.buildsrc.Libs

plugins {
    with(com.kizitonwose.calendar.buildsrc.Plugins) {
        id(androidApp)
        id(kotlinAndroid)
        id(composeCompiler)
    }
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kizitonwose.calendar.sample"
    defaultConfig {
        applicationId = "com.kizitonwose.calendar.sample"
        minSdk = Android.minSdkSampleApp
        targetSdk = Android.targetSdk
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
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
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(project(":view"))
    implementation(project(":compose"))
    coreLibraryDesugaring(Libs.Core.deSugar)
    implementation(Kotlin.stdLib)

    implementation(Libs.View.legacySupport)
    implementation(Libs.View.appCompat)
    implementation(Libs.View.coreKtx)
    implementation(Libs.View.constraintLayout)
    implementation(Libs.View.cardView)
    implementation(Libs.View.material)

    implementation(Libs.Compose.ui)
    implementation(Libs.Compose.tooling)
    implementation(Libs.Compose.foundation)
    implementation(Libs.Compose.material)
    implementation(Libs.Compose.activity)
    implementation(Libs.Compose.navigation)

    testImplementation(Libs.Core.Test.junit)

    androidTestImplementation(Libs.View.Test.espressoCore)
    androidTestImplementation(Libs.View.Test.espressoContrib) // RecyclerView actions.
    androidTestImplementation(Libs.View.Test.runner)
    androidTestImplementation(Libs.View.Test.rules)
    androidTestImplementation(Libs.View.Test.extJunit)

    androidTestImplementation(Libs.Compose.Test.uiJunit)
    debugImplementation(Libs.Compose.Test.uiManifest) // Compose test runner activity
}
