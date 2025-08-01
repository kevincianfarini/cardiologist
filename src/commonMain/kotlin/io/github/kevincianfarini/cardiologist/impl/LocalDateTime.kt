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
        time.nextMonth(sortedMonths)
            .nextDay(sortedDaysOfMonth, sortedDaysOfWeek, sortedMonths)
            .nextHour(sortedHours, sortedDaysOfMonth, sortedDaysOfWeek, sortedMonths)
            .nextMinute(sortedMinutes, sortedHours, sortedDaysOfMonth, sortedDaysOfWeek, sortedMonths)
            .nextSecond(sortedSeconds, sortedMinutes, sortedHours, sortedDaysOfMonth, sortedDaysOfWeek, sortedMonths)
    }
}

private fun LocalDateTime.nextMonth(
    inMonths: List<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val incrementedMonth = if (increment) month.inc() else month
    val minMonth = inMonths.first()
    val maxMonth = inMonths.last()
    return when {
        incrementedMonth < month -> copy(year = year + 1, month = incrementedMonth.number)
        incrementedMonth in inMonths -> copy(month = incrementedMonth.number)
        incrementedMonth < minMonth -> copy(
            month = minMonth.number,
            day = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        )
        incrementedMonth > maxMonth -> copy(
            year = year + 1,
            month = minMonth.number,
            day = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        )
        else -> copy(
            month = inMonths.first { it > incrementedMonth }.number,
            day = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0,
        )
    }
}

private fun LocalDateTime.nextDay(
    onDaysOfMonth: List<Int>,
    onDaysOfWeek: List<DayOfWeek>,
    inMonths: List<Month>,
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
    onDaysOfMonth: List<Int>,
    inMonths: List<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val minDayOfMonth = onDaysOfMonth.first()
    val maxDayOfMonth = onDaysOfMonth.last()
    val incrementedDay = when {
        increment && day + 1 <= month.numberOfDays(year) -> day + 1
        increment -> 1
        else -> day
    }
    return when {
        incrementedDay < day -> copy(day = incrementedDay).nextMonth(inMonths, increment = true)
        incrementedDay in onDaysOfMonth -> copy(day = incrementedDay)
        incrementedDay < minDayOfMonth -> {
            if (minDayOfMonth <= month.numberOfDays(year)) {
                copy(day = minDayOfMonth)
            } else {
                nextMonth(inMonths, increment = true).nextDayOfMonth(onDaysOfMonth, inMonths)
            }
        }
        incrementedDay > maxDayOfMonth -> {
            nextMonth(inMonths, increment = true).nextDayOfMonth(onDaysOfMonth, inMonths)
        }
        else -> copy(
            day = onDaysOfMonth.first { it > incrementedDay },
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0,
        )
    }
}

private fun LocalDateTime.nextDayOfWeek(
    onDaysOfWeek: List<DayOfWeek>,
    inMonths: List<Month>,
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
            val nextScheduledDayOfMonth = day + dayOfWeek.daysUntil(nextMatchedDayOfWeek)
            when {
                nextScheduledDayOfMonth == day -> this
                nextScheduledDayOfMonth <= month.numberOfDays(year) -> copy(
                    day = nextScheduledDayOfMonth,
                    hour = 0,
                    minute = 0,
                    second = 0,
                    nanosecond = 0,
                )
                else -> copy(
                    day = 1,
                    hour = 0,
                    minute = 0,
                    second = 0,
                    nanosecond = 0,
                ).nextMonth(inMonths, increment = true).nextDayOfWeek(onDaysOfWeek, inMonths)
            }
        }
    }
}

private fun LocalDateTime.nextHour(
    atHours: List<Int>,
    onDaysOfMonth: List<Int>,
    onDaysOfWeek: List<DayOfWeek>,
    inMonths: List<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val minHour = atHours.first()
    val maxHour = atHours.last()
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
        else -> copy(
            hour = atHours.first { it > incrementedHour },
            minute = 0,
            second = 0,
            nanosecond = 0,
        )
    }
}

private fun LocalDateTime.nextMinute(
    atMinutes: List<Int>,
    atHours: List<Int>,
    onDaysOfMonth: List<Int>,
    onDaysOfWeek: List<DayOfWeek>,
    inMonths: List<Month>,
    increment: Boolean = false,
): LocalDateTime {
    val minMinute = atMinutes.first()
    val maxMinute = atMinutes.last()
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
        else -> copy(
            minute = atMinutes.first { it > incrementedMinute },
            second = 0,
            nanosecond = 0,
        )
    }
}

private fun LocalDateTime.nextSecond(
    atSeconds: List<Int>,
    atMinutes: List<Int>,
    atHours: List<Int>,
    onDaysOfMonth: List<Int>,
    onDaysOfWeek: List<DayOfWeek>,
    inMonths: List<Month>,
): LocalDateTime {
    val minSecond = atSeconds.first()
    val maxSeconds = atSeconds.last()
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
        else -> copy(second = atSeconds.first { it > incrementedSecond })
    }.copy(nanosecond = 0)
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
    month: Int = this.month.number,
    day: Int = this.day,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    nanosecond: Int = this.nanosecond,
) = LocalDateTime(year, month, day, hour, minute, second, nanosecond)

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

private fun DayOfWeek.incrementUntilMatch(matches: List<DayOfWeek>): DayOfWeek {
    var day = this
    while (day !in matches) { day++ }
    return day
}

private val DISTANT_LOCAL_FUTURE = LocalDateTime(100_000, 1, 1, 0, 0)
private val WILDCARD_DAYS_OF_MONTH: List<Int> = (1..31).toList()
