package tech.kraken.secretary

import kotlinx.coroutines.delay
import kotlinx.datetime.*

public suspend fun Clock.delayUntil(instant: Instant) {
    delay(duration = instant - now())
}

public suspend fun Clock.delayUntil(dateTime: LocalDateTime, timeZone: TimeZone) {
    delayUntil(instant = dateTime.toInstant(timeZone))
}

public suspend fun Clock.delayFor(period: DateTimePeriod, timeZone: TimeZone) {
    val now = now()
    val futureInstant = now.plus(period, timeZone)
    delay(futureInstant - now)
}

@DelicateSecretaryApi
public suspend fun Clock.delayUntilNext(time: LocalTime, timeZone: TimeZone) {
    val now = now()
    val nowLocal = now.toLocalDateTime(timeZone)
    val desiredLocal = nowLocal.date.atTime(time)
    val futureInstant = when {
        desiredLocal < nowLocal -> desiredLocal.toInstant(timeZone).plus(DateTimePeriod(days = 1), timeZone)
        else -> desiredLocal.toInstant(timeZone)
    }

    delay(duration = futureInstant - now)
}