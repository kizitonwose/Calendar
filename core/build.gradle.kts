import com.kizitonwose.calendar.buildsrc.Config
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
    implementation(Libs.Compose.runtime) // Only needed for @Immutable annotation.
}
