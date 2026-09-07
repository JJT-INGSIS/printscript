plugins {
    id("printscript.kotlin-library")
}

dependencies {
    testImplementation(project(":common"))
    testImplementation(project(":source-reader"))
    testImplementation(project(":token-source"))
    testImplementation(project(":statement-source"))
    testImplementation(project(":parser"))
    testImplementation(project(":interpreter"))
    testImplementation(project(":formatter"))
    testImplementation(project(":linter"))
    testImplementation(project(":printscript-runtime"))
    testImplementation(project(":printscript-v1"))
}
