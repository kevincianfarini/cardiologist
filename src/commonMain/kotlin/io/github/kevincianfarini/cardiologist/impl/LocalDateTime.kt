package io.github.kevincianfarini.cardiologist.impl

import io.github.kevincianfarini.cardiologist.PulseSchedule
import io.github.kevincianfarini.cardiologist.PulseScheduleBuilder
import io.github.kevincianfarini.cardiologist.buildPulseSchedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.number

internal fun LocalDateTime.nextMatch(scheduleBuilder: PulseScheduleBuilder.() -> Unit = {}): LocalDateTime {
    return nextMatch(buildPulseSchedule(scheduleBuilder))
}

internal fun LocalDateTime.nextMatch(schedule: PulseSchedule): LocalDateTime {
    // Ensure that the nextMatch of this LocalDateTime doesn't produce itself. If it does then increment the
    // nanosecond component by one to ensure that we produce a match that's distinct from this value.
    val time = if (matches(schedule)) copy(nanosecond = 1) else this
    return with(schedule) {
        time.nextMonth(inMonths)
            .nextDay(onDaysOfMonth, onDaysOfWeek,inMonths)
            .nextHour(atHours, onDaysOfMonth, onDaysOfWeek, inMonths)
            .nextMinute(atMinutes, atHours, onDaysOfMonth, onDaysOfWeek, inMonths)
            .nextSecond(atSeconds, atMinutes, atHours, onDaysOfMonth, onDaysOfWeek, inMonths)
    }
}

private fun LocalDateTime.matches(schedule: PulseSchedule): Boolean = nanosecond == 0 &&
        second in schedule.atSeconds &&
        minute in schedule.atMinutes &&
        hour in schedule.atHours &&
        matchesDay(schedule) &&
        month in schedule.inMonths

private fun LocalDateTime.matchesDay(schedule: PulseSchedule): Boolean {
    val matchesDayOfWeekOnly = schedule.onDaysOfMonth == WILDCARD_DAYS_OF_MONTH && dayOfWeek in schedule.onDaysOfWeek
    val matchesDayOfMonthAndDayOfWeek = schedule.onDaysOfMonth != WILDCARD_DAYS_OF_MONTH &&
            schedule.onDaysOfWeek.isNotEmpty() &&
            (day in schedule.onDaysOfMonth || dayOfWeek in schedule.onDaysOfWeek)
    val matchesDayOfMonthOnly = schedule.onDaysOfWeek.isEmpty() && day in schedule.onDaysOfMonth
    return matchesDayOfWeekOnly || matchesDayOfMonthAndDayOfWeek || matchesDayOfMonthOnly
}

private fun LocalDateTime.nextMonth(
    inMonths: Set<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val incrementedMonth = if (increment) month.inc() else month
    val minMonth = inMonths.minOrNull()!!
    val maxMonth = inMonths.maxOrNull()!!
    return when {
        incrementedMonth < month -> copy(year = year + 1, monthNumber = incrementedMonth.number)
        incrementedMonth in inMonths -> copy(monthNumber = incrementedMonth.number)
        incrementedMonth < minMonth -> copy(
            monthNumber = minMonth.number,
            dayOfMonth = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        )
        incrementedMonth > maxMonth -> copy(
            year = year + 1,
            monthNumber = minMonth.number,
            dayOfMonth = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        )
        else -> copy(monthNumber = inMonths.sorted().first { it > incrementedMonth }.number)
    }
}

private val WILDCARD_DAYS_OF_MONTH: Set<Int> = (1..31).toSet()

