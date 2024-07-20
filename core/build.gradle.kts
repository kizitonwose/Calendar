import com.kizitonwose.calendar.buildsrc.Config
import com.kizitonwose.calendar.buildsrc.Version

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.mavenPublish)
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

dependencies {
    implementation(libs.compose.runtime) // Only needed for @Immutable annotation.
}

mavenPublishing {
    coordinates(version = Version.android)
}
