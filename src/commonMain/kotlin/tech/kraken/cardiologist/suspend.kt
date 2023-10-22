package tech.kraken.cardiologist

import kotlinx.coroutines.delay
import kotlinx.datetime.*
import kotlin.time.Duration

public suspend fun Clock.delayUntil(instant: Instant) {
    val duration = instant - now()
    delay(duration.roundToMillis())
}

/**
 * We don't use duration based [delay] from kotlinx.coroutines because it has a bug
 * that loses nanosecond granularity when rounding to milliseconds.
 *
 * See: https://github.com/Kotlin/kotlinx.coroutines/issues/3920
 */
internal fun Duration.roundToMillis(): Long = if (this > Duration.ZERO) {
    val millis = inWholeMilliseconds
    if (millis * 1_000_000 < inWholeNanoseconds) millis + 1 else millis
} else 0

public suspend fun Clock.delayUntil(dateTime: LocalDateTime, timeZone: TimeZone) {
    delayUntil(instant = dateTime.toInstant(timeZone))
}

public suspend fun Clock.delayFor(period: DateTimePeriod, timeZone: TimeZone) {
    val now = now()
    val futureInstant = now.plus(period, timeZone)
    val duration = futureInstant - now
    delay(duration.roundToMillis())
}

public suspend fun Clock.delayUntilNext(time: LocalTime, timeZone: TimeZone) {
    val now = now()
    val nowLocal = now.toLocalDateTime(timeZone)
    val desiredLocal = nowLocal.date.atTime(time)
    val futureInstant = when {
        desiredLocal < nowLocal -> desiredLocal.toInstant(timeZone).plus(DateTimePeriod(days = 1), timeZone)
        else -> desiredLocal.toInstant(timeZone)
    }

    val duration = futureInstant - now
    delay(duration.roundToMillis())
}