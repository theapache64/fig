plugins {
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
    kotlin("jvm") version "1.9.24"
    id("maven-publish")
}

group = "com.github.theapache64.fig"
version = "0.0.3"


repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

/*
// Set sourceJar
task sourcesJar(type: Jar, dependsOn: classes) {
    archiveClassifier.set('sources')
    from sourceSets.main.allSource
}
*/
// KTS version of above
tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

tasks.javadoc {
    isFailOnError = false
}
tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

artifacts {
    add("archives", tasks.named<Jar>("sourcesJar"))
    add("archives", tasks.named<Jar>("javadocJar"))
}

// pomConfig
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.named<Jar>("sourcesJar"))
            artifact(tasks.named<Jar>("javadocJar"))
            pom {
                name.set("fig")
                description.set("Use Google sheet as remote config")
                url.set("https://github.com/theapache64/fig")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("theapache64")
                        name.set("theapache64")
                        email.set("theapache64@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/theapache64/fig.git")
                    developerConnection.set("scm:git:ssh://github.com:theapache64/fig.git")
                    url.set("https://github.com/theapache64/fig")
                }
            }
        }
    }
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

