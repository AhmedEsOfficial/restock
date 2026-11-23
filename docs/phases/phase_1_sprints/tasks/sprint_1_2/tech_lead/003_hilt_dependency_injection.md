# Task: Set Up Hilt Dependency Injection

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 5 hours (T-1.5 + T-1.6 combined)
**Priority:** P0
**Prerequisites:** 001_project_structure_setup.md, 002_gradle_version_catalog.md

---

## Sprint Context

Hilt is the official Android dependency injection solution. Setting it up correctly now enables testable, maintainable code throughout the project. All ViewModels, repositories, and use cases will use Hilt for dependency management.

### Architecture Pattern
Restock uses MVVM + Clean Architecture with Unidirectional Data Flow (UDF). Hilt enables:
- Constructor injection for ViewModels
- Scoped dependencies (Singleton, ViewModelScoped)
- Easy testing with @HiltAndroidTest

---

## Task Description

Configure Hilt across all modules and create DI modules for each layer.

## Implementation Steps

### 1. Root build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

### 2. App Module build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.restock"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.restock"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Other dependencies...
}
```

### 3. RestockApplication.kt

```kotlin
package com.restock

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RestockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-level services here
    }
}
```

### 4. Core Data Module - DataModule.kt

```kotlin
package com.restock.core.data.di

import android.content.Context
import androidx.room.Room
import com.restock.core.data.database.RestockDatabase
import com.restock.core.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RestockDatabase {
        return Room.databaseBuilder(
            context,
            RestockDatabase::class.java,
            "restock.db"
        ).build()
    }

    @Provides
    fun provideItemDao(database: RestockDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideCurrentListDao(database: RestockDatabase): CurrentListDao = database.currentListDao()

    @Provides
    fun providePurchaseDao(database: RestockDatabase): PurchaseDao = database.purchaseDao()

    @Provides
    fun providePatternDao(database: RestockDatabase): PatternDao = database.patternDao()

    @Provides
    fun provideMealTagDao(database: RestockDatabase): MealTagDao = database.mealTagDao()
}
```

### 5. Repository Module - RepositoryModule.kt

```kotlin
package com.restock.core.data.di

import com.restock.core.data.repository.*
import com.restock.core.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    abstract fun bindCurrentListRepository(impl: CurrentListRepositoryImpl): CurrentListRepository

    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(impl: PurchaseRepositoryImpl): PurchaseRepository

    @Binds
    @Singleton
    abstract fun bindPatternRepository(impl: PatternRepositoryImpl): PatternRepository

    @Binds
    @Singleton
    abstract fun bindMealTagRepository(impl: MealTagRepositoryImpl): MealTagRepository
}
```

### 6. Dispatcher Module - DispatcherModule.kt

```kotlin
package com.restock.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
```

### 7. Library Module Setup (core:data/build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.restock.core.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
```

## Acceptance Criteria

- [ ] `@HiltAndroidApp` on Application class
- [ ] Sample ViewModel with injected repository compiles
- [ ] Test can use `@HiltAndroidTest`
- [ ] Module-specific DI modules exist for data layer
- [ ] Repository bindings defined
- [ ] Dispatcher qualifiers available for injection
- [ ] All modules with DI have Hilt plugin applied

## Test Verification

```kotlin
@HiltAndroidTest
class SampleDiTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var itemRepository: ItemRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun repositoryIsInjected() {
        assertThat(itemRepository).isNotNull()
    }
}
```

## Definition of Done

- [ ] Application class annotated with @HiltAndroidApp
- [ ] DataModule provides all DAOs
- [ ] RepositoryModule binds all repository interfaces
- [ ] DispatcherModule provides coroutine dispatchers
- [ ] Sample injection test passes
- [ ] No Hilt compilation errors
- [ ] Documentation updated with DI patterns

## Dependent Tasks

This task blocks:
- 004_room_database_entities.md (repositories need DI)
- 005_base_viewmodel_pattern.md (ViewModels need injection)
- All feature module development

## Notes

- Use @Singleton for database and repositories
- Use @ViewModelScoped for view-model-specific dependencies (when needed)
- Prefer constructor injection over field injection
- Keep modules focused - don't put everything in one module
