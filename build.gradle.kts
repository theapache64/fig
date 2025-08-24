plugins {
    kotlin("jvm") version "2.2.10"
}

group = "com.github.theapache64"
// [latest version - i promise!]
version = "0.0.3"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // library
    implementation(project(":fig"))

    // test
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Kotlinx Coroutines Test : Coroutines support libraries for Kotlin
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    // Expekt : An assertion library for Kotlin
    implementation("com.github.theapache64:expekt:1.0.0")

    // Kotlinx Coroutines Core : Coroutines support libraries for Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}