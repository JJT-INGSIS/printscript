plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":common"))
    api(project(":token-source"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}