package tech.kraken.secretary.impl

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

internal fun LocalDateTime.nextMatch(
    atSeconds: IntRange = 0..59,
    atMinutes: IntRange = 0..59,
    atHours: IntRange = 0..23,
    onDaysOfMonth: IntRange = 1..31,
    inMonths: ClosedRange<Month> = Month.JANUARY..Month.DECEMBER,
): LocalDateTime {
    val incrementedSecond = if (nanosecond > 0) second + 1 else second
    val incrementedMinute = if (incrementedSecond > atSeconds.last) minute + 1 else minute
    val incrementedHour = if (incrementedMinute > atMinutes.last) hour + 1 else hour

    val shouldIncrementDay = incrementedHour > atHours.last
    val coercedDay = when {
        shouldIncrementDay && (dayOfMonth + 1) in onDaysOfMonth && (dayOfMonth + 1) <= month.numberOfDays(year) -> {
            dayOfMonth + 1
        }
        shouldIncrementDay && month.numberOfDays(year) < (dayOfMonth + 1) -> 1.coerceIn(onDaysOfMonth)
        else -> dayOfMonth
    }

    val incrementedMonth = if (coercedDay < dayOfMonth) month.inc() else month

    return copy(
        nanosecond = 0,
        second = (incrementedSecond % 60).coerceIn(atSeconds),
        minute = (incrementedMinute % 60).coerceIn(atMinutes),
        hour = (incrementedHour % 24).coerceIn(atHours),
        dayOfMonth = coercedDay,
        month = incrementedMonth.coerceIn(inMonths),
        year = if (incrementedMonth < month) year + 1 else year,
    )
}


private val MONTHS = Month.values()
private operator fun Month.inc(): Month {
    return MONTHS[(ordinal + 1) % 12]
}

private fun Month.numberOfDays(year: Int) = when (this) {
    Month.JANUARY -> 31
    Month.FEBRUARY -> if (year.isLeapYear) 29 else 28
    Month.MARCH -> 31
    Month.APRIL -> 30
    Month.MAY -> 31
    Month.JUNE -> 30
    Month.JULY -> 31
    Month.AUGUST -> 31
    Month.SEPTEMBER -> 30
    Month.OCTOBER -> 31
    Month.NOVEMBER -> 30
    Month.DECEMBER -> 31
    else -> error("Impossible.")
}

private val Int.isLeapYear: Boolean get() {
    val prolepticYear: Long = toLong()
    return prolepticYear and 3 == 0L && (prolepticYear % 100 != 0L || prolepticYear % 400 == 0L)
}

internal fun LocalDateTime.copy(
    year: Int = this.year,
    monthNumber: Int = this.monthNumber,
    dayOfMonth: Int = this.dayOfMonth,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    nanosecond: Int = this.nanosecond,
) = LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)

private fun LocalDateTime.copy(
    year: Int = this.year,
    month: Month = this.month,
    dayOfMonth: Int = this.dayOfMonth,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    nanosecond: Int = this.nanosecond,
) = LocalDateTime(year, month, dayOfMonth, hour, minute, second, nanosecond)
