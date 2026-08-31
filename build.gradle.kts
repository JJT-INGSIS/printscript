val printScriptVersion =
    providers.gradleProperty("releaseVersion")
        .getOrElse("1.0.0-SNAPSHOT")

allprojects {
    group = "io.github.jjt-ingsis.printscript"
    version = printScriptVersion
}

tasks.register<Copy>("installHooks") {
    description = "Copia los hooks versionados a .git/hooks"
    group = "build setup"

    from("$rootDir/.githooks")
    into("$rootDir/.git/hooks")

    filePermissions {
        unix("0755")
    }
}
