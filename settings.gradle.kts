pluginManagement {
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Restock"

// App module
include(":app")

// Core modules
include(":core:ui")
include(":core:data")
include(":core:domain")
include(":core:ml")

// Feature modules
include(":feature:list")
include(":feature:suggestions")
include(":feature:meals")
include(":feature:history")
include(":feature:settings")
