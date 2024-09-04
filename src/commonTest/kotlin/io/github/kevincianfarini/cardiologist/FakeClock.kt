package io.github.kevincianfarini.cardiologist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.datetime.*
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
private class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {
    override fun now() = Instant.fromEpochMilliseconds(scheduler.currentTime)
}

val TestScope.testClock: Clock get() = TestClock(testScheduler)

@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.testClockAt(instant: Instant): Clock {
    val currentTime = Instant.fromEpochMilliseconds(currentTime)
    advanceTimeBy(instant - currentTime)
    return TestClock(testScheduler)
}

fun TestScope.testClockAt(localDateTime: LocalDateTime, timeZone: TimeZone): Clock {
    return testClockAt(localDateTime.toInstant(timeZone))
}

private class QueueClock(private val instants: List<Instant>) : Clock {
    private var index = 0
    override fun now(): Instant = instants[index++]
}

fun List<Instant>.asClock(): Clock = QueueClock(this)