plugins {
    id("printscript.publishable-library")
}

dependencies {
    api(project(":statement-source"))
    api(project(":common"))
}
