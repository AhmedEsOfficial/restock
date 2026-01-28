package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.repository.SuggestionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [GetShoppingTripCountUseCase].
 */
class GetShoppingTripCountUseCaseTest {
    private val suggestionRepository: SuggestionRepository = mockk()
    private lateinit var useCase: GetShoppingTripCountUseCase

    @BeforeEach
    fun setup() {
        useCase =
            GetShoppingTripCountUseCase(
                suggestionRepository = suggestionRepository,
            )
    }

    @Nested
    @DisplayName("Getting shopping trip count")
    inner class GettingTripCountTests {
        @Test
        fun `returns count from repository`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } returns 5

                val result = useCase()

                assertThat(result).isEqualTo(5)
            }

        @Test
        fun `returns zero when no trips`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } returns 0

                val result = useCase()

                assertThat(result).isEqualTo(0)
            }

        @Test
        fun `returns one for single trip`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } returns 1

                val result = useCase()

                assertThat(result).isEqualTo(1)
            }

        @Test
        fun `returns two for two trips`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } returns 2

                val result = useCase()

                assertThat(result).isEqualTo(2)
            }

        @Test
        fun `returns three or more for ready state`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } returns 3

                val result = useCase()

                assertThat(result).isEqualTo(3)
            }

        @Test
        fun `handles large trip counts`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } returns 100

                val result = useCase()

                assertThat(result).isEqualTo(100)
            }

        @Test
        fun `calls repository getShoppingTripCount`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } returns 0

                useCase()

                coVerify { suggestionRepository.getShoppingTripCount() }
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `propagates repository exceptions`() =
            runTest {
                coEvery { suggestionRepository.getShoppingTripCount() } throws RuntimeException("DB error")

                try {
                    useCase()
                    // Should not reach here
                    assert(false) { "Expected exception to be thrown" }
                } catch (e: RuntimeException) {
                    assertThat(e.message).isEqualTo("DB error")
                }
            }
    }
}
