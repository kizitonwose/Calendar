
import com.kizitonwose.calendar.buildsrc.Android
import com.kizitonwose.calendar.buildsrc.Config

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

android {
    compileSdk = Android.compileSdk
    namespace = "com.kizitonwose.calendar.sample"
    defaultConfig {
        applicationId = "com.kizitonwose.calendar.sample"
        minSdk = Android.minSdk
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
            isMinifyEnabled = true
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
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.kotlin.stdlib)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.material.view)

    implementation(libs.compose.ui.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)

    testImplementation(libs.test.junit4)

    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.contrib) // RecyclerView actions.
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.junit)

    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest) // Compose test runner activity
}
