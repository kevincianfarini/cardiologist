package io.github.kevincianfarini.cardiologist.impl

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

internal data class CronValues(
    val atMinutes: Set<Int>,
    val atHours: Set<Int>,
    val onDaysOfMonth: Set<Int>,
    val onDaysOfWeek: Set<DayOfWeek>,
    val inMonths: Set<Month>,
)

internal fun String.parseCronExpression(): CronValues {
    val segments = split(Regex("\\s+"))
    require(segments.size == 5) { "'$this' is not a valid cron expression." }
    val minutes = segments[0].parseMinutesExpressionOrNull()
    val hours = segments[1].parseHoursExpressionOrNull()
    val daysOfMonth = segments[2].parseDaysOfMonthExpressionOrNull()
    val months = segments[3].parseMonthsExpressionOrNull()
    val daysOfWeek = segments[4].parseDaysOfWeekExpressionOrNull()
    return if (minutes != null && hours != null && daysOfMonth != null && months != null && daysOfWeek != null) {
        CronValues(
            atMinutes = minutes,
            atHours = hours,
            onDaysOfMonth = daysOfMonth,
            onDaysOfWeek = daysOfWeek,
            inMonths = months
        )
    } else {
        val errorMessage = buildMalformedCronErrorMessage(
            segments = segments,
            erroredSegmentIndices = listOfNotNull(
                0.takeIf { minutes == null },
                1.takeIf { hours == null },
                2.takeIf { daysOfMonth == null },
                3.takeIf { months == null },
                4.takeIf { daysOfWeek == null },
            )
        )
        throw IllegalArgumentException(errorMessage)
    }
}

private fun String.parseMinutesExpressionOrNull(): Set<Int>? = parseIntegerComponentExpressionOrNull(
    wildcardValue = (0..59).toSet(),
    validValues = 0..59,
)

private fun String.parseHoursExpressionOrNull(): Set<Int>? = parseIntegerComponentExpressionOrNull(
    wildcardValue = (0..23).toSet(),
    validValues = 0..23,
)

private fun String.parseDaysOfMonthExpressionOrNull(): Set<Int>? = parseIntegerComponentExpressionOrNull(
    wildcardValue = (1..31).toSet(),
    validValues = 1..31,
)

private fun String.parseDaysOfWeekExpressionOrNull(): Set<DayOfWeek>? {
    val raw = parseIntegerComponentExpressionOrNull(
        wildcardValue = emptySet(), // A wildcard for the day of week cron expression is an empty set.
        validValues = 0..6,
        stringToIntegerMapping = mapOf(
            "SUN" to 0,
            "MON" to 1,
            "TUE" to 2,
            "WED" to 3,
            "THU" to 4,
            "FRI" to 5,
            "SAT" to 6,
        )
    )
    return raw?.let { numbers ->
        buildSet {
            numbers.forEach { dayOfWeekInt ->
                val result = when (dayOfWeekInt) {
                    0 -> DayOfWeek.SUNDAY
                    1 -> DayOfWeek.MONDAY
                    2 -> DayOfWeek.TUESDAY
                    3 -> DayOfWeek.WEDNESDAY
                    4 -> DayOfWeek.THURSDAY
                    5 -> DayOfWeek.FRIDAY
                    6 -> DayOfWeek.SATURDAY
                    else -> error("Invalid day of week integer $dayOfWeekInt.")
                }
                add(result)
            }
        }
    }
}

private fun String.parseMonthsExpressionOrNull(): Set<Month>? {
    val raw = parseIntegerComponentExpressionOrNull(
        wildcardValue = (1..12).toSet(),
        validValues = 1..12,
        stringToIntegerMapping = mapOf(
            "JAN" to 1,
            "FEB" to 2,
            "MAR" to 3,
            "APR" to 4,
            "MAY" to 5,
            "JUN" to 6,
            "JUL" to 7,
            "AUG" to 8,
            "SEP" to 9,
            "OCT" to 10,
            "NOV" to 11,
            "DEC" to 12,
        )
    )
    return raw?.let { numbers ->
        buildSet {
            numbers.forEach { monthNumber ->
                add(Month(monthNumber))
            }
        }
    }
}

private fun String.parseIntegerComponentExpressionOrNull(
    wildcardValue: Set<Int>,
    validValues: IntRange,
    stringToIntegerMapping: Map<String, Int> = emptyMap(),
): Set<Int>? = when {
    this == "*" -> wildcardValue
    "," in this -> {
        val parts = split(",").map { it.parseIntegerComponentExpressionOrNull(wildcardValue, validValues) }
        val noNulls = parts.filterNotNull()
        if (parts.size == noNulls.size) {
            buildSet {
                noNulls.forEach { part -> addAll(part) }
            }
        } else {
            null
        }
    }
    "-" in this -> {
        val parts = split("-")
        if (parts.size == 2) {
            val start = parts[0].toIntOrNull() ?: stringToIntegerMapping[parts[0].uppercase()]
            val end = parts[1].toIntOrNull() ?: stringToIntegerMapping[parts[1].uppercase()]
            if (start == null || end == null) {
                null
            } else {
                (start..end).takeIf { !it.isEmpty() }?.toSet()
            }
        } else {
            // Because we recurse on commas first, we guarantee that an inclusive range will always only be parsed as
            // a single value in this branch of code. Therefore, if there's more than two parts it's invalid.
            null
        }
    }
    else -> (toIntOrNull() ?: stringToIntegerMapping[uppercase()])?.takeIf { it in validValues }?.let {
        // Base case is just a single integer. If it can't be coerced to an Int it's invalid.
        setOf(it)
    }
}

private fun buildMalformedCronErrorMessage(segments: List<String>, erroredSegmentIndices: List<Int>): String {
    return buildString {
        append("Cron expression is malformed:")
        segments.forEachIndexed{ index, segment ->
            append(" ")
            if (index in erroredSegmentIndices) {
                append("<<")
            }
            append(segment)
            if (index in erroredSegmentIndices) {
                append(">>")
            }
        }
    }
}