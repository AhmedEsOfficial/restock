package com.restock.feature.meals.domain

import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.model.MealTag
import com.restock.core.domain.model.MealType
import com.restock.core.domain.repository.MealTagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [AddMealTagUseCase].
 */
class AddMealTagUseCaseTest {
    private val mealTagRepository: MealTagRepository = mockk()
    private lateinit var useCase: AddMealTagUseCase

    @BeforeEach
    fun setup() {
        useCase = AddMealTagUseCase(mealTagRepository)
    }

    @Nested
    @DisplayName("invoke")
    inner class InvokeTests {
        @Test
        fun `returns success with new tag when created`() =
            runTest {
                val expectedTag =
                    MealTag(
                        id = "tag-1",
                        itemId = "item-1",
                        mealType = MealType.BREAKFAST,
                        frequencyCount = 1,
                    )
                coEvery {
                    mealTagRepository.addTag("item-1", MealType.BREAKFAST)
                } returns Result.success(expectedTag)

                val result = useCase("item-1", MealType.BREAKFAST)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()).isEqualTo(expectedTag)
                coVerify { mealTagRepository.addTag("item-1", MealType.BREAKFAST) }
            }

        @Test
        fun `returns success with updated tag when frequency incremented`() =
            runTest {
                val expectedTag =
                    MealTag(
                        id = "tag-1",
                        itemId = "item-1",
                        mealType = MealType.LUNCH,
                        frequencyCount = 5,
                    )
                coEvery {
                    mealTagRepository.addTag("item-1", MealType.LUNCH)
                } returns Result.success(expectedTag)

                val result = useCase("item-1", MealType.LUNCH)

                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrNull()?.frequencyCount).isEqualTo(5)
            }

        @Test
        fun `returns failure when repository fails`() =
            runTest {
                coEvery {
                    mealTagRepository.addTag(any(), any())
                } returns Result.failure(RuntimeException("DB error"))

                val result = useCase("item-1", MealType.DINNER)

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `passes correct meal type to repository`() =
            runTest {
                coEvery { mealTagRepository.addTag(any(), any()) } returns Result.success(mockk())

                useCase("item-1", MealType.SNACK)

                coVerify { mealTagRepository.addTag("item-1", MealType.SNACK) }
            }
    }
}
