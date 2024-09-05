package io.github.kevincianfarini.cardiologist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
class SuspendingTests {

    @Test fun delays_for_proper_amount_of_time_until_future_instant() = runTest {
        val now = testClock.now()
        val future = now + 10.minutes
        val elapsed = testTimeSource.measureTime {
            testClock.delayUntil(future)
        }
        assertEquals(expected = 10.minutes, actual = elapsed)
    }

    @Test fun delayUntil_considers_positive_time_drift() = runTest {
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

    @Test fun considers_negative_time_drift() = runTest {
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

    @Test fun resumes_immediately_for_0_duration() = runTest {
        val now = testClock.now()
        val elapsed = testTimeSource.measureTime {
            testClock.delayUntil(now)
        }
        assertEquals(expected = 0.minutes, actual = elapsed)
    }

    @Test fun resumes_immediately_for_negative_duration() = runTest {
        val now = testClock.now()
        val past = now - 10.minutes
        val elapsed = testTimeSource.measureTime {
            testClock.delayUntil(past)
        }
        assertEquals(expected = 0.minutes, actual = elapsed)
    }

    @Test fun delayFor_proper_time_period() = runTest {
        val timePeriod = DateTimePeriod(minutes = 10, seconds = 10, nanoseconds = 10_000_000L)
        val elapsed = testTimeSource.measureTime {
            testClock.delayFor(timePeriod, TimeZone.UTC)
        }
        assertEquals(
            expected = 10.minutes + 10.seconds + 10.milliseconds,
            actual = elapsed,
        )
    }

    @Test fun delayFor_proper_date_period() = runTest {
        val datePeriod = DateTimePeriod(days = 1)
        val elapsed = testTimeSource.measureTime {
            testClock.delayFor(datePeriod, TimeZone.UTC)
        }
        assertEquals(expected = 1.days, actual = elapsed)
    }

    @Test fun delayUntilNext_simple_in_future_delays_for_proper_duration() = runTest {
        val tz = TimeZone.UTC
        val localDt = LocalDateTime(year = 2023, monthNumber = 10, dayOfMonth = 4, hour = 16, minute = 0)
        val clock = testClockAt(localDt, tz)
        val elapsed = testTimeSource.measureTime {
            clock.delayUntilNext(LocalTime(hour = 17, minute = 0), tz)
        }
        assertEquals(expected = 1.hours, actual = elapsed)
    }

    @Test fun delayUntilNext_simple_in_past_delays_until_tomorrow_duration() = runTest {
        val tz = TimeZone.UTC
        val localDt = LocalDateTime(year = 2023, monthNumber = 10, dayOfMonth = 4, hour = 16, minute = 0)
        val clock = testClockAt(localDt, tz)
        val elapsed = testTimeSource.measureTime {
            clock.delayUntilNext(LocalTime(hour = 14, minute = 0), tz)
        }
        assertEquals(expected = 22.hours, actual = elapsed)
    }

    @Test fun delayUntilNext_time_zone_switch_forwards_delays_proper_duration() = runTest {
        val tz = TimeZone.of("America/New_York")
        val localDt = LocalDateTime(year = 2023, monthNumber = 3, dayOfMonth = 12, hour = 1, minute = 0)
        val clock = testClockAt(localDt, tz)
        val elapsed = testTimeSource.measureTime {
            clock.delayUntilNext(LocalTime(hour = 2, minute = 30), tz)
        }
        assertEquals(expected = 1.hours + 30.minutes, actual = elapsed)
    }

    @Test fun delayUntilNext_time_zone_switch_backwards_delays_proper_duration() = runTest {
        val tz = TimeZone.of("America/New_York")
        val localDt = LocalDateTime(year = 2023, monthNumber = 11, dayOfMonth = 5, hour = 1, minute = 0)
        val clock = testClockAt(localDt, tz)
        val elapsed = testTimeSource.measureTime {
            clock.delayUntilNext(LocalTime(hour = 2, minute = 0), tz)
        }
        assertEquals(expected = 2.hours, actual = elapsed)
    }
}