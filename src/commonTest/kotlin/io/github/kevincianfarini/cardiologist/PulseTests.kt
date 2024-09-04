package io.github.kevincianfarini.cardiologist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class PulseTests {

    @Test fun durationPulse_emits_initial_instant_before_delaying() = runTest {
        assertEquals(
            expected = 0.seconds,
            actual = testTimeSource.measureTime {
                testClock.intervalPulse(5.seconds).take(1).beat { }
            }
        )
    }

    @Test fun durationPulse_emits_subsequent_instants_after_delaying() = runTest {
        assertEquals(
            expected = 5.seconds,
            actual = testTimeSource.measureTime {
                testClock.intervalPulse(5.seconds).take(2).beat { }
            }
        )
    }

    @Test fun periodPulse_emits_initial_instant_before_delaying() = runTest {
        val pulse = testClock.intervalPulse(DateTimePeriod(seconds = 5), TimeZone.UTC).take(1)
        assertEquals(
            expected = 0.seconds,
            actual = testTimeSource.measureTime { pulse.beat { } }
        )
    }

    @Test fun periodPulse_emits_subsequent_instants_after_delaying() = runTest {
        val pulse = testClock.intervalPulse(DateTimePeriod(seconds = 5), TimeZone.UTC).take(2)
        assertEquals(
            expected = 5.seconds,
            actual = testTimeSource.measureTime { pulse.beat { } }
        )
    }

    @Test fun superTest() = runBlocking {

    }
}