package com.restock.core.domain.usecase

import com.restock.core.domain.repository.SuggestionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [DismissSuggestionUseCase].
 */
class DismissSuggestionUseCaseTest {
    private val suggestionRepository: SuggestionRepository = mockk()
    private lateinit var useCase: DismissSuggestionUseCase

    @BeforeEach
    fun setup() {
        useCase =
            DismissSuggestionUseCase(
                suggestionRepository = suggestionRepository,
            )
    }

    @Nested
    @DisplayName("Dismissing suggestions")
    inner class DismissingSuggestionsTests {
        @Test
        fun `calls dismissSuggestion on repository`() =
            runTest {
                coEvery { suggestionRepository.dismissSuggestion(any()) } just runs

                useCase("item-123")

                coVerify { suggestionRepository.dismissSuggestion("item-123") }
            }

        @Test
        fun `passes correct item ID to repository`() =
            runTest {
                coEvery { suggestionRepository.dismissSuggestion(any()) } just runs

                useCase("unique-item-id-xyz")

                coVerify { suggestionRepository.dismissSuggestion("unique-item-id-xyz") }
            }

        @Test
        fun `handles multiple dismissals for same item`() =
            runTest {
                coEvery { suggestionRepository.dismissSuggestion(any()) } just runs

                useCase("item-123")
                useCase("item-123")
                useCase("item-123")

                coVerify(exactly = 3) { suggestionRepository.dismissSuggestion("item-123") }
            }

        @Test
        fun `handles dismissals for different items`() =
            runTest {
                coEvery { suggestionRepository.dismissSuggestion(any()) } just runs

                useCase("item-1")
                useCase("item-2")
                useCase("item-3")

                coVerify { suggestionRepository.dismissSuggestion("item-1") }
                coVerify { suggestionRepository.dismissSuggestion("item-2") }
                coVerify { suggestionRepository.dismissSuggestion("item-3") }
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `propagates repository exceptions`() =
            runTest {
                coEvery { suggestionRepository.dismissSuggestion(any()) } throws RuntimeException("DB error")

                try {
                    useCase("item-123")
                    // If we reach here, the test should fail
                    assert(false) { "Expected exception to be thrown" }
                } catch (e: RuntimeException) {
                    assert(e.message == "DB error")
                }
            }
    }
}
