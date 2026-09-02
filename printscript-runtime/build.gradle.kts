plugins {
    id("printscript.publishable-library")
}

dependencies {
    api(project(":interpreter"))
    api(project(":printscript-ast"))
}
