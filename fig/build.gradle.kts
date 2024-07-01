plugins {
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
    kotlin("jvm") version "1.9.24"
}

group = "com.github.theapache64.fig"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // retrosheet
    implementation("com.github.theapache64:retrosheet:2.0.1")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // moshi
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // coroutines core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")


}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}