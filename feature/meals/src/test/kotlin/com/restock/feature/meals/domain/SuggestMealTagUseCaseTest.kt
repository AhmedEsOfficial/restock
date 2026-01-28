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
 * Unit tests for [SuggestMealTagUseCase].
 */
class SuggestMealTagUseCaseTest {
    private val mealTagRepository: MealTagRepository = mockk()
    private lateinit var useCase: SuggestMealTagUseCase

    @BeforeEach
    fun setup() {
        useCase = SuggestMealTagUseCase(mealTagRepository)
    }

    @Nested
    @DisplayName("invoke")
    inner class InvokeTests {
        @Test
        fun `returns breakfast suggestion`() =
            runTest {
                coEvery { mealTagRepository.suggestTag("eggs") } returns MealType.BREAKFAST

                val result = useCase("eggs")

                assertThat(result).isEqualTo(MealType.BREAKFAST)
            }

        @Test
        fun `returns lunch suggestion`() =
            runTest {
                coEvery { mealTagRepository.suggestTag("sandwich") } returns MealType.LUNCH

                val result = useCase("sandwich")

                assertThat(result).isEqualTo(MealType.LUNCH)
            }

        @Test
        fun `returns dinner suggestion`() =
            runTest {
                coEvery { mealTagRepository.suggestTag("steak") } returns MealType.DINNER

                val result = useCase("steak")

                assertThat(result).isEqualTo(MealType.DINNER)
            }

        @Test
        fun `returns snack suggestion`() =
            runTest {
                coEvery { mealTagRepository.suggestTag("chips") } returns MealType.SNACK

                val result = useCase("chips")

                assertThat(result).isEqualTo(MealType.SNACK)
            }

        @Test
        fun `returns null when no suggestion`() =
            runTest {
                coEvery { mealTagRepository.suggestTag("random item") } returns null

                val result = useCase("random item")

                assertThat(result).isNull()
            }

        @Test
        fun `delegates to repository`() =
            runTest {
                coEvery { mealTagRepository.suggestTag(any()) } returns null

                useCase("Test Item")

                coVerify { mealTagRepository.suggestTag("Test Item") }
            }
    }
}
