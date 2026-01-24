package com.restock.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.CurrentListRepository
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
 * Unit tests for [SearchItemsUseCase].
 */
class SearchItemsUseCaseTest {
    private val itemRepository: ItemRepository = mockk()
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: SearchItemsUseCase

    @BeforeEach
    fun setup() {
        useCase = SearchItemsUseCase(itemRepository, currentListRepository)
    }

    @Nested
    @DisplayName("Empty query handling")
    inner class EmptyQueryTests {
        @Test
        fun `returns empty list for empty query`() =
            runTest {
                useCase("").test {
                    val result = awaitItem()
                    assertThat(result).isEmpty()
                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Search operations")
    inner class SearchOperationsTests {
        @Test
        fun `returns matching items from repository`() =
            runTest {
                val items =
                    listOf(
                        createTestGroceryItem(id = "1", name = "Milk"),
                        createTestGroceryItem(id = "2", name = "Almond Milk"),
                    )
                coEvery { itemRepository.searchByNamePrefix("milk", 5) } returns items
                coEvery { currentListRepository.getCurrentItemIds() } returns emptyList()

                useCase("milk").test {
                    val result = awaitItem()
                    assertThat(result).hasSize(2)
                    assertThat(result[0].name).isEqualTo("Milk")
                    awaitComplete()
                }

                coVerify { itemRepository.searchByNamePrefix("milk", 5) }
            }

        @Test
        fun `limits results to MAX_SUGGESTIONS`() =
            runTest {
                val items = listOf(createTestGroceryItem())
                coEvery { itemRepository.searchByNamePrefix("m", 5) } returns items
                coEvery { currentListRepository.getCurrentItemIds() } returns emptyList()

                useCase("m").test {
                    awaitItem()
                    awaitComplete()
                }

                coVerify { itemRepository.searchByNamePrefix("m", SearchItemsUseCase.MAX_SUGGESTIONS) }
            }
    }

    @Nested
    @DisplayName("Filtering out items on list")
    inner class FilteringTests {
        @Test
        fun `filters out items already on current list`() =
            runTest {
                val items =
                    listOf(
                        createTestGroceryItem(id = "1", name = "Milk"),
                        createTestGroceryItem(id = "2", name = "Almond Milk"),
                        createTestGroceryItem(id = "3", name = "Oat Milk"),
                    )
                coEvery { itemRepository.searchByNamePrefix("milk", 5) } returns items
                coEvery { currentListRepository.getCurrentItemIds() } returns listOf("1", "3")

                useCase("milk").test {
                    val result = awaitItem()
                    assertThat(result).hasSize(1)
                    assertThat(result[0].id).isEqualTo("2")
                    assertThat(result[0].name).isEqualTo("Almond Milk")
                    awaitComplete()
                }
            }

        @Test
        fun `returns empty when all matches are on list`() =
            runTest {
                val items =
                    listOf(
                        createTestGroceryItem(id = "1", name = "Milk"),
                    )
                coEvery { itemRepository.searchByNamePrefix("milk", 5) } returns items
                coEvery { currentListRepository.getCurrentItemIds() } returns listOf("1")

                useCase("milk").test {
                    val result = awaitItem()
                    assertThat(result).isEmpty()
                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Single character queries")
    inner class SingleCharQueryTests {
        @Test
        fun `works with single character query`() =
            runTest {
                val items = listOf(createTestGroceryItem(id = "1", name = "Milk"))
                coEvery { itemRepository.searchByNamePrefix("m", 5) } returns items
                coEvery { currentListRepository.getCurrentItemIds() } returns emptyList()

                useCase("m").test {
                    val result = awaitItem()
                    assertThat(result).hasSize(1)
                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Constants validation")
    inner class ConstantsTests {
        @Test
        fun `MAX_SUGGESTIONS is 5`() {
            assertThat(SearchItemsUseCase.MAX_SUGGESTIONS).isEqualTo(5)
        }

        @Test
        fun `DEFAULT_DEBOUNCE_MS is 200`() {
            assertThat(SearchItemsUseCase.DEFAULT_DEBOUNCE_MS).isEqualTo(200L)
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
