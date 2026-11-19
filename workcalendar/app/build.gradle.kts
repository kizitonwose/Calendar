import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "io.github.ceniorpomidor.workcalendar"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.ceniorpomidor.workcalendar.preview"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "1.6.0-beta.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // The key is kept in the repository so that every build (local or CI) has the same
        // signature and can be installed over the previous version without losing data.
        // Set WORKCAL_KEYSTORE* environment variables to sign with a private key instead.
        create("shared") {
            storeFile = file(System.getenv("WORKCAL_KEYSTORE") ?: "signing/workcalendar.jks")
            storePassword = System.getenv("WORKCAL_KEYSTORE_PASSWORD") ?: "workcalendar"
            keyAlias = System.getenv("WORKCAL_KEY_ALIAS") ?: "workcalendar"
            keyPassword = System.getenv("WORKCAL_KEY_PASSWORD") ?: "workcalendar"
        }
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("shared")
        }
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("shared")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

androidComponents {
    // The app has no instrumentation tests: without this, connectedDebugAndroidTest of the
    // library CI would try to install the app on emulators older than its minSdk.
    beforeVariants { variant -> variant.enableAndroidTest = false }
}

dependencies {
    implementation(project(":workcalendar:domain"))
    implementation(project(":compose"))
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.compose.activity)
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.navigation)
    implementation(libs.compose.lifecycle.runtime)
    implementation(libs.compose.lifecycle.viewmodel)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}
