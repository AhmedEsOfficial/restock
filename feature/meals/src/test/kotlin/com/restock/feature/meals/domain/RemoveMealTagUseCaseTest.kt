package com.restock.feature.meals.domain

import com.google.common.truth.Truth.assertThat
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
 * Unit tests for [RemoveMealTagUseCase].
 */
class RemoveMealTagUseCaseTest {
    private val mealTagRepository: MealTagRepository = mockk()
    private lateinit var useCase: RemoveMealTagUseCase

    @BeforeEach
    fun setup() {
        useCase = RemoveMealTagUseCase(mealTagRepository)
    }

    @Nested
    @DisplayName("invoke")
    inner class InvokeTests {
        @Test
        fun `returns success when tag removed`() =
            runTest {
                coEvery {
                    mealTagRepository.removeTag("item-1", MealType.BREAKFAST)
                } returns Result.success(Unit)

                val result = useCase("item-1", MealType.BREAKFAST)

                assertThat(result.isSuccess).isTrue()
                coVerify { mealTagRepository.removeTag("item-1", MealType.BREAKFAST) }
            }

        @Test
        fun `returns failure when repository fails`() =
            runTest {
                coEvery {
                    mealTagRepository.removeTag(any(), any())
                } returns Result.failure(RuntimeException("DB error"))

                val result = useCase("item-1", MealType.LUNCH)

                assertThat(result.isFailure).isTrue()
            }

        @Test
        fun `passes correct meal type to repository`() =
            runTest {
                coEvery { mealTagRepository.removeTag(any(), any()) } returns Result.success(Unit)

                useCase("item-1", MealType.DINNER)

                coVerify { mealTagRepository.removeTag("item-1", MealType.DINNER) }
            }

        @Test
        fun `handles all meal types`() =
            runTest {
                coEvery { mealTagRepository.removeTag(any(), any()) } returns Result.success(Unit)

                MealType.entries.forEach { mealType ->
                    val result = useCase("item-1", mealType)
                    assertThat(result.isSuccess).isTrue()
                }
            }
    }
}
