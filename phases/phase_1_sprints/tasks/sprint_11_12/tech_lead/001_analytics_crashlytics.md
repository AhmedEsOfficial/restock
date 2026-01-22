# Task: Complete Analytics & Crashlytics Integration

**Sprint:** 11-12 (Launch Preparation)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 19 hours (T-11.1 through T-11.8 combined)
**Priority:** P0
**Prerequisites:** Sprint 9-10 completed (beta testing in progress)

---

## Sprint Context

This is the final sprint before production launch. The focus is on launch readiness: crash reporting, analytics, security hardening, and operational preparation. No new features—only polish, fixes, and launch infrastructure.

### Sprint Theme
> *"Ship with confidence."*

### Key Principles
1. **No new features:** Only fixes and launch prep
2. **Zero tolerance for P0 bugs:** Must be fixed before launch
3. **Monitoring first:** Can't fix what you can't see

---

## Task Description

Complete Crashlytics integration and implement core analytics events for tracking user behavior.

## T-11.1-11.4: Complete Crashlytics Integration (9h)

### ProGuard Mapping Upload

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Auto-upload mapping file
            firebaseCrashlytics {
                mappingFileUploadEnabled = true
            }
        }
    }
}
```

### Custom Crash Keys

```kotlin
// CrashlyticsHelper.kt
object CrashlyticsHelper {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun setUserContext(userId: String?) {
        // Hash user ID for privacy
        userId?.let {
            crashlytics.setUserId(it.hashCode().toString())
        }
    }

    fun setScreenContext(screenName: String) {
        crashlytics.setCustomKey("current_screen", screenName)
    }

    fun setListContext(itemCount: Int, checkedCount: Int) {
        crashlytics.setCustomKey("list_item_count", itemCount)
        crashlytics.setCustomKey("list_checked_count", checkedCount)
    }

    fun setSuggestionContext(suggestionCount: Int) {
        crashlytics.setCustomKey("suggestion_count", suggestionCount)
    }

    fun setPatternContext(patternCount: Int) {
        crashlytics.setCustomKey("pattern_count", patternCount)
    }

    fun logBreadcrumb(message: String) {
        crashlytics.log(message)
    }

    fun recordNonFatal(throwable: Throwable, context: Map<String, String> = emptyMap()) {
        context.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(throwable)
    }
}
```

### Non-Fatal Error Logging

```kotlin
// Use throughout the app for non-fatal errors
class SomeRepository {

    suspend fun someOperation(): Result<Unit> {
        return try {
            // operation
            Result.success(Unit)
        } catch (e: Exception) {
            CrashlyticsHelper.recordNonFatal(
                throwable = e,
                context = mapOf(
                    "operation" to "someOperation",
                    "retry_count" to retryCount.toString(),
                )
            )
            Result.failure(e)
        }
    }
}
```

## T-11.5-11.8: Implement Analytics Events (10h)

### AnalyticsManager.kt

```kotlin
package com.restock.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) {
    // App lifecycle
    fun logAppOpen(launchType: LaunchType) {
        firebaseAnalytics.logEvent("app_open") {
            param("launch_type", launchType.name.lowercase())
        }
    }

    // List operations
    fun logItemAdded(source: ItemSource) {
        firebaseAnalytics.logEvent("item_added") {
            param("source", source.name.lowercase())
        }
    }

    fun logItemChecked(timeOnListHours: Long) {
        firebaseAnalytics.logEvent("item_checked") {
            param("time_on_list_hours", timeOnListHours)
        }
    }

    fun logItemDeleted(wasChecked: Boolean) {
        firebaseAnalytics.logEvent("item_deleted") {
            param("was_checked", if (wasChecked) "true" else "false")
        }
    }

    // Suggestions
    fun logSuggestionsShown(count: Int) {
        firebaseAnalytics.logEvent("suggestions_shown") {
            param("count", count.toLong())
        }
    }

    fun logSuggestionAccepted(position: Int) {
        firebaseAnalytics.logEvent("suggestion_accepted") {
            param("position", position.toLong())
        }
    }

    fun logSuggestionDismissed() {
        firebaseAnalytics.logEvent("suggestion_dismissed") {
            // No additional params to avoid PII
        }
    }

    // Meal tags
    fun logMealTagAdded(mealType: String) {
        firebaseAnalytics.logEvent("meal_tag_added") {
            param("meal_type", mealType.lowercase())
        }
    }

    // List completion
    fun logListCompleted(itemCount: Int, durationMinutes: Long) {
        firebaseAnalytics.logEvent("list_completed") {
            param("item_count", itemCount.toLong())
            param("duration_minutes", durationMinutes)
        }
    }

    // User properties
    fun setShoppingTripCount(count: Int) {
        firebaseAnalytics.setUserProperty("shopping_trip_count", count.toString())
    }

    fun setItemsLifetime(count: Int) {
        firebaseAnalytics.setUserProperty("items_lifetime", count.toString())
    }

    fun setSuggestionAcceptanceRate(rate: Float) {
        firebaseAnalytics.setUserProperty(
            "suggestion_acceptance_rate",
            "%.2f".format(rate)
        )
    }

    fun setMealTagsUsed(used: Boolean) {
        firebaseAnalytics.setUserProperty("meal_tags_used", used.toString())
    }

    fun setDaysSinceInstall(days: Int) {
        firebaseAnalytics.setUserProperty("days_since_install", days.toString())
    }

    enum class LaunchType {
        COLD, WARM
    }

    enum class ItemSource {
        MANUAL, SUGGESTION, AUTO_COMPLETE
    }
}
```

### Analytics Integration Points

```kotlin
// In AddItemUseCase
class AddItemUseCase @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    // ... other deps
) {
    suspend operator fun invoke(name: String, source: ItemSource): Result<CurrentListItem> {
        // ... existing logic

        result.onSuccess {
            analyticsManager.logItemAdded(source)
        }

        return result
    }
}

