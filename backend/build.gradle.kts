plugins {
    kotlin("jvm") version "2.2.20"
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
}

group = "rs.etf.snippet.rest.ktor"
version = "0.0.1"

application {
    mainClass = "rs.etf.snippet.rest.ktor.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.mongodb:mongodb-driver-bom:5.6.4"))
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine")
    implementation("org.mongodb:bson-kotlinx")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    testImplementation(kotlin("test"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization.json)
    implementation(libs.logback.classic)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
