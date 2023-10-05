package tech.kraken.secretary

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.datetime.*
import tech.kraken.secretary.impl.copy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class, DelicateSecretaryApi::class)
class FlowTests {

    @Test fun durationPulse_emits_initial_instant_before_delaying() = runTest {
        testClock.pulse(5.seconds).test {
            assertEquals(
                expected = 0.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }

    @Test fun durationPulse_emits_subsequent_instants_after_delaying() = runTest {
        testClock.pulse(5.seconds).test {
            skipItems(1)
            assertEquals(
                expected = 5.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }

    @Test fun periodPulse_emits_initial_instant_before_delaying() = runTest {
        testClock.pulse(DateTimePeriod(seconds = 5), TimeZone.UTC).test {
            assertEquals(
                expected = 0.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }

    @Test fun periodPulse_emits_subsequent_instants_after_delaying() = runTest {
        testClock.pulse(DateTimePeriod(seconds = 5), TimeZone.UTC).test {
            skipItems(1)
            assertEquals(
                expected = 5.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }

    @Test fun unqualified_schedulePulse_emits_every_second() = runTest {
        val start = LocalDateTime(year = 2023, monthNumber = 10, dayOfMonth = 4, hour = 0, minute = 0, second = 0)
        val elapsed = testTimeSource.measureTime {
            schedulePulseParameterized(start.copy(second = 1), start.copy(second = 2), startTime = start)
        }

        assertEquals(expected = 2.seconds, actual = elapsed)
    }

    @Test fun specified_second_schedulePulse_executes_once_per_minute() = runTest {
        val start = LocalDateTime(year = 2023, monthNumber = 10, dayOfMonth = 4, hour = 0, minute = 0, second = 0)
        val elapsed = testTimeSource.measureTime {
            schedulePulseParameterized(
                start.copy(second = 50),
                start.copy(minute = 1, second = 50),
                startTime = start,
                atSecond = 50,
            )
        }

        assertEquals(expected = 1.minutes + 50.seconds, actual = elapsed)
    }

    @Test fun specified_minute_schedulePulse_executes_60_times_for_a_given_minute() = runTest {
        val start = LocalDateTime(year = 2023, monthNumber = 10, dayOfMonth = 4, hour = 0, minute = 0, second = 0)
        val endTimes = Array(60) { index -> start.copy(minute = 50, second = index) }
        val elapsed = testTimeSource.measureTime {
            schedulePulseParameterized(
                *endTimes,
                startTime = start,
                atMinute = 50,
            )
        }

        assertEquals(expected = 51.minutes, actual = elapsed)
    }

    private suspend fun schedulePulseParameterized(
        vararg endTimes: LocalDateTime,
        startTime: LocalDateTime,
        timeZone: TimeZone = TimeZone.UTC,
        atSecond: Int? = null,
        atMinute: Int? = null,
        atHour: Int? = null,
        onDayOfWeek: DayOfWeek? = null,
        inMonth: Month? = null,
    ) {
        val clock = startTime.asClock(timeZone)
        clock.schedulePulse(timeZone, atSecond, atMinute, atHour, onDayOfWeek, inMonth).test {
            for (endTime in endTimes) {
                assertEquals(expected = endTime.toInstant(timeZone), actual = awaitItem())
            }
        }
    }
}