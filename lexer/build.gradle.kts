plugins {
    id("printscript.publishable-library")
}

dependencies {
    api(project(":source-reader"))
    api(project(":token-source"))
    api(project(":common"))
}
