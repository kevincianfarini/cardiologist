package tech.kraken.secretary

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
import kotlin.time.Duration.Companion.days
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

//    @Test fun foo() = runTest(timeout = 1.days) {
//        withContext(Dispatchers.Default) {
//            val tz = TimeZone.of("America/New_York")
//            Clock.System.schedulePulse(tz).beat(mode = RecurringJobMode.Concurrent) { instant ->
//                delay(1500L)
//                println("Delayed for 1.5 seconds! $instant")
//            }
//        }
//    }
}