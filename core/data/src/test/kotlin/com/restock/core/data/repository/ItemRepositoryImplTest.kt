package com.restock.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.restock.core.data.database.dao.ItemDao
import com.restock.core.data.database.entity.ItemEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [ItemRepositoryImpl].
 *
 * Tests verify correct mapping between domain models and entities,
 * proper delegation to DAOs, and error handling.
 */
class ItemRepositoryImplTest {
    private val itemDao: ItemDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ItemRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = ItemRepositoryImpl(itemDao, testDispatcher)
    }

    @Nested
    @DisplayName("observeActiveItems")
    inner class ObserveActiveItemsTests {
        @Test
        fun `returns flow of mapped domain models`() =
            runTest(testDispatcher) {
                val entities =
                    listOf(
                        createTestEntity(id = "1", name = "Milk"),
                        createTestEntity(id = "2", name = "Eggs"),
                    )
                every { itemDao.observeActiveItems() } returns flowOf(entities)

                val result = repository.observeActiveItems().first()

                assertThat(result).hasSize(2)
                assertThat(result[0].id).isEqualTo("1")
                assertThat(result[0].name).isEqualTo("Milk")
                assertThat(result[1].id).isEqualTo("2")
                assertThat(result[1].name).isEqualTo("Eggs")
            }

        @Test
        fun `returns empty list when no active items`() =
            runTest(testDispatcher) {
                every { itemDao.observeActiveItems() } returns flowOf(emptyList())

                val result = repository.observeActiveItems().first()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    @DisplayName("getById")
    inner class GetByIdTests {
        @Test
        fun `returns mapped domain model when item exists`() =
            runTest(testDispatcher) {
                val entity = createTestEntity(id = "test-id", name = "Milk")
                coEvery { itemDao.getById("test-id") } returns entity

                val result = repository.getById("test-id")

                assertThat(result).isNotNull()
                assertThat(result?.id).isEqualTo("test-id")
                assertThat(result?.name).isEqualTo("Milk")
            }

        @Test
        fun `returns null when item does not exist`() =
            runTest(testDispatcher) {
                coEvery { itemDao.getById("nonexistent") } returns null

                val result = repository.getById("nonexistent")

                assertThat(result).isNull()
            }
    }

    @Nested
    @DisplayName("findByNormalizedName")
    inner class FindByNormalizedNameTests {
        @Test
        fun `returns item when found by normalized name`() =
            runTest(testDispatcher) {
                val entity = createTestEntity(normalizedName = "milk")
                coEvery { itemDao.findByNormalizedName("milk") } returns entity

                val result = repository.findByNormalizedName("milk")

                assertThat(result).isNotNull()
                assertThat(result?.normalizedName).isEqualTo("milk")
            }

        @Test
        fun `returns null when not found`() =
            runTest(testDispatcher) {
                coEvery { itemDao.findByNormalizedName("unknown") } returns null

                val result = repository.findByNormalizedName("unknown")

                assertThat(result).isNull()
            }
    }

    @Nested
    @DisplayName("searchByName")
    inner class SearchByNameTests {
        @Test
        fun `returns matching items limited by count`() =
            runTest(testDispatcher) {
                val entities =
                    listOf(
                        createTestEntity(id = "1", name = "Milk"),
                        createTestEntity(id = "2", name = "Almond Milk"),
                    )
                coEvery { itemDao.searchByName("milk", 10) } returns entities

                val result = repository.searchByName("milk", 10)

                assertThat(result).hasSize(2)
                coVerify { itemDao.searchByName("milk", 10) }
            }
    }

    @Nested
    @DisplayName("getOrCreate")
    inner class GetOrCreateTests {
        @Test
        fun `returns existing item when found by normalized name`() =
            runTest(testDispatcher) {
                val existingEntity = createTestEntity(id = "existing-id", name = "Milk", normalizedName = "milk")
                coEvery { itemDao.findByNormalizedName("milk") } returns existingEntity

                val result = repository.getOrCreate("Milk")

                assertThat(result.id).isEqualTo("existing-id")
                assertThat(result.name).isEqualTo("Milk")
                coVerify(exactly = 0) { itemDao.insert(any()) }
            }

        @Test
        fun `creates new item when not found`() =
            runTest(testDispatcher) {
                coEvery { itemDao.findByNormalizedName("milk") } returns null
                val insertedEntity = slot<ItemEntity>()
                coEvery { itemDao.insert(capture(insertedEntity)) } returns Unit

                val result = repository.getOrCreate("Milk")

                assertThat(result.name).isEqualTo("Milk")
                assertThat(result.normalizedName).isEqualTo("milk")
                assertThat(result.isActive).isTrue()
                coVerify { itemDao.insert(any()) }
                assertThat(insertedEntity.captured.name).isEqualTo("Milk")
                assertThat(insertedEntity.captured.normalizedName).isEqualTo("milk")
            }

        @Test
        fun `normalizes name with extra whitespace`() =
            runTest(testDispatcher) {
                coEvery { itemDao.findByNormalizedName("whole milk") } returns null
                coEvery { itemDao.insert(any()) } returns Unit

                val result = repository.getOrCreate("  Whole   Milk  ")

                assertThat(result.name).isEqualTo("Whole   Milk")
                assertThat(result.normalizedName).isEqualTo("whole milk")
            }

        @Test
        fun `matches existing item regardless of case`() =
            runTest(testDispatcher) {
                val existingEntity = createTestEntity(id = "existing-id", name = "Milk", normalizedName = "milk")
                coEvery { itemDao.findByNormalizedName("milk") } returns existingEntity

                val result = repository.getOrCreate("MILK")

                assertThat(result.id).isEqualTo("existing-id")
                coVerify(exactly = 0) { itemDao.insert(any()) }
            }
    }

    @Nested
    @DisplayName("insert")
    inner class InsertTests {
        @Test
        fun `returns success when insert succeeds`() =
            runTest(testDispatcher) {
                coEvery { itemDao.insert(any()) } returns Unit
                val item = createTestDomainItem(id = "new-id", name = "Eggs")

                val result = repository.insert(item)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.id).isEqualTo("new-id")
            }

        @Test
        fun `returns failure when insert throws`() =
            runTest(testDispatcher) {
                coEvery { itemDao.insert(any()) } throws RuntimeException("DB error")
                val item = createTestDomainItem()

                val result = repository.insert(item)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("update")
    inner class UpdateTests {
        @Test
        fun `returns success when update succeeds`() =
            runTest(testDispatcher) {
                coEvery { itemDao.update(any()) } returns Unit
                val item = createTestDomainItem()

                val result = repository.update(item)

                assertThat(result.isSuccess).isTrue()
            }

        @Test
        fun `returns failure when update throws`() =
            runTest(testDispatcher) {
                coEvery { itemDao.update(any()) } throws RuntimeException("DB error")
                val item = createTestDomainItem()

                val result = repository.update(item)

                assertThat(result.isFailure).isTrue()
            }
    }

    @Nested
    @DisplayName("softDelete")
    inner class SoftDeleteTests {
        @Test
        fun `returns success when soft delete succeeds`() =
            runTest(testDispatcher) {
                coEvery { itemDao.softDelete("test-id") } returns Unit

                val result = repository.softDelete("test-id")

                assertThat(result.isSuccess).isTrue()
                coVerify { itemDao.softDelete("test-id") }
            }
    }

    @Nested
    @DisplayName("hardDelete")
    inner class HardDeleteTests {
        @Test
        fun `returns success when hard delete succeeds`() =
            runTest(testDispatcher) {
                coEvery { itemDao.hardDelete("test-id") } returns Unit

                val result = repository.hardDelete("test-id")

                assertThat(result.isSuccess).isTrue()
                coVerify { itemDao.hardDelete("test-id") }
            }
    }

    // Helper functions
    private fun createTestEntity(
        id: String = "test-id",
        name: String = "Test Item",
        normalizedName: String = "test item",
        createdAt: Long = 1700000000000L,
        isActive: Boolean = true,
    ) = ItemEntity(
        id = id,
        name = name,
        normalizedName = normalizedName,
        createdAt = createdAt,
        isActive = isActive,
    )

    private fun createTestDomainItem(
        id: String = "test-id",
        name: String = "Test Item",
        normalizedName: String = "test item",
        createdAt: Instant = Instant.ofEpochMilli(1700000000000L),
        isActive: Boolean = true,
    ) = com.restock.core.domain.model.GroceryItem(
        id = id,
        name = name,
        normalizedName = normalizedName,
        createdAt = createdAt,
        isActive = isActive,
    )
}
