plugins {
    id("printscript.kotlin-library")
}

dependencies {
    api(project(":statement-source"))
    api(project(":common"))
    testImplementation(project(":printscript-v1"))
}
