package io.github.kevincianfarini.cardiologist

import io.github.kevincianfarini.cardiologist.impl.nextMatch
import io.github.kevincianfarini.cardiologist.impl.parseCronExpression
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Return a [Pulse] which beats every [period].
 *
 * The period cadence which this [Pulse] beats is defined as the time between the start of one [beat][Pulse.beat] and the
 * scheduled start of the subsequent [beat][Pulse.beat]. The returned [Pulse] will delay for [period] prior to its first
 * [beat][Pulse.beat].
 */
@ExperimentalTime
public fun Clock.fixedPeriodPulse(period: Duration): Pulse {
    val flow = flow {
        var nextPulse: Instant = now() + period
        while (true) {
            delayUntil(nextPulse)
            emit(nextPulse)
            nextPulse += period
        }
    }
    return Pulse(flow)
}

/**
 * Return a [Pulse] which beats every [period] in [timeZone].
 *
 * The period cadence which this [Pulse] beats is defined as the time between the start of one [beat][Pulse.beat] and the
 * scheduled start of the subsequent [beat][Pulse.beat]. The returned [Pulse] will delay for [period] prior to its first
 * [beat][Pulse.beat].
 */
@ExperimentalTime
public fun Clock.fixedPeriodPulse(period: DateTimePeriod, timeZone: TimeZone): Pulse {
    val flow = flow {
        var nextPulse: Instant = now().plus(period, timeZone)
        while (true) {
            delayUntil(nextPulse)
            emit(nextPulse)
            nextPulse = nextPulse.plus(period, timeZone)
        }
    }
    return Pulse(flow)
}

/**
 * Schedule a [Pulse] whose beats occur [atSecond], [atMinute], [atHour], [onDayOfMonth], and [inMonth] for a specific
 * [timeZone].
 *
 * Null values for any parameter indicate that a beat can occur on _any_ second, minute, hour, etc. For example,
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
 * @param onDayOfWeek The day of week to pulse at. Null matches any day of the week.
 * @param timeZone The TimeZone to schedule pulses in.
 * @throws [IllegalArgumentException] if any parameter is out of the above range.
 */
@ExperimentalTime
public fun Clock.schedulePulse(
    timeZone: TimeZone = TimeZone.UTC,
    atSecond: Int? = null,
    atMinute: Int? = null,
    atHour: Int? = null,
    onDayOfMonth: Int? = null,
    inMonth: Month? = null,
    onDayOfWeek: DayOfWeek? = null
): Pulse = schedulePulse(timeZone) {
    atSecond?.run(this::atSeconds)
    atMinute?.run(this::atMinutes)
    atHour?.run(this::atHours)
    onDayOfMonth?.run(this::onDaysOfMonth)
    inMonth?.run(this::inMonths)
    onDayOfWeek?.run(this::onDaysOfWeek)
}

/**
 * Schedule a [Pulse] whose beats occur in accordance with the [built schedule][scheduleBuilder] for a specific
 * [timeZone].
 *
 * For example, the following invocation will schedule a Pulse on the fifth minute of every hour.
 *
 * ```kt
 * clock.schedulePulse {
 *   atSeconds(0)
 *   atMinutes(5)
 * }
 * ```
 *
 * Scheduling pulses is done in local time and is therefore subject to daylight savings time adjustments. Local time
 * conversion is sometimes ambiguous, and therefore it's recommended to schedule pulses in a fixed UTC offset timezone.
 * See [LocalDateTime.toInstant] for more details.
 *
 * @param scheduleBuilder The DSL lambda for building a Pulse schedule.
 * @param timeZone The TimeZone to schedule pulses in.
 * @throws IllegalArgumentException if [scheduleBuilder] is not valid.
 */
@ExperimentalTime
public fun Clock.schedulePulse(
    timeZone: TimeZone = TimeZone.UTC,
    scheduleBuilder: PulseScheduleBuilder.() -> Unit,
): Pulse = schedulePulse(
    schedule = buildPulseSchedule(scheduleBuilder),
    timeZone = timeZone,
)

/**
 * Schedule a [Pulse] whose beats occur in accordance with the provided [cronExpression] for a specific [timeZone].
 *
 * See [PulseSchedule.parseCron] for supported cron expression features.
 *
 * Scheduling pulses is done in local time and is therefore subject to daylight savings time adjustments. Local time
 * conversion is sometimes ambiguous, and therefore it's recommended to schedule pulses in a fixed UTC offset timezone.
 * See [LocalDateTime.toInstant] for more details.
 *
 * @param cronExpression The standard cron expression.
 * @param timeZone The TimeZone to schedule pulses in.
 * @throws IllegalArgumentException if [cronExpression] is not valid.
 */
@Deprecated(
    message = "Parse cron expression separately from scheduling a pulse",
    replaceWith = ReplaceWith(
        "this.schedulePulse(PulseSchedule.parseCron(cronExpression), timeZone)",
        "io.github.kevincianfarini.cardiologist.PulseSchedule",
    ),
)
@ExperimentalTime
public fun Clock.schedulePulse(
    cronExpression: String,
    timeZone: TimeZone = TimeZone.UTC,
): Pulse = schedulePulse(
    schedule = cronExpression.parseCronExpression(),
    timeZone = timeZone,
)

/**
 * Schedule a [Pulse] whose beats occur in accordance with [schedule] for a specific [timeZone].
 *
 * Scheduling pulses is done in local time and is therefore subject to daylight savings time adjustments. Local time
 * conversion is sometimes ambiguous, and therefore it's recommended to schedule pulses in a fixed UTC offset timezone.
 * See [LocalDateTime.toInstant] for more details.
 *
 * @param schedule The pre-built [PulseSchedule].
 * @param timeZone The TimeZone to schedule pulses in.
 */
@ExperimentalTime
public fun Clock.schedulePulse(
    schedule: PulseSchedule,
    timeZone: TimeZone = TimeZone.UTC,
): Pulse {
    val flow = flow {
        var lastPulse: LocalDateTime = now().toLocalDateTime(timeZone)
        while (true) {
            val nextPulse = lastPulse.nextMatch(schedule)
            delayUntil(nextPulse, timeZone)
            emit(nextPulse.toInstant(timeZone))
            lastPulse = nextPulse
        }
    }
    return Pulse(flow)
}
