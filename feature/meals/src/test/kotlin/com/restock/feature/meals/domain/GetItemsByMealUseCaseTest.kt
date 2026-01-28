package com.restock.feature.meals.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.ItemWithMealTags
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.MealTagRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [GetItemsByMealUseCase].
 */
class GetItemsByMealUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private val mealTagRepository: MealTagRepository = mockk()
    private lateinit var useCase: GetItemsByMealUseCase

    @BeforeEach
    fun setup() {
        useCase = GetItemsByMealUseCase(currentListRepository, mealTagRepository)
    }

    @Nested
    @DisplayName("invoke")
    inner class InvokeTests {
        @Test
        fun `groups items by meal type`() =
            runTest {
                val items =
                    listOf(
                        createListItem(itemId = "1", name = "Eggs"),
                        createListItem(itemId = "2", name = "Sandwich"),
                        createListItem(itemId = "3", name = "Steak"),
                    )
                val itemsWithTags =
                    listOf(
                        createItemWithTags("1", "Eggs", listOf(MealType.BREAKFAST)),
                        createItemWithTags("2", "Sandwich", listOf(MealType.LUNCH)),
                        createItemWithTags("3", "Steak", listOf(MealType.DINNER)),
                    )

                every { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                every { mealTagRepository.observeItemsWithTags() } returns flowOf(itemsWithTags)

                useCase().test {
                    val result = awaitItem()

                    assertThat(result[MealType.BREAKFAST]).hasSize(1)
                    assertThat(result[MealType.BREAKFAST]?.first()?.name).isEqualTo("Eggs")
                    assertThat(result[MealType.LUNCH]).hasSize(1)
                    assertThat(result[MealType.DINNER]).hasSize(1)

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `places untagged items under null key`() =
            runTest {
                val items =
                    listOf(
                        createListItem(itemId = "1", name = "Random Item"),
                    )
                val itemsWithTags =
                    listOf(
                        createItemWithTags("1", "Random Item", emptyList()),
                    )

                every { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                every { mealTagRepository.observeItemsWithTags() } returns flowOf(itemsWithTags)

                useCase().test {
                    val result = awaitItem()

                    assertThat(result[null]).hasSize(1)
                    assertThat(result[null]?.first()?.name).isEqualTo("Random Item")

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `items with multiple tags appear in multiple groups`() =
            runTest {
                val items =
                    listOf(
                        createListItem(itemId = "1", name = "Toast"),
                    )
                val itemsWithTags =
                    listOf(
                        createItemWithTags(
                            itemId = "1",
                            name = "Toast",
                            tags = listOf(MealType.BREAKFAST, MealType.SNACK),
                        ),
                    )

                every { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                every { mealTagRepository.observeItemsWithTags() } returns flowOf(itemsWithTags)

                useCase().test {
                    val result = awaitItem()

                    assertThat(result[MealType.BREAKFAST]).hasSize(1)
                    assertThat(result[MealType.SNACK]).hasSize(1)
                    assertThat(result[MealType.BREAKFAST]?.first()?.itemId)
                        .isEqualTo(result[MealType.SNACK]?.first()?.itemId)

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `returns empty map when no items`() =
            runTest {
                every { currentListRepository.observeUncheckedItems() } returns flowOf(emptyList())
                every { mealTagRepository.observeItemsWithTags() } returns flowOf(emptyList())

                useCase().test {
                    val result = awaitItem()

                    assertThat(result).isEmpty()

                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun `filters out items not in unchecked list`() =
            runTest {
                val items =
                    listOf(
                        createListItem(itemId = "1", name = "Eggs"),
                    )
                // Item 2 has tags but is not in unchecked list (maybe checked)
                val itemsWithTags =
                    listOf(
                        createItemWithTags("1", "Eggs", listOf(MealType.BREAKFAST)),
                        createItemWithTags("2", "Milk", listOf(MealType.BREAKFAST)),
                    )

                every { currentListRepository.observeUncheckedItems() } returns flowOf(items)
                every { mealTagRepository.observeItemsWithTags() } returns flowOf(itemsWithTags)

                useCase().test {
                    val result = awaitItem()

                    assertThat(result[MealType.BREAKFAST]).hasSize(1)
                    assertThat(result[MealType.BREAKFAST]?.first()?.name).isEqualTo("Eggs")

                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    // Helper functions
    private fun createListItem(
        id: String = "list-id",
        itemId: String = "item-id",
        name: String = "Test Item",
        position: Int = 0,
    ) = CurrentListItem(
        id = id,
        itemId = itemId,
        name = name,
        normalizedName = name.lowercase(),
        quantity = 1,
        unit = null,
        addedAt = Instant.now(),
        checkedAt = null,
        position = position,
        isChecked = false,
    )

    private fun createItemWithTags(
        itemId: String,
        name: String,
        tags: List<MealType>,
    ) = ItemWithMealTags(
        item = createListItem(itemId = itemId, name = name),
        mealTags = tags,
    )
}
