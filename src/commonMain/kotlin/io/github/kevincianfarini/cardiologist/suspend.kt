package io.github.kevincianfarini.cardiologist

import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Execute the provided [action] at or after [instant].
 *
 * The coroutine will be suspended until [instant] without blocking the thread. If [Clock.now] returns an [Instant]
 * greater than the supplied [instant], this function executes [action] without suspending.
 *
 * The supplied [action] is invoked with an [Instant] parameter specifying when the action was actually executed.
 */
public suspend fun <T> Clock.executeAt(
    instant: Instant,
    action: suspend (occurred: Instant) -> T,
): T {
    delayUntil(instant)
    return action(now())
}

/**
 * Execute the provided [action] at or after [dateTime] in [timeZone].
 *
 * The coroutine will be suspended until [dateTime] in [timeZone] without blocking the thread. If [Clock.now] returns an
 * [Instant] greater than the [Instant] associated with [dateTime] in [timeZone], this function executes [action] without
 * suspending.
 *
 * The supplied [action] is invoked with a [LocalDateTime] parameter specifying the instant that [action] was executed
 * in [timeZone]. Local time conversion is sometimes ambiguous, and therefore it's recommended to schedule pulses in a
 * fixed UTC offset timezone. See [LocalDateTime.toInstant] for more details.
 */
public suspend fun <T> Clock.executeAt(
    dateTime: LocalDateTime,
    timeZone: TimeZone,
    action: suspend (occurred: LocalDateTime) -> T,
): T {
    delayUntil(dateTime, timeZone)
    return action(now().toLocalDateTime(timeZone))
}

/**
 * Suspend the coroutine until [instant] without blocking the thread. If [Clock.now] is greater than [instant], this
 * function returns immediately.
 *
 * Coroutines delayed for more than one minute will occasionally be resumed to account for clock drift.
 */
internal suspend fun Clock.delayUntil(instant: Instant) {
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
internal suspend fun Clock.delayUntil(dateTime: LocalDateTime, timeZone: TimeZone) {
    delayUntil(instant = dateTime.toInstant(timeZone))
}
