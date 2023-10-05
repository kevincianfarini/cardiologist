package tech.kraken.secretary

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.*
import kotlin.time.Duration

/**
 * Return a [Flow] which emits the [Instant] returned from [Clock.now] every [interval]. This
 * function will immediately emit [Clock.now] when collected prior to delaying.
 */
@DelicateSecretaryApi
public fun Clock.pulse(interval: Duration): Flow<Instant> = flow {
    while (true) {
        emit(now())
        delay(interval)
    }
}

/**
 * Return a [Flow] which emits the [Instant] returned from [Clock.now] every [period] in [timeZone].
 * This function will immediately emit [Clock.now] when collected prior to delaying.
 */
@DelicateSecretaryApi
public fun Clock.pulse(period: DateTimePeriod, timeZone: TimeZone): Flow<Instant> = flow {
    while (true) {
        emit(now())
        delayFor(period, timeZone)
    }
}

public fun Clock.schedulePulse(
    timeZone: TimeZone = TimeZone.UTC,
    atSecond: Int? = null,
    atMinute: Int? = null,
    atHour: Int? = null,
    onDayOfWeek: DayOfWeek? = null,
    inMonth: Month? = null,
): Flow<Instant> = schedulePulse(
    timeZone = timeZone,
    atSeconds = atSecond?.let { it..it } ?: 0..59,
    atMinutes = atMinute?.let { it..it } ?: 0..59,
    atHours = atHour?.let { it..it } ?: 0..23,
    onDaysOfWeek = onDayOfWeek?.let { it..it } ?: DayOfWeek.MONDAY..DayOfWeek.SUNDAY,
    inMonths = inMonth?.let { it..it } ?: Month.JANUARY..Month.DECEMBER,
)

public fun Clock.schedulePulse(
    timeZone: TimeZone = TimeZone.UTC,
    atSeconds: IntRange = 0..59,
    atMinutes: IntRange = 0..59,
    atHours: IntRange = 0..23,
    onDaysOfWeek: ClosedRange<DayOfWeek> = DayOfWeek.MONDAY..DayOfWeek.SUNDAY,
    inMonths: ClosedRange<Month> = Month.JANUARY..Month.DECEMBER,
): Flow<Instant> {
    TODO()
}

public fun Clock.schedulePulse(
    timeZone: TimeZone = TimeZone.UTC,
    atSecond: Int? = null,
    atMinute: Int? = null,
    atHour: Int? = null,
    onDayOfMonth: Int? = null,
    inMonth: Month? = null,
): Flow<Instant> {
    TODO()
}
