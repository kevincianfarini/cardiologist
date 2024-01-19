package io.github.kevincianfarini.cardiologist.impl

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.number

internal fun LocalDateTime.nextMatch(
    atSeconds: IntRange = 0..59,
    atMinutes: IntRange = 0..59,
    atHours: IntRange = 0..23,
    onDaysOfMonth: IntRange = 1..31,
    inMonths: ClosedRange<Month> = Month.JANUARY..Month.DECEMBER,
): LocalDateTime {
    // Ensure that the nextMatch of this LocalDateTime doesn't produce itself. If it does then increment the
    // nanosecond component by one to ensure that we produce a match that's distinct from this value.
    val time = if (matches(atSeconds, atMinutes, atHours, onDaysOfMonth, inMonths)) copy(nanosecond = 1) else this
    return time.nextMonth(inMonths)
        .nextDay(onDaysOfMonth, inMonths)
        .nextHour(atHours, onDaysOfMonth, inMonths)
        .nextMinute(atMinutes, atHours, onDaysOfMonth, inMonths)
        .nextSecond(atSeconds, atMinutes, atHours, onDaysOfMonth, inMonths)
}

private fun LocalDateTime.matches(
    atSeconds: IntRange = 0..59,
    atMinutes: IntRange = 0..59,
    atHours: IntRange = 0..23,
    onDaysOfMonth: IntRange = 1..31,
    inMonths: ClosedRange<Month> = Month.JANUARY..Month.DECEMBER,
): Boolean {
    return nanosecond == 0 && second in atSeconds && minute in atMinutes && hour in atHours
            && dayOfMonth in onDaysOfMonth && month in inMonths
}

private fun LocalDateTime.nextMonth(
    inMonths: ClosedRange<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val incrementedMonth = if (increment) month.inc() else month
    return when {
        incrementedMonth < month -> copy(year = year + 1, monthNumber = incrementedMonth.number)
        incrementedMonth in inMonths -> copy(monthNumber = incrementedMonth.number)
        incrementedMonth < inMonths.start -> copy(
            monthNumber = inMonths.start.number,
            dayOfMonth = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        )
        incrementedMonth > inMonths.endInclusive -> copy(
            year = year + 1,
            monthNumber = inMonths.start.number,
            dayOfMonth = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        )
        else -> error("This should be impossible")
    }
}

private fun LocalDateTime.nextDay(
    onDaysOfMonth: IntRange,
    inMonths: ClosedRange<Month>,
    increment: Boolean = false,
): LocalDateTime {
    require(onDaysOfMonth.first >= 1 && onDaysOfMonth.last <= 31) { "onDaysOfMonth $onDaysOfMonth not in range 1..31." }
    val incrementedDay = when {
        increment && dayOfMonth + 1 <= month.numberOfDays(year) -> dayOfMonth + 1
        increment -> 1
        else -> dayOfMonth
    }
    return when {
        incrementedDay < dayOfMonth -> copy(dayOfMonth = incrementedDay).nextMonth(inMonths, increment = true)
        incrementedDay in onDaysOfMonth -> copy(dayOfMonth = incrementedDay)
        incrementedDay < onDaysOfMonth.first -> {
            if (onDaysOfMonth.first <= month.numberOfDays(year)) {
                copy(dayOfMonth = onDaysOfMonth.first)
            } else {
                nextMonth(inMonths, increment = true).copy(dayOfMonth = 1)
            }
        }
        incrementedDay > onDaysOfMonth.last -> {
            nextMonth(inMonths, increment = true).copy(dayOfMonth = 1)
        }
        else -> error("This should be impossible.")
    }
}

private fun LocalDateTime.nextHour(
    atHours: IntRange,
    onDaysOfMonth: IntRange,
    inMonths: ClosedRange<Month>,
    increment: Boolean = false,
): LocalDateTime {
    require(atHours.first >= 0 && atHours.last <= 23) { "atHours $atHours not in range 0..23." }
    val incrementedHour = if (increment) (hour + 1) % 24 else hour
    return when {
        incrementedHour < hour -> nextDay(onDaysOfMonth, inMonths, increment = true).copy(hour = incrementedHour)
        incrementedHour in atHours -> copy(hour = incrementedHour)
        incrementedHour < atHours.first -> copy(hour = atHours.first, minute = 0, second = 0, nanosecond = 0)
        incrementedHour > atHours.last -> nextDay(onDaysOfMonth, inMonths, increment = true).copy(
            hour = atHours.first,
            minute = 0,
            second = 0,
            nanosecond = 0,
        )
        else -> error("This should be impossible.")
    }
}

private fun LocalDateTime.nextMinute(
    atMinutes: IntRange,
    atHours: IntRange,
    onDaysOfMonth: IntRange,
    inMonths: ClosedRange<Month>,
    increment: Boolean = false,
): LocalDateTime {
    require(atMinutes.first >= 0 && atMinutes.last <= 59) { "atMinutes $atMinutes not in range 0..59." }
    val incrementedMinute = if (increment) (minute + 1) % 60 else minute
    return when {
        incrementedMinute < minute -> nextHour(atHours, onDaysOfMonth, inMonths, increment = true).copy(
            minute = incrementedMinute
        )
        incrementedMinute in atMinutes -> copy(minute = incrementedMinute)
        incrementedMinute < atMinutes.first -> copy(minute = atMinutes.first, second = 0, nanosecond = 0)
        incrementedMinute > atMinutes.last -> nextHour(atHours, onDaysOfMonth, inMonths, increment = true).copy(
            minute = atMinutes.first,
            second = 0,
            nanosecond = 0,
        )
        else -> error("This should be impossible.")
    }
}

private fun LocalDateTime.nextSecond(
    atSeconds: IntRange,
    atMinutes: IntRange,
    atHours: IntRange,
    onDaysOfMonth: IntRange,
    inMonths: ClosedRange<Month>,
): LocalDateTime {
    require(atSeconds.first >= 0 && atSeconds.last <= 59) { " atSeconds $atSeconds not in range 0..59." }
    val incrementedSecond = if (nanosecond > 0) (second + 1) % 60 else second
    return when {
        incrementedSecond < second -> nextMinute(atMinutes, atHours, onDaysOfMonth, inMonths, increment = true).copy(
            second = incrementedSecond
        )
        incrementedSecond in atSeconds -> copy(second = incrementedSecond)
        incrementedSecond < atSeconds.first -> copy(second = atSeconds.first)
        incrementedSecond > atSeconds.last -> nextMinute(
            atMinutes,
            atHours,
            onDaysOfMonth,
            inMonths,
            increment = true,
        ).copy(second = atSeconds.first)
        else -> error("This should be impossible.")
    }.copy(nanosecond = 0)
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

private operator fun Month.inc(): Month {
    return Month.entries[(ordinal + 1) % 12]
}
