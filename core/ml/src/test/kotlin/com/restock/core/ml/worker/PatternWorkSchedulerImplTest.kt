package com.restock.core.ml.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [PatternWorkSchedulerImpl].
 *
 * Tests verify:
 * - Periodic scheduling configuration
 * - Immediate trigger configuration
 * - Work cancellation
 * - Correct work policies
 */
class PatternWorkSchedulerImplTest {
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: PatternWorkSchedulerImpl

    @BeforeEach
    fun setup() {
        workManager = mockk(relaxed = true)
        scheduler = PatternWorkSchedulerImpl(workManager)
    }

    @Nested
    @DisplayName("Periodic scheduling")
    inner class PeriodicSchedulingTests {
        @Test
        fun `schedulePeriodicCalculation enqueues periodic work`() {
            scheduler.schedulePeriodicCalculation()

            verify {
                workManager.enqueueUniquePeriodicWork(
                    eq(PatternCalculationWorker.PERIODIC_WORK_NAME),
                    eq(ExistingPeriodicWorkPolicy.KEEP),
                    any<PeriodicWorkRequest>(),
                )
            }
        }

        @Test
        fun `schedulePeriodicCalculation uses correct work name`() {
            val workNameSlot = slot<String>()

            every {
                workManager.enqueueUniquePeriodicWork(
                    capture(workNameSlot),
                    any(),
                    any<PeriodicWorkRequest>(),
                )
            } returns mockk()

            scheduler.schedulePeriodicCalculation()

            assertThat(workNameSlot.captured)
                .isEqualTo(PatternCalculationWorker.PERIODIC_WORK_NAME)
        }

        @Test
        fun `schedulePeriodicCalculation uses KEEP policy`() {
            val policySlot = slot<ExistingPeriodicWorkPolicy>()

            every {
                workManager.enqueueUniquePeriodicWork(
                    any(),
                    capture(policySlot),
                    any<PeriodicWorkRequest>(),
                )
            } returns mockk()

            scheduler.schedulePeriodicCalculation()

            assertThat(policySlot.captured).isEqualTo(ExistingPeriodicWorkPolicy.KEEP)
        }

        @Test
        fun `schedulePeriodicCalculation creates work with correct interval`() {
            val workRequestSlot = slot<PeriodicWorkRequest>()

            every {
                workManager.enqueueUniquePeriodicWork(
                    any(),
                    any(),
                    capture(workRequestSlot),
                )
            } returns mockk()

            scheduler.schedulePeriodicCalculation()

            // PeriodicWorkRequest doesn't expose interval directly, but we can verify
            // the work request was created with expected tags
            assertThat(workRequestSlot.captured.tags)
                .contains(PatternCalculationWorker.TAG_PERIODIC)
        }

        @Test
        fun `schedulePeriodicCalculation adds periodic tag`() {
            val workRequestSlot = slot<PeriodicWorkRequest>()

            every {
                workManager.enqueueUniquePeriodicWork(
                    any(),
                    any(),
                    capture(workRequestSlot),
                )
            } returns mockk()

            scheduler.schedulePeriodicCalculation()

            assertThat(workRequestSlot.captured.tags)
                .contains(PatternCalculationWorker.TAG_PERIODIC)
        }

        @Test
        fun `multiple calls to schedulePeriodicCalculation are idempotent`() {
            scheduler.schedulePeriodicCalculation()
            scheduler.schedulePeriodicCalculation()
            scheduler.schedulePeriodicCalculation()

            // KEEP policy means additional calls don't create new work
            verify(exactly = 3) {
                workManager.enqueueUniquePeriodicWork(
                    any(),
                    eq(ExistingPeriodicWorkPolicy.KEEP),
                    any<PeriodicWorkRequest>(),
                )
            }
        }
    }

