package io.github.kevincianfarini.cardiologist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
class PulseTests {

    @Test
    fun intervalPulse_Duration_emits_subsequent_instant_after_delaying() = runTest {
        assertEquals(
            expected = 5.seconds,
            actual = testTimeSource.measureTime {
                testClock.fixedPeriodPulse(5.seconds).take(1).beat { }
            }
        )
    }

    @Test
    fun intervalPulse_DateTimePeriod_emits_subsequent_instants_after_delaying() = runTest {
        val pulse = testClock.fixedPeriodPulse(DateTimePeriod(seconds = 5), TimeZone.UTC).take(1)
        assertEquals(
            expected = 5.seconds,
            actual = testTimeSource.measureTime { pulse.beat { } }
        )
    }

    @Test
    fun intervalPulse_Duration_accounts_for_positive_time_drift() = runTest {
        var index = 0
        val scheduledInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val occurredInstants = listOf(
            Instant.fromEpochSeconds(65),
            Instant.fromEpochSeconds(120),
        )
        val clock = listOf(
            Instant.fromEpochSeconds(0), // Initial reference in intervalPulse.
            Instant.fromEpochSeconds(0), // Initial reference in delayUntil.
            Instant.fromEpochSeconds(65), // After initial delay in delayUntil.
            Instant.fromEpochSeconds(65), // During emission in intervalPulse
            Instant.fromEpochSeconds(65), // Second call to delayUntil.
            Instant.fromEpochSeconds(120), // After delayUntil resumes.
            Instant.fromEpochSeconds(120), // Second emissions in intervalPulse.
        ).asClock()
        val pulse = clock.fixedPeriodPulse(60.seconds)
        pulse.take(2).beat { scheduled ->
            assertEquals(expected = scheduledInstants[index], scheduled)
            assertEquals(expected = occurredInstants[index++], clock.now())
        }
    }

    @Test
    fun intervalPulse_Duration_accounts_for_negative_time_drift() = runTest {
        var index = 0
        val scheduledInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val occurredInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val clock = listOf(
            Instant.fromEpochSeconds(0), // Initial reference in intervalPulse.
            Instant.fromEpochSeconds(0), // Initial reference in delayUntil.
            Instant.fromEpochSeconds(60), // After initial delay in delayUntil.
            Instant.fromEpochSeconds(60), // During emission in intervalPulse
            Instant.fromEpochSeconds(60), // Second call to delayUntil.
            Instant.fromEpochSeconds(55), // Shift back 5 seconds when delayUntil resumes.
            Instant.fromEpochSeconds(55), // After delay resumes.
            Instant.fromEpochSeconds(120), // After delayUntil resumes.
            Instant.fromEpochSeconds(120), // Second emissions in intervalPulse.
        ).asClock()
        val pulse = clock.fixedPeriodPulse(60.seconds)
        pulse.take(2).beat { scheduled ->
            assertEquals(expected = scheduledInstants[index], scheduled)
            assertEquals(expected = occurredInstants[index++], clock.now())
        }
    }

    @Test
    fun intervalPulse_DateTimePeriod_accounts_for_positive_time_drift() = runTest {
        var index = 0
        val scheduledInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val occurredInstants = listOf(
            Instant.fromEpochSeconds(65),
            Instant.fromEpochSeconds(120),
        )
        val clock = listOf(
            Instant.fromEpochSeconds(0), // Initial reference in intervalPulse.
            Instant.fromEpochSeconds(0), // Initial reference in delayUntil.
            Instant.fromEpochSeconds(65), // After initial delay in delayUntil.
            Instant.fromEpochSeconds(65), // During emission in intervalPulse
            Instant.fromEpochSeconds(65), // Second call to delayUntil.
            Instant.fromEpochSeconds(120), // After delayUntil resumes.
            Instant.fromEpochSeconds(120), // Second emissions in intervalPulse.
        ).asClock()
        val pulse = clock.fixedPeriodPulse(DateTimePeriod(minutes = 1), timeZone = TimeZone.UTC)
        pulse.take(2).beat { scheduled ->
            assertEquals(expected = scheduledInstants[index], scheduled)
            assertEquals(expected = occurredInstants[index++], clock.now())
        }
    }