private fun LocalDateTime.nextDay(
    onDaysOfMonth: Set<Int>,
    onDaysOfWeek: Set<DayOfWeek>,
    inMonths: Set<Month>,
    increment: Boolean = false,
): LocalDateTime = when {
    onDaysOfMonth != WILDCARD_DAYS_OF_MONTH && onDaysOfWeek.isNotEmpty() -> {
        // Both day of month and day of week are non-default values, and therefore we take
        // the minimum resulting value from both of their calculations.
        val nextDayOfMonth = nextDayOfMonth(onDaysOfMonth, inMonths, increment)
        val nextDayOfWeek = nextDayOfWeek(onDaysOfWeek, inMonths, increment)
        minOf(nextDayOfMonth, nextDayOfWeek)
    }
    onDaysOfMonth == WILDCARD_DAYS_OF_MONTH && onDaysOfWeek.isNotEmpty() -> {
        nextDayOfWeek(onDaysOfWeek, inMonths, increment)
    }
    else -> nextDayOfMonth(onDaysOfMonth, inMonths, increment)
}

private fun LocalDateTime.nextDayOfMonth(
    onDaysOfMonth: Set<Int>,
    inMonths: Set<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val minDayOfMonth = onDaysOfMonth.minOrNull()!!
    val maxDayOfMonth = onDaysOfMonth.maxOrNull()!!
    require(minDayOfMonth >= 1 && maxDayOfMonth <= 31) { "onDaysOfMonth $onDaysOfMonth not in range 1..31." }
    val incrementedDay = when {
        increment && day + 1 <= month.numberOfDays(year) -> day + 1
        increment -> 1
        else -> day
    }
    return when {
        incrementedDay < day -> copy(dayOfMonth = incrementedDay).nextMonth(inMonths, increment = true)
        incrementedDay in onDaysOfMonth -> copy(dayOfMonth = incrementedDay)
        incrementedDay < minDayOfMonth -> {
            if (minDayOfMonth <= month.numberOfDays(year)) {
                copy(dayOfMonth = minDayOfMonth)
            } else {
                nextMonth(inMonths, increment = true).copy(dayOfMonth = 1)
            }
        }
        incrementedDay > maxDayOfMonth -> {
            nextMonth(inMonths, increment = true).copy(dayOfMonth = 1)
        }
        else -> copy(dayOfMonth = onDaysOfMonth.sorted().first { it > incrementedDay })
    }
}

private val DISTANT_LOCAL_FUTURE = LocalDateTime(100_000, 1, 1, 0, 0)

private fun LocalDateTime.nextDayOfWeek(
    onDaysOfWeek: Set<DayOfWeek>,
    inMonths: Set<Month>,
    increment: Boolean = false,
): LocalDateTime {
    return when {
        onDaysOfWeek.isEmpty() -> DISTANT_LOCAL_FUTURE
        else -> {
            val incrementedDayOfWeek = if (increment) dayOfWeek.inc() else dayOfWeek
            val nextMatchedDayOfWeek = when {
                incrementedDayOfWeek in onDaysOfWeek -> incrementedDayOfWeek
                else -> incrementedDayOfWeek.incrementUntilMatch(onDaysOfWeek)
            }
            val futureDayOfMonth = day + dayOfWeek.daysUntil(nextMatchedDayOfWeek)
            when {
                futureDayOfMonth <= month.numberOfDays(year) -> copy(dayOfMonth = futureDayOfMonth)
                else -> copy(dayOfMonth = 1).nextMonth(inMonths, increment = true).nextDayOfWeek(onDaysOfWeek, inMonths)
            }
        }
    }
}

private fun DayOfWeek.incrementUntilMatch(matches: Set<DayOfWeek>): DayOfWeek {
    var day = this
    while (day !in matches) { day++ }
    return day
}

private fun LocalDateTime.nextHour(
    atHours: Set<Int>,
    onDaysOfMonth: Set<Int>,
    onDaysOfWeek: Set<DayOfWeek>,
    inMonths: Set<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val minHour = atHours.minOrNull()!!
    val maxHour = atHours.maxOrNull()!!
    require(minHour >= 0 && maxHour <= 23) { "atHours $atHours not in range 0..23." }
    val incrementedHour = if (increment) (hour + 1) % 24 else hour
    return when {
        incrementedHour < hour -> nextDay(onDaysOfMonth, onDaysOfWeek, inMonths, increment = true).copy(hour = incrementedHour)
        incrementedHour in atHours -> copy(hour = incrementedHour)
        incrementedHour < minHour -> copy(hour = minHour, minute = 0, second = 0, nanosecond = 0)
        incrementedHour > maxHour -> nextDay(onDaysOfMonth, onDaysOfWeek, inMonths, increment = true).copy(
            hour = minHour,
            minute = 0,
            second = 0,
            nanosecond = 0,
        )
        else -> copy(hour = atHours.sorted().first { it > incrementedHour })
    }
}

