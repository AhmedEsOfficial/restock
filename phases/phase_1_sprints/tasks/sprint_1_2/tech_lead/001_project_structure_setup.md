# Task: Multi-Module Project Structure Setup

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 4 hours
**Priority:** P0
**Prerequisites:** None (first task)

---

## Sprint Context

Sprint 1-2 establishes the technical foundation for the entire Restock application. This task creates the multi-module project structure that enables parallel development, clear separation of concerns, and scalable architecture.

### Sprint Goals
- Multi-module project structure operational
- Hilt DI configured across all modules
- Room database with all core entities
- CI/CD pipeline running
- Design system foundation complete

### Why This Matters
- Architectural decisions made now affect the entire project
- Multi-module structure enables independent feature development
- Clear module boundaries prevent circular dependencies
- Sets the foundation for all subsequent sprints

---

## Task Description

Initialize the multi-module Gradle project structure following Clean Architecture principles with MVVM pattern.

## Module Structure to Create

```
restock/
├── app/                          # Application entry point
├── core/
│   ├── ui/                       # Design system, themes, components
│   ├── data/                     # Database, data sources
│   ├── domain/                   # Business logic, entities, use cases
│   └── ml/                       # Pattern learning engine (placeholder)
├── feature/
│   ├── list/                     # Main grocery list
│   ├── suggestions/              # Smart suggestions
│   ├── meals/                    # Meal tagging
│   ├── history/                  # Purchase history
│   └── settings/                 # User preferences
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── build.gradle.kts              # Root build file
└── settings.gradle.kts           # Module settings
```

## Acceptance Criteria

- [ ] All modules created with proper directory structure
- [ ] `./gradlew build` succeeds without errors
- [ ] Module dependencies are acyclic (no circular dependencies)
- [ ] Build time < 2 minutes (clean build)
- [ ] All modules have consistent Kotlin and Android versions
- [ ] settings.gradle.kts includes all modules
- [ ] Root build.gradle.kts configures common settings

## Technical Specifications

### settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google()
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

include(":app")
include(":core:ui")
include(":core:data")
include(":core:domain")
include(":core:ml")
include(":feature:list")
include(":feature:suggestions")
include(":feature:meals")
include(":feature:history")
include(":feature:settings")
```

### Module Dependencies
```
:app
├── :core:ui
├── :core:data
├── :core:domain
├── :feature:list
├── :feature:suggestions
├── :feature:meals
├── :feature:history
└── :feature:settings

:feature:* (all feature modules)
├── :core:ui
├── :core:domain
└── :core:data (if needed)

:core:data
├── :core:domain

:core:ui
└── (no internal dependencies)

:core:domain
└── (no internal dependencies - pure Kotlin)

:core:ml
├── :core:domain
```

### SDK Requirements
```kotlin
android {
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }
}
```

## Definition of Done

- [ ] All module directories created
- [ ] Each module has build.gradle.kts with appropriate plugins
- [ ] Clean build succeeds
- [ ] No warnings about deprecated configurations
- [ ] Module graph is acyclic (verified manually)
- [ ] README.md updated with module descriptions

## Dependent Tasks

This task blocks:
- 002_gradle_version_catalog.md
- 003_hilt_dependency_injection.md
- All other Sprint 1-2 tasks

## Notes

- Use Android Studio's module creation wizard for consistency
- Ensure package naming follows `com.restock.[module]` convention
- Feature modules should use dynamic feature module format if planning for app bundles later
- Keep :core:domain as pure Kotlin (no Android dependencies) for testability
