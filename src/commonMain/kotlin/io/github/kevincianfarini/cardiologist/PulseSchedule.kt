package io.github.kevincianfarini.cardiologist

import dev.drewhamilton.poko.Poko
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

/**
 * A [Pulse] schedule that can be used with [schedulePulse] to define complex schedules.
 *
 * @constructor Creates a new PulseSchedule. Second values must be in range 0..59, minute values must be in range 0..59,
 *              hour values must be in range 0..23, and day of month values must be in range 1..31. This constructor also
 *              requires that seconds, minutes, hours, days of month, and months cannot be empty sets.
 * @throws IllegalArgumentException if the constructor is called with any of our bounds value or an improperly empty set.
 */
@Poko
public class PulseSchedule(
    public val atSeconds: Set<Int>,
    public val atMinutes: Set<Int>,
    public val atHours: Set<Int>,
    public val onDaysOfMonth: Set<Int>,
    public val inMonths: Set<Month>,
    public val onDaysOfWeek: Set<DayOfWeek>,
) {
    init {
        require(!atSeconds.any { it !in 0..59 }) { "Seconds has an out of bound value: $atSeconds" }
        require(atSeconds.isNotEmpty()) { "Seconds cannot be empty!" }
        require(!atMinutes.any { it !in 0..59 }) { "Minutes has an out of bound value: $atMinutes" }
        require(atMinutes.isNotEmpty()) { "Minutes cannot be empty!" }
        require(!atHours.any { it !in 0..23 }) { "Hours has an out of bound value: $atHours" }
        require(atHours.isNotEmpty()) { "Hours cannot be empty!" }
        require(!onDaysOfMonth.any { it !in 1..31 }) { "Days of month has an out of bound value: $onDaysOfMonth" }
        require(onDaysOfMonth.isNotEmpty()) { "Days of month cannot be empty!" }
        require(inMonths.isNotEmpty()) { "Months cannot be empty!" }
        // Don't check if onDaysOfWeek is empty because an empty set is equivalent to the wildcard `*` value in cron
        // expressions.
    }

    /**
     * Serialize this schedule to a standard Cron expression using their integer field representations.
     *
     * Note that [standard Cron](https://pubs.opengroup.org/onlinepubs/9699919799/utilities/crontab.html#tag_20_25_07)
     * only includes fields for minutes, hours, days of month, months of year, and days of week. Second compponents of
     * a schedule will be omitted in this conversion.
     */
    @DelicateCardiologistApi
    public fun toCronExpression(): String = buildString {
        appendCronField(atMinutes, WILDCARD_MINUTES)
        append(" ")
        appendCronField(atHours, WILDCARD_HOURS)
        append(" ")
        appendCronField(onDaysOfMonth, WILDCARD_DAYS_OF_MONTH)
        append(" ")
        appendCronField(inMonths.map(Month::number).toSet(), WILDCARD_MONTHS)
        append(" ")
        appendCronField(onDaysOfWeek.map { it.isoDayNumber % 7 }.toSet(), WILDCARD_DAYS_OF_WEEK)
    }
}


private fun StringBuilder.appendCronField(values: Set<Int>, wildcardValues: Set<Int>) = when {
    values == wildcardValues -> append("*")
    else -> {
        val chunks = values.sorted()
    }
}

private val WILDCARD_MINUTES = (0..59).toSet()
private val WILDCARD_HOURS = (0..23).toSet()
private val WILDCARD_DAYS_OF_MONTH = (1..31).toSet()
private val WILDCARD_MONTHS = Month.entries.map(Month::number).toSet()
private val WILDCARD_DAYS_OF_WEEK = emptySet<Int>()