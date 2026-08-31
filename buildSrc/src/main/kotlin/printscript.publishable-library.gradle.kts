import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("printscript.kotlin-library")
    `maven-publish`
}

val githubActor = providers.environmentVariable("GITHUB_ACTOR")
val githubToken = providers.environmentVariable("GITHUB_TOKEN")

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/jjt-ingsis/printscript")

            credentials {
                username = githubActor.orNull
                password = githubToken.orNull
            }
        }
    }
}
