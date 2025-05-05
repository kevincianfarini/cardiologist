package io.github.kevincianfarini.cardiologist

import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PulseScheduleTest {

    @Test
    fun second_out_of_range_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = setOf(0, 75),
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Seconds has an out of bound value: [0, 75]",
            actual = e.message,
        )
    }

    @Test
    fun empty_second_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = emptySet(),
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Seconds cannot be empty!",
            actual = e.message,
        )
    }

    @Test
    fun minute_out_of_range_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = (0..59).toSet(),
                atMinutes = setOf(0, 75),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Minutes has an out of bound value: [0, 75]",
            actual = e.message,
        )
    }

    @Test
    fun empty_minute_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = (0..59).toSet(),
                atMinutes = emptySet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Minutes cannot be empty!",
            actual = e.message,
        )
    }

    @Test
    fun hour_out_of_range_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = (0..59).toSet(),
                atMinutes = (0..59).toSet(),
                atHours = setOf(0, 24),
                onDaysOfMonth = (1..31).toSet(),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Hours has an out of bound value: [0, 24]",
            actual = e.message,
        )
    }

    @Test
    fun empty_hour_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = (0..59).toSet(),
                atMinutes = (0..59).toSet(),
                atHours = emptySet(),
                onDaysOfMonth = (1..31).toSet(),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Hours cannot be empty!",
            actual = e.message,
        )
    }

    @Test
    fun day_of_month_out_of_range_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = (0..59).toSet(),
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = setOf(0),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Days of month has an out of bound value: [0]",
            actual = e.message,
        )
    }

    @Test
    fun empty_day_of_month_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = (0..59).toSet(),
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = emptySet(),
                inMonths = Month.entries.toSet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Days of month cannot be empty!",
            actual = e.message,
        )
    }

    @Test
    fun empty_month_fails() {
        val e = assertFails {
            PulseSchedule(
                atSeconds = (0..59).toSet(),
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                inMonths = emptySet(),
                onDaysOfWeek = emptySet(),
            )
        }
        assertEquals(
            expected = "Months cannot be empty!",
            actual = e.message,
        )
    }
}