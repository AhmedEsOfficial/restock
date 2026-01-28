package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
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
 * Unit tests for [AcceptSuggestionUseCase].
 */
class AcceptSuggestionUseCaseTest {
    private val addItemUseCase: AddItemUseCase = mockk()
    private val suggestionRepository: SuggestionRepository = mockk(relaxed = true)
    private lateinit var useCase: AcceptSuggestionUseCase

    @BeforeEach
    fun setup() {
        useCase =
            AcceptSuggestionUseCase(
                addItemUseCase = addItemUseCase,
                suggestionRepository = suggestionRepository,
            )
    }

    @Nested
    @DisplayName("Accepting suggestions")
    inner class AcceptingSuggestionsTests {
        @Test
        fun `adds item to list using item name from suggestion`() =
            runTest {
                val suggestion = createTestSuggestion(itemName = "Milk")
                val listItem = createTestListItem(name = "Milk")
                coEvery { addItemUseCase(any()) } returns Result.success(listItem)

                useCase(suggestion)

                coVerify { addItemUseCase("Milk") }
            }

        @Test
        fun `returns success with CurrentListItem when add succeeds`() =
            runTest {
                val suggestion = createTestSuggestion()
                val listItem = createTestListItem()
                coEvery { addItemUseCase(any()) } returns Result.success(listItem)

                val result = useCase(suggestion)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()).isEqualTo(listItem)
            }

        @Test
        fun `returns failure when add fails`() =
            runTest {
                val suggestion = createTestSuggestion()
                val exception = IllegalArgumentException("Add failed")
                coEvery { addItemUseCase(any()) } returns Result.failure(exception)

                val result = useCase(suggestion)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull()).isEqualTo(exception)
            }
    }

    @Nested
    @DisplayName("Recording acceptance")
    inner class RecordingAcceptanceTests {
        @Test
        fun `records acceptance when add succeeds`() =
            runTest {
                val suggestion = createTestSuggestion(itemId = "item-123")
                val listItem = createTestListItem()
                coEvery { addItemUseCase(any()) } returns Result.success(listItem)

                useCase(suggestion)

                coVerify { suggestionRepository.acceptSuggestion("item-123") }
            }

        @Test
        fun `does not record acceptance when add fails`() =
            runTest {
                val suggestion = createTestSuggestion(itemId = "item-123")
                coEvery { addItemUseCase(any()) } returns Result.failure(RuntimeException())

                useCase(suggestion)

                coVerify(exactly = 0) { suggestionRepository.acceptSuggestion(any()) }
            }
    }

    @Nested
    @DisplayName("Preserving suggestion data")
    inner class PreservingSuggestionDataTests {
        @Test
        fun `uses exact item name from suggestion`() =
            runTest {
                val suggestion = createTestSuggestion(itemName = "Organic Whole Milk")
                val listItem = createTestListItem()
                coEvery { addItemUseCase(any()) } returns Result.success(listItem)

                useCase(suggestion)

                coVerify { addItemUseCase("Organic Whole Milk") }
            }

        @Test
        fun `records acceptance for correct item ID`() =
            runTest {
                val suggestion = createTestSuggestion(itemId = "unique-item-id-xyz")
                val listItem = createTestListItem()
                coEvery { addItemUseCase(any()) } returns Result.success(listItem)

                useCase(suggestion)

                coVerify { suggestionRepository.acceptSuggestion("unique-item-id-xyz") }
            }
    }

    // Helper functions

    private fun createTestSuggestion(
        itemId: String = "item-id",
        itemName: String = "Test Item",
        reason: String = "Usually every 7 days",
        score: Double = 1.5,
        confidence: Double = 0.8,
        daysSinceLastPurchase: Int = 8,
        avgIntervalDays: Int = 7,
    ) = Suggestion(
        itemId = itemId,
        itemName = itemName,
        reason = reason,
        score = score,
        confidence = confidence,
        daysSinceLastPurchase = daysSinceLastPurchase,
        avgIntervalDays = avgIntervalDays,
    )

    private fun createTestListItem(
        id: String = "list-item-id",
        itemId: String = "item-id",
        name: String = "Test Item",
        normalizedName: String = "test item",
        quantity: Int = 1,
        unit: String? = null,
        position: Int = 0,
        isChecked: Boolean = false,
    ) = CurrentListItem(
        id = id,
        itemId = itemId,
        name = name,
        normalizedName = normalizedName,
        quantity = quantity,
        unit = unit,
        addedAt = Instant.now(),
        checkedAt = null,
        position = position,
        isChecked = isChecked,
    )
}
