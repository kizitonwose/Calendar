import com.kizitonwose.calendar.buildsrc.Config

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("jvm")  // fancy name for jvm("desktop")

    sourceSets {
        val jvmMain by getting
        val commonMain by getting

        jvmMain.dependencies {
            implementation(project(":compose-multiplatform:lib-core"))
            implementation(compose.runtime)
        }
    }
    jvmToolchain {
        languageVersion.set(Config.compatibleJavaLanguageVersion)
    }
}
