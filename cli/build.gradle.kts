plugins {
    id("printscript.kotlin-application")
}

application {
    mainClass.set("printscript.cli.MainKt")
    applicationName = "printscript"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":source-reader"))
    implementation(project(":token-source"))
    implementation(project(":lexer"))
    implementation(project(":statement-source"))
    implementation(project(":printscript-ast"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(project(":linter"))
    implementation(project(":printscript-v1"))

    implementation("com.github.ajalt.clikt:clikt:5.1.0")
}
