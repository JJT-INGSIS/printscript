plugins {
    id("printscript.kotlin-library")
}

dependencies {
    testImplementation(project(":source-reader"))
    testImplementation(project(":interpreter"))
    testImplementation(project(":printscript-v1"))
}
