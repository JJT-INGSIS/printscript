plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":language-model"))
    api(project(":token-source"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}