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

rootProject.name = "ExpenseTracker"
include(
    ":app",
    ":core:commons",
    ":core:domain",
    ":core:data",
    ":core:designsystem",
    ":feature:transactions",
    ":feature:budgets",
    ":feature:reports",
    ":feature:accounts",
    ":feature:categories",
)
