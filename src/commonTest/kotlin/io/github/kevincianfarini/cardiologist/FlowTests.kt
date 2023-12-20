package io.github.kevincianfarini.cardiologist

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class FlowTests {

    @Test fun durationPulse_emits_initial_instant_before_delaying() = runTest {
        testClock.intervalPulse(5.seconds).test {
            assertEquals(
                expected = 0.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }

    @Test fun durationPulse_emits_subsequent_instants_after_delaying() = runTest {
        testClock.intervalPulse(5.seconds).test {
            skipItems(1)
            assertEquals(
                expected = 5.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }

    @Test fun periodPulse_emits_initial_instant_before_delaying() = runTest {
        testClock.intervalPulse(DateTimePeriod(seconds = 5), TimeZone.UTC).test {
            assertEquals(
                expected = 0.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }

    @Test fun periodPulse_emits_subsequent_instants_after_delaying() = runTest {
        testClock.intervalPulse(DateTimePeriod(seconds = 5), TimeZone.UTC).test {
            skipItems(1)
            assertEquals(
                expected = 5.seconds,
                actual = testTimeSource.measureTime { awaitItem() }
            )
        }
    }
}