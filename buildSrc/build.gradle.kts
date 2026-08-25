plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.1")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
}
