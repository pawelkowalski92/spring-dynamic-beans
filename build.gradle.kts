import org.gradle.api.JavaVersion.VERSION_17

plugins {
    java
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    id("org.springframework.boot") version "2.7.4"
}

repositories {
    mavenCentral()
}

group = "com.github.pawelkowalski92"
version = "JDD-2022"

java {
    sourceCompatibility = VERSION_17
}

ext {
    set(
        "versions", mapOf(
            "testcontainers" to "1.17.4",
            "reactor" to "3.4.23"
        )
    )
}

dependencies {
    val versions: Map<String, String> by project.extra

    implementation("org.springframework.boot", "spring-boot-starter-webflux")
    annotationProcessor("org.springframework.boot", "spring-boot-configuration-processor")

    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.testcontainers", "testcontainers", versions["testcontainers"])
    testImplementation("org.testcontainers", "junit-jupiter", versions["testcontainers"])
    testImplementation("io.projectreactor", "reactor-test", versions["reactor"])
}

tasks {
    test {
        useJUnitPlatform()
    }
}

