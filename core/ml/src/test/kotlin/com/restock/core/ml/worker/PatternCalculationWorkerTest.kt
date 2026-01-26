package com.restock.core.ml.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.restock.core.domain.usecase.CalculatePatternsUseCase
import com.restock.core.domain.usecase.CalculationResult
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [PatternCalculationWorker].
 *
 * Tests verify correct behavior for:
 * - Successful pattern calculation
 * - Partial failures (some items fail)
 * - Complete failure scenarios
 * - Retry logic
 */
class PatternCalculationWorkerTest {
    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var calculatePatternsUseCase: CalculatePatternsUseCase

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        calculatePatternsUseCase = mockk()
    }

    private fun createWorker(): PatternCalculationWorker =
        PatternCalculationWorker(
            context = context,
            params = workerParams,
            calculatePatternsUseCase = calculatePatternsUseCase,
        )

    @Nested
    @DisplayName("Successful calculation scenarios")
    inner class SuccessfulCalculationTests {
        @Test
        fun `returns success when all patterns calculated successfully`() =
            runTest {
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 10,
                        patternsCalculated = 5,
                        patternsUpdated = 5,
                        patternsFailed = 0,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }

        @Test
        fun `returns success when no items to process`() =
            runTest {
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 0,
                        patternsCalculated = 0,
                        patternsUpdated = 0,
                        patternsFailed = 0,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }

        @Test
        fun `returns success when new patterns created`() =
            runTest {
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 5,
                        patternsCalculated = 5,
                        patternsUpdated = 0,
                        patternsFailed = 0,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }

        @Test
        fun `returns success when patterns only updated`() =
            runTest {
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 3,
                        patternsCalculated = 0,
                        patternsUpdated = 3,
                        patternsFailed = 0,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }
    }

    @Nested
    @DisplayName("Partial failure scenarios")
    inner class PartialFailureTests {
        @Test
        fun `returns success when less than half fail`() =
            runTest {
                // 3 out of 10 failed = 30%, less than 50%
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 10,
                        patternsCalculated = 5,
                        patternsUpdated = 2,
                        patternsFailed = 3,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }

        @Test
        fun `returns success when exactly half fail`() =
            runTest {
                // 5 out of 10 failed = 50%, equal to half
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 10,
                        patternsCalculated = 2,
                        patternsUpdated = 3,
                        patternsFailed = 5,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }

        @Test
        fun `returns retry when more than half fail`() =
            runTest {
                // 6 out of 10 failed = 60%, more than 50%
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 10,
                        patternsCalculated = 2,
                        patternsUpdated = 2,
                        patternsFailed = 6,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.retry())
            }

        @Test
        fun `returns retry when all items fail`() =
            runTest {
                // All failed
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 5,
                        patternsCalculated = 0,
                        patternsUpdated = 0,
                        patternsFailed = 5,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.retry())
            }
    }

    @Nested
    @DisplayName("Complete failure scenarios")
    inner class CompleteFailureTests {
        @Test
        fun `returns retry when use case throws exception`() =
            runTest {
                coEvery { calculatePatternsUseCase() } returns
                    Result.failure(RuntimeException("Database error"))

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.retry())
            }

        @Test
        fun `returns retry when network error occurs`() =
            runTest {
                coEvery { calculatePatternsUseCase() } returns
                    Result.failure(java.io.IOException("Network error"))

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.retry())
            }

        @Test
        fun `returns retry when unknown error occurs`() =
            runTest {
                coEvery { calculatePatternsUseCase() } returns
                    Result.failure(IllegalStateException("Unknown error"))

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.retry())
            }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCaseTests {
        @Test
        fun `returns success when processing single item successfully`() =
            runTest {
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 1,
                        patternsCalculated = 1,
                        patternsUpdated = 0,
                        patternsFailed = 0,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }

        @Test
        fun `returns retry when single item fails`() =
            runTest {
                // 1 out of 1 = 100% failure
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 1,
                        patternsCalculated = 0,
                        patternsUpdated = 0,
                        patternsFailed = 1,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.retry())
            }

        @Test
        fun `returns success when processing many items with few failures`() =
            runTest {
                // 10% failure rate
                val calculationResult =
                    CalculationResult(
                        itemsProcessed = 100,
                        patternsCalculated = 80,
                        patternsUpdated = 10,
                        patternsFailed = 10,
                    )
                coEvery { calculatePatternsUseCase() } returns Result.success(calculationResult)

                val worker = createWorker()
                val workResult = worker.doWork()

                assertThat(workResult).isEqualTo(ListenableWorker.Result.success())
            }
    }

    @Nested
    @DisplayName("Worker constants")
    inner class WorkerConstantsTests {
        @Test
        fun `periodic work name is defined`() {
            assertThat(PatternCalculationWorker.PERIODIC_WORK_NAME)
                .isEqualTo("pattern_calculation_periodic")
        }

        @Test
        fun `immediate work name is defined`() {
            assertThat(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
                .isEqualTo("pattern_calculation_immediate")
        }

        @Test
        fun `periodic tag is defined`() {
            assertThat(PatternCalculationWorker.TAG_PERIODIC)
                .isEqualTo("pattern_periodic")
        }

        @Test
        fun `immediate tag is defined`() {
            assertThat(PatternCalculationWorker.TAG_IMMEDIATE)
                .isEqualTo("pattern_immediate")
        }
    }
}
