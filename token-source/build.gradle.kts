plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":common"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}