private fun LocalDateTime.nextMinute(
    atMinutes: Set<Int>,
    atHours: Set<Int>,
    onDaysOfMonth: Set<Int>,
    onDaysOfWeek: Set<DayOfWeek>,
    inMonths: Set<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val minMinute = atMinutes.minOrNull()!!
    val maxMinute = atMinutes.maxOrNull()!!
    require(minMinute >= 0 && maxMinute <= 59) { "atMinutes $atMinutes not in range 0..59." }
    val incrementedMinute = if (increment) (minute + 1) % 60 else minute
    return when {
        incrementedMinute < minute -> nextHour(atHours, onDaysOfMonth, onDaysOfWeek, inMonths, increment = true).copy(
            minute = incrementedMinute
        )
        incrementedMinute in atMinutes -> copy(minute = incrementedMinute)
        incrementedMinute < minMinute -> copy(minute = minMinute, second = 0, nanosecond = 0)
        incrementedMinute > maxMinute -> nextHour(atHours, onDaysOfMonth, onDaysOfWeek, inMonths, increment = true).copy(
            minute = minMinute,
            second = 0,
            nanosecond = 0,
        )
        else -> copy(minute = atMinutes.sorted().first { it > incrementedMinute })
    }
}

private fun LocalDateTime.nextSecond(
    atSeconds: Set<Int>,
    atMinutes: Set<Int>,
    atHours: Set<Int>,
    onDaysOfMonth: Set<Int>,
    onDaysOfWeek: Set<DayOfWeek>,
    inMonths: Set<Month>,
): LocalDateTime {
    val minSecond = atSeconds.minOrNull()!!
    val maxSeconds = atSeconds.maxOrNull()!!
    require(minSecond >= 0 && maxSeconds <= 59) { " atSeconds $atSeconds not in range 0..59." }
    val incrementedSecond = if (nanosecond > 0) (second + 1) % 60 else second
    return when {
        incrementedSecond < second -> nextMinute(atMinutes, atHours, onDaysOfMonth, onDaysOfWeek, inMonths, increment = true).copy(
            second = minSecond
        )
        incrementedSecond in atSeconds -> copy(second = incrementedSecond)
        incrementedSecond < minSecond -> copy(second = minSecond)
        incrementedSecond > maxSeconds -> nextMinute(
            atMinutes,
            atHours,
            onDaysOfMonth,
            onDaysOfWeek,
            inMonths,
            increment = true,
        ).copy(second = minSecond)
        else -> copy(second = atSeconds.sorted().first { it > incrementedSecond })
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
}

private val Int.isLeapYear: Boolean get() {
    val prolepticYear: Long = toLong()
    return prolepticYear and 3 == 0L && (prolepticYear % 100 != 0L || prolepticYear % 400 == 0L)
}

internal fun LocalDateTime.copy(
    year: Int = this.year,
    monthNumber: Int = this.month.number,
    dayOfMonth: Int = this.day,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    nanosecond: Int = this.nanosecond,
) = LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)

private operator fun Month.inc(): Month {
    return Month.entries[(ordinal + 1) % 12]
}

private operator fun DayOfWeek.inc(): DayOfWeek {
    return DayOfWeek.entries[(ordinal + 1) % 7]
}

private fun DayOfWeek.daysUntil(other: DayOfWeek): Int = when {
    other >= this -> other.ordinal - ordinal
    else -> 7 - (ordinal - other.ordinal)
}
