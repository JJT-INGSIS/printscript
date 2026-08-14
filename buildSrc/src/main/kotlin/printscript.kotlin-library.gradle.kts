plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    "testImplementation"(kotlin("test"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
