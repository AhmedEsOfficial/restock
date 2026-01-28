package com.restock.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.Suggestion
import com.restock.core.domain.repository.SuggestionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [GetSuggestionsUseCase].
 */
class GetSuggestionsUseCaseTest {
    private val suggestionRepository: SuggestionRepository = mockk()
    private lateinit var useCase: GetSuggestionsUseCase

    @BeforeEach
    fun setup() {
        useCase =
            GetSuggestionsUseCase(
                suggestionRepository = suggestionRepository,
            )
    }

    @Nested
    @DisplayName("Observing suggestions (reactive)")
    inner class ObservingSuggestionsTests {
        @Test
        fun `returns flow from repository`() =
            runTest {
                val suggestions = listOf(createTestSuggestion())
                every { suggestionRepository.observeSuggestions() } returns flowOf(suggestions)

                useCase().test {
                    val result = awaitItem()
                    assertThat(result).isEqualTo(suggestions)
                    awaitComplete()
                }

                verify { suggestionRepository.observeSuggestions() }
            }

        @Test
        fun `emits multiple updates from repository`() =
            runTest {
                val suggestions1 = listOf(createTestSuggestion(itemId = "item-1"))
                val suggestions2 =
                    listOf(
                        createTestSuggestion(itemId = "item-1"),
                        createTestSuggestion(itemId = "item-2"),
                    )
                every { suggestionRepository.observeSuggestions() } returns
                    flowOf(
                        suggestions1,
                        suggestions2,
                    )

                useCase().test {
                    assertThat(awaitItem()).hasSize(1)
                    assertThat(awaitItem()).hasSize(2)
                    awaitComplete()
                }
            }

        @Test
        fun `returns empty list when no suggestions available`() =
            runTest {
                every { suggestionRepository.observeSuggestions() } returns flowOf(emptyList())

                useCase().test {
                    val result = awaitItem()
                    assertThat(result).isEmpty()
                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Getting suggestions once (non-reactive)")
    inner class GetOnceSuggestionsTests {
        @Test
        fun `returns suggestions from repository`() =
            runTest {
                val suggestions = listOf(createTestSuggestion())
                coEvery { suggestionRepository.getSuggestions(any()) } returns suggestions

                val result = useCase.getOnce()

                assertThat(result).isEqualTo(suggestions)
            }

        @Test
        fun `uses default limit when not specified`() =
            runTest {
                coEvery { suggestionRepository.getSuggestions(any()) } returns emptyList()

                useCase.getOnce()

                coVerify { suggestionRepository.getSuggestions(Suggestion.MAX_SUGGESTIONS) }
            }

        @Test
        fun `passes custom limit to repository`() =
            runTest {
                coEvery { suggestionRepository.getSuggestions(any()) } returns emptyList()

                useCase.getOnce(limit = 3)

                coVerify { suggestionRepository.getSuggestions(3) }
            }

        @Test
        fun `returns empty list when no suggestions available`() =
            runTest {
                coEvery { suggestionRepository.getSuggestions(any()) } returns emptyList()

                val result = useCase.getOnce()

                assertThat(result).isEmpty()
            }

        @Test
        fun `returns multiple suggestions`() =
            runTest {
                val suggestions =
                    listOf(
                        createTestSuggestion(itemId = "item-1"),
                        createTestSuggestion(itemId = "item-2"),
                        createTestSuggestion(itemId = "item-3"),
                    )
                coEvery { suggestionRepository.getSuggestions(any()) } returns suggestions

                val result = useCase.getOnce()

                assertThat(result).hasSize(3)
            }
    }

    // Helper function

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
}
