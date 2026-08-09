plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":language-model"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}