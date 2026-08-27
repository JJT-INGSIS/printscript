plugins {
    id("printscript.kotlin-library")
}

dependencies {
    api(project(":token-source"))
    api(project(":statement-source"))
}
