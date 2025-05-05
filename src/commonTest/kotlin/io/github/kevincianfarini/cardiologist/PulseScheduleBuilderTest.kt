package io.github.kevincianfarini.cardiologist

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class PulseScheduleBuilderTest {

    @Test
    fun empty_builder_produces_default_value() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule { },
    )

    @Test
    fun simple_seconds() = assertEquals(
        expected = PulseSchedule(
            atSeconds = setOf(0, 30),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            atSeconds(0, 30)
        },
    )

    @Test
    fun multiple_seconds_calls_are_additive() = assertEquals(
        expected = PulseSchedule(
            atSeconds = setOf(0, 15, 30, 45),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            atSeconds(0, 30)
            atSeconds(15, 45)
        },
    )

    @Test
    fun simple_minutes() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = setOf(0, 30),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            atMinutes(0, 30)
        },
    )

    @Test
    fun multiple_minutes_calls_are_additive() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = setOf(0, 15, 30, 45),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            atMinutes(0, 30)
            atMinutes(15, 45)
        },
    )

    @Test
    fun simple_hours() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = setOf(0, 12),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            atHours(0, 12)
        },
    )

    @Test
    fun multiple_hours_are_additive() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = setOf(0, 6, 12, 18),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            atHours(0, 12)
            atHours(6, 18)
        },
    )

    @Test
    fun simple_days_of_month() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = setOf(1, 15),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            onDaysOfMonth(1, 15)
        },
    )

    @Test
    fun multiple_days_of_month_calls_are_additive() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = setOf(1, 7, 15, 21),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            onDaysOfMonth(1, 15)
            onDaysOfMonth(7, 21)
        },
    )

    @Test
    fun simple_months() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = setOf(Month.OCTOBER, Month.DECEMBER),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            inMonths(Month.OCTOBER, Month.DECEMBER)
        },
    )

    @Test
    fun multiple_months_calls_are_additive() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = setOf(Month.OCTOBER, Month.DECEMBER, Month.MAY),
            onDaysOfWeek = emptySet(),
        ),
        actual = buildPulseSchedule {
            inMonths(Month.OCTOBER, Month.DECEMBER)
            inMonths(Month.MAY)
        },
    )

    @Test
    fun simple_day_of_week() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        ),
        actual = buildPulseSchedule {
            onDaysOfWeek(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        },
    )

    @Test
    fun multiple_day_of_week_calls_are_additive() = assertEquals(
        expected = PulseSchedule(
            atSeconds = (0..59).toSet(),
            atMinutes = (0..59).toSet(),
            atHours = (0..23).toSet(),
            onDaysOfMonth = (1..31).toSet(),
            inMonths = Month.entries.toSet(),
            onDaysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        ),
        actual = buildPulseSchedule {
            onDaysOfWeek(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
            onDaysOfWeek(DayOfWeek.WEDNESDAY)
        },
    )
}