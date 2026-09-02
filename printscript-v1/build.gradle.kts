plugins {
    id("printscript.publishable-library")
}

dependencies {
    api(project(":interpreter"))
    api(project(":lexer"))
    api(project(":parser"))
    api(project(":formatter"))
    api(project(":linter"))

    api(project(":token-source"))
    api(project(":statement-source"))
    api(project(":printscript-ast"))
    api(project(":printscript-runtime"))
    api(project(":common"))
}