    @Test
    fun intervalPulse_DateTimePeriod_accounts_for_negative_time_drift() = runTest {
        var index = 0
        val scheduledInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val occurredInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val clock = listOf(
            Instant.fromEpochSeconds(0), // Initial reference in intervalPulse.
            Instant.fromEpochSeconds(0), // Initial reference in delayUntil.
            Instant.fromEpochSeconds(60), // After initial delay in delayUntil.
            Instant.fromEpochSeconds(60), // During emission in intervalPulse
            Instant.fromEpochSeconds(60), // Second call to delayUntil.
            Instant.fromEpochSeconds(55), // Shift back 5 seconds when delayUntil resumes.
            Instant.fromEpochSeconds(55), // After delay resumes.
            Instant.fromEpochSeconds(120), // After delayUntil resumes.
            Instant.fromEpochSeconds(120), // Second emissions in intervalPulse.
        ).asClock()
        val pulse = clock.fixedPeriodPulse(DateTimePeriod(minutes = 1), timeZone = TimeZone.UTC)
        pulse.take(2).beat { scheduled ->
            assertEquals(expected = scheduledInstants[index], scheduled)
            assertEquals(expected = occurredInstants[index++], clock.now())
        }
    }

    @Test
    fun schedulePulse_accounts_for_positive_time_drift() = runTest {
        val clock = listOf(
            Instant.fromEpochSeconds(0), // Initial reference in intervalPulse.
            Instant.fromEpochSeconds(0), // Initial reference in delayUntil.
            Instant.fromEpochSeconds(65), // After initial delay in delayUntil.
            Instant.fromEpochSeconds(65), // During emission in intervalPulse
        ).asClock()
        val pulse = clock.schedulePulse(timeZone = TimeZone.UTC, atMinute = 1, atSecond = 0)
        pulse.take(1).beat { scheduled ->
            assertEquals(expected = Instant.fromEpochSeconds(60), scheduled)
            assertEquals(expected = Instant.fromEpochSeconds(65), clock.now())
        }
    }

    @Test
    fun schedulePulse_accounts_for_negative_time_drift() = runTest {
        var index = 0
        val scheduledInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val occurredInstants = listOf(
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
        )
        val clock = listOf(
            Instant.fromEpochSeconds(0),
            Instant.fromEpochSeconds(0),
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(55),
            Instant.fromEpochSeconds(55),
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(60),
            Instant.fromEpochSeconds(120),
            Instant.fromEpochSeconds(120),
        ).asClock()
        val pulse = clock.schedulePulse(timeZone = TimeZone.UTC, atSecond = 0)
        pulse.take(2).beat { scheduled ->
            assertEquals(expected = scheduledInstants[index], scheduled)
            assertEquals(expected = occurredInstants[index++], clock.now())
        }
    }

    @Test
    fun pulse_execute_concurrently_accumulates_jobs() = runTest {
        val count = MutableStateFlow(0)
        backgroundScope.launch {
            val pulse = testClock.fixedPeriodPulse(1.seconds).take(3)
            pulse.beat(PulseBackpressureStrategy.ExecuteConcurrently) {
                try {
                    count.update { it + 1 }
                    awaitCancellation()
                } finally {
                    count.update { it - 1 }
                }
            }
        }
        delay(10.seconds)
        assertEquals(expected = 3, actual = count.value)
    }

    @Test
    fun pulse_cancel_previous_cancels_correct_jobs() = runTest {
        val list = MutableStateFlow<List<Instant>>(emptyList())
        backgroundScope.launch {
            val pulse = testClock.fixedPeriodPulse(1.seconds).take(3)
            pulse.beat(PulseBackpressureStrategy.CancelPrevious) { scheduled ->
                try {
                    list.update { l -> l + scheduled }
                    awaitCancellation()
                } finally {
                    list.update { l -> l - scheduled }
                }
            }
        }
        delay(10.seconds)
        assertEquals(
            expected = listOf(Instant.fromEpochSeconds(3)),
            actual = list.value,
        )
    }

    @Test
    fun pulse_skip_next_skips_correct_jobs() = runTest {
        val list = MutableStateFlow<List<Instant>>(emptyList())
        backgroundScope.launch {
            val pulse = testClock.fixedPeriodPulse(1.seconds).take(3)
            pulse.beat(PulseBackpressureStrategy.SkipNext) { scheduled ->
                try {
                    list.update { l -> l + scheduled }
                    awaitCancellation()
                } finally {
                    list.update { l -> l - scheduled }
                }
            }
        }
        delay(10.seconds)
        assertEquals(
            expected = listOf(Instant.fromEpochSeconds(1)),
            actual = list.value,
        )
    }
}