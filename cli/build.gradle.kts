plugins {
    id("printscript.kotlin-application")
}

application {
    mainClass.set("printscript.cli.MainKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":source-reader"))
    implementation(project(":token-source"))
    implementation(project(":statement-source"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
}
