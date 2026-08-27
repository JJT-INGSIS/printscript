plugins {
    id("printscript.kotlin-library")
}

dependencies {
    api(project(":source-reader"))
    api(project(":token-source"))
    api(project(":common"))
}
