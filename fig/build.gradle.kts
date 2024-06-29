plugins {
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
    id("com.vanniktech.maven.publish") version "0.28.0"
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

mavenPublishing {
    coordinates("com.github.theapache64", "fig", "0.0.1")

    // the following is optional

    pom {
        name.set("fig")
        description.set("Google sheet based remote config library")
        inceptionYear.set("2024")
        url.set("https://github.com/theapache64/fig/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("theapache64")
                name.set("theapache64")
                url.set("https://github.com/theapache64/")
            }
        }
        scm {
            url.set("https://github.com/theapache64/fig/")
            connection.set("scm:git:git://github.com/theapache64/fig.git")
            developerConnection.set("scm:git:ssh://git@github.com/theapache64/fig.git")
        }
    }
}