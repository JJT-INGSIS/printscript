plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":token-source"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
