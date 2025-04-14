
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    // Thymeleaf
    implementation("io.ktor:ktor-server-thymeleaf:3.1.2")

    // Firebase

    implementation("com.google.firebase:firebase-admin:9.1.1")

    // Kotlinx serialization
    implementation("io.ktor:ktor-serialization-jackson:3.1.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.2")
}
