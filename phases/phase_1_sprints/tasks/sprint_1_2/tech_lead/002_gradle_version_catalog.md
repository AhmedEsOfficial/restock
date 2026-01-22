# Task: Configure Gradle with Version Catalogs

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 3 hours
**Priority:** P0
**Prerequisites:** 001_project_structure_setup.md

---

## Sprint Context

This sprint establishes the technical foundation for Restock. Version catalogs provide centralized dependency management, ensuring consistent versions across all modules and easier dependency updates.

### Sprint Theme
> *"Build the foundation right so everything else is fast."*

---

## Task Description

Create and configure Gradle version catalogs (libs.versions.toml) with all dependencies needed for Phase 1 development.

## libs.versions.toml

```toml
[versions]
kotlin = "2.0.0"
agp = "8.2.0"
ksp = "2.0.0-1.0.21"

# AndroidX
core-ktx = "1.12.0"
lifecycle = "2.7.0"
activity-compose = "1.8.2"

# Compose
compose-bom = "2024.02.00"
compose-compiler = "1.5.10"

# Hilt
hilt = "2.50"
hilt-navigation-compose = "1.2.0"

# Room
room = "2.6.1"

# DataStore
datastore = "1.0.0"

# Coroutines
coroutines = "1.8.0"

# Serialization
serialization = "1.6.0"

# Networking
ktor = "2.3.7"

# Image Loading
coil = "2.5.0"

# ML
tensorflow-lite = "2.14.0"

# WorkManager
work = "2.9.0"

# Testing
junit = "5.10.1"
mockk = "1.13.9"
turbine = "1.0.0"
androidx-test = "1.5.0"
espresso = "3.5.1"

# Code Quality
detekt = "1.23.4"
ktlint = "12.1.0"

[libraries]
# AndroidX Core
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "core-ktx" }
androidx-lifecycle-runtime = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }

# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons = { module = "androidx.compose.material:material-icons-extended" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
room-testing = { module = "androidx.room:room-testing", version.ref = "room" }

# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

# Serialization
serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

# Networking
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }

# Image Loading
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }

# ML
tensorflow-lite = { module = "org.tensorflow:tensorflow-lite", version.ref = "tensorflow-lite" }

# WorkManager
work-runtime = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }
work-testing = { module = "androidx.work:work-testing", version.ref = "work" }

# Testing
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
junit-jupiter-api = { module = "org.junit.jupiter:junit-jupiter-api", version.ref = "junit" }
junit-jupiter-engine = { module = "org.junit.jupiter:junit-jupiter-engine", version.ref = "junit" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
mockk-android = { module = "io.mockk:mockk-android", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
androidx-test-core = { module = "androidx.test:core", version.ref = "androidx-test" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidx-test" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }

[bundles]
compose = ["compose-ui", "compose-ui-graphics", "compose-ui-tooling-preview", "compose-material3"]
compose-debug = ["compose-ui-tooling", "compose-ui-test-manifest"]
room = ["room-runtime", "room-ktx"]
testing = ["junit-jupiter", "mockk", "turbine", "coroutines-test"]
testing-android = ["mockk-android", "androidx-test-core", "androidx-test-runner", "espresso-core"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

## Acceptance Criteria

- [ ] libs.versions.toml created in gradle/ directory
- [ ] All modules can reference dependencies via version catalog
- [ ] Build succeeds with catalog dependencies
- [ ] Version catalog syntax is valid (IDE shows no errors)
- [ ] Bundles defined for commonly-used dependency groups
- [ ] Plugins defined for all needed Gradle plugins

## Definition of Done

- [ ] Version catalog file created
- [ ] At least one module uses catalog dependencies
- [ ] No duplicate version definitions
- [ ] All Phase 1 dependencies included
- [ ] Build verification passes

## Dependent Tasks

This task blocks:
- 003_hilt_dependency_injection.md (needs Hilt versions)
- 004_room_database_entities.md (needs Room versions)
- All component implementation tasks

## Notes

- Keep versions up-to-date with stable releases
- Use BOM for Compose to ensure compatible versions
- Document any version constraints or known issues
- Prefer stable versions over alpha/beta for production code
