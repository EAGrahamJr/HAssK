/*
 * Copyright 2022-2025 by E. A. Graham, Jr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

plugins {
    kotlin("jvm") version "1.9.0"
    `java-library`
    idea
    id("org.jmailen.kotlinter") version "3.12.0"
    id("org.jetbrains.dokka") version "1.8.10"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("crackers.buildstuff.crackers-gradle-plugins") version "1.1.0"
}

group = "crackers.automation"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("org.json:json:20231013")
    api("org.slf4j:slf4j-api:2.0.0")

    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.mockk:mockk:1.13.3")
    testImplementation("org.slf4j:slf4j-simple:2.0.0")
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
    check {
//        dependsOn("installKotlinterPrePushHook")
        dependsOn("formatKotlin")
    }
    test {
        useJUnitPlatform()
    }
    // make docs
    dokkaJavadoc {
        mustRunAfter("javadoc")
        outputDirectory.set(file("$projectDir/build/docs"))
    }
    javadocJar {
        mustRunAfter("dokkaJavadoc")
        include("$projectDir/build/docs")
    }
    // jar docs
    register<Jar>("dokkaJavadocJar") {
        dependsOn(dokkaJavadoc)
        from(dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }
    generateMetadataFileForLibraryPublication {
        mustRunAfter("dokkaJavadocJar")
    }
}

defaultTasks("clean", "build", "dokkaJavadocJar", "libraryDistribution")
