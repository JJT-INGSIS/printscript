plugins {
    id("printscript.kotlin-library")
}

dependencies {
    api(project(":lexer"))
    api(project(":parser"))
    api(project(":formatter"))

    api(project(":token-source"))
    api(project(":statement-source"))
    api(project(":common"))
}
