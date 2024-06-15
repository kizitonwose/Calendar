import com.kizitonwose.calendar.buildsrc.Config
import com.kizitonwose.calendar.buildsrc.Kotlin
import com.kizitonwose.calendar.buildsrc.Libs

plugins {
    with(com.kizitonwose.calendar.buildsrc.Plugins) {
        id(kotlinJvm)
        id(mavenPublish)
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

dependencies {
    implementation(project(":core"))
    implementation(Kotlin.stdLib)

    testImplementation(Libs.Core.Test.junit)
}
