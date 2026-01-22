# Task: Performance Optimization

**Sprint:** 9-10 (Meal Tags & Polish)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 19 hours (T-11.1 through T-11.6 combined)
**Priority:** P0
**Prerequisites:** 001_meal_tags_data_layer.md

---

## Sprint Context

Performance optimization is critical for user retention. This task focuses on achieving cold start < 2 seconds, 60fps scroll performance, and optimal memory usage.

### Sprint Theme
> *"Refine every detail until it delights."*

### Performance Targets

| Metric | Target |
|--------|--------|
| Cold start time | < 2 seconds |
| Warm start time | < 500ms |
| List scroll (500 items) | 60fps |
| Memory usage | < 150MB |

---

## Task Description

Implement Baseline Profiles, optimize database queries, reduce recompositions, and profile/fix performance bottlenecks.

## T-11.1: Set up Baseline Profiles (4h)

### Benchmark Module Setup

```kotlin
// settings.gradle.kts
include(":benchmark")

// benchmark/build.gradle.kts
plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.restock.benchmark"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
}

dependencies {
    implementation("androidx.test.ext:junit:1.1.5")
    implementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.2.3")
}
```

### Baseline Profile Generator

```kotlin
package com.restock.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(packageName = "com.restock") {
            // Cold start
            startActivityAndWait()

            // Wait for list to load
            device.wait(Until.hasObject(By.res("grocery_list")), 5000)

            // Type in add item field
            device.findObject(By.res("add_item_field"))?.click()
            device.pressBack()

            // Scroll the list
            device.findObject(By.res("grocery_list"))?.let { list ->
                list.scroll(Direction.DOWN, 1.0f)
                list.scroll(Direction.UP, 1.0f)
            }

            // Navigate to meals tab
            device.findObject(By.text("Meals"))?.click()
            device.wait(Until.hasObject(By.text("Breakfast")), 2000)

            // Navigate back to list
            device.findObject(By.text("List"))?.click()
        }
    }
}
```

### App Configuration

```kotlin
// app/build.gradle.kts
plugins {
    id("androidx.baselineprofile")
}

android {
    buildTypes {
        release {
            // Enable baseline profiles
            baselineProfile {
                automaticGenerationDuringBuild = true
            }
        }
    }
}

dependencies {
    baselineProfile(project(":benchmark"))
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
}
```

## T-11.2: Profile Cold Start & Identify Bottlenecks (3h)

### Startup Tracing

```kotlin
// Add to Application.onCreate()
class RestockApplication : Application() {

    override fun onCreate() {
        // Start tracing for startup
        Trace.beginSection("RestockApplication.onCreate")

        super.onCreate()

        Trace.beginSection("Hilt initialization")
        // Hilt auto-initializes here
        Trace.endSection()

        Trace.beginSection("WorkManager setup")
        patternWorkScheduler.schedulePeriodicCalculation()
        Trace.endSection()

        Trace.beginSection("Timber setup")
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Trace.endSection()

        Trace.endSection() // RestockApplication.onCreate
    }
}
```

### Common Bottlenecks & Fixes

| Bottleneck | Fix |
|------------|-----|
| Database initialization | Lazy init on first query |
| Hilt injection | Scope appropriately |
| Large initial query | Paginate or limit |
| Synchronous I/O | Move to background |

## T-11.3: Optimize Database Queries (3h)

### Add Missing Indices

```kotlin
@Entity(
    tableName = "current_list",
    indices = [
        Index(value = ["position"]),
        Index(value = ["is_checked"]),
        Index(value = ["item_id"]),
        Index(value = ["added_at"]),
    ]
)

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["item_id", "purchased_at"]),
        Index(value = ["purchased_at"]),
    ]
)

@Entity(
    tableName = "patterns",
    indices = [
        Index(value = ["confidence_score"]),
    ]
)
```

### Efficient Joins

```kotlin
// Instead of multiple queries, use single join
@Query("""
    SELECT cl.*, i.name as item_name, i.normalized_name
    FROM current_list cl
    INNER JOIN items i ON cl.item_id = i.id
    WHERE cl.is_checked = 0
    ORDER BY cl.position ASC
""")
fun observeUncheckedItemsWithNames(): Flow<List<CurrentListWithItem>>

// Use projection to avoid fetching unnecessary columns
@Query("""
    SELECT id, name FROM items
    WHERE normalized_name LIKE '%' || :query || '%'
    LIMIT :limit
""")
suspend fun searchByName(query: String, limit: Int): List<ItemNameProjection>
```

### Query Analysis

