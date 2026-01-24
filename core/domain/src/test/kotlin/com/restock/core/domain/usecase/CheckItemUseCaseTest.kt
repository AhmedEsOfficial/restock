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
 * Unit tests for [CheckItemUseCase].
 */
class CheckItemUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: CheckItemUseCase

    @BeforeEach
    fun setup() {
        useCase = CheckItemUseCase(currentListRepository)
    }

    @Nested
    @DisplayName("Checking items")
    inner class CheckItemTests {
        @Test
        fun `calls checkItem when isChecked is true`() =
            runTest {
                coEvery { currentListRepository.checkItem("item-1") } returns Result.success(Unit)

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListRepository.checkItem("item-1") }
                coVerify(exactly = 0) { currentListRepository.uncheckItem(any()) }
            }

        @Test
        fun `returns success when check succeeds`() =
            runTest {
                coEvery { currentListRepository.checkItem("item-1") } returns Result.success(Unit)

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when check fails`() =
            runTest {
                coEvery { currentListRepository.checkItem("item-1") } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("Unchecking items")
    inner class UncheckItemTests {
        @Test
        fun `calls uncheckItem when isChecked is false`() =
            runTest {
                coEvery { currentListRepository.uncheckItem("item-1") } returns Result.success(Unit)

                val result = useCase("item-1", isChecked = false)

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListRepository.uncheckItem("item-1") }
                coVerify(exactly = 0) { currentListRepository.checkItem(any()) }
            }

        @Test
        fun `returns success when uncheck succeeds`() =
            runTest {
                coEvery { currentListRepository.uncheckItem("item-1") } returns Result.success(Unit)

                val result = useCase("item-1", isChecked = false)

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when uncheck fails`() =
            runTest {
                coEvery { currentListRepository.uncheckItem("item-1") } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase("item-1", isChecked = false)

                assertThat(result.isFailure).isTrue()
            }
    }
}
