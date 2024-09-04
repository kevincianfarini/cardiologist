package io.github.kevincianfarini.cardiologist

import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Delay the coroutine until [instant] without blocking the thread. If [Clock.now] is greater than [instant], this
 * function returns immediately.
 *
 * This function checks for time drift in one minute increments. Coroutines delayed for more than one minute will be
 * periodically resumed, allowing them to account for drift.
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
 * Delay the coroutine until [dateTime] in [timeZone] without blocking the thread. If [Clock.now] is greater than
 * the [Instant] associated with [dateTime] and [timeZone], this function returns immediately.
 *
 * This function checks for time drift in one minute increments. Coroutines delayed for more than one minute will be
 * periodically resumed, allowing them to account for drift.
 */
public suspend fun Clock.delayUntil(dateTime: LocalDateTime, timeZone: TimeZone) {
    delayUntil(instant = dateTime.toInstant(timeZone))
}

/**
 * Delay the coroutine for [period] in [timeZone] without blocking the thread.
 *
 * This function checks for time drift in one minute increments. Coroutines delayed for more than one minute will be
 * periodically resumed, allowing them to account for drift.
 */
public suspend fun Clock.delayFor(period: DateTimePeriod, timeZone: TimeZone) {
    val now = now()
    val futureInstant = now.plus(period, timeZone)
    delayUntil(futureInstant)
}

/**
 * Delay the coroutine until the next occurance of [time] in [timeZone] without blocking the thread.
 *
 * This function checks for time drift in one minute increments. Coroutines delayed for more than one minute will be
 * periodically resumed, allowing them to account for drift.
 */
public suspend fun Clock.delayUntilNext(time: LocalTime, timeZone: TimeZone) {
    val now = now()
    val nowLocal = now.toLocalDateTime(timeZone)
    val desiredLocal = nowLocal.date.atTime(time)
    val futureInstant = when {
        desiredLocal < nowLocal -> desiredLocal.toInstant(timeZone).plus(DateTimePeriod(days = 1), timeZone)
        else -> desiredLocal.toInstant(timeZone)
    }
    delayUntil(futureInstant)
}