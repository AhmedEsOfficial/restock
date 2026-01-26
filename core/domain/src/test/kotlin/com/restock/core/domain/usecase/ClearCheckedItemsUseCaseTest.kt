package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.repository.CurrentListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [ClearCheckedItemsUseCase].
 */
class ClearCheckedItemsUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: ClearCheckedItemsUseCase

    @BeforeEach
    fun setup() {
        useCase = ClearCheckedItemsUseCase(currentListRepository)
    }

    @Nested
    @DisplayName("Clearing checked items")
    inner class ClearCheckedItemsTests {
        @Test
        fun `calls clearCheckedItems on repository`() =
            runTest {
                coEvery { currentListRepository.clearCheckedItems() } returns Result.success(Unit)

                useCase()

                coVerify { currentListRepository.clearCheckedItems() }
            }

        @Test
        fun `returns success when clear succeeds`() =
            runTest {
                coEvery { currentListRepository.clearCheckedItems() } returns Result.success(Unit)

                val result = useCase()

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when clear fails`() =
            runTest {
                coEvery { currentListRepository.clearCheckedItems() } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase()

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `propagates exception message on failure`() =
            runTest {
                val expectedMessage = "Failed to clear checked items"
                coEvery { currentListRepository.clearCheckedItems() } returns
                    Result.failure(RuntimeException(expectedMessage))

                val result = useCase()

                assertThat(result.exceptionOrNull()?.message).isEqualTo(expectedMessage)
            }
    }
}
