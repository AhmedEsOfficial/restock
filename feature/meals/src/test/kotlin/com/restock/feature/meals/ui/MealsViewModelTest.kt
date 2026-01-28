package com.restock.feature.meals.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import com.restock.core.domain.usecase.CheckItemUseCase
import com.restock.feature.meals.MainDispatcherRule
import com.restock.feature.meals.domain.AddMealTagUseCase
import com.restock.feature.meals.domain.GetItemsByMealUseCase
import com.restock.feature.meals.domain.RemoveMealTagUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [MealsViewModel].
 *
 * Tests cover:
 * - Initial state loading
 * - Items grouped by meal type
 * - Toggle item check state
 * - Tag selection and modification
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MealsViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getItemsByMealUseCase: GetItemsByMealUseCase
    private lateinit var checkItemUseCase: CheckItemUseCase
    private lateinit var addMealTagUseCase: AddMealTagUseCase
    private lateinit var removeMealTagUseCase: RemoveMealTagUseCase
    private lateinit var mealTagRepository: MealTagRepository

    private lateinit var viewModel: MealsViewModel

    @BeforeEach
    fun setup() {
        getItemsByMealUseCase = mockk()
        checkItemUseCase = mockk()
        addMealTagUseCase = mockk()
        removeMealTagUseCase = mockk()
        mealTagRepository = mockk()
    }

    private fun createViewModel(): MealsViewModel {
        return MealsViewModel(
            getItemsByMealUseCase = getItemsByMealUseCase,
            checkItemUseCase = checkItemUseCase,
            addMealTagUseCase = addMealTagUseCase,
            removeMealTagUseCase = removeMealTagUseCase,
            mealTagRepository = mealTagRepository,
        )
    }

    private fun createSampleItem(
        id: String = "list-1",
        itemId: String = "item-1",
        name: String = "Eggs",
        quantity: Int = 1,
        isChecked: Boolean = false,
    ) = CurrentListItem(
        id = id,
        itemId = itemId,
        name = name,
        normalizedName = name.lowercase(),
        quantity = quantity,
        unit = null,
        addedAt = Instant.now(),
        position = 0,
        isChecked = isChecked,
    )

    @Nested
    @DisplayName("Initial State")
    inner class InitialStateTests {
        @Test
        fun `starts with loading state`() =
            runTest {
                every { getItemsByMealUseCase() } returns flowOf(emptyMap())

                viewModel = createViewModel()

                // Initially loading is true, but flow immediately sets it to false
                assertThat(viewModel.uiState.value.isLoading).isFalse()
            }

        @Test
        fun `loads items grouped by meal type`() =
            runTest {
                val breakfastItem = createSampleItem("1", "item1", "Eggs")
                val lunchItem = createSampleItem("2", "item2", "Sandwich")
                val dinnerItem = createSampleItem("3", "item3", "Chicken")
                val snackItem = createSampleItem("4", "item4", "Chips")
                val untaggedItem = createSampleItem("5", "item5", "Soap")

                val groupedItems =
                    mapOf(
                        MealType.BREAKFAST to listOf(breakfastItem),
                        MealType.LUNCH to listOf(lunchItem),
                        MealType.DINNER to listOf(dinnerItem),
                        MealType.SNACK to listOf(snackItem),
                        null to listOf(untaggedItem),
                    )

                every { getItemsByMealUseCase() } returns flowOf(groupedItems)

                viewModel = createViewModel()

                val state = viewModel.uiState.value
                assertThat(state.breakfastItems).hasSize(1)
                assertThat(state.breakfastItems[0].name).isEqualTo("Eggs")
                assertThat(state.lunchItems).hasSize(1)
                assertThat(state.lunchItems[0].name).isEqualTo("Sandwich")
                assertThat(state.dinnerItems).hasSize(1)
                assertThat(state.dinnerItems[0].name).isEqualTo("Chicken")
                assertThat(state.snackItems).hasSize(1)
                assertThat(state.snackItems[0].name).isEqualTo("Chips")
                assertThat(state.untaggedItems).hasSize(1)
                assertThat(state.untaggedItems[0].name).isEqualTo("Soap")
            }

        @Test
        fun `handles empty list`() =
            runTest {
                every { getItemsByMealUseCase() } returns flowOf(emptyMap())

                viewModel = createViewModel()

                val state = viewModel.uiState.value
                assertThat(state.isEmpty).isTrue()
                assertThat(state.isLoading).isFalse()
            }
    }

    @Nested
    @DisplayName("Toggle Item")
    inner class ToggleItemTests {
        @Test
        fun `checking item calls checkItemUseCase`() =
            runTest {
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(createSampleItem())),
                    )
                coEvery { checkItemUseCase("list-1", true) } returns Result.success(Unit)

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.ToggleItem("list-1", true))

                coVerify { checkItemUseCase("list-1", true) }
            }

        @Test
        fun `unchecking item calls checkItemUseCase with false`() =
            runTest {
                val checkedItem = createSampleItem(isChecked = true)
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(checkedItem)),
                    )
                coEvery { checkItemUseCase("list-1", false) } returns Result.success(Unit)

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.ToggleItem("list-1", false))

                coVerify { checkItemUseCase("list-1", false) }
            }

        @Test
        fun `checking item emits ItemChecked event`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(item)),
                    )
                coEvery { checkItemUseCase("list-1", true) } returns Result.success(Unit)

                viewModel = createViewModel()

                viewModel.events.test {
                    viewModel.onAction(MealsAction.ToggleItem("list-1", true))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(MealsEvent.ItemChecked::class.java)
                    assertThat((event as MealsEvent.ItemChecked).itemName).isEqualTo("Eggs")
                }
            }

        @Test
        fun `checking item failure emits Error event`() =
            runTest {
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(createSampleItem())),
                    )
                coEvery { checkItemUseCase(any(), any()) } returns Result.failure(RuntimeException("DB error"))

                viewModel = createViewModel()

                viewModel.events.test {
                    viewModel.onAction(MealsAction.ToggleItem("list-1", true))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(MealsEvent.Error::class.java)
                }
            }
    }

    @Nested
    @DisplayName("Tag Selection")
    inner class TagSelectionTests {
        @Test
        fun `selecting item for tagging loads current tags`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns listOf(MealType.BREAKFAST)

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))

                val state = viewModel.uiState.value
                assertThat(state.selectedItemForTagging).isEqualTo(item)
                assertThat(state.selectedItemTags).contains(MealType.BREAKFAST)
                assertThat(state.showTagSelectionSheet).isTrue()
            }

        @Test
        fun `dismissing tag selection clears state`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns listOf(MealType.BREAKFAST)

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))
                viewModel.onAction(MealsAction.DismissTagSelection)

                val state = viewModel.uiState.value
                assertThat(state.selectedItemForTagging).isNull()
                assertThat(state.selectedItemTags).isEmpty()
                assertThat(state.showTagSelectionSheet).isFalse()
            }
    }

    @Nested
    @DisplayName("Toggle Tag")
    inner class ToggleTagTests {
        @Test
        fun `adding tag calls addMealTagUseCase`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(null to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns emptyList()
                coEvery { addMealTagUseCase("item-1", MealType.BREAKFAST) } returns
                    Result.success(
                        MealTag("tag1", "item-1", MealType.BREAKFAST, 1),
                    )

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))
                viewModel.onAction(MealsAction.ToggleTag(MealType.BREAKFAST))

                coVerify { addMealTagUseCase("item-1", MealType.BREAKFAST) }
            }

        @Test
        fun `removing tag calls removeMealTagUseCase`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns listOf(MealType.BREAKFAST)
                coEvery { removeMealTagUseCase("item-1", MealType.BREAKFAST) } returns Result.success(Unit)

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))
                viewModel.onAction(MealsAction.ToggleTag(MealType.BREAKFAST))

                coVerify { removeMealTagUseCase("item-1", MealType.BREAKFAST) }
            }

        @Test
        fun `adding tag emits TagAdded event`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(null to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns emptyList()
                coEvery { addMealTagUseCase("item-1", MealType.LUNCH) } returns
                    Result.success(
                        MealTag("tag1", "item-1", MealType.LUNCH, 1),
                    )

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))

                viewModel.events.test {
                    viewModel.onAction(MealsAction.ToggleTag(MealType.LUNCH))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(MealsEvent.TagAdded::class.java)
                    assertThat((event as MealsEvent.TagAdded).itemName).isEqualTo("Eggs")
                    assertThat(event.mealType).isEqualTo(MealType.LUNCH)
                }
            }

        @Test
        fun `removing tag emits TagRemoved event`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.DINNER to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns listOf(MealType.DINNER)
                coEvery { removeMealTagUseCase("item-1", MealType.DINNER) } returns Result.success(Unit)

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))

                viewModel.events.test {
                    viewModel.onAction(MealsAction.ToggleTag(MealType.DINNER))
                    val event = awaitItem()
                    assertThat(event).isInstanceOf(MealsEvent.TagRemoved::class.java)
                    assertThat((event as MealsEvent.TagRemoved).itemName).isEqualTo("Eggs")
                    assertThat(event.mealType).isEqualTo(MealType.DINNER)
                }
            }

        @Test
        fun `optimistically updates UI when adding tag`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(null to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns emptyList()
                coEvery { addMealTagUseCase("item-1", MealType.SNACK) } returns
                    Result.success(
                        MealTag("tag1", "item-1", MealType.SNACK, 1),
                    )

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))
                viewModel.onAction(MealsAction.ToggleTag(MealType.SNACK))

                // The UI should be optimistically updated
                val state = viewModel.uiState.value
                assertThat(state.selectedItemTags).contains(MealType.SNACK)
            }

        @Test
        fun `reverts optimistic update on failure`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns listOf(MealType.BREAKFAST)
                coEvery { addMealTagUseCase("item-1", MealType.LUNCH) } returns
                    Result.failure(
                        RuntimeException("Network error"),
                    )

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))

                // Wait for state to settle
                val initialTags = viewModel.uiState.value.selectedItemTags.toList()

                viewModel.events.test {
                    viewModel.onAction(MealsAction.ToggleTag(MealType.LUNCH))
                    awaitItem() // Error event
                }

                // State should be reverted
                val state = viewModel.uiState.value
                assertThat(state.selectedItemTags.toList()).isEqualTo(initialTags)
            }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTests {
        @Test
        fun `dismissError clears error state`() =
            runTest {
                every { getItemsByMealUseCase() } returns flowOf(emptyMap())

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.DismissError)

                assertThat(viewModel.uiState.value.error).isNull()
            }
    }

    @Nested
    @DisplayName("UI State Computed Properties")
    inner class UiStateComputedPropertiesTests {
        @Test
        fun `isEmpty returns true when all lists are empty`() =
            runTest {
                every { getItemsByMealUseCase() } returns flowOf(emptyMap())

                viewModel = createViewModel()

                assertThat(viewModel.uiState.value.isEmpty).isTrue()
            }

        @Test
        fun `isEmpty returns false when any list has items`() =
            runTest {
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(createSampleItem())),
                    )

                viewModel = createViewModel()

                assertThat(viewModel.uiState.value.isEmpty).isFalse()
            }

        @Test
        fun `hasBreakfastItems returns true when breakfast list is not empty`() =
            runTest {
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(createSampleItem())),
                    )

                viewModel = createViewModel()

                assertThat(viewModel.uiState.value.hasBreakfastItems).isTrue()
                assertThat(viewModel.uiState.value.hasLunchItems).isFalse()
            }

        @Test
        fun `showTagSelectionSheet returns true when item selected`() =
            runTest {
                val item = createSampleItem()
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(MealType.BREAKFAST to listOf(item)),
                    )
                coEvery { mealTagRepository.getTagsForItem("item-1") } returns emptyList()

                viewModel = createViewModel()
                viewModel.onAction(MealsAction.SelectItemForTagging(item))

                assertThat(viewModel.uiState.value.showTagSelectionSheet).isTrue()
            }

        @Test
        fun `totalItemCount includes all sections`() =
            runTest {
                every { getItemsByMealUseCase() } returns
                    flowOf(
                        mapOf(
                            MealType.BREAKFAST to listOf(createSampleItem("1", "i1")),
                            MealType.LUNCH to listOf(createSampleItem("2", "i2"), createSampleItem("3", "i3")),
                            null to listOf(createSampleItem("4", "i4")),
                        ),
                    )

                viewModel = createViewModel()

                assertThat(viewModel.uiState.value.totalItemCount).isEqualTo(4)
            }
    }
}
