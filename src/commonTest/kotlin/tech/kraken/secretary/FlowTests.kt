package tech.kraken.secretary

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
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
}