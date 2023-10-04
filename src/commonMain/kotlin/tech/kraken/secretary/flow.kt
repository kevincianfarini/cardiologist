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

public fun Clock.pulse(
    atSecond: Int = 0,
    atMinute: Int? = null,
    atHour: Int? = null,
): Flow<Instant> {

}

@DelicateSecretaryApi
public fun Clock.pulse(
    timeZone: TimeZone = TimeZone.UTC,
    atTime: LocalTime = LocalTime(hour = 0, minute = 0),
    onDayOfWeek: DayOfWeek? = null,
    inMonth: Month? = null
): Flow<Instant> = flow {

}

@DelicateSecretaryApi
public fun Clock.pulse(
    timeZone: TimeZone = TimeZone.UTC,
    atTime: LocalTime = LocalTime(hour = 0, minute = 0),
    onDayOfMonth: Int = 1,
    inMonth: Month? = null
): Flow<Instant> = flow {

}
