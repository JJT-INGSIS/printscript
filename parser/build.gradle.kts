plugins {
    id("printscript.publishable-library")
}

dependencies {
    api(project(":token-source"))
    api(project(":statement-source"))
}
