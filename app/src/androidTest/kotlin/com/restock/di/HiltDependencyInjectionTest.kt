package com.restock.di

import com.restock.core.data.di.ApplicationScope
import com.restock.core.data.di.DefaultDispatcher
import com.restock.core.data.di.IoDispatcher
import com.restock.core.data.di.MainDispatcher
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.ItemRepository
import com.restock.core.domain.repository.MealTagRepository
import com.restock.core.domain.repository.PatternRepository
import com.restock.core.domain.repository.PurchaseRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * Instrumented test to verify Hilt dependency injection is working correctly.
 *
 * This test validates that all core dependencies can be injected:
 * - All repository interfaces
 * - Coroutine dispatchers
 * - Application scope
 *
 * Run with: ./gradlew :app:connectedAndroidTest
 */
@HiltAndroidTest
class HiltDependencyInjectionTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var itemRepository: ItemRepository

    @Inject
    lateinit var currentListRepository: CurrentListRepository

    @Inject
    lateinit var purchaseRepository: PurchaseRepository

    @Inject
    lateinit var patternRepository: PatternRepository

    @Inject
    lateinit var mealTagRepository: MealTagRepository

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    @DefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun itemRepository_isInjected() {
        assertNotNull("ItemRepository should be injected", itemRepository)
    }

    @Test
    fun currentListRepository_isInjected() {
        assertNotNull("CurrentListRepository should be injected", currentListRepository)
    }

    @Test
    fun purchaseRepository_isInjected() {
        assertNotNull("PurchaseRepository should be injected", purchaseRepository)
    }

    @Test
    fun patternRepository_isInjected() {
        assertNotNull("PatternRepository should be injected", patternRepository)
    }

    @Test
    fun mealTagRepository_isInjected() {
        assertNotNull("MealTagRepository should be injected", mealTagRepository)
    }

    @Test
    fun ioDispatcher_isInjected() {
        assertNotNull("IoDispatcher should be injected", ioDispatcher)
    }

    @Test
    fun defaultDispatcher_isInjected() {
        assertNotNull("DefaultDispatcher should be injected", defaultDispatcher)
    }

    @Test
    fun mainDispatcher_isInjected() {
        assertNotNull("MainDispatcher should be injected", mainDispatcher)
    }

    @Test
    fun applicationScope_isInjected() {
        assertNotNull("ApplicationScope should be injected", applicationScope)
    }
}
