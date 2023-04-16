buildscript {
    dependencies {
        classpath("crackers.buildstuff:crackers-gradle-plugins:1.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.20"
    idea
    id("org.jmailen.kotlinter") version "3.12.0"
    id("library-publish") version "1.0.0"
}

group = "crackers.automation"
version = "1.0-SNAPSHOT"
val jacksonVersion = "2.14.2"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
//    api(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
//    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
//    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
//    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    api("ch.qos.logback:logback-classic:1.4.0")

    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.mockk:mockk:1.13.3")}

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

defaultTasks("clean","libraryDistribution")
