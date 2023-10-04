package tech.kraken.secretary

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.*

@OptIn(ExperimentalCoroutinesApi::class)
private class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {
    override fun now() = Instant.fromEpochMilliseconds(scheduler.currentTime)
}

val TestScope.testClock: Clock get() = TestClock(testScheduler)

private class StaticClock(private val now: Instant) : Clock {
    override fun now() = now
}

fun Instant.asClock(): Clock = StaticClock(this)

fun LocalDateTime.asClock(timeZone: TimeZone): Clock = StaticClock(toInstant(timeZone))
