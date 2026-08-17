plugins {
    id("printscript.kotlin-library")
}

dependencies {
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
    testImplementation(project(":interpreter"))
}