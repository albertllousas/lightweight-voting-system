plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.arrow)
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.web)
    implementation(libs.spring.boot.data.jdbc)
    implementation(libs.micrometer.core)
    implementation(libs.postgresql)
    implementation(libs.flyway)
    implementation(libs.flyway.postgresql)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.spring.boot.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.mockk)
    testImplementation(libs.spring.mockk)
    testImplementation(libs.rest.assured.all)
    testImplementation(libs.rest.assured)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
