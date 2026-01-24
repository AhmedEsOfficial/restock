package com.restock.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [GetCurrentListUseCase].
 */
class GetCurrentListUseCaseTest {
    private val currentListRepository: CurrentListRepository = mockk()
    private lateinit var useCase: GetCurrentListUseCase

    @BeforeEach
    fun setup() {
        useCase = GetCurrentListUseCase(currentListRepository)
    }

    @Nested
    @DisplayName("Observing full list")
    inner class ObserveFullListTests {
        @Test
        fun `invoke returns flow from repository`() =
            runTest {
                val items =
                    listOf(
                        createTestListItem(id = "1", name = "Milk"),
                        createTestListItem(id = "2", name = "Eggs"),
                    )
                every { currentListRepository.observeCurrentList() } returns flowOf(items)

                useCase().test {
                    val result = awaitItem()
                    assertThat(result).hasSize(2)
                    assertThat(result[0].name).isEqualTo("Milk")
                    assertThat(result[1].name).isEqualTo("Eggs")
                    awaitComplete()
                }

                verify { currentListRepository.observeCurrentList() }
            }

        @Test
        fun `returns empty list when no items`() =
            runTest {
                every { currentListRepository.observeCurrentList() } returns flowOf(emptyList())

                useCase().test {
                    val result = awaitItem()
                    assertThat(result).isEmpty()
                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Observing unchecked items")
    inner class ObserveUncheckedTests {
        @Test
        fun `observeUnchecked returns flow from repository`() =
            runTest {
                val items =
                    listOf(
                        createTestListItem(id = "1", name = "Milk", isChecked = false),
                        createTestListItem(id = "2", name = "Eggs", isChecked = false),
                    )
                every { currentListRepository.observeUncheckedItems() } returns flowOf(items)

                useCase.observeUnchecked().test {
                    val result = awaitItem()
                    assertThat(result).hasSize(2)
                    assertThat(result.all { !it.isChecked }).isTrue()
                    awaitComplete()
                }

                verify { currentListRepository.observeUncheckedItems() }
            }
    }

    @Nested
    @DisplayName("Observing checked items")
    inner class ObserveCheckedTests {
        @Test
        fun `observeChecked returns flow from repository`() =
            runTest {
                val items =
                    listOf(
                        createTestListItem(id = "1", name = "Milk", isChecked = true, checkedAt = Instant.now()),
                        createTestListItem(id = "2", name = "Eggs", isChecked = true, checkedAt = Instant.now()),
                    )
                every { currentListRepository.observeCheckedItems() } returns flowOf(items)

                useCase.observeChecked().test {
                    val result = awaitItem()
                    assertThat(result).hasSize(2)
                    assertThat(result.all { it.isChecked }).isTrue()
                    awaitComplete()
                }

                verify { currentListRepository.observeCheckedItems() }
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
