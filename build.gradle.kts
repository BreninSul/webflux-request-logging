/*
 * MIT License
 *
 * Copyright (c) 2023 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("maven-publish")
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.22"
    id("org.jetbrains.kotlin.kapt") version "1.9.22"
}
group = "com.github.breninsul"
version = "1.1.03"
java.sourceCompatibility = JavaVersion.VERSION_21


repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
java {
    withSourcesJar()
}
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
            val softwareComponent = components.first()
            from(softwareComponent)
        }

    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/BreninSul/webflux-request-logging")
            credentials {
                username = "${System.getenv()["GIHUB_PACKAGE_USERNAME"]}"
                password = "${System.getenv()["GIHUB_PACKAGE_TOKEN"]}"
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}
tasks.compileJava{
    val dependsOn = dependsOn
    dependsOn.add(tasks.processResources)
}
tasks.compileKotlin{
    dependsOn.add(tasks.processResources)
}

tasks.withType<Test> {
    useJUnitPlatform()
}


dependencies {
    implementation("com.github.jitpack:gradle-simple:1.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux:")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway:4.1.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    annotationProcessor("org.apache.logging.log4j:log4j-core")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

