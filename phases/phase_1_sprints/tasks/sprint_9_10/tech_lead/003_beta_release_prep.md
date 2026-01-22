# Task: Beta Release Preparation

**Sprint:** 9-10 (Meal Tags & Polish)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 12 hours (T-12.1 through T-12.7 combined)
**Priority:** P0
**Prerequisites:** 002_performance_optimization.md

---

## Sprint Context

This task prepares the app for beta testing with Firebase App Distribution and Crashlytics integration.

### Sprint Theme
> *"Refine every detail until it delights."*

---

## Task Description

Set up Firebase App Distribution, configure Crashlytics, and prepare the beta build for distribution to testers.

## T-12.1: Firebase App Distribution Setup (2h)

### Firebase Configuration

```kotlin
// project-level build.gradle.kts
plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.firebase.appdistribution") version "4.0.1" apply false
}

// app/build.gradle.kts
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}

android {
    buildTypes {
        create("beta") {
            initWith(getByName("release"))
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"

            firebaseAppDistribution {
                artifactType = "APK"
                groups = "internal-testers"
                releaseNotes = "Beta release for internal testing"
            }
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

### Firebase Setup Script

```bash
#!/bin/bash
# scripts/setup_firebase.sh

# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Add google-services.json to app directory
# (Download from Firebase Console)

echo "Firebase setup complete!"
```

## T-12.2: Crashlytics Configuration (2h)

### Application Setup

```kotlin
// RestockApplication.kt
@HiltAndroidApp
class RestockApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            // Disable for debug builds
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

            // Set custom keys for debugging
            setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            setCustomKey("version_code", BuildConfig.VERSION_CODE)
        }

        // Initialize other components
        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}
```

### Crashlytics Timber Tree

```kotlin
// CrashlyticsTree.kt
class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return

        val crashlytics = FirebaseCrashlytics.getInstance()

        // Log message
        crashlytics.log("$priority/$tag: $message")

        // Record exceptions
        when {
            t != null -> crashlytics.recordException(t)
            priority >= Log.ERROR -> crashlytics.recordException(
                Exception("Error without exception: $message")
            )
        }
    }
}
```

### Custom Keys for Debugging

```kotlin
// Add context to crash reports
fun setCrashlyticsContext(
    userId: String? = null,
    screenName: String? = null,
) {
    val crashlytics = FirebaseCrashlytics.getInstance()

    userId?.let { crashlytics.setUserId(it.hashCode().toString()) }
    screenName?.let { crashlytics.setCustomKey("current_screen", it) }
}

// Usage in ViewModel or Activity
fun setCurrentScreen(screenName: String) {
    setCrashlyticsContext(screenName = screenName)
}
```

## T-12.3: Build Beta APK/AAB (1h)

### Build Script

```bash
#!/bin/bash
# scripts/build_beta.sh

set -e

# Clean previous builds
./gradlew clean

# Run tests
./gradlew testBetaUnitTest

# Build beta APK
./gradlew assembleBeta

# Build beta AAB (for Play Store testing)
./gradlew bundleBeta

echo "Beta builds complete!"
echo "APK: app/build/outputs/apk/beta/app-beta.apk"
echo "AAB: app/build/outputs/bundle/beta/app-beta.aab"
```

### Version Management

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = calculateVersionCode()
        versionName = "1.0.0"
    }
}

fun calculateVersionCode(): Int {
    // Use timestamp for unique version codes in beta
    return if (project.hasProperty("betaVersionCode")) {
        project.property("betaVersionCode").toString().toInt()
    } else {
        1
    }
}
```

## T-12.4: Deploy to Testers (1h)

### Deployment Script

```bash
#!/bin/bash
# scripts/deploy_beta.sh

set -e

# Build first
./scripts/build_beta.sh

# Deploy to Firebase App Distribution
./gradlew appDistributionUploadBeta \
    --releaseNotes="$(cat CHANGELOG.md | head -n 20)"

echo "Beta deployed to Firebase App Distribution!"
```

### Tester Groups Configuration

```groovy
// Firebase App Distribution groups
// Configure in Firebase Console:
// - internal-testers: Team members
// - external-testers: Friends & family
// - early-adopters: Waitlist signups
```

## T-12.5: Bug Fixes from Internal Testing (6h)

### Bug Tracking Template

```markdown
## Bug Report: [Title]

**Device:** [Device model, Android version]
**Build:** [Version name, version code]
**Steps to Reproduce:**
1.
2.
3.

**Expected:** [What should happen]
**Actual:** [What actually happens]
**Crashlytics Link:** [If applicable]

**Priority:** P0/P1/P2
**Assigned:** [Name]
```

### Common Beta Issues Checklist

- [ ] App crashes on specific devices
- [ ] Performance issues on low-end devices
- [ ] UI issues on different screen sizes
- [ ] Offline functionality issues
- [ ] Data persistence issues
- [ ] Animation jank
- [ ] Memory leaks

## T-12.6: Code Review Final Pass (4h)

### Review Checklist

- [ ] No TODO comments in release code
- [ ] No debug logging in release builds
- [ ] All hardcoded strings extracted
- [ ] Error handling complete
- [ ] Edge cases covered
- [ ] Security review passed
- [ ] Performance acceptable
- [ ] Accessibility complete

### Pre-Release Checks

```kotlin
// Build config checks
object ReleaseChecks {
    fun verify() {
        check(!BuildConfig.DEBUG) { "Release build should not be debug" }
        check(BuildConfig.VERSION_CODE > 0) { "Version code must be positive" }
    }
}
```

## T-12.7: Documentation Update (2h)

### Update README

```markdown
# Restock

## Getting Started

### Development Setup
1. Clone the repository
2. Copy `google-services.json` to `app/`
3. Run `./gradlew assembleDebug`

### Running Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Building Beta
```bash
./scripts/build_beta.sh
./scripts/deploy_beta.sh
```

## Architecture
- MVVM + Clean Architecture
- Jetpack Compose UI
- Room Database
- Hilt Dependency Injection
```

### Update CHANGELOG

```markdown
# Changelog

## [1.0.0-beta.1] - 2024-XX-XX

### Added
- Grocery list with add, check, delete, reorder
- Smart suggestions based on purchase patterns
- Meal tags for item organization
- Offline-first functionality

### Known Issues
- [List any known issues]
```

## Acceptance Criteria

- [ ] Firebase App Distribution configured
- [ ] Crashlytics receiving crash reports
- [ ] Beta APK builds successfully
- [ ] Beta deployed to testers
- [ ] Critical bugs fixed
- [ ] Code review completed
- [ ] Documentation updated

## Definition of Done

- [ ] Beta build distributed to 20+ testers
- [ ] Crash-free rate > 99%
- [ ] No P0 bugs open
- [ ] Documentation complete
- [ ] Ready for Sprint 11-12 launch prep

## Notes

- Keep beta tester list updated
- Monitor Crashlytics daily during beta
- Respond quickly to tester feedback
- Document all known issues
