plugins {
    id("printscript.kotlin-library")
}

dependencies {
    api(project(":token-source"))
    implementation(project(":common"))
}
