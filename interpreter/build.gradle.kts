plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":statement-source"))
    api(project(":language-model"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}