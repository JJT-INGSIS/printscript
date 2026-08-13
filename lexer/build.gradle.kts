plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":token-source"))
    api(project(":common"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}