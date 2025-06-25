package io.github.kevincianfarini.cardiologist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.measureTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.datetime.*

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class SuspendingTests {

    @Test
    fun delays_for_proper_amount_of_time_until_future_instant() = runTest {
        val now = testClock.now()
        val future = now + 10.minutes
        val elapsed = testTimeSource.measureTime {
            testClock.delayUntil(future)
        }
        assertEquals(expected = 10.minutes, actual = elapsed)
    }

    @Test
    fun delayUntil_considers_positive_time_drift() = runTest {
        val instants = listOf(
            Instant.fromEpochMilliseconds(0),
            Instant.fromEpochSeconds(65),
            Instant.fromEpochSeconds(120),
        )
        val clock = instants.asClock()
        val elapsed = testTimeSource.measureTime {
            clock.delayUntil(Instant.fromEpochSeconds(120))
        }
        // Below we assert that 115 seconds elapsed because the test time source is monotonic. It doesn't care about
        // time drift on the Clock. The above setup implies that between the first and the second invocation of
        // Clock.now, we experienced 5 seconds of positive time drift. This happens in scenarios where the device's
        // clock runs slowly and NTP adjusts it forwards five seconds.
        assertEquals(expected = 115.seconds, actual = elapsed)
    }

    @Test
    fun considers_negative_time_drift() = runTest {
        val instants = listOf(
            Instant.fromEpochMilliseconds(0),
            Instant.fromEpochSeconds(55),
            Instant.fromEpochSeconds(60),
        )
        val clock = instants.asClock()
        val elapsed = testTimeSource.measureTime {
            clock.delayUntil(Instant.fromEpochSeconds(60))
        }
        // Below we assert that 65 seconds elapsed because the test time source is monotonic. It doesn't care about
        // time drift on the Clock. The above setup implies that between the first and the second invocation of
        // Clock.now, we experienced 5 seconds of negative time drift. This happens in scenarios where the device's
        // clock runs quickly and NTP adjusts it backwards five seconds.
        assertEquals(expected = 65.seconds, actual = elapsed)
    }

    @Test
    fun resumes_immediately_for_0_duration() = runTest {
        val now = testClock.now()
        val elapsed = testTimeSource.measureTime {
            testClock.delayUntil(now)
        }
        assertEquals(expected = 0.minutes, actual = elapsed)
    }

    @Test
    fun resumes_immediately_for_negative_duration() = runTest {
        val now = testClock.now()
        val past = now - 10.minutes
        val elapsed = testTimeSource.measureTime {
            testClock.delayUntil(past)
        }
        assertEquals(expected = 0.minutes, actual = elapsed)
    }

    @Test
    fun executeAt_invokes_with_correct_occurred_instant() = runTest {
        val tenSeconds = Instant.fromEpochSeconds(10)
        val duration = testTimeSource.measureTime {
            testClock.executeAt(tenSeconds) {
                assertEquals(expected = tenSeconds, actual = testClock.now())
            }
        }
        assertEquals(expected = 10.seconds, actual = duration)
    }

    @Test
    fun executeAt_invokes_with_correct_occurred_local_date_time() = runTest {
        val localDateTime = LocalDateTime(1970, 1, 2, 0, 0)
        val tz = TimeZone.UTC
        val duration = testTimeSource.measureTime {
            testClock.executeAt(localDateTime, tz) {
                assertEquals(expected = localDateTime, actual = testClock.now().toLocalDateTime(tz))
            }
        }
        assertEquals(expected = 24.hours, actual = duration)
    }
}