package io.github.kevincianfarini.cardiologist.impl

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ParseCronTest {

    @Test
    fun incorrect_number_of_segments_errors() {
        val e = assertFails { "".parseCronExpression() }
        assertEquals(
            expected = "'' is not a valid cron expression.",
            actual = e.message
        )
    }

    @Test
    fun all_wildcard_segments_returns_correct_value() {
        assertEquals(
            expected = CronValues(
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = emptySet(),
                inMonths = Month.entries.toSet(),
            ),
            actual = "* * * * *".parseCronExpression()
        )
    }

    @Test
    fun arbitrary_whitespace_does_not_matter() {
        assertEquals(
            expected = CronValues(
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = emptySet(),
                inMonths = Month.entries.toSet(),
            ),
            actual = "*       * \t *   *   *".parseCronExpression()
        )
    }

    @Test
    fun minutes_base_case() {
        assertEquals(
            expected = CronValues(
                atMinutes = setOf(0),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = emptySet(),
                inMonths = Month.entries.toSet(),
            ),
            actual = "0 * * * *".parseCronExpression()
        )
    }

    @Test
    fun minutes_multiple_values() {
        assertEquals(
            expected = CronValues(
                atMinutes = setOf(0, 5),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = emptySet(),
                inMonths = Month.entries.toSet(),
            ),
            actual = "0,5 * * * *".parseCronExpression()
        )
    }

    @Test
    fun minutes_range_values() {
        assertEquals(
            expected = CronValues(
                atMinutes = setOf(0, 1, 2, 3, 4, 5),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = emptySet(),
                inMonths = Month.entries.toSet(),
            ),
            actual = "0-5 * * * *".parseCronExpression()
        )
    }

    @Test
    fun minutes_mixes_comma_range_values() {
        assertEquals(
            expected = CronValues(
                atMinutes = setOf(0, 1, 2, 3, 4, 5, 10, 15),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = emptySet(),
                inMonths = Month.entries.toSet(),
            ),
            actual = "0-5,10,15 * * * *".parseCronExpression()
        )
    }

    @Test
    fun single_comma_two_ranges() {
        assertEquals(
            expected = CronValues(
                atMinutes = setOf(0, 1, 2, 3, 4, 5, 10, 11, 12, 13, 14, 15),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = emptySet(),
                inMonths = Month.entries.toSet(),
            ),
            actual = "0-5,10-15 * * * *".parseCronExpression()
        )
    }

    @Test
    fun minutes_double_comma_invalid() {
        val e = assertFails {
            "0,,1 * * * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: <<0,,1>> * * * *",
            actual = e.message,
        )
    }

    @Test
    fun minutes_double_range_invalid() {
        val e = assertFails {
            "0--1 * * * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: <<0--1>> * * * *",
            actual = e.message,
        )
    }

    @Test
    fun minutes_range_no_end_invalid() {
        val e = assertFails {
            "0- * * * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: <<0->> * * * *",
            actual = e.message,
        )
    }

    @Test
    fun minutes_range_no_start_invalid() {
        val e = assertFails {
            "-1 * * * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: <<-1>> * * * *",
            actual = e.message,
        )
    }

    @Test
    fun minutes_range_empty_invalid() {
        val e = assertFails {
            "5-3 * * * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: <<5-3>> * * * *",
            actual = e.message,
        )
    }

    @Test
    fun minutes_invalid_value() {
        val e = assertFails {
            "75 * * * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: <<75>> * * * *",
            actual = e.message,
        )
    }


    @Test
    fun hours_invalid_value() {
        val e = assertFails {
            "* 24 * * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: * <<24>> * * *",
            actual = e.message,
        )
    }

    @Test
    fun day_of_month_invalid_value() {
        val e = assertFails {
            "* * 0 * *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: * * <<0>> * *",
            actual = e.message,
        )
    }

    @Test
    fun month_invalid_int_value() {
        val e = assertFails {
            "* * * 13 *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: * * * <<13>> *",
            actual = e.message,
        )
    }

    @Test
    fun day_of_week_invalid_int_value() {
        val e = assertFails {
            "* * * * 7".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: * * * * <<7>>",
            actual = e.message,
        )
    }

    @Test
    fun month_names_map_to_values_properly() {
        // Spongebob case them because these are case-insensitive.
        val months = mapOf(
            "jan" to Month.JANUARY,
            "feB" to Month.FEBRUARY,
            "mAR" to Month.MARCH,
            "APR" to Month.APRIL,
            "MAy" to Month.MAY,
            "Jun" to Month.JUNE,
            "jul" to Month.JULY,
            "AUG" to Month.AUGUST,
            "sep" to Month.SEPTEMBER,
            "Oct" to Month.OCTOBER,
            "NOV" to Month.NOVEMBER,
            "dec" to Month.DECEMBER,
        )
        months.forEach { (string, enum) ->
            assertEquals(
                expected = CronValues(
                    atMinutes = (0..59).toSet(),
                    atHours = (0..23).toSet(),
                    onDaysOfMonth = (1..31).toSet(),
                    onDaysOfWeek = emptySet(),
                    inMonths = setOf(enum),
                ),
                actual = "* * * $string *".parseCronExpression()
            )
        }
    }

    @Test
    fun days_of_week_map_to_values_propertly() {
        // Spongebob case them because these are case-insensitive.
        val days = mapOf(
            "mon" to DayOfWeek.MONDAY,
            "tuE" to DayOfWeek.TUESDAY,
            "wED" to DayOfWeek.WEDNESDAY,
            "THU" to DayOfWeek.THURSDAY,
            "FRi" to DayOfWeek.FRIDAY,
            "Sat" to DayOfWeek.SATURDAY,
            "sun" to DayOfWeek.SUNDAY,
        )
        days.forEach { (string, enum) ->
            assertEquals(
                expected = CronValues(
                    atMinutes = (0..59).toSet(),
                    atHours = (0..23).toSet(),
                    onDaysOfMonth = (1..31).toSet(),
                    onDaysOfWeek = setOf(enum),
                    inMonths = Month.entries.toSet()
                ),
                actual = "* * * * $string".parseCronExpression()
            )
        }
    }

    @Test
    fun full_range_day_of_week_maps_properly() {
        assertEquals(
            expected = CronValues(
                atMinutes = (0..59).toSet(),
                atHours = (0..23).toSet(),
                onDaysOfMonth = (1..31).toSet(),
                onDaysOfWeek = DayOfWeek.entries.toSet(),
                inMonths = Month.entries.toSet()
            ),
            actual = "* * * * SUN-SAT".parseCronExpression()
        )
    }

    @Test
    fun invalid_month_string() {
        val e = assertFails {
            "* * * WRONG *".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: * * * <<WRONG>> *",
            actual = e.message,
        )
    }

    @Test
    fun invalid_day_of_week_string() {
        val e = assertFails {
            "* * * * WRONG".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: * * * * <<WRONG>>",
            actual = e.message,
        )
    }

    @Test
    fun multiple_incorrect_values_yield_many_errors() {
        val e = assertFails {
            "60 24 32 13 7".parseCronExpression()
        }
        assertEquals(
            expected = "Cron expression is malformed: <<60>> <<24>> <<32>> <<13>> <<7>>",
            actual = e.message,
        )
    }
}