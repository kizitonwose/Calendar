import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

// Pure Kotlin business logic of the work calendar app: schedule generation,
// pay and payout calculations, absence pay, notification planning, export.
// Kept free of Android dependencies so it can be unit-tested on the JVM.

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(libs.kotlinx.serialization.json)

    testImplementation(platform(libs.test.junit5.bom))
    testImplementation(libs.test.junit5.api)
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.test.junit5.engine)
    testRuntimeOnly(libs.test.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
