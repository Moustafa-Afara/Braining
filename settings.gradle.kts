pluginManagement {
    includeBuild("build-logic/convention")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Braining"
include(":app")
include(":core-domain")
include(":core-data")
include(":core-ui")
include(":ai-providers")
include(":speech")
include(":feature-settings")
include(":feature-chat")
include(":feature-clarify")
include(":feature-history")
