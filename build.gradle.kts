allprojects {
    group = "printscript"
    version = "1.0-SNAPSHOT"
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
