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
 * Unit tests for [UpdateQuantityUseCase].
 */
class UpdateQuantityUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: UpdateQuantityUseCase

    @BeforeEach
    fun setup() {
        useCase = UpdateQuantityUseCase(currentListRepository)
    }

    @Nested
    @DisplayName("Quantity validation")
    inner class QuantityValidationTests {
        @Test
        fun `returns failure when quantity is 0`() =
            runTest {
                val result = useCase(id = "item-1", quantity = 0)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IllegalArgumentException::class.java)
                assertThat(result.exceptionOrNull()?.message)
                    .contains("Quantity must be at least 1")
            }

        @Test
        fun `returns failure when quantity is negative`() =
            runTest {
                val result = useCase(id = "item-1", quantity = -5)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IllegalArgumentException::class.java)
            }

        @Test
        fun `accepts quantity of 1`() =
            runTest {
                coEvery { currentListRepository.updateQuantity("item-1", 1, null) } returns
                    Result.success(Unit)

                val result = useCase(id = "item-1", quantity = 1)

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListRepository.updateQuantity("item-1", 1, null) }
            }

        @Test
        fun `accepts large quantities`() =
            runTest {
                coEvery { currentListRepository.updateQuantity("item-1", 100, null) } returns
                    Result.success(Unit)

                val result = useCase(id = "item-1", quantity = 100)

                assertThat(result.isSuccess).isTrue()
            }
    }

    @Nested
    @DisplayName("Update operations")
    inner class UpdateOperationsTests {
        @Test
        fun `updates quantity without unit`() =
            runTest {
                coEvery { currentListRepository.updateQuantity("item-1", 5, null) } returns
                    Result.success(Unit)

                val result = useCase(id = "item-1", quantity = 5)

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListRepository.updateQuantity("item-1", 5, null) }
            }

        @Test
        fun `updates quantity with unit`() =
            runTest {
                coEvery { currentListRepository.updateQuantity("item-1", 2, "lb") } returns
                    Result.success(Unit)

                val result = useCase(id = "item-1", quantity = 2, unit = "lb")

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListRepository.updateQuantity("item-1", 2, "lb") }
            }

        @Test
        fun `passes null unit to clear existing unit`() =
            runTest {
                coEvery { currentListRepository.updateQuantity("item-1", 3, null) } returns
                    Result.success(Unit)

                val result = useCase(id = "item-1", quantity = 3, unit = null)

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListRepository.updateQuantity("item-1", 3, null) }
            }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {
        @Test
        fun `returns failure when repository fails`() =
            runTest {
                coEvery { currentListRepository.updateQuantity(any(), any(), any()) } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase(id = "item-1", quantity = 5)

                assertThat(result.isFailure).isTrue()
            }
    }
}