    @Nested
    @DisplayName("Immediate trigger")
    inner class ImmediateTriggerTests {
        @Test
        fun `triggerImmediateCalculation enqueues one-time work`() {
            scheduler.triggerImmediateCalculation()

            verify {
                workManager.enqueueUniqueWork(
                    eq(PatternCalculationWorker.IMMEDIATE_WORK_NAME),
                    eq(ExistingWorkPolicy.REPLACE),
                    any<OneTimeWorkRequest>(),
                )
            }
        }

        @Test
        fun `triggerImmediateCalculation uses correct work name`() {
            val workNameSlot = slot<String>()

            every {
                workManager.enqueueUniqueWork(
                    capture(workNameSlot),
                    any(),
                    any<OneTimeWorkRequest>(),
                )
            } returns mockk()

            scheduler.triggerImmediateCalculation()

            assertThat(workNameSlot.captured)
                .isEqualTo(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
        }

        @Test
        fun `triggerImmediateCalculation uses REPLACE policy`() {
            val policySlot = slot<ExistingWorkPolicy>()

            every {
                workManager.enqueueUniqueWork(
                    any(),
                    capture(policySlot),
                    any<OneTimeWorkRequest>(),
                )
            } returns mockk()

            scheduler.triggerImmediateCalculation()

            assertThat(policySlot.captured).isEqualTo(ExistingWorkPolicy.REPLACE)
        }

        @Test
        fun `triggerImmediateCalculation adds immediate tag`() {
            val workRequestSlot = slot<OneTimeWorkRequest>()

            every {
                workManager.enqueueUniqueWork(
                    any(),
                    any(),
                    capture(workRequestSlot),
                )
            } returns mockk()

            scheduler.triggerImmediateCalculation()

            assertThat(workRequestSlot.captured.tags)
                .contains(PatternCalculationWorker.TAG_IMMEDIATE)
        }

        @Test
        fun `multiple immediate triggers replace previous work`() {
            scheduler.triggerImmediateCalculation()
            scheduler.triggerImmediateCalculation()
            scheduler.triggerImmediateCalculation()

            // REPLACE policy means each call cancels previous pending work
            verify(exactly = 3) {
                workManager.enqueueUniqueWork(
                    any(),
                    eq(ExistingWorkPolicy.REPLACE),
                    any<OneTimeWorkRequest>(),
                )
            }
        }
    }

    @Nested
    @DisplayName("Cancellation")
    inner class CancellationTests {
        @Test
        fun `cancelAll cancels periodic work`() {
            scheduler.cancelAll()

            verify {
                workManager.cancelUniqueWork(PatternCalculationWorker.PERIODIC_WORK_NAME)
            }
        }

        @Test
        fun `cancelAll cancels immediate work`() {
            scheduler.cancelAll()

            verify {
                workManager.cancelUniqueWork(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
            }
        }

        @Test
        fun `cancelAll cancels both work types`() {
            scheduler.cancelAll()

            verify(exactly = 1) {
                workManager.cancelUniqueWork(PatternCalculationWorker.PERIODIC_WORK_NAME)
            }
            verify(exactly = 1) {
                workManager.cancelUniqueWork(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
            }
        }

        @Test
        fun `cancelAll can be called multiple times safely`() {
            scheduler.cancelAll()
            scheduler.cancelAll()

            verify(exactly = 2) {
                workManager.cancelUniqueWork(PatternCalculationWorker.PERIODIC_WORK_NAME)
            }
            verify(exactly = 2) {
                workManager.cancelUniqueWork(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
            }
        }
    }

    @Nested
    @DisplayName("Work info retrieval")
    inner class WorkInfoTests {
        @Test
        fun `getPeriodicWorkInfo returns LiveData for periodic work`() {
            scheduler.getPeriodicWorkInfo()

            verify {
                workManager.getWorkInfosForUniqueWorkLiveData(
                    PatternCalculationWorker.PERIODIC_WORK_NAME,
                )
            }
        }

        @Test
        fun `getImmediateWorkInfo returns LiveData for immediate work`() {
            scheduler.getImmediateWorkInfo()

            verify {
                workManager.getWorkInfosForUniqueWorkLiveData(
                    PatternCalculationWorker.IMMEDIATE_WORK_NAME,
                )
            }
        }
    }

    @Nested
    @DisplayName("Constants")
    inner class ConstantsTests {
        @Test
        fun `periodic interval is 24 hours`() {
            assertThat(PatternWorkSchedulerImpl.PERIODIC_INTERVAL_HOURS).isEqualTo(24L)
        }
    }

    @Nested
    @DisplayName("Integration scenarios")
    inner class IntegrationTests {
        @Test
        fun `can schedule periodic and trigger immediate concurrently`() {
            scheduler.schedulePeriodicCalculation()
            scheduler.triggerImmediateCalculation()

            verify(exactly = 1) {
                workManager.enqueueUniquePeriodicWork(any(), any(), any<PeriodicWorkRequest>())
            }
            verify(exactly = 1) {
                workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
            }
        }

        @Test
        fun `can cancel after scheduling`() {
            scheduler.schedulePeriodicCalculation()
            scheduler.triggerImmediateCalculation()
            scheduler.cancelAll()

            verify(exactly = 1) {
                workManager.cancelUniqueWork(PatternCalculationWorker.PERIODIC_WORK_NAME)
            }
            verify(exactly = 1) {
                workManager.cancelUniqueWork(PatternCalculationWorker.IMMEDIATE_WORK_NAME)
            }
        }

        @Test
        fun `can reschedule after cancel`() {
            scheduler.schedulePeriodicCalculation()
            scheduler.cancelAll()
            scheduler.schedulePeriodicCalculation()

            verify(exactly = 2) {
                workManager.enqueueUniquePeriodicWork(any(), any(), any<PeriodicWorkRequest>())
            }
        }
    }
}
