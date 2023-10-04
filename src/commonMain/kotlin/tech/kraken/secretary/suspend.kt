package tech.kraken.secretary

import kotlinx.coroutines.delay
import kotlinx.datetime.*

public suspend fun Clock.delayUntil(instant: Instant) {
    delay(duration = instant - now())
}

public suspend fun Clock.delayFor(period: DateTimePeriod, timeZone: TimeZone) {
    val now = now()
    val futureInstant = now.plus(period, timeZone)
    delay(futureInstant - now)
}

@DelicateSecretaryApi
public suspend fun Clock.delayUntilNext(time: LocalTime, timeZone: TimeZone) {
    val now = now()
    val nowLocalDateTime = now.toLocalDateTime(timeZone)
    val calculatedLocalDateTime = nowLocalDateTime.date.atTime(time)
    val futureInstant = when {
        calculatedLocalDateTime < nowLocalDateTime -> {
            val previousDayInstant = calculatedLocalDateTime.toInstant(timeZone)
            previousDayInstant.plus(DateTimePeriod(days = 1), timeZone)
        }
        else -> calculatedLocalDateTime.toInstant(timeZone)
    }

    delay(duration = futureInstant - now)
}