// In CheckItemUseCase
class CheckItemUseCase @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    // ... other deps
) {
    suspend operator fun invoke(id: String, isChecked: Boolean): Result<Unit> {
        val listItem = currentListRepository.getById(id)

        // ... existing logic

        if (isChecked && result.isSuccess && listItem != null) {
            val hoursOnList = (Clock.System.now() - listItem.addedAt).inWholeHours
            analyticsManager.logItemChecked(hoursOnList)
        }

        return result
    }
}

// In SuggestionsViewModel
class SuggestionsViewModel @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    // ... other deps
) {
    private fun observeSuggestions() {
        viewModelScope.launch {
            getSuggestionsUseCase()
                .collect { suggestions ->
                    if (suggestions.isNotEmpty()) {
                        analyticsManager.logSuggestionsShown(suggestions.size)
                    }
                    // ... rest of collection
                }
        }
    }

    private fun acceptSuggestion(suggestion: Suggestion, position: Int) {
        viewModelScope.launch {
            acceptSuggestionUseCase(suggestion)
                .onSuccess {
                    analyticsManager.logSuggestionAccepted(position)
                }
        }
    }
}
```

### Debug Mode for Testing

```kotlin
// AnalyticsDebugManager.kt
class AnalyticsDebugManager @Inject constructor(
    private val analyticsManager: AnalyticsManager,
) {
    fun enableDebugMode() {
        // Enable Firebase Analytics debug mode
        // Run: adb shell setprop debug.firebase.analytics.app com.restock
    }

    fun logTestEvent() {
        analyticsManager.logItemAdded(AnalyticsManager.ItemSource.MANUAL)
    }
}
```

### Privacy Considerations

```kotlin
// NEVER log PII
// ❌ Bad:
analytics.logEvent("item_added") {
    param("item_name", "Milk")  // Could reveal dietary info
}

// ✅ Good:
analytics.logEvent("item_added") {
    param("source", "manual")
}
```

## Acceptance Criteria

- [ ] Crashlytics receiving crash reports
- [ ] ProGuard mapping uploading correctly
- [ ] Custom keys providing useful context
- [ ] Non-fatal errors being recorded
- [ ] All core analytics events implemented
- [ ] User properties being set
- [ ] Debug mode working for testing
- [ ] No PII in analytics

## Testing

```kotlin
class AnalyticsManagerTest {

    @Test
    fun `logItemAdded sends correct event`() {
        val mockAnalytics = mockk<FirebaseAnalytics>(relaxed = true)
        val manager = AnalyticsManager(mockAnalytics)

        manager.logItemAdded(AnalyticsManager.ItemSource.SUGGESTION)

        verify {
            mockAnalytics.logEvent(
                eq("item_added"),
                withArg { bundle ->
                    assertEquals("suggestion", bundle.getString("source"))
                }
            )
        }
    }
}
```

## Definition of Done

- [ ] Crashlytics fully operational
- [ ] All analytics events implemented
- [ ] Events verified in DebugView
- [ ] No PII in any logs or events
- [ ] Documentation complete

## Notes

- Enable debug mode: `adb shell setprop debug.firebase.analytics.app com.restock`
- Check events in Firebase Console > Analytics > DebugView
- User properties update within 24 hours in dashboard
- Keep event names consistent and lowercase
