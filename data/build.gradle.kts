
import com.kizitonwose.calendar.buildsrc.Config

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
    implementation(project(":core"))
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.test.junit)
}
