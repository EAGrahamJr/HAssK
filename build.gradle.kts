buildscript {
    dependencies {
        classpath("crackers.buildstuff:crackers-gradle-plugins:1.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.8.10"
    idea
    id("org.jmailen.kotlinter") version "3.12.0"
    id("library-publish") version "1.0.0"
}

group = "crackers.automation"
version = "0.0.1"
val jacksonVersion = "2.14.2"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("org.json:json:20230227")
    api("ch.qos.logback:logback-classic:1.4.0")

    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.mockk:mockk:1.13.3")
}

kotlin {
    jvmToolchain(17)
}

kotlinter {
    // ignore failures because the build re-formats it
    ignoreFailures = true
    disabledRules = arrayOf("no-wildcard-imports")
}


tasks {
    build {
        dependsOn("formatKotlin")
    }
    test {
        useJUnitPlatform()
    }
}

defaultTasks("clean", "build", "libraryDistribution")
