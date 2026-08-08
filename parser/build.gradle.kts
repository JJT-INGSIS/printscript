plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":lexer"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
