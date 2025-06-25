package io.github.kevincianfarini.cardiologist

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalTime
private class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {
    override fun now() = Instant.fromEpochMilliseconds(scheduler.currentTime)
}

@ExperimentalTime
val TestScope.testClock: Clock get() = TestClock(testScheduler)

@ExperimentalTime
private class QueueClock(private val instants: List<Instant>) : Clock {
    private var index = 0
    override fun now(): Instant = instants[index++]
}

@ExperimentalTime
fun List<Instant>.asClock(): Clock = QueueClock(this)