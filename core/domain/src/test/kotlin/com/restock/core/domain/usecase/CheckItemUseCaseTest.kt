package com.restock.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [CheckItemUseCase].
 */
class CheckItemUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private val recordPurchaseUseCase: RecordPurchaseUseCase = mockk(relaxed = true)
    private lateinit var useCase: CheckItemUseCase

    @BeforeEach
    fun setup() {
        useCase = CheckItemUseCase(currentListRepository, recordPurchaseUseCase)
    }

    @Nested
    @DisplayName("Checking items")
    inner class CheckItemTests {
        @Test
        fun `calls checkItem when isChecked is true`() =
            runTest {
                val listItem = createTestListItem(id = "item-1", itemId = "grocery-1", quantity = 2)
                coEvery { currentListRepository.getById("item-1") } returns listItem
                coEvery { currentListRepository.checkItem("item-1") } returns Result.success(Unit)

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isSuccess).isTrue()
                coVerify { currentListRepository.checkItem("item-1") }
                coVerify(exactly = 0) { currentListRepository.uncheckItem(any()) }
            }

        @Test
        fun `records purchase when check succeeds`() =
            runTest {
                val listItem = createTestListItem(id = "item-1", itemId = "grocery-1", quantity = 3)
                coEvery { currentListRepository.getById("item-1") } returns listItem
                coEvery { currentListRepository.checkItem("item-1") } returns Result.success(Unit)

                useCase("item-1", isChecked = true)

                verify { recordPurchaseUseCase(itemId = "grocery-1", quantity = 3) }
            }

        @Test
        fun `does not record purchase when item not found`() =
            runTest {
                coEvery { currentListRepository.getById("item-1") } returns null

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isFailure).isTrue()
                verify(exactly = 0) { recordPurchaseUseCase(any(), any()) }
            }

        @Test
        fun `does not record purchase when check fails`() =
            runTest {
                val listItem = createTestListItem(id = "item-1", itemId = "grocery-1", quantity = 1)
                coEvery { currentListRepository.getById("item-1") } returns listItem
                coEvery { currentListRepository.checkItem("item-1") } returns
                    Result.failure(RuntimeException("DB error"))

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isFailure).isTrue()
                verify(exactly = 0) { recordPurchaseUseCase(any(), any()) }
            }

        @Test
        fun `returns success when check succeeds`() =
            runTest {
                val listItem = createTestListItem(id = "item-1", itemId = "grocery-1")
                coEvery { currentListRepository.getById("item-1") } returns listItem
                coEvery { currentListRepository.checkItem("item-1") } returns Result.success(Unit)

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when item not found`() =
            runTest {
                coEvery { currentListRepository.getById("item-1") } returns null

                val result = useCase("item-1", isChecked = true)

                assertThat(result.isFailure).isTrue()
                assertThat(result.exceptionOrNull())
                    .isInstanceOf(IllegalArgumentException::class.java)
                assertThat(result.exceptionOrNull()?.message)
                    .contains("Item not found")
            }

        @Test
        fun `returns failure when check fails`() =
            runTest {
                val listItem = createTestListItem(id = "item-1", itemId = "grocery-1")
                coEvery { currentListRepository.getById("item-1") } returns listItem
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
        fun `does not record purchase when unchecking`() =
            runTest {
                coEvery { currentListRepository.uncheckItem("item-1") } returns Result.success(Unit)

                useCase("item-1", isChecked = false)

                verify(exactly = 0) { recordPurchaseUseCase(any(), any()) }
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

    // Helper function
    private fun createTestListItem(
        id: String = "list-id",
        itemId: String = "item-id",
        name: String = "Test Item",
        normalizedName: String = name.lowercase(),
        quantity: Int = 1,
        unit: String? = null,
        addedAt: Instant = Instant.now(),
        checkedAt: Instant? = null,
        position: Int = 0,
        isChecked: Boolean = false,
    ) = CurrentListItem(
        id = id,
        itemId = itemId,
        name = name,
        normalizedName = normalizedName,
        quantity = quantity,
        unit = unit,
        addedAt = addedAt,
        checkedAt = checkedAt,
        position = position,
        isChecked = isChecked,
    )
}
