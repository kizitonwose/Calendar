plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.versionCheck)
}

allprojects {
    apply(plugin = rootProject.libs.plugins.kotlinter.get().pluginId)
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}