```kotlin
// Enable query logging in debug builds
@Database(...)
abstract class RestockDatabase : RoomDatabase() {
    companion object {
        fun create(context: Context): RestockDatabase {
            val builder = Room.databaseBuilder(...)

            if (BuildConfig.DEBUG) {
                builder.setQueryCallback({ sqlQuery, bindArgs ->
                    Timber.d("SQL: $sqlQuery, Args: $bindArgs")
                }, Executors.newSingleThreadExecutor())
            }

            return builder.build()
        }
    }
}
```

## T-11.4: Reduce Unnecessary Recompositions (4h)

### Use Stable Collections

```kotlin
// Use ImmutableList for Compose stability
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class ListUiState(
    val uncheckedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val checkedItems: ImmutableList<CurrentListItem> = persistentListOf(),
    val suggestions: ImmutableList<Suggestion> = persistentListOf(),
)
```

### Use derivedStateOf for Expensive Computations

```kotlin
@Composable
fun GroceryList(
    items: List<CurrentListItem>,
) {
    // Derived state prevents recomputation
    val sortedItems by remember(items) {
        derivedStateOf {
            items.sortedBy { it.position }
        }
    }

    LazyColumn {
        items(
            items = sortedItems,
            key = { it.id } // Stable keys
        ) { item ->
            GroceryItemCard(item = item)
        }
    }
}
```

### Use remember for Stable References

```kotlin
@Composable
fun SuggestionsSection(
    onAccept: (Suggestion) -> Unit,
    onDismiss: (String) -> Unit,
) {
    // Stabilize lambda references
    val stableOnAccept = remember(onAccept) { onAccept }
    val stableOnDismiss = remember(onDismiss) { onDismiss }

    // Now child composables won't recompose unnecessarily
}
```

### Composition Tracing

```kotlin
// Add to debug builds for recomposition tracking
@Composable
fun GroceryItemCard(
    item: CurrentListItem,
) {
    SideEffect {
        Timber.d("GroceryItemCard recomposed: ${item.id}")
    }

    // ... rest of composable
}
```

## T-11.5: Implement Lazy Loading Improvements (2h)

### Prefetch Configuration

```kotlin
@Composable
fun GroceryList(
    items: List<CurrentListItem>,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        // Prefetch items beyond visible area
        userScrollEnabled = true,
    ) {
        items(
            items = items,
            key = { it.id },
            contentType = { "grocery_item" } // Helps with item type pooling
        ) { item ->
            GroceryItemCard(
                item = item,
                modifier = Modifier.animateItem()
            )
        }
    }
}
```

### Content Type for Recycling

```kotlin
LazyColumn {
    // Different content types for different item types
    items(
        items = uncheckedItems,
        key = { it.id },
        contentType = { "unchecked_item" }
    ) { /* ... */ }

    item(
        key = "in_cart_header",
        contentType = "header"
    ) {
        InCartHeader(count = checkedItems.size)
    }

    items(
        items = checkedItems,
        key = { it.id },
        contentType = { "checked_item" }
    ) { /* ... */ }
}
```

## T-11.6: Memory Profiling and Optimization (3h)

### Memory Leak Detection

```kotlin
// Add LeakCanary for debug builds
// build.gradle.kts
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
```

### Image/Resource Optimization

```kotlin
// Use Coil for efficient image loading
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build(),
    contentDescription = null,
)
```

### Clear Caches on Low Memory

```kotlin
class RestockApplication : Application() {

    override fun onLowMemory() {
        super.onLowMemory()
        // Clear non-essential caches
        imageLoader.memoryCache?.clear()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                // App went to background
            }
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // System is low on memory
                imageLoader.memoryCache?.clear()
            }
        }
    }
}
```

## Performance Benchmarks

```kotlin
@RunWith(AndroidJUnit4::class)
class PerformanceBenchmarkTest {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.restock",
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
        ) {
            startActivityAndWait()
        }
    }

    @Test
    fun scrollBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.restock",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
        ) {
            startActivityAndWait()

            device.findObject(By.res("grocery_list"))?.let { list ->
                list.setGestureMargin(device.displayWidth / 5)
                list.scroll(Direction.DOWN, 2.0f)
            }
        }
    }
}
```

## Acceptance Criteria

- [ ] Baseline Profiles generated and integrated
- [ ] Cold start < 2 seconds verified
- [ ] Database queries optimized with indices
- [ ] Recompositions minimized
- [ ] LazyColumn properly configured
- [ ] Memory usage < 150MB verified
- [ ] No memory leaks detected

## Definition of Done

- [ ] Performance benchmarks pass targets
- [ ] Profiler shows no major bottlenecks
- [ ] LeakCanary reports no leaks
- [ ] 60fps scroll verified on test devices
- [ ] Documentation updated

## Notes

- Run benchmarks on release builds
- Test on low-end devices too
- Keep baseline profiles updated with app changes
- Memory optimization is ongoing
