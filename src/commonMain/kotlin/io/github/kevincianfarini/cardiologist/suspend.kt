package io.github.kevincianfarini.cardiologist

import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Suspend the coroutine until [instant] without blocking the thread. If [Clock.now] is greater than [instant], this
 * function returns immediately.
 *
 * Coroutines delayed for more than one minute will occasionally be resumed to account for clock drift.
 */
public suspend fun Clock.delayUntil(instant: Instant) {
    var now = now()
    while (now < instant) {
        val durationUntilInstant = instant - now
        val delayIncrement = minOf(durationUntilInstant, 1.minutes)
        delay(delayIncrement)
        now = now()
    }
}

/**
 * Suspend the coroutine until [dateTime] in [timeZone] without blocking the thread. If [Clock.now] is greater than
 * the [Instant] associated with [dateTime] and [timeZone], this function returns immediately.
 *
 * Coroutines delayed for more than one minute will occasionally be resumed to account for clock drift.
 */
public suspend fun Clock.delayUntil(dateTime: LocalDateTime, timeZone: TimeZone) {
    delayUntil(instant = dateTime.toInstant(timeZone))
}
