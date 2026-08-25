pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }
}

rootProject.name = "printscript"

include(
    "common",
    "source-reader",
    "lexer",
    "parser",
    "token-source",
    "interpreter",
    "linter",
    "statement-source",
    "integration-tests",
    "formatter",
)