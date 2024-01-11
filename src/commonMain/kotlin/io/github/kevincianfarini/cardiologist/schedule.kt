package io.github.kevincianfarini.cardiologist

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.*
import io.github.kevincianfarini.cardiologist.impl.nextMatch
import kotlin.time.Duration

/**
 * Return a [Pulse] which beats every [interval]. The return value will immediately beat prior to delaying.
 */
public fun Clock.intervalPulse(interval: Duration): Pulse {
    val flow = flow {
        while (true) {
            emit(now())
            delay(interval)
        }
    }
    return Pulse(flow)
}

/**
 * Return a [Pulse] which beats every [period] in [timeZone]. The return value will immediately beat prior to delaying.
 */
public fun Clock.intervalPulse(period: DateTimePeriod, timeZone: TimeZone): Pulse {
    val flow = flow {
        while (true) {
            emit(now())
            delayFor(period, timeZone)
        }
    }
    return Pulse(flow)
}

/**
 * Schedule a [Pulse] whose beats occur [atSecond], [atMinute], [atHour], [onDayOfMonth], and [inMonth] for a specific
 * [timeZone].
 *
 * Null values for any parameter indiciates that a beat can occur on _any_ second, minute, hour, etc. For example,
 * scheduling a pulse on the fifth second of every minute would look like the following:
 *
 * ```kt
 * clock.schedulePulse(atSecond = 5)
 * ```
 *
 * While scheduling a pulse to occur on the third of every month at 12:30 would look like the following:
 *
 * ```kt
 * clock.schedulePulse(atSecond = 0, atMinute = 30, atHour = 12, onDayOfMonth = 3)
 * ```
 *
 * Scheduling pulses is done in local time and is therefore subject to daylight savings time adjustments. Local time
 * conversion is sometimes ambiguous, and therefore it's recommended to schedule pulses in a fixed UTC offset timezone.
 * See [LocalDateTime.toInstant] for more details.
 *
 * @param atSecond The second of a minute to pulse at. Null matches the whole valid range, 0..59.
 * @param atMinute The minute of an hour to pulse at. Null matches the whole valid range, 0..59.
 * @param atHour The hour of a day to pulse at. Null matches the whole valid range, 0..23.
 * @param onDayOfMonth The day of a month to pulse at. Null matches the whole valid range, 0..31.
 * @param inMonth The month of a year to pulse at. Null matches the whole valid range, January..December.
 * @throws [IllegalArgumentException] if any parameter is out of the above range.
 */
public fun Clock.schedulePulse(
    timeZone: TimeZone = TimeZone.UTC,
    atSecond: Int? = null,
    atMinute: Int? = null,
    atHour: Int? = null,
    onDayOfMonth: Int? = null,
    inMonth: Month? = null,
): Pulse {
    val flow = flow {
        while (true) {
            val nowLocal = now().toLocalDateTime(timeZone)
            val nextPulse = nowLocal.nextMatch(
                atSeconds = atSecond?.let { it..it } ?: 0..59,
                atMinutes = atMinute?.let { it..it } ?: 0..59,
                atHours = atHour?.let { it..it } ?: 0..23,
                onDaysOfMonth = onDayOfMonth?.let { it..it } ?: 1..31,
                inMonths = inMonth?.let { it..it } ?: Month.JANUARY..Month.DECEMBER,
            )
            delayUntil(nextPulse, timeZone)
            emit(now())
        }
    }
    return Pulse(flow)
}
