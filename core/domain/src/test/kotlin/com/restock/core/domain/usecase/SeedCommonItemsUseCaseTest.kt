package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [SeedCommonItemsUseCase].
 */
class SeedCommonItemsUseCaseTest {
    private val itemRepository: ItemRepository = mockk()
    private lateinit var useCase: SeedCommonItemsUseCase

    @BeforeEach
    fun setup() {
        useCase = SeedCommonItemsUseCase(itemRepository)
    }

    @Nested
    @DisplayName("Seeding behavior")
    inner class SeedingBehaviorTests {
        @Test
        fun `calls getOrCreate for each common item`() =
            runTest {
                coEvery { itemRepository.getOrCreate(any()) } answers {
                    createTestGroceryItem(name = firstArg())
                }

                useCase()

                SeedCommonItemsUseCase.COMMON_ITEMS.forEach { name ->
                    coVerify { itemRepository.getOrCreate(name) }
                }
            }

        @Test
        fun `seeds all expected common items`() =
            runTest {
                coEvery { itemRepository.getOrCreate(any()) } answers {
                    createTestGroceryItem(name = firstArg())
                }

                useCase()

                coVerify(exactly = SeedCommonItemsUseCase.COMMON_ITEMS.size) {
                    itemRepository.getOrCreate(any())
                }
            }
    }

    @Nested
    @DisplayName("Common items list")
    inner class CommonItemsListTests {
        @Test
        fun `contains essential dairy items`() {
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Milk")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Eggs")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Butter")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Cheese")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Yogurt")
        }

        @Test
        fun `contains common produce`() {
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Tomatoes")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Onions")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Potatoes")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Apples")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Bananas")
        }

        @Test
        fun `contains pantry staples`() {
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Sugar")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Salt")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Olive oil")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Flour")
        }

        @Test
        fun `contains protein items`() {
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Chicken")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Beef")
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS).contains("Fish")
        }

        @Test
        fun `has a reasonable number of items`() {
            // Should be enough for good autocomplete without being overwhelming
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS.size).isAtLeast(30)
            assertThat(SeedCommonItemsUseCase.COMMON_ITEMS.size).isAtMost(100)
        }

        @Test
        fun `has no duplicates`() {
            val uniqueItems = SeedCommonItemsUseCase.COMMON_ITEMS.toSet()
            assertThat(uniqueItems.size).isEqualTo(SeedCommonItemsUseCase.COMMON_ITEMS.size)
        }

        @Test
        fun `all items are properly capitalized`() {
            SeedCommonItemsUseCase.COMMON_ITEMS.forEach { name ->
                assertThat(name.first().isUpperCase()).isTrue()
            }
        }
    }

    // Helper function
    private fun createTestGroceryItem(
        id: String = "test-id",
        name: String = "Test Item",
        normalizedName: String = name.lowercase(),
        createdAt: Instant = Instant.now(),
        isActive: Boolean = true,
    ) = GroceryItem(
        id = id,
        name = name,
        normalizedName = normalizedName,
        createdAt = createdAt,
        isActive = isActive,
    )
}
