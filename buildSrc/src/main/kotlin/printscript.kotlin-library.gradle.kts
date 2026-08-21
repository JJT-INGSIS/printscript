plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

dependencies {
    "testImplementation"(kotlin("test"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
